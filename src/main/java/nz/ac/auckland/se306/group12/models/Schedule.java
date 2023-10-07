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
  private final List<Task> readyTasks;

  // Estimation variables
  private final int totalTaskWeights;
  private final int endTimeEstimate;
  private final int totalIdleTime;

  /**
   * A constructor for creating a new schedule
   *
   * @param taskGraph      The task graph being scheduled
   * @param processorCount The number of processors in the schedule
   */
  public Schedule(Graph taskGraph, int processorCount) {
    this.scheduledTasks = new ScheduledTask[taskGraph.taskCount()];
    this.processorEndTimes = new int[processorCount];
    this.scheduledTaskCount = 0;
    this.latestEndTime = 0;
    this.readyTasks = taskGraph.getSourceTasks();
    this.totalTaskWeights = taskGraph.getTotalTaskWeights();
    this.totalIdleTime = 0;
    this.endTimeEstimate = this.getIdleEndTimeEstimate(this.totalIdleTime);
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
    int processorIndex = scheduledTask.getProcessorIndex();
    int taskIdleTime = scheduledTask.getStartTime() - newProcessorEndTimes[processorIndex];
    newProcessorEndTimes[processorIndex] = scheduledTask.getEndTime();

    int newTotalIdleTime = this.totalIdleTime + taskIdleTime;
    int newLatestEndTime = Math.max(this.latestEndTime, scheduledTask.getEndTime());
    int newEndTimeEstimate = this.calculateNewEndTimeEstimate(
        scheduledTask, task, newTotalIdleTime);

    return new Schedule(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1,
        this.getNewReadyTasks(task, newScheduledTasks),
        this.totalTaskWeights,
        newEndTimeEstimate,
        newTotalIdleTime
    );
  }

  /**
   * This method returns a list of tasks that are ready to be scheduled based on the current task
   * being scheduled
   *
   * @param task              The task that is being scheduled
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @return List of tasks that are ready to be scheduled
   */
  private List<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    List<Task> newReadyTasks = new ArrayList<>(this.readyTasks);
    newReadyTasks.remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (this.isTaskReady(newScheduledTasks, child)) {
        newReadyTasks.add(child);
      }
    }
    return newReadyTasks;
  }

  /**
   * This method checks if a task is ready to be scheduled
   * <p>
   * While this method checks through all incoming edges, the profiler shows that this method has
   * low impact on performance even though there are possible alternatives such as decrementing a
   * counter that counts the number of parents that have not been scheduled yet.
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param child             Child task to be checked if ready
   * @return True if the task is ready to be scheduled, false otherwise
   */
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
   * @param task Task to find the latest start time for
   * @return Array of latest start times for the task on each processor
   */
  public int[] getLatestStartTimesOf(Task task) {
    int processorCount = this.getProcessorCount();
    int[] latestStartTimes = new int[processorCount];

    // Loop through all parent tasks
    for (Edge incomingEdge : task.getIncomingEdges()) {
      int taskIndex = incomingEdge.getSource().getIndex();
      ScheduledTask parentScheduledTask = this.getScheduledTasks()[taskIndex];

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

  /**
   * This method returns the number of processors in the schedule
   *
   * @return The number of processors in the schedule
   */
  public int getProcessorCount() {
    return this.processorEndTimes.length;
  }

  /**
   * Determines the new end time estimate for the resulting schedule after adding a new scheduled
   * task. The estimate is admissible, which means that it will always be less than the actual time
   * of the resulting schedule. This ensures that we don't accidentally prune any optimal schedules
   * by thinking it will take longer than it actually does.
   *
   * @param scheduledTask    The {@link ScheduledTask} that was added to this schedule
   * @param task             The corresponding {@link Task} for the scheduled task
   * @param newTotalIdleTime The new total idle time
   * @return The new end time estimate
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Optimal
   * Scheduling of Task Graphs on Parallel Systems, Section 3.1</a>
   */
  private int calculateNewEndTimeEstimate(
      ScheduledTask scheduledTask,
      Task task,
      int newTotalIdleTime
  ) {
    return Math.max(
        Math.max(this.endTimeEstimate, this.getIdleEndTimeEstimate(newTotalIdleTime)),
        this.getBottomLevelEndTimeEstimate(scheduledTask, task));
  }

  /**
   * A possible underestimate of the end time of this schedule is the total weight of all the tasks
   * and the new accumulated idle time divided by the number of processors. We know this will be an
   * underestimate because it assumes all tasks can be evenly divided between processors, and it
   * doesn't factor in transfer time between processors.
   * <p>
   * This type of underestimate is beneficial with task graphs that have few edges.
   *
   * @param newTotalIdleTime The new total idle time
   * @return The idle underestimate of the end time
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Optimal
   * Scheduling of Task Graphs on Parallel Systems, Section 3.1</a>
   */
  private int getIdleEndTimeEstimate(int newTotalIdleTime) {
    return (newTotalIdleTime + this.totalTaskWeights) / this.getProcessorCount();
  }

  /**
   * This method returns the bottom level estimate of the end time of this scheduled task. This
   * estimate is determined by the start time and the bottom level of the task. This will always be
   * an underestimate because it doesn't factor in the transfer time between processors.
   *
   * @param scheduledTask The {@link ScheduledTask} to get the bottom level estimate for
   * @param task          The corresponding {@link Task} for the scheduled task
   * @return The bottom level estimate of this scheduled task
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Optimal
   * Scheduling of Task Graphs on Parallel Systems, Section 3.1</a>
   */
  private int getBottomLevelEndTimeEstimate(ScheduledTask scheduledTask, Task task) {
    return scheduledTask.getEndTime() + task.getBottomLevel();
  }

}
