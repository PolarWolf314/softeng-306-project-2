package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Set<Thread> threads = ConcurrentHashMap.newKeySet();
  private AtomicInteger idleWorkers = new AtomicInteger(0);
  private Set<String> closed = ConcurrentHashMap.newKeySet();
  private List<DfsWorker> workers = new ArrayList<>();
  private int syncThreshold = 64;
  private int workerNum = 1;

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
    this.workerNum = 16;

    DfsWorker worker = new DfsWorker();
    worker.give(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));
    workers.add(worker);

    for (int i = 1; i < this.workerNum; i++) {
      workers.add(new DfsWorker());
    }

    // Grab the schedule result from first worker
    Schedule schedule = branchAndBound(taskGraph, worker);

    // Instantiate other workers to steal from worker
    for (int i = 1; i < this.workerNum; i++) {
      DfsWorker currentWorker = workers.get(i);
      Thread thread = new Thread(() -> {
        branchAndBound(taskGraph, currentWorker);
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : this.threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    this.status = SchedulerStatus.SCHEDULED;
    return schedule;
  }

  /**
   * Performs branch and bound algorithm on a given graph.
   *
   * @param taskGraph graph to perform branch and bound on
   * @param worker    Worker who contains to a thread that processes the branch and bound.
   * @return
   */
  private Schedule branchAndBound(Graph taskGraph, DfsWorker worker) {
    // DFS iteration (no optimisations)
    int syncCounter = 0;
    int localMinMakespan = this.currentMinMakespan.get();
    while (this.idleWorkers.get() < this.workers.size()) {
      while (true) {
        if (worker.getQueue().isEmpty()) {
          this.idleWorkers.incrementAndGet();
          break;
        }
        Schedule currentSchedule = worker.steal();
        syncCounter++;

        if (syncCounter == this.syncThreshold) {
          localMinMakespan = this.currentMinMakespan.get();
          syncCounter = 0;
        }

        // Prune if current schedule is worse than current best
        if (currentSchedule.getEstimatedMakespan() >= localMinMakespan) {
          this.prunedCount.incrementAndGet();
          continue;
        }

        this.searchedCount.incrementAndGet();

        // Check if current schedule is complete
        if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
          localMinMakespan = currentSchedule.getLatestEndTime();
          if (localMinMakespan < this.currentMinMakespan.get()) {
            this.currentMinMakespan = new AtomicInteger(localMinMakespan);
            this.bestSchedule.set(currentSchedule);
          }
          continue;
        }

        // Check to find if any tasks can be scheduled and schedule them
        for (Task task : currentSchedule.getReadyTasks()) {
          int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
          for (int i = 0; i < currentSchedule.getAllocableProcessorCount(); i++) {
            Schedule newSchedule = scheduleNextTask(task, latestStartTimes[i],
                currentSchedule.getProcessorEndTimes()[i], i, currentSchedule);

            if (scheduleIsPruned(newSchedule, localMinMakespan)) {
              continue;
            }

            worker.give(newSchedule);
          }
        }
      }

      Random rand = new Random();
      int randomInt = rand.nextInt(this.workerNum);

      if (workers.get(randomInt).isHasWork()) {
        worker.give(workers.get(randomInt).steal());
        this.idleWorkers.decrementAndGet();
      }

    }
    return this.bestSchedule.get();
  }

  /**
   * @param task
   * @param latestStartTime
   * @param latestProcessorEndTime
   * @param processorIndex
   * @param currentSchedule
   * @return
   */
  private Schedule scheduleNextTask(Task task, int latestStartTime, int latestProcessorEndTime,
      int processorIndex, Schedule currentSchedule) {
    // Ensure that it either schedules by latest time or after the last task on the processor
    int startTime = Math.max(latestStartTime, latestProcessorEndTime);
    int endTime = startTime + task.getWeight();
    ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, processorIndex);
    return currentSchedule.extendWithTask(newScheduledTask, task);
  }

  private boolean scheduleIsPruned(Schedule schedule, int localMinMakespan) {
    if (schedule.getEstimatedMakespan() >= localMinMakespan) {
      this.prunedCount.incrementAndGet();
      return true;
    }

    String stringHash = schedule.generateUniqueString();

    // No need to add the schedule to the closed set at this point as if we find this schedule
    // again it'll get pruned at this point again anyway, which saves memory.
    if (this.closed.contains(stringHash)) {
      this.prunedCount.incrementAndGet();
      return true;
    }

    this.closed.add(stringHash);
    return false;
  }

}
