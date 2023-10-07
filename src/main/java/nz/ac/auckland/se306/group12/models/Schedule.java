package nz.ac.auckland.se306.group12.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Schedule class represents a schedule of tasks
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class Schedule {

  private final ScheduledTask[] scheduledTasks;
  private final int[] processorEndTimes;
  private final int latestEndTime;
  private final int scheduledTaskCount;
  // Could consider storing readyTaskIndex, but only once task lookup is O(1)
  private final List<Task> readyTasks;

  /**
   * A constructor for creating a new schedule
   *
   * @param taskCount      The number of tasks in the schedule
   * @param processorCount The number of processors in the schedule
   */

  public Schedule(Graph taskGraph, int processorCount) {
    this.scheduledTasks = new ScheduledTask[taskGraph.taskCount()];
    this.processorEndTimes = new int[processorCount];
    this.scheduledTaskCount = 0;
    this.latestEndTime = 0;
    this.readyTasks = new ArrayList<>();
    // Add all source tasks as a ready task
    for (Task task : taskGraph.getTasks()) {
      if (task.getParentTasks().size() == 0) {
        readyTasks.add(task);
      }
    }
  }

  /**
   * Returns a new schedule with the given task added to the end of the schedule
   *
   * @param scheduledTask The scheduledTask repesentation of the task to add
   * @param task          The task to add
   * @return A new schedule with the given task added to the end of the schedule
   */
  public Schedule extendWithTask(ScheduledTask scheduledTask, Task task) {
    ScheduledTask[] newScheduledTasks = Arrays.copyOf(this.scheduledTasks,
        this.scheduledTasks.length);
    int[] newProcessorEndTimes = Arrays.copyOf(this.processorEndTimes,
        this.processorEndTimes.length);

    newScheduledTasks[task.getIndex()] = scheduledTask;
    newProcessorEndTimes[scheduledTask.getProcessorIndex()] = scheduledTask.getEndTime();

    int newLatestEndTime = Math.max(this.latestEndTime, scheduledTask.getEndTime());

    return new Schedule(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1,
        getNewReadyTasks(task, newScheduledTasks)
    );
  }

  private List<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    List<Task> newReadyTasks = new ArrayList<>(this.readyTasks);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (isTaskReady(newScheduledTasks, child)) {
        newReadyTasks.add(child);
      }
    }
    newReadyTasks.remove(task);
    return newReadyTasks;
  }

  private boolean isTaskReady(ScheduledTask[] newScheduledTasks, Task child) {
    for (Edge incomingEdge : child.getIncomingEdges()) {
      if (newScheduledTasks[incomingEdge.getSource().getIndex()] == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * This method finds the latest start time for a task on each processor
   *
   * @param processorCount Number of processors
   * @param task           Task to find the latest start time for
   * @return Array of latest start times for the task on each processor
   */
  public int[] getLatestStartTimesOf(Task task) {
    int processorCount = getProcessorCount();
    int[] latestStartTimes = new int[processorCount];

    // Loop through all parent tasks
    for (Edge incomingEdge : task.getIncomingEdges()) {
      int taskIndex = incomingEdge.getSource().getIndex();
      ScheduledTask parentScheduledTask = getScheduledTasks()[taskIndex];

      // Loop through all processors for latest start time
      for (int processorIndex = 0; processorIndex < processorCount; processorIndex++) {
        int newLatestStartTime = processorIndex == parentScheduledTask.getProcessorIndex()
            ? parentScheduledTask.getEndTime()
            : parentScheduledTask.getEndTime() + incomingEdge.getWeight();

        // Update latest start time if new latest start time is greater
        if (newLatestStartTime > latestStartTimes[processorIndex]) {
          latestStartTimes[processorIndex] = newLatestStartTime;
        }
      }

    }
    return latestStartTimes;
  }

  private int getProcessorCount() {
    return processorEndTimes.length;
  }

}
