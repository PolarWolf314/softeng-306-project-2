package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;

import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.Task;

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
      if (currentSchedule.getLatestEndTime() >= currentMinMakespan) {
        continue;
      }

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        currentMinMakespan = currentSchedule.getLatestEndTime();
        this.bestSchedule = currentSchedule;
        continue;
      }

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : taskGraph.getTasks()) {
        if (!isSchedulable(currentSchedule, task)) {
          continue;
        }
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

    return this.bestSchedule;
  }

  /**
   * This method checks if a task is schedulable based on if all its parents are scheduled
   *
   * @param currentSchedule Schedule at the current state of the DFS
   * @param task            Task to check if it is schedulable
   * @return true if the task is schedulable, false otherwise
   */
  private boolean isSchedulable(Schedule currentSchedule, Task task) {
    // Loop through all parent tasks
    if (currentSchedule.getScheduledTasks()[task.getIndex()] != null) {
      return false;
    }
    for (Edge incomingEdge : task.getIncomingEdges()) {
      int sourceIndex = incomingEdge.getSource().getIndex();
      ScheduledTask parentScheduledTask = currentSchedule.getScheduledTasks()[sourceIndex];
      if (parentScheduledTask == null) {
        return false;
      }
    }
    return true;
  }

}
