package nz.ac.auckland.se306.group12.models;

import java.util.Set;

public class ScheduleWithAnEmptyProcessor extends Schedule {

  private final int nonEmptyProcessorCount;

  public ScheduleWithAnEmptyProcessor(Graph graph, int processorCount) {
    super(graph, processorCount);
    this.nonEmptyProcessorCount = 0;
  }

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
   * @inheritDoc
   */
  @Override
  public int getAllocatableProcessors() {
    return this.nonEmptyProcessorCount + 1;
  }

  /**
   * When all the processors have at least one task on it, it will return an {@link Schedule},
   * otherwise it will continue to return a {@link ScheduleWithAnEmptyProcessor}.
   *
   * @inheritDoc
   */
  @Override
  protected Schedule createInstance(
      ScheduledTask[] newScheduledTasks, int[] newProcessorEndTimes, int newLatestEndTime,
      Set<Task> newReadyTasks, int newEstimatedMakespan, int newTotalIdleTime
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
