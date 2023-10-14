package nz.ac.auckland.se306.group12.models;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.datastructures.BitSet;

@Getter
@RequiredArgsConstructor
public class AOSchedule2 {

  private final AOScheduledTask[] scheduledTasks;
  private final int scheduledTaskCount;

  private final Allocation allocation;
  private final Graph taskGraph;
  private final Set<Task>[] readyTasks;
  private final int[] orderedCount;
  // this defines the current processor being scheduled
  private final int localIndex;
  private final int[] processorsLastTask;

  public AOSchedule2(Graph taskGraph, int processorCount, Allocation allocation) {
    this.scheduledTasks = new AOScheduledTask[taskGraph.taskCount()];
    this.processorsLastTask = new int[processorCount];
    this.scheduledTaskCount = 0;
    this.allocation = allocation;
    this.taskGraph = taskGraph;
    this.orderedCount = new int[processorCount];
    this.localIndex = 0;
    this.readyTasks = initialiseReadyTasks(taskGraph, processorCount, allocation);
  }

  public AOSchedule2 extendWithTask(Task task) {
    AOScheduledTask scheduledTask = newScheduledTaskFrom(task);
    AOScheduledTask[] newScheduledTasks = deepCopyScheduledTasks();
    newScheduledTasks[task.getIndex()] = scheduledTask;

    int[] newProcessorLastTask = Arrays.copyOf(this.processorsLastTask,
        this.processorsLastTask.length);

    int[] newOrderedCount = Arrays.copyOf(this.orderedCount, this.orderedCount.length);
    newOrderedCount[scheduledTask.getProcessorIndex()]++;
    int newLocalIndex = this.localIndex;

    // if all tasks on processor are scheduled, move to next processor
    if (newOrderedCount[newLocalIndex] == this.allocation.getProcessors()[newLocalIndex].size()) {
      newLocalIndex++;
    }

    Set<Task>[] newReadyTasks = getNewReadyTasks(task, newScheduledTasks);

    // set next task of previous task to current task
    AOScheduledTask previousTask = newScheduledTasks[this.processorsLastTask[scheduledTask
        .getProcessorIndex()]];
    if (previousTask != null) {
      previousTask.setNext(task.getIndex());
    }

    // set the latestTask of the processor to the current task for next iteration
    newProcessorLastTask[scheduledTask.getProcessorIndex()] = task.getIndex();

    if (!this.propagate(newScheduledTasks, task)) {
      return null;
    }

    return new AOSchedule2(
        newScheduledTasks,
        this.scheduledTaskCount + 1,
        this.allocation,
        this.taskGraph,
        newReadyTasks,
        newOrderedCount,
        newLocalIndex,
        newProcessorLastTask
    );

  }

  private boolean propagate(AOScheduledTask[] newScheduledTasks, Task task) {
    Deque<Task> stack = new ArrayDeque<>();
    stack.push(task);
    while (!stack.isEmpty()) {
      Task parentTask = stack.pop();
      AOScheduledTask parentScheduledTask = newScheduledTasks[parentTask.getIndex()];
      for (Edge outEdge : parentTask.getOutgoingEdges()) {
        // don't continue if there is a invalid loop in the schedule
        // I can't be sure that this is guaranteed
        if (parentScheduledTask.getEndTime() > taskGraph.getTotalTaskWeights()) {
          return false;
        }
        Task childTask = outEdge.getDestination();
        int transferWeight = outEdge.getWeight();
        ScheduledTask childScheduledTask = newScheduledTasks[childTask.getIndex()];
        // don't propagate if the child schedule is not scheduled yet
        if (childScheduledTask == null) {
          continue;
        }
        updateChildTimes(stack, parentScheduledTask, childTask,
            transferWeight, childScheduledTask);
      }

      // propagate the decendant (this will be on the same processor)
      int decendantIndex = parentScheduledTask.getNext();
      // don't run this if the parent task does not have a decendant
      if (decendantIndex == -1) {
        continue;
      }
      Task decendantTask = taskGraph.getTask(decendantIndex);
      ScheduledTask decendantScheduledTask = newScheduledTasks[decendantIndex];
      updateChildTimes(stack, parentScheduledTask, decendantTask, 0, decendantScheduledTask);
    }
    return true;

  }

  private void updateChildTimes(Deque<Task> stack, AOScheduledTask parentScheduledTask,
      Task childTask, int transferWeight, ScheduledTask childScheduledTask) {
    // update child scheduled task times
    int newChildEstStartTime = parentScheduledTask.getEndTime();
    if (parentScheduledTask.getProcessorIndex() != childScheduledTask.getProcessorIndex()) {
      newChildEstStartTime += transferWeight;
    }

    if (childScheduledTask.getStartTime() < newChildEstStartTime) {
      // if the child task needs updating then update
      childScheduledTask.setStartTime(newChildEstStartTime);
      childScheduledTask.setEndTime(newChildEstStartTime + childTask.getWeight());
      // add the child task to the propagate stack
      stack.add(childTask);
    }
  }

  public Set<Task> getReadyTasks() {
    return this.readyTasks[this.localIndex];
  }

  private AOScheduledTask[] deepCopyScheduledTasks() {
    AOScheduledTask[] newScheduledTasks = new AOScheduledTask[this.scheduledTasks.length];
    for (int i = 0; i < this.scheduledTasks.length; i++) {
      if (this.scheduledTasks[i] != null) {
        newScheduledTasks[i] = new AOScheduledTask(this.scheduledTasks[i]);
      }
    }
    return newScheduledTasks;
  }

  private AOScheduledTask newScheduledTaskFrom(Task task) {
    int startTime = getLatestStartTimeOf(task);
    int endTime = startTime + task.getWeight();
    return new AOScheduledTask(startTime, endTime, getAllocatedProcessorOf(task));
  }

  private Set<Task>[] initialiseReadyTasks(Graph taskGraph, int processorCount,
      Allocation allocation) {
    Set<Task>[] readyTasks = new BitSet[processorCount];
    for (int i = 0; i < processorCount; i++) {
      readyTasks[i] = new BitSet<Task>(taskGraph);
    }
    for (Task task : taskGraph.getTasks()) {
      // add to ready task based on allocated processors
      if (isTaskReady(this.scheduledTasks, task)) {
        readyTasks[getAllocatedProcessorOf(task)].add(task);
      }
    }
    return readyTasks;
  }

  /**
   * This method checks if a task is ready to be scheduled locally on the processor
   *
   * @param scheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param task           Child task to be checked if ready
   * @return True if the task is ready to be scheduled, false otherwise
   */
  private boolean isTaskReady(ScheduledTask[] scheduledTasks, Task task) {
    int processorNumber = getAllocatedProcessorOf(task);
    for (Edge incomingEdge : task.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();

      // only check parent tasks allocated on the same processor
      if (getAllocatedProcessorOf(parentTask) == processorNumber) {
        ScheduledTask parent = scheduledTasks[parentTask.getIndex()];

        // return not ready if the parent task is not scheduled
        if (parent == null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * This method returns a set of locally ready tasks based on the processor of the current task
   * being scheduled
   *
   * @param task           Current task being scheduled
   * @param scheduledTasks List of scheduled tasks representing the schedule at the next state
   * @return Set of locally ready tasks
   */
  private Set<Task>[] getNewReadyTasks(Task task, ScheduledTask[] scheduledTasks) {
    int parentProcessorIndex = getAllocatedProcessorOf(task);
    Set<Task>[] newReadyTasks = deepCopyReadyTasks();
    newReadyTasks[parentProcessorIndex].remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (getAllocatedProcessorOf(child) == parentProcessorIndex) {
        if (this.isTaskReady(scheduledTasks, child)) {
          newReadyTasks[parentProcessorIndex].add(child);
        }
      }
    }
    return newReadyTasks;
  }

  /**
   * This method finds the latest start time for a task on its allocated processor
   * <p>
   *
   * @param task Task to find the latest start time for
   * @return Array of latest start times for the task on each processor
   */
  private int getLatestStartTimeOf(Task task) {
    int taskProcessorIndex = getAllocatedProcessorOf(task);
    int latestStartTime = getLatestEndTimeOf(taskProcessorIndex);
    // Loop through all parent tasks
    for (Edge incomingEdge : task.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();
      ScheduledTask parentScheduledTask = getScheduledTask(parentTask);

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

  private int getLatestEndTimeOf(int processorIndex) {
    return Arrays.stream(scheduledTasks).mapToInt(scheduledTask -> {
      if (scheduledTask == null || scheduledTask.getProcessorIndex() != processorIndex) {
        return 0;
      }
      return scheduledTask.getEndTime();
    }).max().orElse(0);
  }

  private int getAllocatedProcessorOf(Task task) {
    return this.allocation.getTasksProcessor()[task.getIndex()];
  }

  private ScheduledTask getScheduledTask(Task task) {
    return this.scheduledTasks[task.getIndex()];
  }

  public Set<Task>[] deepCopyReadyTasks() {
    Set<Task>[] newReadyTasks = new BitSet[this.readyTasks.length];
    for (int i = 0; i < this.readyTasks.length; i++) {
      newReadyTasks[i] = new BitSet<Task>(this.readyTasks[i]);
    }
    return newReadyTasks;
  }

  public int getEstimatedMakespan() {
    return getLatestEndTime();
  }

  private int getLatestEndTime() {
    return Arrays.stream(scheduledTasks).mapToInt(scheduledTask -> {
      if (scheduledTask == null) {
        return 0;
      }
      return scheduledTask.getEndTime();
    }).max().orElse(0);
  }

  /**
   * Temporary implementation to convert AOSchedule to Schedule
   * <p>
   * Better would be to use the penguin thing and have AOSCheduler implement a schedule
   * 
   * @return Schedule representation of the AOSchedule
   */
  public Schedule asSchedule() {
    return new Schedule(toScheduledTasks(),
        getProcessorEndTimes(),
        getLatestEndTime(),
        this.scheduledTaskCount,
        this.readyTasks[0],
        taskGraph.getTotalTaskWeights(),
        0, 0);
  }

  private ScheduledTask[] toScheduledTasks() {
    ScheduledTask[] newScheduledTasks = new ScheduledTask[this.scheduledTasks.length];
    for (int i = 0; i < this.scheduledTasks.length; i++) {
      if (this.scheduledTasks[i] != null) {
        newScheduledTasks[i] = new ScheduledTask(this.scheduledTasks[i]);
      }
    }
    return newScheduledTasks;
  }

  private int[] getProcessorEndTimes() {
    int[] processorEndTimes = new int[this.allocation.getProcessors().length];
    for (int i = 0; i < this.allocation.getProcessors().length; i++) {
      processorEndTimes[i] = getLatestEndTimeOf(i);
    }
    return processorEndTimes;
  }

}
