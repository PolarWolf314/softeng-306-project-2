package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;

import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.Task;

public class DFS implements Scheduler {

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph graph, int processorCount) {

    Deque<Schedule> stack = new ArrayDeque<>();

    stack.push(new Schedule(graph.getTasks().size(), processorCount));

    // DFS iteration (no optimisations)
    while (!stack.isEmpty()) {
      Schedule currentSchedule = stack.pop();

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : graph.getTasks()) {
        if (!isSchedulable(currentSchedule, task)) {
          continue;
        }
        System.out.println(task);

        int[] latestStartTimes = getLatestStartTimes(processorCount, currentSchedule, task);
        System.out.println("Lastest");
        for (int i = 0; i < latestStartTimes.length; i++) {
          int startTime = latestStartTimes[i];
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          System.out.println(newScheduledTask);
          // stack.push(currentSchedule.extendWithTask(newScheduledTask, task.getIndex()));
          //
          // System.out.println("Proc: " + startTime);
        }
      }
    }

    // not throwing exception for testing purposes
    return null;
    // throw new UnsupportedOperationException("Unimplemented method 'schedule'");
  }

  /**
   * This method finds the latest start time for a task on each processor
   *
   * @param processorCount  Number of processors
   * @param currentSchedule Schedule at the current state of the DFS
   * @param task            Task to find the latest start time for
   * @return Array of latest start times for the task on each processor
   */
  private int[] getLatestStartTimes(int processorCount, Schedule currentSchedule, Task task) {
    // Find the latest start time for the task on each processor
    int[] latestStartTimes = new int[processorCount];

    // Loop through all parent tasks
    for (Edge sources : task.getIncomingEdges()) {
      int taskIndex = sources.getSource().getIndex();
      ScheduledTask parentScheduledTask = currentSchedule.getScheduledTasks()[taskIndex];

      // Loop through all processors for latest start time
      for (int currentProcessor = 0; currentProcessor < processorCount; currentProcessor++) {
        int newLatestStartTime = currentProcessor == parentScheduledTask.getProcessorIndex()
            ? parentScheduledTask.getEndTime()
            : parentScheduledTask.getEndTime() + sources.getWeight();

        // Update latest start time if new latest start time is greater
        if (newLatestStartTime > latestStartTimes[currentProcessor]) {
          latestStartTimes[currentProcessor] = newLatestStartTime;
        }
      }
    }
    return latestStartTimes;
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
    for (Edge sources : task.getIncomingEdges()) {
      int sourceIndex = sources.getSource().getIndex();
      ScheduledTask parentScheduledTask = currentSchedule.getScheduledTasks()[sourceIndex];
      if (parentScheduledTask == null) {
        return false;
      }
    }
    return true;
  }

}
