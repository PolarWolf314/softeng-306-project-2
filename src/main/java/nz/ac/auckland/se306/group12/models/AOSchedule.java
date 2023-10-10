package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import nz.ac.auckland.se306.group12.models.datastructures.TaskSet;

/**
 * AOSchedule
 */
public class AOSchedule extends Schedule {

  private final Allocation allocation;
  private final int processorIndex;
  private final Set<Task> readyTasks;

  public AOSchedule(Graph taskGraph, int processorCount, Allocation allocation) {
    // readyTasks defined in super constructor will get overwritten
    super(taskGraph, processorCount);
    this.allocation = allocation;
    this.processorIndex = 0;
    this.readyTasks = new TaskSet(taskGraph);
    for (Task task : taskGraph.getTasks()) {
      // check if the task being checked is the current local processor
      if (getTaskProcessor(task) == this.processorIndex) {
        if (isTaskReady(this.scheduledTasks, task)) {
          this.readyTasks.add(task);
        }
      }
    }

  }

  /**
   * This method adds all children of the current schedule to the stack
   *
   * @param queue Queue to add children to
   */
  public void extendSchedule(Queue<Schedule> queue) {
    // Check to find if any tasks can be scheduled and schedule them
    for (Task task : getReadyTasks()) {
      int latestStartTime = getLatestStartTimeOf(task);
      // Ensure that it either schedules by latest time or after the last task on the processor
      int startTime = Math.max(latestStartTime, getProcessorEndTimes()[this.processorIndex]);
      int endTime = startTime + task.getWeight();
      ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, this.processorIndex);
      queue.add(extendWithTask(newScheduledTask, task));
    }
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

    return new Schedule(
        newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        this.scheduledTaskCount + 1,
        this.getNewReadyTasks(task, newScheduledTasks),
        this.totalTaskWeights,
        newEstimatedMakespan,
        newTotalIdleTime
    );
  }

  /**
   * This method checks if a task is ready to be scheduled locally on the processor
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param child             Child task to be checked if ready
   * @return True if the task is ready to be scheduled, false otherwise
   */
  private boolean isTaskReady(ScheduledTask[] newScheduledTasks, Task child) {
    int processorNumber = getTaskProcessor(child);
    for (Edge incomingEdge : child.getIncomingEdges()) {
      int parentIndex = incomingEdge.getSource().getIndex();
      ScheduledTask parent = newScheduledTasks[parentIndex];

      // return not ready if the parent isn't scheduled and is allocated on the same processor
      if (parent == null && this.allocation.getTasksProcessor()[parentIndex] == processorNumber) {
        return false;
      }
    }
    return true;
  }

  private int getTaskProcessor(Task child) {
    return this.allocation.getTasksProcessor()[child.getIndex()];
  }

  /**
   * This method finds the latest start time for a task on its allocated processor
   * <p>
   *
   * @param task Task to find the latest start time for
   * @return Array of latest start times for the task on each processor
   */
  private int getLatestStartTimeOf(Task task) {
    int taskProcessor = this.getTaskProcessor(task);
    int latestStartTime = 0;
    // Loop through all parent tasks
    for (Edge incomingEdge : task.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();
      ScheduledTask parentScheduledTask = this.getScheduledTasks()[parentTask.getIndex()];
      if (parentScheduledTask == null) {
        continue;
      }
      int newLatestStartTime = taskProcessor == parentScheduledTask.getProcessorIndex()
          ? parentScheduledTask.getEndTime()
          : parentScheduledTask.getEndTime() + incomingEdge.getWeight();

      // Update latest start time if new latest start time is greater
      if (newLatestStartTime > latestStartTime) {
        latestStartTime = newLatestStartTime;
      }
    }
    return latestStartTime;

  }

  private Set<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    Set<Task> newReadyTasks = new TaskSet(this.readyTasks);
    newReadyTasks.remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (this.isTaskReady(newScheduledTasks, child)) {
        newReadyTasks.add(child);
      }
    }
    return newReadyTasks;
  }

}
