package nz.ac.auckland.se306.group12.models;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
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
  private final int[] processorLastTasks;
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
    this.scheduledTasks = new ScheduledTask[taskGraph.taskCount()];
    this.processorLastTasks = new int[processorCount];
    Arrays.fill(this.processorLastTasks, -1);
    this.scheduledTaskCount = 0;

    this.allocation = allocation;
    this.localIndex = 0;
    this.taskGraph = taskGraph;
    this.readyTasks = this.getProcessorReadyTasks(0);
    this.localOrderedCount = 0;
    this.localOrderedWeight = 0;
    this.nextTasks = new int[taskGraph.taskCount()];
    Arrays.fill(this.nextTasks, -1);
    this.previousTaskIndex = -1;

  }

  /**
   * Gets the set of tasks that are ready to be locally scheduled on the processor
   *
   * @param processorIndex Index of the processor to get the ready tasks for
   * @return Set of ready tasks
   */
  private Set<Task> getProcessorReadyTasks(int processorIndex) {
    Set<Task> newReadyTasks = new BitSet<Task>(this.taskGraph);
    for (Task task : this.taskGraph.getTasks()) {
      // check if the task being checked is the current local processor
      if (this.getAllocatedProcessorOf(task) == processorIndex) {
        if (this.isTaskReady(this.scheduledTasks, task)) {
          newReadyTasks.add(task);
        }
      }
    }
    return newReadyTasks;
  }

  /**
   * This method gets the next schedule based on the task being scheduled
   *
   * @param task Task to be scheduled
   * @return AOSchedule with the task scheduled representing the next iteration
   */
  public ScheduledTask extendWithTask(Task task) {
    int startTime = this.getLatestStartTimeOf(task);
    int endTime = startTime + task.getWeight();
    ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, this.localIndex);
    return newScheduledTask;
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
    int[] newProcessorLastTasks = Arrays.copyOf(this.processorLastTasks,
        this.processorLastTasks.length);

    newScheduledTasks[task.getIndex()] = scheduledTask;
    int newLocalOrderedCount = this.localOrderedCount + 1;
    int newLocalOrderedWeight = this.localOrderedWeight + task.getWeight();
    int newLocalIndex = this.localIndex;

    // set the next task on the processor of a task to be the extending task
    int[] newNextTasks = Arrays.copyOf(this.nextTasks, this.nextTasks.length);
    if (this.previousTaskIndex != -1) {
      newNextTasks[this.previousTaskIndex] = task.getIndex();
    }
    int newPreviousTaskIndex = task.getIndex();
    if (!this.propagate(newScheduledTasks, task)) {
      return null;
    }

    int processorIndex = scheduledTask.getProcessorIndex();
    // After propagation, the end time of the processor may have changed, i.e. This task was added
    // before another task on this processor
    int newProcessorEndTime = this.getLatestEndTimeOf(
        processorIndex, newProcessorLastTasks, newScheduledTasks);
    if (newProcessorEndTime < scheduledTask.getEndTime()) {
      newProcessorLastTasks[processorIndex] = task.getIndex();
    }

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

    return new AOSchedule(
        newScheduledTasks,
        newProcessorLastTasks,
        this.scheduledTaskCount + 1,
        this.allocation,
        newLocalIndex,
        newLocalOrderedCount,
        newLocalOrderedWeight,
        newReadyTasks,
        this.taskGraph,
        newNextTasks,
        newPreviousTaskIndex
    );
  }

  /**
   * Iteratively propagates the new scheduled task's end time to all children nodes (graph-wise) and
   * descendants (processor-wise)
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param task              Task to start propagation from
   * @return TODO
   */
  private boolean propagate(ScheduledTask[] newScheduledTasks, Task task) {
    Deque<Task> stack = new ArrayDeque<>();
    stack.push(task);
    while (!stack.isEmpty()) {
      Task parentTask = stack.pop();
      ScheduledTask parentScheduledTask = newScheduledTasks[parentTask.getIndex()];
      int parentEndTime = parentScheduledTask.getEndTime();
      if (parentScheduledTask.getEndTime() > this.taskGraph.getTotalTaskWeights()) {
        return false;
      }
      for (Edge outEdge : parentTask.getOutgoingEdges()) {
        // don't continue if there is an invalid loop in the schedule
        Task childTask = outEdge.getDestination();
        ScheduledTask childScheduledTask = newScheduledTasks[childTask.getIndex()];
        // don't propagate if the child task is not scheduled yet
        if (childScheduledTask == null) {
          continue;
        }
        int childStartTime = parentScheduledTask.getProcessorIndex() == childScheduledTask
            .getProcessorIndex()
            ? parentEndTime
            : parentEndTime + outEdge.getWeight();
        ScheduledTask newScheduledTask = this.getUpdatedScheduledTask(childTask,
            childScheduledTask, childStartTime);
        if (newScheduledTask != null) {
          newScheduledTasks[childTask.getIndex()] = newScheduledTask;
          stack.push(childTask);
        }
      }
      // propagate the descendant (this will be on the same processor)
      int descendantIndex = this.nextTasks[parentTask.getIndex()];
      // don't run this if the parent task does not have a descendant
      if (descendantIndex != -1) {
        Task descendantTask = this.taskGraph.getTask(descendantIndex);
        ScheduledTask descendantScheduledTask = newScheduledTasks[descendantIndex];
        ScheduledTask newScheduledTask = this.getUpdatedScheduledTask(descendantTask,
            descendantScheduledTask, parentEndTime);
        if (newScheduledTask != null) {
          newScheduledTasks[descendantIndex] = newScheduledTask;
          stack.push(descendantTask);
        }
      }
    }
    return true;
  }

  /**
   * Updates the scheduled task if the new start time is greater than the current start time
   *
   * @param task            Task to be updated
   * @param scheduledTask   Scheduled task representation of the task to be updated
   * @param newMinStartTime New start time of the task
   * @return A new {@link ScheduledTask} if the task was updated, the same instance otherwise
   */
  private ScheduledTask getUpdatedScheduledTask(
      Task task,
      ScheduledTask scheduledTask,
      int newMinStartTime
  ) {
    // if the child task needs updating then update
    if (scheduledTask.getStartTime() < newMinStartTime) {
      return new ScheduledTask(newMinStartTime, newMinStartTime + task.getWeight(),
          scheduledTask.getProcessorIndex());
    }
    return null;
  }

  /**
   * This method checks if a task is ready to be scheduled locally on the processor
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param child             Child task to be checked if ready
   * @return True if the task is ready to be scheduled, false otherwise
   */
  private boolean isTaskReady(ScheduledTask[] newScheduledTasks, Task child) {
    int processorNumber = this.getAllocatedProcessorOf(child);
    for (Edge incomingEdge : child.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();
      int parentIndex = parentTask.getIndex();
      ScheduledTask parent = newScheduledTasks[parentIndex];

      // return not ready if the parent isn't scheduled and is allocated on the same processor
      if (parent == null && this.getAllocatedProcessorOf(parentTask) == processorNumber) {
        return false;
      }
    }
    return true;
  }

  /**
   * This method gets the processor that a task is allocated to
   *
   * @param task Task to get the allocated processor of
   * @return Processor index that the task is allocated to
   */
  private int getAllocatedProcessorOf(Task task) {
    return this.allocation.getTasksProcessor()[task.getIndex()];
  }

  /**
   * This method finds the latest start time for a task on its allocated processor
   *
   * @param task Task to find the latest start time for
   * @return The latest start time for the task on its allocated processor
   */
  private int getLatestStartTimeOf(Task task) {
    int taskProcessorIndex = this.getAllocatedProcessorOf(task);
    int latestStartTime = this.getLatestEndTimeOf(taskProcessorIndex);
    // Loop through all parent tasks
    for (Edge incomingEdge : task.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();
      ScheduledTask parentScheduledTask = this.getScheduledTask(parentTask);

      // Skip if parent task is not scheduled
      if (parentScheduledTask == null) {
        continue;
      }

      // Skip if parent task is scheduled on the same processor as processor end time is always later
      if (parentScheduledTask.getProcessorIndex() == taskProcessorIndex) {
        continue;
      }

      int newLatestStartTime = parentScheduledTask.getEndTime() + incomingEdge.getWeight();

      // Update latest start time if new latest start time is greater
      if (newLatestStartTime > latestStartTime) {
        latestStartTime = newLatestStartTime;
      }
    }
    return latestStartTime;

  }

  /**
   * This method returns a set of locally ready tasks based on the processor of the current task
   * being scheduled
   *
   * @param task              Current task being scheduled
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @return Set of locally ready tasks
   */
  private Set<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    Set<Task> newReadyTasks = new BitSet<>(this.readyTasks);
    newReadyTasks.remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (this.getAllocatedProcessorOf(child) == this.getAllocatedProcessorOf(task)) {
        if (this.isTaskReady(newScheduledTasks, child)) {
          newReadyTasks.add(child);
        }
      }
    }
    return newReadyTasks;
  }

  /**
   * Temporary implementation to convert AOSchedule to Schedule
   * <p>
   * Better would be to use the penguin thing and have AOSCheduler implement a schedule
   *
   * @return Schedule representation of the AOSchedule
   */
  public Schedule asSchedule() {
    return new Schedule(this.scheduledTasks,
        new int[this.processorLastTasks.length],
        this.getLatestEndTime(),
        this.scheduledTaskCount,
        this.readyTasks,
        this.taskGraph.getTotalTaskWeights(),
        0, 0);
  }

  /**
   * Gets the latest end time of the schedule
   *
   * @return Latest end time of the schedule
   */
  public int getLatestEndTime() {
    return Arrays.stream(this.scheduledTasks).mapToInt(scheduledTask -> {
      if (scheduledTask == null) {
        return 0;
      }
      return scheduledTask.getEndTime();
    }).max().orElse(0);
  }

  /**
   * Gets the latest end time of a given processor
   *
   * @param processorIndex The index of the processor to get the latest end time of
   * @return Latest end time of the processor
   */
  public int getLatestEndTimeOf(int processorIndex) {
    return this.getLatestEndTimeOf(processorIndex, this.processorLastTasks, this.scheduledTasks);
  }

  /**
   * Gets the latest end time of a given processor from a given set of scheduled tasks and the last
   * task indices on each processor.
   *
   * @param processorIndex     The index of the processor to get the latest end time of
   * @param processorLastTasks The index on the last task on each processor
   * @param scheduledTasks     The scheduled tasks
   * @return The latest end time of the processor
   */
  private int getLatestEndTimeOf(
      int processorIndex,
      int[] processorLastTasks,
      ScheduledTask[] scheduledTasks
  ) {
    int lastTaskIndex = processorLastTasks[processorIndex];
    if (lastTaskIndex == -1) {
      return 0;
    }
    return scheduledTasks[lastTaskIndex].getEndTime();
  }

  /**
   * Gets the scheduled task representation of a task
   *
   * @param task Task to get the scheduled task representation of
   * @return Scheduled task representation of the task
   */
  private ScheduledTask getScheduledTask(Task task) {
    return this.scheduledTasks[task.getIndex()];
  }

}
