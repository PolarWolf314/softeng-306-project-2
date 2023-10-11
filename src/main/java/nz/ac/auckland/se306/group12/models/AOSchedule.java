package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.datastructures.TaskSet;

/**
 * AOSchedule
 */
@Getter
@RequiredArgsConstructor
public class AOSchedule {

  private final ScheduledTask[] scheduledTasks;
  private final int[] processorEndTimes;
  private final int latestEndTime;
  private final int scheduledTaskCount;

  private final Allocation allocation;
  private final int localIndex;
  private final int localOrderedCount;
  private final int localOrderedWeight;
  private final Set<Task> readyTasks;
  private final Graph taskGraph;

  public AOSchedule(Graph taskGraph, int processorCount, Allocation allocation) {
    // readyTasks defined in super constructor will get overwritten
    this.scheduledTasks = new ScheduledTask[taskGraph.taskCount()];
    this.processorEndTimes = new int[processorCount];
    this.scheduledTaskCount = 0;
    this.latestEndTime = 0;

    this.allocation = allocation;
    this.localIndex = 0;
    this.taskGraph = taskGraph;
    this.readyTasks = getProcessorReadyTasks(0);
    this.localOrderedCount = 0;
    this.localOrderedWeight = 0;

  }

  private Set<Task> getProcessorReadyTasks(int processorIndex) {
    Set<Task> newReadyTasks = new TaskSet(this.taskGraph);
    for (Task task : taskGraph.getTasks()) {
      // check if the task being checked is the current local processor
      if (getTaskProcessor(task) == processorIndex) {
        if (isTaskReady(this.scheduledTasks, task)) {
          newReadyTasks.add(task);
        }
      }
    }
    return newReadyTasks;
  }

  /**
   * This method adds all children of the current schedule to the stack
   *
   * @param queue Queue to add children to
   */
  public void extendSchedule(Queue<AOSchedule> queue) {
    // Check to find if any tasks can be scheduled and schedule them
    for (Task task : getReadyTasks()) {
      int latestStartTime = getLatestStartTimeOf(task);
      // Ensure that it either schedules by latest time or after the last task on the processor
      int startTime = Math.max(latestStartTime, getProcessorEndTimes()[this.localIndex]);
      int endTime = startTime + task.getWeight();
      ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, this.localIndex);
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
  public AOSchedule extendWithTask(ScheduledTask scheduledTask, Task task) {
    ScheduledTask[] newScheduledTasks = Arrays.copyOf(this.scheduledTasks,
        this.scheduledTasks.length);
    int[] newProcessorEndTimes = Arrays.copyOf(this.processorEndTimes,
        this.processorEndTimes.length);

    newScheduledTasks[task.getIndex()] = scheduledTask;
    int processorIndex = scheduledTask.getProcessorIndex();
    newProcessorEndTimes[processorIndex] = scheduledTask.getEndTime();
    int newLatestEndTime = Math.max(this.latestEndTime, scheduledTask.getEndTime());
    int newLocalOrderedCount = this.localOrderedCount + 1;
    int newLocalOrderedWeight = this.localOrderedWeight + task.getWeight();
    int newLocalIndex = this.localIndex;
    Set<Task> newReadyTasks;
    // if all tasks on current processor have been allocated move to next processor
    if (this.localOrderedCount == this.allocation.getProcessors()[this.localIndex].size()) {
      newLocalOrderedCount = 0;
      newLocalOrderedWeight = 0;
      newLocalIndex++;
      newReadyTasks = this.getProcessorReadyTasks(newLocalIndex);
    } else {
      newReadyTasks = this.getNewReadyTasks(task, newScheduledTasks);
    }

    return new AOSchedule(newScheduledTasks,
        newProcessorEndTimes,
        newLatestEndTime,
        scheduledTaskCount + 1,
        allocation,
        newLocalIndex,
        newLocalOrderedCount,
        newLocalOrderedWeight,
        newReadyTasks,
        taskGraph);
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
