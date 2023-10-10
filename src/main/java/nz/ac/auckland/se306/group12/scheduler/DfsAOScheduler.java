package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;

import lombok.Getter;
import nz.ac.auckland.se306.group12.models.AOSchedule;
import nz.ac.auckland.se306.group12.models.Allocation;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsAOScheduler implements Scheduler {

  private int currentMinMakespan = Integer.MAX_VALUE;

  @Getter
  private long searchedCount = 0;
  @Getter
  private long prunedCount = 0;
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.SCHEDULING;
    Queue<Allocation> stack = Collections.asLifoQueue(new ArrayDeque<>());
    stack.add(new Allocation(taskGraph, processorCount));

    // DFS iteration (no optimisations)
    while (!stack.isEmpty()) {
      Allocation currentAllocation = stack.remove();

      // Prune if current allocation is worse than current best schedule
      // Later change this to the allocation heuristic check
      if (currentAllocation.getMaxWeight() >= currentMinMakespan) {
        // pruned count here is pruning allocations which is technically different from pruning branches
        continue;
      }
      // Same for searching here

      // Check if current allocation is complete
      if (currentAllocation.getAllocationCount() == taskGraph.taskCount()) {
        // TODO: do the ordering algorithm
        order(currentAllocation);
        // printAllProcessors(currentAllocation);
        continue;
      }
      currentAllocation.extendAllocation(stack);
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule;
  }

  private void order(Allocation allocation) {
    Queue<AOSchedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
    queue.add(new AOSchedule(allocation.getTaskGraph(), allocation.getProcessors().length,
        allocation));

  }

  private void printAllProcessors(Allocation currentAllocation) {
    System.out.println("Allocation");
    for (int i = 0; i < currentAllocation.getProcessors().length; i++) {
      Set<Task> processor = currentAllocation.getProcessors()[i];
      if (processor == null) {
        continue;
      }
      System.out.print("Processor " + i + ": ");
      for (Task task : processor) {
        System.out.print(task.getLabel() + ", ");
      }
      System.out.println();
    }
    for (int i = 0; i < currentAllocation.getProcessors().length; i++) {
      System.out.print(currentAllocation.getProcessorWeights()[i] + ", ");
    }
    System.out.println();
    System.out.println();
  }

}
