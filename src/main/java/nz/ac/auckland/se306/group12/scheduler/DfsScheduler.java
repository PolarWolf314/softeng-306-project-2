package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.DfsWorker;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduleWithAnEmptyProcessor;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;
import nz.ac.auckland.se306.group12.models.datastructures.MaxSizeHashMap;

public class DfsScheduler implements Scheduler {

  private static final String HUMAN_READABLE_NAME = "DFS branch-and-bound (ELS state space)";
  /**
   * After a little bit of trial and error, this seems to be a decent balance between being able to
   * store a lot of schedules in the closed set and not running out of memory. This is subject to
   * change as we do more testing.
   */
  private static final int MAX_CLOSED_SET_SIZE = 1 << 18; // 262144
  private final int workerCount;
  private AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  private AtomicInteger idleWorkers = new AtomicInteger(0);
  private List<DfsWorker> workers = new ArrayList<>();
  private List<Thread> threads = new ArrayList<>();
  private Random random = new Random();
  private int syncThreshold = 1024;

  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;

  /**
   * Creates a new single-threaded {@link DfsScheduler}.
   */
  public DfsScheduler() {
    this.workerCount = 1;
  }

  /**
   * Creates a new {@link DfsScheduler} with the specified number of threads (Or workers).
   *
   * @param workerCount The number of threads to run the scheduler with
   */
  public DfsScheduler(int workerCount) {
    this.workerCount = workerCount;
  }

  @Override
  public long getSearchedCount() {
    return this.searchedCount.get();
  }

  @Override
  public long getPrunedCount() {
    return this.prunedCount.get();
  }

  @Override
  public Schedule getBestSchedule() {
    return this.bestSchedule.get();
  }

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.resetScheduler();
    this.status = SchedulerStatus.SCHEDULING;

    DfsWorker initWorker = new DfsWorker();
    Schedule initWork = new ScheduleWithAnEmptyProcessor(taskGraph, processorCount);
    initWorker.give(initWork);
    this.workers.add(initWorker);

    Queue<Schedule> initialStates = this.aStarInitialStates(taskGraph, processorCount);

    for (int i = 1; i < this.workerCount; i++) {
      DfsWorker worker = new DfsWorker();
      Schedule work = initialStates.poll();
      if (work != null) {
        worker.give(work);
      }
      this.workers.add(worker);
    }

    for (DfsWorker dfsWorker : this.workers) {
      Thread thread = new Thread(() -> this.branchAndBound(taskGraph, dfsWorker));
      this.threads.add(thread);
      thread.start();
    }

    try {
      for (Thread thread : this.threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule.get();
  }

  /**
   * Performs the branch and bound algorithm on a given graph.
   *
   * @param taskGraph graph to perform branch and bound on
   * @param worker    Worker who contains to a thread that processes the branch and bound.
   */
  private void branchAndBound(Graph taskGraph, DfsWorker worker) {
    int syncCounter = 0;
    int localMinMakespan = this.currentMinMakespan.get();
    long localSearchCount = 0;
    long localPruneCount = 0;
    Map<String, Boolean> closed = new MaxSizeHashMap<>(
        MAX_CLOSED_SET_SIZE / this.workerCount,
        Scheduler.INITIAL_CLOSED_SET_CAPACITY / this.workerCount);

    boolean hasWork = true;

    while (this.hasRunningWorker()) {

      while (hasWork) {
        Schedule currentSchedule = worker.steal();
        if (currentSchedule == null) {
          this.idleWorkers.incrementAndGet();
          hasWork = false;
          break;
        }
        syncCounter++;

        if (syncCounter == this.syncThreshold) {
          localMinMakespan = this.currentMinMakespan.get();
          this.prunedCount.getAndAdd(localPruneCount);
          this.searchedCount.getAndAdd(localSearchCount);
          localSearchCount = 0;
          localPruneCount = 0;
          syncCounter = 0;
        }

        // Prune if current schedule is worse than current best
        if (currentSchedule.getEstimatedMakespan() >= localMinMakespan) {
          localPruneCount++;
          continue;
        }

        localSearchCount++;

        // Check if current schedule is complete
        if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
          localMinMakespan = currentSchedule.getLatestEndTime();
          this.updateGlobalMinMakespanAndSchedule(currentSchedule);
          continue;
        }

        // Check to find if any tasks can be scheduled and schedule them
        for (Task task : currentSchedule.getReadyTasks()) {
          int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
          int allocableCount = currentSchedule.getAllocableProcessorCount();

          for (int i = 0; i < allocableCount; i++) {
            Schedule newSchedule = this.scheduleNextTask(task, latestStartTimes[i],
                currentSchedule.getProcessorEndTimes()[i], i, currentSchedule);

            if (this.scheduleIsPruned(newSchedule, localMinMakespan, closed)) {
              localPruneCount++;
              continue;
            }

            worker.give(newSchedule);
          }
        }
      }

      hasWork = this.takeWorkFromRandomWorker(worker);
    }
  }

  /**
   * Does a rudimentary run of A* to get initial states for the workers to have.
   *
   * @param taskGraph      graph to be scheduled with
   * @param processorCount amount of processors to schedule to
   * @return list of initial states in a queue
   */
  public Queue<Schedule> aStarInitialStates(Graph taskGraph, int processorCount) {
    Queue<Schedule> priorityQueue = new PriorityQueue<>();
    long localSearchedCount = 0;

    priorityQueue.add(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));

    while (!priorityQueue.isEmpty()) {
      localSearchedCount++;

      Schedule currentSchedule = priorityQueue.poll();
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        // It's possible for A* to solve the graph before we get enough tasks in the queue to stop
        this.currentMinMakespan.set(currentSchedule.getEstimatedMakespan());
        this.bestSchedule.set(currentSchedule);
        break;
      }

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : currentSchedule.getReadyTasks()) {
        int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
        for (int i = 0; i < currentSchedule.getAllocableProcessorCount(); i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          Schedule newSchedule = currentSchedule.extendWithTask(newScheduledTask, task);

          priorityQueue.add(newSchedule);
          if (priorityQueue.size() >= this.workerCount - 1) {
            this.searchedCount.set(localSearchedCount);
            return priorityQueue;
          }
        }
      }
    }

    this.searchedCount.set(localSearchedCount);
    return priorityQueue;
  }

  /**
   * Steals work from a random worker if the current worker is idle. This will be attempted 3 times
   * and only return {@code false} if it failed on all 3 attempts. It can fail to steal if the
   * randomly selected worker is itself or if that worker doesn't have any work available.
   *
   * @param worker that is idle, trying to steal work from another
   * @return {@code true} if the steal was successful, {@code false} otherwise
   */
  private boolean takeWorkFromRandomWorker(DfsWorker worker) {
    // Randomly choose a worker to steal from. This attempts to evenly distribute the workers being stolen from
    for (int attempt = 0; attempt < 3; attempt++) {
      int index = this.random.nextInt(this.workerCount);
      DfsWorker otherWorker = this.workers.get(index);

      // We can't steal from ourselves
      if (otherWorker == worker) {
        continue;
      }

      Schedule work = otherWorker.steal();
      if (work != null) {
        worker.give(work);
        this.idleWorkers.decrementAndGet();
        return true;
      }
    }
    return false;
  }

  /**
   * Check if there are any workers running
   *
   * @return if there are any workers working
   */
  private boolean hasRunningWorker() {
    return this.idleWorkers.get() < this.workerCount;
  }

  /**
   * Updates the local schedule with the global concurrent schedule, as well as the makespan.
   *
   * @param currentSchedule schedule on the local thread that will be compared.
   */
  private synchronized void updateGlobalMinMakespanAndSchedule(Schedule currentSchedule) {
    int localMinMakespan = currentSchedule.getLatestEndTime();
    if (localMinMakespan < this.currentMinMakespan.get()) {
      this.currentMinMakespan.set(localMinMakespan);
      this.bestSchedule.set(currentSchedule);
    }
  }

  /**
   * Schedules the next task on a processor, taking into account its dependencies and constraints.
   *
   * @param task                   The task to be scheduled.
   * @param latestStartTime        The latest allowable start time for the task to be allocated on
   *                               the processor.
   * @param latestProcessorEndTime The latest end time of the last task on the processor.
   * @param processorIndex         The index of the processor where the task is scheduled.
   * @param currentSchedule        The current schedule to be extended with the new task.
   * @return A new schedule that includes the scheduled task, respecting timing constraints.
   */
  private Schedule scheduleNextTask(Task task, int latestStartTime, int latestProcessorEndTime,
      int processorIndex, Schedule currentSchedule) {
    // Ensure that it either schedules by latest time or after the last task on the processor
    int startTime = Math.max(latestStartTime, latestProcessorEndTime);
    int endTime = startTime + task.getWeight();
    ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, processorIndex);
    return currentSchedule.extendWithTask(newScheduledTask, task);
  }


  /**
   * Checks if a schedule should be pruned based on its estimated makespan and whether it exists in
   * the closed set to avoid duplicate evaluations.
   *
   * @param schedule         The schedule to be checked for pruning.
   * @param localMinMakespan The local minimum makespan, used as a pruning threshold.
   * @return true if the schedule should was pruned, false otherwise.
   */
  private boolean scheduleIsPruned(Schedule schedule, int localMinMakespan,
      Map<String, Boolean> closed) {
    if (schedule.getEstimatedMakespan() >= localMinMakespan) {
      return true;
    }

    String stringHash = schedule.generateUniqueString();

    // No need to add the schedule to the closed set at this point as if we find this schedule
    // again it'll get pruned at this point again anyway, which saves memory.
    if (closed.containsKey(stringHash)) {
      return true;
    }

    closed.put(stringHash, Boolean.TRUE);
    return false;
  }

  /**
   * Resets the scheduler to its initial state so that it can be used to schedule a new task graph.
   */
  private void resetScheduler() {
    this.searchedCount.set(0);
    this.prunedCount.set(0);
    this.bestSchedule.set(null);
    this.currentMinMakespan.set(Integer.MAX_VALUE);
  }

  @Override
  public String getHumanReadableName() {
    return HUMAN_READABLE_NAME;
  }

}
