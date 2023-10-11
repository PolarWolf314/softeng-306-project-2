package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.datastructures.BitSet;

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
  // The following fields are to allow for quick lookup of the previous scheduled task on the same processor
  private final int[] nextTasks;
  private final int previousTaskIndex;

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
    this.nextTasks = new int[taskGraph.taskCount()];
    Arrays.fill(this.nextTasks, -1);
    this.previousTaskIndex = -1;

  }

  private Set<Task> getProcessorReadyTasks(int processorIndex) {
    Set<Task> newReadyTasks = new BitSet<Task>(this.taskGraph);
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
    int newLocalOrderedCount = this.localOrderedCount + 1;
    int newLocalOrderedWeight = this.localOrderedWeight + task.getWeight();
    int newLocalIndex = this.localIndex;

    // set the next task on the processor of a task to be the extending task
    int[] newNextTasks = this.nextTasks;
    if (previousTaskIndex != -1) {
      newNextTasks[this.previousTaskIndex] = task.getIndex();
    }
    int newPreviousTaskIndex = task.getIndex();
    this.propagate(newScheduledTasks, scheduledTask, task, newProcessorEndTimes);
    int newLatestEndTime = Math.max(this.latestEndTime, Arrays.stream(newProcessorEndTimes).max()
        .getAsInt());

    Set<Task> newReadyTasks;
    // if all tasks on current processor have been allocated move to next processor
    if (newLocalOrderedCount == this.allocation.getProcessors()[this.localIndex].size()) {
      newLocalOrderedCount = 0;
      newLocalOrderedWeight = 0;
      newLocalIndex++;
      newReadyTasks = this.getProcessorReadyTasks(newLocalIndex);
      newPreviousTaskIndex = -1;
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
        taskGraph,
        newNextTasks,
        newPreviousTaskIndex
    );
  }

  private void propagate(ScheduledTask[] newScheduledTasks, ScheduledTask scheduledTask,
      Task task, int[] newProcessorEndTimes) {
    for (Edge outEdge : task.getOutgoingEdges()) {
      if (scheduledTask.getEndTime() > taskGraph.getTotalTaskWeights()) {
        // could be replaced with an early return out of recursion somehow instead of this
        return;
      }
      Task child = outEdge.getDestination();
      ScheduledTask childScheduledTask = newScheduledTasks[child.getIndex()];
      if (childScheduledTask == null) {
        continue;
      }
      // all child tasks that need to be propagated will be scheduled on a different processor
      int newChildEstStartTime = scheduledTask.getEndTime() + outEdge.getWeight();
      if (childScheduledTask.getStartTime() < newChildEstStartTime) {
        childScheduledTask.setStartTime(newChildEstStartTime);
        childScheduledTask.setEndTime(newChildEstStartTime + task.getWeight());
        if (childScheduledTask.getEndTime() > newProcessorEndTimes[childScheduledTask
            .getProcessorIndex()]) {
          newProcessorEndTimes[childScheduledTask.getProcessorIndex()] = childScheduledTask
              .getEndTime();
          propagate(newScheduledTasks, childScheduledTask, child, newProcessorEndTimes);
        }
      }
    }
    // propagate the decendant (this will be on the same processor)
    int decendantIndex = nextTasks[task.getIndex()];
    if (decendantIndex != -1) {
      Task decendant = taskGraph.getTask(decendantIndex);
      ScheduledTask decendantScheduledTask = newScheduledTasks[decendantIndex];
      decendantScheduledTask.setStartTime(scheduledTask.getEndTime());
      decendantScheduledTask.setEndTime(scheduledTask.getEndTime() + task.getWeight());
      if (decendantScheduledTask.getEndTime() > newProcessorEndTimes[decendantScheduledTask
          .getProcessorIndex()]) {
        newProcessorEndTimes[decendantScheduledTask.getProcessorIndex()] = decendantScheduledTask
            .getEndTime();
        propagate(newScheduledTasks, decendantScheduledTask, decendant, newProcessorEndTimes);
      }
    }
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
      Task parentTask = incomingEdge.getSource();
      int parentIndex = parentTask.getIndex();
      ScheduledTask parent = newScheduledTasks[parentIndex];

      // return not ready if the parent isn't scheduled and is allocated on the same processor
      if (parent == null && getTaskProcessor(parentTask) == processorNumber) {
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
    Set<Task> newReadyTasks = new BitSet<Task>(this.readyTasks);
    newReadyTasks.remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (getTaskProcessor(child) == getTaskProcessor(task)) {
        if (this.isTaskReady(newScheduledTasks, child)) {
          newReadyTasks.add(child);
        }
      }
    }
    return newReadyTasks;
  }

  public Schedule asSchedule() {
    return new Schedule(this.scheduledTasks, this.processorEndTimes, this.scheduledTaskCount,
        Arrays.stream(this.processorEndTimes).max().getAsInt(), this.readyTasks, taskGraph
            .getTotalTaskWeights(), 0, 0);
  }

}
