package nz.ac.auckland.se306.group12.models;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
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
  // TODO: abstract this into array of last tasks on a processor
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
      if (getAllocatedProcessorOf(task) == processorIndex) {
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
      ScheduledTask newScheduledTask = extendWithTask(task);
      AOSchedule newSchedule = extendWithTask(newScheduledTask, task);
      if (newSchedule != null) {
        queue.add(newSchedule);
      }
    }
  }

  /**
   * This method gets the next schedule based on the task being scheduled
   *
   * @param task Task to be scheduled
   * @return AOSchedule with the task scheduled representing the next iteration
   */
  private ScheduledTask extendWithTask(Task task) {
    int startTime = getLatestStartTimeOf(task);
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
    ScheduledTask[] newScheduledTasks = deepCopyScheduledTasks();
    int[] newProcessorEndTimes = Arrays.copyOf(this.processorEndTimes,
        this.processorEndTimes.length);

    newScheduledTasks[task.getIndex()] = scheduledTask;
    int processorIndex = scheduledTask.getProcessorIndex();
    newProcessorEndTimes[processorIndex] = scheduledTask.getEndTime();
    int newLocalOrderedCount = this.localOrderedCount + 1;
    int newLocalOrderedWeight = this.localOrderedWeight + task.getWeight();
    int newLocalIndex = this.localIndex;

    // set the next task on the processor of a task to be the extending task
    int[] newNextTasks = Arrays.copyOf(this.nextTasks, this.nextTasks.length);
    if (previousTaskIndex != -1) {
      newNextTasks[this.previousTaskIndex] = task.getIndex();
    }
    int newPreviousTaskIndex = task.getIndex();
    if (this.propagate(newScheduledTasks, task, newProcessorEndTimes) == false) {
      return null;
    }

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

  private ScheduledTask[] deepCopyScheduledTasks() {
    ScheduledTask[] newScheduledTasks = new ScheduledTask[this.scheduledTasks.length];
    for (int i = 0; i < this.scheduledTasks.length; i++) {
      if (this.scheduledTasks[i] != null) {
        newScheduledTasks[i] = new ScheduledTask(this.scheduledTasks[i]);
      }
    }
    return newScheduledTasks;
  }

  /**
   * Recursively propagate the new scheduled task's end time to all children nodes (graph-wise) and
   * decendants (processor-wise)
   *
   * @param newScheduledTasks
   * @param task
   * @param newProcessorEndTimes
   */
  private boolean propagate(ScheduledTask[] newScheduledTasks, Task task,
      int[] newProcessorEndTimes) {
    Deque<Task> stack = new ArrayDeque<>();
    stack.push(task);
    while (!stack.isEmpty()) {
      Task parentTask = stack.pop();
      ScheduledTask parentScheduledTask = newScheduledTasks[parentTask.getIndex()];
      if (parentScheduledTask.getEndTime() > taskGraph.getTotalTaskWeights()) {
        return false;
      }
      for (Edge outEdge : parentTask.getOutgoingEdges()) {
        // don't continue if there is a invalid loop in the schedule
        Task childTask = outEdge.getDestination();
        ScheduledTask childScheduledTask = newScheduledTasks[childTask.getIndex()];
        // don't propagate if the child schedule is not scheduled yet
        if (childScheduledTask == null) {
          continue;
        }
        int newChildEstStartTime = parentScheduledTask.getProcessorIndex() == childScheduledTask
            .getProcessorIndex()
                ? parentScheduledTask.getEndTime()
                : parentScheduledTask.getEndTime() + outEdge.getWeight();

        // if the child task needs updating then update
        if (childScheduledTask.getStartTime() < newChildEstStartTime) {
          childScheduledTask.setStartTime(newChildEstStartTime);
          childScheduledTask.setEndTime(newChildEstStartTime + childTask.getWeight());
          // add the child task to the propagate stack
          stack.push(childTask);
        }
      }
      // propagate the decendant (this will be on the same processor)
      int decendantIndex = nextTasks[parentTask.getIndex()];
      // don't run this if the parent task does not have a decendant
      if (decendantIndex != -1) {
        Task decendantTask = taskGraph.getTask(decendantIndex);
        ScheduledTask decendantScheduledTask = newScheduledTasks[decendantIndex];
        if (decendantScheduledTask.getStartTime() < parentScheduledTask.getEndTime()) {
          decendantScheduledTask.setStartTime(parentScheduledTask.getEndTime());
          decendantScheduledTask.setEndTime(parentScheduledTask.getEndTime() + decendantTask
              .getWeight());
          stack.push(decendantTask);
        }
      }
    }
    return true;
  }

  /**
   * This method checks if a task is ready to be scheduled locally on the processor
   *
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @param child             Child task to be checked if ready
   * @return True if the task is ready to be scheduled, false otherwise
   */
  private boolean isTaskReady(ScheduledTask[] newScheduledTasks, Task child) {
    int processorNumber = getAllocatedProcessorOf(child);
    for (Edge incomingEdge : child.getIncomingEdges()) {
      Task parentTask = incomingEdge.getSource();
      int parentIndex = parentTask.getIndex();
      ScheduledTask parent = newScheduledTasks[parentIndex];

      // return not ready if the parent isn't scheduled and is allocated on the same processor
      if (parent == null && getAllocatedProcessorOf(parentTask) == processorNumber) {
        return false;
      }
    }
    return true;
  }

  private int getAllocatedProcessorOf(Task child) {
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

  /**
   * This method returns a set of locally ready tasks based on the processor of the current task
   * being scheduled
   *
   * @param task              Current task being scheduled
   * @param newScheduledTasks List of scheduled tasks representing the schedule at the next state
   * @return Set of locally ready tasks
   */
  private Set<Task> getNewReadyTasks(Task task, ScheduledTask[] newScheduledTasks) {
    Set<Task> newReadyTasks = new BitSet<Task>(this.readyTasks);
    newReadyTasks.remove(task);
    for (Edge outEdge : task.getOutgoingEdges()) {
      Task child = outEdge.getDestination();
      if (getAllocatedProcessorOf(child) == getAllocatedProcessorOf(task)) {
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
    return new Schedule(deepCopyScheduledTasks(),
        this.processorEndTimes,
        getLatestEndTime(),
        this.scheduledTaskCount,
        this.readyTasks,
        taskGraph.getTotalTaskWeights(),
        0, 0);
  }

  public int getLatestEndTime() {
    return Arrays.stream(scheduledTasks).mapToInt(scheduledTask -> {
      if (scheduledTask == null) {
        return 0;
      }
      return scheduledTask.getEndTime();
    }).max().orElse(0);

  }

  public int getLatestEndTimeOf(int processor) {
    return Arrays.stream(scheduledTasks).mapToInt(scheduledTask -> {
      if (scheduledTask == null || scheduledTask.getProcessorIndex() != processor) {
        return 0;
      }
      return scheduledTask.getEndTime();
    }).max().orElse(0);

  }

  private ScheduledTask getScheduledTask(Task task) {
    return this.scheduledTasks[task.getIndex()];
  }

}
