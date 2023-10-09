package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsScheduler implements Scheduler {

  private int currentMinMakespan = Integer.MAX_VALUE;

  @Getter
  private long searchedCount;
  @Getter
  private long prunedCount;
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.RUNNING;
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

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : currentSchedule.getReadyTasks()) {
        int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
        for (int i = 0; i < latestStartTimes.length; i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          stack.push(currentSchedule.extendWithTask(newScheduledTask, task));
        }
      }
    }

    this.status = SchedulerStatus.IDLE;
    return this.bestSchedule;
  }

}
