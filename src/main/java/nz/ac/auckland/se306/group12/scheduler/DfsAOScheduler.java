package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.AOSchedule;
import nz.ac.auckland.se306.group12.models.Allocation;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsAOScheduler implements Scheduler {

  private static final String HUMAN_READABLE_NAME = "DFS branch-and-bound (AO state space)";
  private int currentMinMakespan = Integer.MAX_VALUE;

  @Getter
  private long searchedCount = 0;
  @Getter
  private long prunedCount = 0;
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;
  private AOSchedule bestAOSchedule = null;

  /**
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.SCHEDULING;
    Queue<Allocation> stack = Collections.asLifoQueue(new ArrayDeque<>());
    stack.add(new Allocation(taskGraph, processorCount));

    while (!stack.isEmpty()) {
      Allocation currentAllocation = stack.remove();

      // Prune if current allocation is worse than current best schedule
      // Later change this to the allocation heuristic check
      if (currentAllocation.getAllocationHeuristic() >= this.currentMinMakespan) {
        // pruned count here is pruning allocations which is technically different from pruning branches
        continue;
      }
      // Same for searching here

      if (currentAllocation.isComplete()) {
        this.order(currentAllocation);
        continue;
      }
      currentAllocation.extendAllocation(stack);
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestAOSchedule.asSchedule();
  }

  /**
   * Perfects the ordering of the tasks in an allocation. This will find all valid possible
   * schedules based on their processor allocations and dependences.
   *
   * @param allocation The allocation to order
   */
  private void order(Allocation allocation) {
    Queue<AOSchedule> queue = Collections.asLifoQueue(new ArrayDeque<>());

    queue.add(new AOSchedule(allocation));

    while (!queue.isEmpty()) {
      AOSchedule currentSchedule = queue.remove();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan) {
        this.prunedCount++;
        continue;
      }

      this.searchedCount++;

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == allocation.getTaskGraph().taskCount()) {
        this.currentMinMakespan = currentSchedule.getLatestEndTime();
        this.bestAOSchedule = currentSchedule;
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

}
