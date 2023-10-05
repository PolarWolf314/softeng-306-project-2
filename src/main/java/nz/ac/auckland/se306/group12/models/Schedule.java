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
   * @param scheduledTask The task to add
   * @param taskIndex     The index of the task to add
   * @return A new schedule with the given task added to the end of the schedule
   */
  public Schedule extendWithTask(ScheduledTask scheduledTask, int taskIndex) {
    ScheduledTask[] newScheduledTasks = Arrays.copyOf(this.scheduledTasks,
        this.scheduledTasks.length);
    int[] newProcessorEndTimes = Arrays.copyOf(this.processorEndTimes,
        this.processorEndTimes.length);
    newScheduledTasks[taskIndex] = scheduledTask;
    newProcessorEndTimes[scheduledTask.getProcessorIndex()] = scheduledTask.getEndTime();
    return new Schedule(newScheduledTasks, newProcessorEndTimes, this.scheduledTaskCount + 1,
        this.readyTasks);
  }

  /**
   * Returns the end time of the schedule by taking the latest end time of all the processors
   *
   * @return The end time of the schedule
   */
  public int getEndTime() {
    //noinspection OptionalGetWithoutIsPresent
    return Arrays.stream(this.processorEndTimes).max().getAsInt();
  }
}
