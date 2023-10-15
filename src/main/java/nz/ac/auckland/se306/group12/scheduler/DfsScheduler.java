package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

public class DfsScheduler implements Scheduler {

  private final AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
  private final AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  private final int workerCount;
  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  private AtomicInteger idleWorkers = new AtomicInteger(0);
  private Set<String> closed = ConcurrentHashMap.newKeySet();
  private List<DfsWorker> workers = new ArrayList<>();
  private Random random = new Random();
  private int syncThreshold = 1024;

  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;

  public DfsScheduler() {
    this.workerCount = 1;
  }

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
    this.status = SchedulerStatus.SCHEDULING;

    DfsWorker worker = new DfsWorker();
    worker.give(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));
    workers.add(worker);

    ExecutorService executor = Executors.newFixedThreadPool(workerCount);

    for (int i = 1; i < this.workerCount; i++) {
      workers.add(new DfsWorker());
    }

    for (DfsWorker dfsWorker : workers) {
      executor.submit(() -> branchAndBound(taskGraph, dfsWorker));
    }

    executor.shutdown();

    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule.get();
  }

  /**
   * Performs branch and bound algorithm on a given graph.
   *
   * @param taskGraph graph to perform branch and bound on
   * @param worker    Worker who contains to a thread that processes the branch and bound.
   */
  private void branchAndBound(Graph taskGraph, DfsWorker worker) {
    // DFS iteration (no optimisations)
    int syncCounter = 0;
    int localMinMakespan = this.currentMinMakespan.get();
    long localSearchCount = 0;
    long localPruneCount = 0;
    Set<String> closed = new HashSet<>();

    boolean hasWork = true;

    while (hasRunningWorker()) {
      while (hasWork) {
        Schedule currentSchedule = worker.steal();
        if (currentSchedule == null) {
          hasWork = false;
          idleWorkers.incrementAndGet();
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
          updateGlobalMinMakespanAndSchedule(currentSchedule);
          continue;
        }

        // Check to find if any tasks can be scheduled and schedule them
        for (Task task : currentSchedule.getReadyTasks()) {
          int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
          int allocableCount = currentSchedule.getAllocableProcessorCount();

          for (int i = 0; i < allocableCount; i++) {
            Schedule newSchedule = scheduleNextTask(task, latestStartTimes[i],
                currentSchedule.getProcessorEndTimes()[i], i, currentSchedule);

            if (scheduleIsPruned(newSchedule, localMinMakespan, closed)) {
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

  private boolean takeWorkFromRandomWorker(DfsWorker worker) {
    // Randomly choose a worker to steal from. This attempts to evenly distribute the workers being stolen from
    int index = this.random.nextInt(this.workerCount);
    DfsWorker dfsWorker = this.workers.get(index);
    if (dfsWorker == worker) {
      return false;
    }

    Schedule work = dfsWorker.steal();
    if (work != null) {
      worker.give(work);
      this.idleWorkers.decrementAndGet();
      return true;
    }

    return false;
  }

  private boolean hasRunningWorker() {
    return this.idleWorkers.get() < this.workerCount;
  }

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
  private boolean scheduleIsPruned(Schedule schedule, int localMinMakespan, Set<String> closed) {
    if (schedule.getEstimatedMakespan() >= localMinMakespan) {
      return true;
    }

    String stringHash = schedule.generateUniqueString();

    // No need to add the schedule to the closed set at this point as if we find this schedule
    // again it'll get pruned at this point again anyway, which saves memory.
    if (closed.contains(stringHash)) {
      return true;
    }

    closed.add(stringHash);
    return false;
  }

}
