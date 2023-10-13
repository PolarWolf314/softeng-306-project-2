package nz.ac.auckland.se306.group12.models;

import java.util.Set;

public class ScheduleWithAnEmptyProcessor extends Schedule {

  private final int nonEmptyProcessorCount;

  /**
   * Creates a new {@link ScheduleWithAnEmptyProcessor} instance from the given task graph and
   * processor count. The resulting schedule will be completely empty.
   */
  public ScheduleWithAnEmptyProcessor(Graph graph, int processorCount) {
    super(graph, processorCount);
    this.nonEmptyProcessorCount = 0;
  }

  /**
   * Creates a new {@link ScheduleWithAnEmptyProcessor} instance with all the required instance
   * fields.
   *
   * @param scheduledTasks         The tasks that have been scheduled so far
   * @param processorEndTimes      The end times of each processor
   * @param latestEndTime          The latest end time of any task in the schedule
   * @param scheduledTaskCount     The number of tasks that have been scheduled so far
   * @param readyTasks             The tasks that are ready to be scheduled
   * @param nonEmptyProcessorCount The number of processors that have at least one task on it
   * @param totalTaskWeights       The total weight of all the tasks in the task graph
   * @param estimatedMakespan      The estimated makespan of the schedule
   * @param totalIdleTime          The total idle time of the schedule
   */
  public ScheduleWithAnEmptyProcessor(
      ScheduledTask[] scheduledTasks,
      int[] processorEndTimes,
      int latestEndTime,
      int scheduledTaskCount,
      Set<Task> readyTasks,
      int nonEmptyProcessorCount,
      int totalTaskWeights,
      int estimatedMakespan,
      int totalIdleTime
  ) {
    super(scheduledTasks, processorEndTimes, latestEndTime, scheduledTaskCount,
        readyTasks, totalTaskWeights, estimatedMakespan, totalIdleTime);
    this.nonEmptyProcessorCount = nonEmptyProcessorCount;
  }

  /**
   * We always want to allow tasks to be allocated to all the processors that have at least one task
   * on it, plus one of the empty processors. This prevents creation of schedules which are simply
   * processor permutations.
   *
   * @inheritDoc
   */
  @Override
  public int getAllocableProcessors() {
    return this.nonEmptyProcessorCount + 1;
  }

  /**
   * When all the processors have at least one task on it, it will return a new {@link Schedule},
   * otherwise it will return a new {@link ScheduleWithAnEmptyProcessor}.
   *
   * @inheritDoc
   */
  @Override
  protected Schedule createInstance(
      ScheduledTask[] newScheduledTasks,
      int[] newProcessorEndTimes,
      int newLatestEndTime,
      Set<Task> newReadyTasks,
      int newEstimatedMakespan,
      int newTotalIdleTime
  ) {
    int newNonEmptyProcessorCount = this.nonEmptyProcessorCount;

    // If the task is scheduled on the empty processor, increment the non-empty processor count
    if (newProcessorEndTimes[this.nonEmptyProcessorCount] != 0) {
      newNonEmptyProcessorCount++;
    }

    // All the processors have a task on it. We can now return a normal schedule
    if (newNonEmptyProcessorCount == this.getProcessorCount()) {
      return super.createInstance(
          newScheduledTasks, newProcessorEndTimes, newLatestEndTime,
          newReadyTasks, newEstimatedMakespan, newTotalIdleTime
      );
    }

    return new ScheduleWithAnEmptyProcessor(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1,
        newReadyTasks,
        newNonEmptyProcessorCount,
        this.totalTaskWeights,
        newEstimatedMakespan,
        newTotalIdleTime
    );
  }

}
