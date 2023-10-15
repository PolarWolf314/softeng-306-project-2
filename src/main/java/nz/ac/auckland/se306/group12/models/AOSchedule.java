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
  private final int[] processorLastTaskIndices;
  private final int scheduledTaskCount;
  private final int latestEndTime;

  private final Allocation allocation;
  private final int localIndex;
  private final int localOrderedCount;
  private final int localOrderedWeight;
  private final Set<Task> readyTasks;
  private final Graph taskGraph;
  // The following fields are to allow for quick lookup of the previous scheduled task on the same processor
  private final int[] nextTasks;
  private final int previousTaskIndex;

  public AOSchedule(Allocation allocation) {
    this.scheduledTasks = new ScheduledTask[allocation.getTaskGraph().taskCount()];
    this.processorLastTaskIndices = new int[allocation.getProcessors().length];
    Arrays.fill(this.processorLastTaskIndices, -1);
    this.scheduledTaskCount = 0;
    this.latestEndTime = 0;

    this.allocation = allocation;
    this.localIndex = 0;
    this.taskGraph = allocation.getTaskGraph();
    this.readyTasks = this.getProcessorReadyTasks(0);
    this.localOrderedCount = 0;
    this.localOrderedWeight = 0;
    this.nextTasks = new int[this.taskGraph.taskCount()];
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
    Set<Task> newReadyTasks = new BitSet<>(this.taskGraph);
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
   * This method determines the {@link ScheduledTask} for a given task.
   *
   * @param task Task to be scheduled
   * @return The created {@link ScheduledTask} representation of the task
   */
  public ScheduledTask extendWithTask(Task task) {
    int startTime = this.getLatestStartTimeOf(task);
    int endTime = startTime + task.getWeight();
    return new ScheduledTask(startTime, endTime, this.localIndex);
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
    int[] newProcessorLastTaskIndices = Arrays.copyOf(this.processorLastTaskIndices,
        this.processorLastTaskIndices.length);

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
    if (!this.propagate(newScheduledTasks, task, newNextTasks)) {
      return null;
    }

    newProcessorLastTaskIndices[scheduledTask.getProcessorIndex()] = task.getIndex();

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
        newProcessorLastTaskIndices,
        this.scheduledTaskCount + 1,
        this.getNewLatestEndTime(newProcessorLastTaskIndices, newScheduledTasks),
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
   * descendants (processor-wise). Propagation can fail if it gets stuck in an infinite propagation
   * loop. This is detected by determining it a task has been propagated past the total weight of
   * the task graph.
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param task              Task to start propagation from
   * @return {@code true} If the propagation completed successfully, {@code false} otherwise
   */
  private boolean propagate(ScheduledTask[] newScheduledTasks, Task task, int[] newNextTasks) {
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
      int descendantIndex = newNextTasks[parentTask.getIndex()];
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
   * @return A new {@link ScheduledTask} if the task was updated, otherwise {@code null}
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
   * This method checks if a task is ready to be scheduled locally on the processor. This occurs
   * when all the task's dependences that are allocated on the same processor have already been
   * scheduled.
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param child             Child task to be checked if ready
   * @return {@code true} if the task is ready to be scheduled, {@code false} otherwise
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
    return this.allocation.getTaskProcessorAllocation()[task.getIndex()];
  }

  /**
   * Determines the new latest end time of the schedule based on the new scheduled tasks and the
   * last tasks on each processor. We have to check every processor as propagation may cause any
   * number of the end times to change, and so we cannot just check the processor of the new task.
   *
   * @param newProcessorLastTasks The index on the last task on each processor
   * @param newScheduledTasks     The scheduled tasks
   * @return The new latest end time of the schedule
   */
  private int getNewLatestEndTime(int[] newProcessorLastTasks, ScheduledTask[] newScheduledTasks) {
    int newLatestEndTime = 0;
    for (int processorIndex = 0; processorIndex < newProcessorLastTasks.length; processorIndex++) {
      int processorEndTime = this.getLatestEndTimeOf(processorIndex, newProcessorLastTasks,
          newScheduledTasks);
      if (processorEndTime > newLatestEndTime) {
        newLatestEndTime = processorEndTime;
      }
    }

    return newLatestEndTime;
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
   * being scheduled. A locally ready task is one where all of its dependences that have been
   * allocated to the same processor have already been scheduled.
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
        new int[this.processorLastTaskIndices.length],
        this.getLatestEndTime(),
        this.scheduledTaskCount,
        this.readyTasks,
        this.taskGraph.getTotalTaskWeights(),
        0, 0);
  }

  /**
   * Gets the latest end time of a given processor
   *
   * @param processorIndex The index of the processor to get the latest end time of
   * @return Latest end time of the processor
   */
  public int getLatestEndTimeOf(int processorIndex) {
    return this.getLatestEndTimeOf(processorIndex, this.processorLastTaskIndices,
        this.scheduledTasks);
  }

  /**
   * Gets the latest end time of a given processor from a given set of scheduled tasks and the last
   * task indices on each processor.
   *
   * @param processorIndex           The index of the processor to get the latest end time of
   * @param processorLastTaskIndices The index on the last task on each processor
   * @param scheduledTasks           The scheduled tasks
   * @return The latest end time of the processor
   */
  private int getLatestEndTimeOf(
      int processorIndex,
      int[] processorLastTaskIndices,
      ScheduledTask[] scheduledTasks
  ) {
    int lastTaskIndex = processorLastTaskIndices[processorIndex];
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

  public int getEstimatedMakespan() {
    return Math.max(latestEndTime, getOrderedLoadEstimate());
  }

  private int getOrderedLoadEstimate() {
    if (this.localIndex >= this.processorLastTaskIndices.length) {
      return 0;
    }
    int localTotalWeight = this.allocation.getProcessorWeights()[this.localIndex];
    int remainingWeight = localTotalWeight - localOrderedWeight;
    return this.getLatestEndTimeOf(this.localIndex) + remainingWeight;
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
   *      Kozlov & Shahul: Optimal Scheduling of Task Graphs on Parallel Systems</a>, Section 3.1
   */
  private int estimateBottomLevelMakespan() {
    return Arrays.stream(this.scheduledTasks).mapToInt((scheduledTask) -> {
      if (scheduledTask == null) {
        return 0;
      }
      Task task = this.taskGraph.getTask(scheduledTask.getProcessorIndex());
      return scheduledTask.getEndTime() + task.getBottomLevel();
    }).max().orElse(0);
  }

}
