package nz.ac.auckland.se306.group12.models;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.datastructures.BitSet;

/*
 * An allocation represents a state in a partition of tasks allocated on a processor
 */
@RequiredArgsConstructor
@Getter
public class Allocation {

  private final Set<Task>[] processors;
  private final int[] processorWeights;
  private final int[] tasksProcessor;
  private final int allocationCount;
  private final int maxWeight;
  private final Graph taskGraph;

  public Allocation(Graph taskGraph, int processorCount) {
    this.processors = new BitSet[processorCount];
    this.allocationCount = 0;
    this.processorWeights = new int[processorCount];
    this.maxWeight = 0;
    this.taskGraph = taskGraph;
    this.tasksProcessor = new int[taskGraph.taskCount()];
  }

  /**
   * Pushes all the child allocations that can be made from this allocation onto the stack
   * 
   * @param queue The queue to add the child allocations onto
   */
  public void extendAllocation(Queue<Allocation> queue) {
    Task newTask = taskGraph.getTask(allocationCount);
    // boolean to handle exit early if creating a new processor
    boolean complete = true;
    // Loop through each processor to allocate the task to it
    for (int i = 0; i < processors.length && complete; i++) {
      Set<Task>[] newProcessors = deepCopyProcessors();
      int[] newProcessorWeights = Arrays.copyOf(this.processorWeights,
          this.processorWeights.length);
      int[] newTasksProcessor = Arrays.copyOf(this.tasksProcessor,
          this.tasksProcessor.length);

      newProcessorWeights[i] += newTask.getWeight();
      newTasksProcessor[newTask.getIndex()] = i;

      // If processor is null, create a new one and assign the task to it 
      if (processors[i] == null) {
        newProcessors[i] = new BitSet<Task>(taskGraph);
        newProcessors[i].add(newTask);
        complete = false;
      } else {
        newProcessors[i].add(newTask);
      }
      // Add the new allocation to the stack
      queue.add(new Allocation(
          newProcessors,
          newProcessorWeights,
          newTasksProcessor,
          this.allocationCount + 1,
          Math.max(this.maxWeight, processorWeights[i]),
          this.taskGraph
      ));
    }
  }

  /**
   * Returns a deep copy of the processors belonging to this allocation
   * 
   * @return
   */
  private Set<Task>[] deepCopyProcessors() {
    Set<Task>[] newProcessors = new BitSet[this.processors.length];
    for (int i = 0; i < this.processors.length; i++) {
      if (this.processors[i] == null) {
        break;
      }
      newProcessors[i] = new BitSet<Task>(this.processors[i]);
    }
    return newProcessors;
  }

}
