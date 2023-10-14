package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import nz.ac.auckland.se306.group12.models.datastructures.BitSet;

/**
 * Schedule class represents a schedule of tasks
 */
@Getter
@RequiredArgsConstructor
@ToString
public class Schedule implements Comparable<Schedule> {

  protected final ScheduledTask[] scheduledTasks;
  protected final int[] processorEndTimes;
  protected final int latestEndTime;
  protected final int scheduledTaskCount;
  protected final Set<Task> readyTasks;

  // Estimation variables
  protected final int totalTaskWeights;
  protected final int estimatedMakespan;
  protected final int totalIdleTime;

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
    this.estimatedMakespan = this.estimateIdleTimeMakespan(this.totalIdleTime);
  }

  /**
   * Generates a unique string that can be used to identify this schedule. This is used for the
   * closed set to prune schedules that have already been visited without having to keep a reference
   * to this instance (allowing it to be garbage collected). A scheduled task is uniquely identified
   * by its taskIndex, startTime and processorIndex, hence all 3 need to be included in the string.
   * <p>
   * Note: This doesn't have to be cached as a field because it is only called once per schedule.
   *
   * @return The unique string representation of this schedule
   */
  public String generateUniqueString() {
    StringBuilder sb = new StringBuilder();
    int taskCount = this.scheduledTasks.length;
    for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
      ScheduledTask scheduledTask = this.scheduledTasks[taskIndex];
      if (scheduledTask != null) {
        // Separate numbers with commas so that we don't accidentally create new numbers that
        // collide with other schedules
        sb.append(taskIndex).append(',')
            .append(scheduledTask.getStartTime()).append(',')
            .append(scheduledTask.getProcessorIndex()).append(';');
      }
    }
    return sb.toString();
  }

  /**
   * Returns the number of processors that support having a task scheduled on it. If this returns n,
   * then that means processors 0 to n-1 are available for scheduling. This enables processor
   * normalization, which prevents permutations of the same schedule from being generated.
   *
   * @return The number of processors that support having a task scheduled on it
   */
  public int getAllocableProcessorCount() {
    return this.getProcessorCount();
  }

  /**
   * Returns a new schedule with the given task added to the end of the schedule
   *
   * @param scheduledTask The scheduledTask representation of the task to add
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
    int newEstimatedMakespan = this.estimateNewMakespan(scheduledTask, task, newTotalIdleTime);

    return this.createInstance(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.getNewReadyTasks(task, newScheduledTasks),
        newEstimatedMakespan,
        newTotalIdleTime
    );
  }

  /**
   * A method for creating a new schedule instance. This has been separated out into a method
   * despite only calling the constructor so that it can be overridden in subclasses.
   *
   * @param newScheduledTasks    The new scheduled tasks
   * @param newProcessorEndTimes The new ends times for each processor
   * @param newLatestEndTime     Thew new latest end time for the schedule
   * @param newReadyTasks        The new tasks that are ready
   * @param newEstimatedMakespan The new estimated makespan for the schedule
   * @param newTotalIdleTime     The new total idle time of the schedule
   * @return The created {@link Schedule} instance
   */
  protected Schedule createInstance(
      ScheduledTask[] newScheduledTasks,
      int[] newProcessorEndTimes,
      int newLatestEndTime,
      Set<Task> newReadyTasks,
      int newEstimatedMakespan,
      int newTotalIdleTime
  ) {
    return new Schedule(
        newScheduledTasks, newProcessorEndTimes, newLatestEndTime, this.scheduledTaskCount + 1,
        newReadyTasks, this.totalTaskWeights, newEstimatedMakespan, newTotalIdleTime
    );
  }

  /**
   * This method returns a set of tasks that are ready to be scheduled based on the current task
   * being scheduled. A task is considered ready if all its parent tasks have been scheduled.
   *
   * @param task              The task that is being scheduled
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @return A {@link Set} containing the tasks that are ready to be scheduled
   */
  protected Set<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    Set<Task> newReadyTasks = new BitSet<>(this.readyTasks);
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
   * Determines the new makespan estimate for the resulting schedule after adding a new scheduled
   * task. The estimate is admissible, which means that it will always be less than the actual time
   * of the resulting schedule. This ensures that we don't accidentally prune any optimal schedules
   * by thinking it will take longer than it actually does.
   *
   * @param scheduledTask    The {@link ScheduledTask} that was added to this schedule
   * @param task             The corresponding {@link Task} for the scheduled task
   * @param newTotalIdleTime The new total idle time
   * @return The new makespan estimate
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Sinnen,
   * Kozlov & Shahul: Optimal Scheduling of Task Graphs on Parallel Systems</a>, Section 3.1
   */
  protected int estimateNewMakespan(ScheduledTask scheduledTask, Task task, int newTotalIdleTime) {
    return Math.max(
        Math.max(this.estimatedMakespan, this.estimateIdleTimeMakespan(newTotalIdleTime)),
        this.estimateBottomLevelMakespan(scheduledTask, task));
  }

  /**
   * A possible underestimate of the makespan of this schedule is the total weight of all the tasks
   * and the new accumulated idle time divided by the number of processors. We know this will be an
   * underestimate because it assumes all tasks can be evenly divided between processors, and it
   * doesn't factor in transfer time between processors.
   * <p>
   * This type of underestimate is beneficial with task graphs that have few edges.
   *
   * @param newTotalIdleTime The new total idle time
   * @return The idle underestimate of the makespan
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Sinnen,
   * Kozlov & Shahul: Optimal Scheduling of Task Graphs on Parallel Systems</a>, Section 3.1
   */
  private int estimateIdleTimeMakespan(int newTotalIdleTime) {
    return (newTotalIdleTime + this.totalTaskWeights) / this.getProcessorCount();
  }

  /**
   * This method returns the bottom level estimate of the makespan of this scheduled task. This
   * estimate is determined by the start time and the bottom level of the task. This will always be
   * an underestimate because it doesn't factor in the transfer time between processors.
   *
   * @param scheduledTask The {@link ScheduledTask} to get the bottom level estimate for
   * @param task          The corresponding {@link Task} for the scheduled task
   * @return The bottom level estimate of this scheduled task
   * @see <a href="https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.329.9084">Sinnen,
   * Kozlov & Shahul: Optimal Scheduling of Task Graphs on Parallel Systems</a>, Section 3.1
   */
  private int estimateBottomLevelMakespan(ScheduledTask scheduledTask, Task task) {
    return scheduledTask.getEndTime() + task.getBottomLevel();
  }

  /**
   * Compares this schedule with another schedule based on the estimated makespan of the schedule.
   * The schedule with the lowest estimated makespan will have a higher priority.
   *
   * @param otherSchedule The {@link Schedule} to compare to
   * @return A positive integer if this schedule has a lower estimated makespan, 0 if they're equal
   * or a negative integer if this schedule has a higher estimated makespan
   */
  @Override
  public int compareTo(Schedule otherSchedule) {
    return Integer.compare(this.estimatedMakespan, otherSchedule.estimatedMakespan);
  }

  /**
   * Computes whether a schedule is equal to another by iterating through their scheduled tasks,
   * returning false if any of the tasks are not the same else returns true.
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Schedule comparison)) {
      return false;
    }
    ScheduledTask[] comparisonTaskList = comparison.getScheduledTasks();
    for (int i = 0; i < this.scheduledTasks.length; i++) {
      if (this.scheduledTasks[i] != comparisonTaskList[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.scheduledTasks);
  }

}
