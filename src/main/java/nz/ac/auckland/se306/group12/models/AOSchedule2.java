package nz.ac.auckland.se306.group12.models;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.datastructures.BitSet;

@Getter
@RequiredArgsConstructor
public class AOSchedule2 {

  private final ScheduledTask[] scheduledTasks;
  private final int[] processorEndTimes;
  private final int latestEndTime;
  private final int scheduledTaskCount;

  private final Allocation allocation;
  private final Set<Task>[] readyTasks;
  // this defines the current processor being scheduled
  private final int localIndex;

  public AOSchedule2(Graph taskGraph, int processorCount, Allocation allocation) {
    this.scheduledTasks = new ScheduledTask[taskGraph.taskCount()];
    this.processorEndTimes = new int[processorCount];
    this.scheduledTaskCount = 0;
    this.latestEndTime = 0;
    this.allocation = allocation;

    this.localIndex = 0;
    this.readyTasks = initialiseReadyTasks(taskGraph, processorCount, allocation);
  }

  public int getEstimatedMakespan() {
    return this.latestEndTime;
  }

  private Set<Task>[] initialiseReadyTasks(Graph taskGraph, int processorCount,
      Allocation allocation) {
    Set<Task>[] readyTasks = new BitSet[processorCount];
    for (Task task : taskGraph.getTasks()) {
      // add to ready task based on allocated processors
      if (isTaskReady(this.scheduledTasks, task)) {
        readyTasks[getAllocatedProcessorOf(task)].add(task);
      }
    }
    return null;
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

  private int getAllocatedProcessorOf(Task task) {
    return this.allocation.getTasksProcessor()[task.getIndex()];
  }

}
