package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
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
    super(scheduledTasks, processorEndTimes, latestEndTime, scheduledTaskCount, readyTasks,
        totalTaskWeights, estimatedMakespan, totalIdleTime);
    this.nonEmptyProcessorCount = nonEmptyProcessorCount;
  }

  @Override
  public int getLoopCount() {
    return this.nonEmptyProcessorCount + 1;
  }

  @Override
  public Schedule extendWithTask(ScheduledTask scheduledTask, Task task) {
    int newNonEmptyProcessorCount = this.nonEmptyProcessorCount;

    // If the task is scheduled on a new processor, increment the non-empty processor count
    if (this.nonEmptyProcessorCount == scheduledTask.getProcessorIndex()) {
      newNonEmptyProcessorCount++;
    }

    // All the processors have a task on it. We no longer need to return a schedule with an empty processor
    if (newNonEmptyProcessorCount == this.getProcessorCount()) {
      return super.extendWithTask(scheduledTask, task);
    }

    // Yes this is duplicate code, but idk how to get rid of it without create a new instance
    // which defeats the point of creating this whole class (To save memory)
    ScheduledTask[] newScheduledTasks = Arrays.copyOf(this.scheduledTasks,
        this.scheduledTasks.length);
    int[] newProcessorEndTimes = Arrays.copyOf(this.processorEndTimes,
        this.processorEndTimes.length);

    newScheduledTasks[task.getIndex()] = scheduledTask;
    int processorIndex = scheduledTask.getProcessorIndex();
    int taskIdleTime = scheduledTask.getStartTime() - newProcessorEndTimes[processorIndex];
    newProcessorEndTimes[processorIndex] = scheduledTask.getEndTime();

    int newTotalIdleTime = this.totalIdleTime + taskIdleTime;
    int newLatestEndTime = Math.max(this.latestEndTime, scheduledTask.getEndTime());
    int newEstimatedMakespan = this.estimateNewMakespan(scheduledTask, task, newTotalIdleTime);

    return new ScheduleWithAnEmptyProcessor(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1,
        this.getNewReadyTasks(task, newScheduledTasks),
        newNonEmptyProcessorCount,
        this.totalTaskWeights,
        newEstimatedMakespan,
        newTotalIdleTime
    );
  }

}
