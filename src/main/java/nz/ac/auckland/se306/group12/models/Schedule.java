package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
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

  /**
   * A constructor for creating a new schedule
   *
   * @param taskCount      The number of tasks in the schedule
   * @param processorCount The number of processors in the schedule
   */
  public Schedule(int taskCount, int processorCount) {
    scheduledTasks = new ScheduledTask[taskCount];
    processorEndTimes = new int[processorCount];
    scheduledTaskCount = 0;
    latestEndTime = 0;
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
    int newLatestEndTime = Math.max(this.latestEndTime, scheduledTask.getEndTime());

    return new Schedule(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1
    );
  }

  /**
   * Returns the end time of the schedule by taking the latest end time of all the processors
   *
   * @return The end time of the schedule
   */
  public int getEndTime() {
    return latestEndTime;
  }
}
