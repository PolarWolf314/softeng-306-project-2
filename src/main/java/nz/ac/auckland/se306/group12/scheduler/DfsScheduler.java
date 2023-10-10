package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;

public class DfsScheduler implements Scheduler {

  private int currentMinMakespan = Integer.MAX_VALUE;
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {

    Deque<Schedule> stack = new ArrayDeque<>();

    stack.push(new Schedule(taskGraph, processorCount));

    // DFS iteration (no optimisations)
    while (!stack.isEmpty()) {
      Schedule currentSchedule = stack.pop();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan) {
        continue;
      }

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        this.currentMinMakespan = currentSchedule.getLatestEndTime();
        this.bestSchedule = currentSchedule;
        continue;
      }

      currentSchedule.extendSchedule(stack);
    }

    return this.bestSchedule;
  }

}
