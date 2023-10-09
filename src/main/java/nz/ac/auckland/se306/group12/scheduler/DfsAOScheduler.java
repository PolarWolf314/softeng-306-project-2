package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import nz.ac.auckland.se306.group12.models.Allocation;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsAOScheduler implements Scheduler {

  private int currentMinMakespan = Integer.MAX_VALUE;
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    Deque<Allocation> stack = new ArrayDeque<>();
    stack.push(new Allocation(taskGraph, processorCount));

    // DFS iteration (no optimisations)
    while (!stack.isEmpty()) {
      Allocation currentAllocation = stack.pop();

      // Prune if current allocation is worse than current best schedule
      // Later change this to the allocation heuristic check
      if (currentAllocation.getMaxWeight() >= currentMinMakespan) {
        continue;
      }

      // Check if current allocation is complete
      if (currentAllocation.getAllocationCount() == taskGraph.taskCount()) {
        // TODO: do the ordering algorithm
        // printAllProcessors(currentAllocation);
        continue;
      }
      currentAllocation.extendAllocation(stack);
    }

    return this.bestSchedule;
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
