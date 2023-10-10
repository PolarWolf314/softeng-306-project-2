package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;

public class DfsScheduler implements Scheduler {

  private int currentMinMakespan = Integer.MAX_VALUE;

  @Getter
  private long searchedCount = 0;
  @Getter
  private long prunedCount = 0;
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.SCHEDULING;
    Deque<Schedule> stack = new ArrayDeque<>();

    stack.push(new Schedule(taskGraph, processorCount));

    // DFS iteration (no optimisations)
    while (!stack.isEmpty()) {
      Schedule currentSchedule = stack.pop();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan) {
        this.prunedCount++;
        continue;
      }

      this.searchedCount++;

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        this.currentMinMakespan = currentSchedule.getLatestEndTime();
        this.bestSchedule = currentSchedule;
        continue;
      }

      currentSchedule.extendSchedule(stack);
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule;
  }

}
