package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.AOSchedule;
import nz.ac.auckland.se306.group12.models.Allocation;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class ParallelDfsAOScheduler implements Scheduler {

  private static final String HUMAN_READABLE_NAME = "DFS branch-and-bound (AO state space)";
  private final int threadCount;
  private final int syncThreshold = 4096;
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private AtomicReference<AOSchedule> bestAOSchedule = new AtomicReference<>();

  public ParallelDfsAOScheduler(int threadCount) {
    this.threadCount = threadCount;
  }

  /**
   * Updates the local schedule with the global concurrent schedule, as well as the makespan.
   *
   * @param schedule The {@link AOSchedule} to use to update the global min makespan.
   */
  private synchronized void updateGlobalMinMakespanAndSchedule(AOSchedule schedule) {
    int makespan = schedule.getLatestEndTime();
    if (makespan < this.currentMinMakespan.get()) {
      this.currentMinMakespan.set(makespan);
      this.bestAOSchedule.set(schedule);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    ExecutorService executor = Executors.newFixedThreadPool(this.threadCount);

    this.status = SchedulerStatus.SCHEDULING;
    Queue<Allocation> stack = Collections.asLifoQueue(new ArrayDeque<>());
    stack.add(new Allocation(taskGraph, processorCount));

    while (!stack.isEmpty()) {
      Allocation currentAllocation = stack.remove();

      // Prune if current allocation is worse than current best schedule
      // Later change this to the allocation heuristic check
      if (currentAllocation.getAllocationHeuristic() >= this.currentMinMakespan.get()) {
        // pruned count here is pruning allocations which is technically different from pruning branches
        continue;
      }
      // Same for searching here

      if (currentAllocation.isComplete()) {
        executor.submit(() -> this.order(currentAllocation));
        continue;
      }
      currentAllocation.extendAllocation(stack);
    }

    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestAOSchedule.get().asSchedule();
  }

  /**
   * Perfects the ordering of the tasks in an allocation. This will find all valid possible
   * schedules based on their processor allocations and dependences.
   *
   * @param allocation The allocation to order
   */
  private void order(Allocation allocation) {
    Queue<AOSchedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
    int syncCounter = 0;
    int localMinMakespan = this.currentMinMakespan.get();
    int localPrunedCount = 0;
    int localSearchedCount = 0;

    queue.add(new AOSchedule(allocation));

    while (!queue.isEmpty()) {
      AOSchedule currentSchedule = queue.remove();
      syncCounter++;

      if (syncCounter == this.syncThreshold) {
        localMinMakespan = this.currentMinMakespan.get();
        this.prunedCount.getAndAdd(localPrunedCount);
        this.searchedCount.getAndAdd(localSearchedCount);
        localSearchedCount = 0;
        localPrunedCount = 0;
        syncCounter = 0;
      }

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= localMinMakespan) {
        localPrunedCount++;
        continue;
      }

      localSearchedCount++;

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == allocation.getTaskGraph().taskCount()) {
        localMinMakespan = currentSchedule.getLatestEndTime();
        this.updateGlobalMinMakespanAndSchedule(currentSchedule);
        continue;
      }

      // Check to find if any tasks can be scheduled and schedule any valid schedules
      for (Task task : currentSchedule.getReadyTasks()) {
        ScheduledTask newScheduledTask = currentSchedule.extendWithTask(task);
        AOSchedule newSchedule = currentSchedule.extendWithTask(newScheduledTask, task);
        if (newSchedule != null) {
          queue.add(newSchedule);
        }
      }
    }

  }

  @Override
  public String getHumanReadableName() {
    return HUMAN_READABLE_NAME;
  }

  @Override
  public Schedule getBestSchedule() {
    AOSchedule bestAOSchedule = this.bestAOSchedule.get();
    if (bestAOSchedule == null) {
      return null;
    }
    return bestAOSchedule.asSchedule();
  }

  @Override
  public long getSearchedCount() {
    return this.searchedCount.get();
  }

  @Override
  public long getPrunedCount() {
    return this.prunedCount.get();
  }

}
