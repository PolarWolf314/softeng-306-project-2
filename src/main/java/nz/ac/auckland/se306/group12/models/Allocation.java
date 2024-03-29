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

  /**
   * A lookup table of the index each task is allocated to
   */
  private final int[] taskProcessorAllocation;

  private final int allocationCount;
  private final int maxWeight;
  private final Graph taskGraph;

  /**
   * Creates a new allocation from a specified task graph and number of processors.
   *
   * @param taskGraph      The task graph to create the allocation from
   * @param processorCount The number of processors to create the allocation with
   */
  public Allocation(Graph taskGraph, int processorCount) {
    this.processors = new BitSet[processorCount];
    this.allocationCount = 0;
    this.processorWeights = new int[processorCount];
    this.maxWeight = 0;
    this.taskGraph = taskGraph;
    this.taskProcessorAllocation = new int[taskGraph.taskCount()];
  }

  /**
   * Pushes all the child allocations that can be made from this allocation onto the stack.
   *
   * @param queue The queue to add the child allocations onto
   */
  public void extendAllocation(Queue<Allocation> queue) {
    Task newTask = this.taskGraph.getTask(this.allocationCount);
    // boolean to handle exit early if creating a new processor
    boolean complete = true;
    // Loop through each processor to allocate the task to it
    for (int i = 0; i < this.processors.length && complete; i++) {
      Set<Task>[] newProcessors = this.deepCopyProcessors();
      int[] newProcessorWeights = Arrays.copyOf(this.processorWeights,
          this.processorWeights.length);
      int[] newTaskProcessorAllocation = Arrays.copyOf(this.taskProcessorAllocation,
          this.taskProcessorAllocation.length);

      newProcessorWeights[i] += newTask.getWeight();
      newTaskProcessorAllocation[newTask.getIndex()] = i;

      // If processor is null, create a new one and assign the task to it 
      if (this.processors[i] == null) {
        newProcessors[i] = new BitSet<>(this.taskGraph);
        newProcessors[i].add(newTask);
        complete = false;
      } else {
        newProcessors[i].add(newTask);
      }
      // Add the new allocation to the stack
      queue.add(new Allocation(
          newProcessors,
          newProcessorWeights,
          newTaskProcessorAllocation,
          this.allocationCount + 1,
          Math.max(this.maxWeight, this.processorWeights[i]),
          this.taskGraph
      ));
    }
  }

  /**
   * Returns whether this is a complete allocation, i.e. All tasks have been allocated to a
   * processor.
   *
   * @return Whether this is a complete allocation
   */
  public boolean isComplete() {
    return this.allocationCount == this.taskGraph.taskCount();
  }

  /**
   * Returns a deep copy of the processors belonging to this allocation.
   *
   * @return The created processor deep copy
   */
  private Set<Task>[] deepCopyProcessors() {
    Set<Task>[] newProcessors = new BitSet[this.processors.length];
    for (int i = 0; i < this.processors.length; i++) {
      if (this.processors[i] == null) {
        break;
      }
      newProcessors[i] = new BitSet<>(this.processors[i]);
    }
    return newProcessors;
  }

  /**
   * Returns the heuristic value of this allocation.
   * 
   * @return The heuristic value of this allocation
   */
  public int getAllocationHeuristic() {
    int maxHeuristic = this.maxWeight;
    for (int i = 0; i < this.processors.length; i++) {
      if (this.processors[i] == null) {
        break;
      }
      int loadHeuristic = getLoadHeuristic(i);
      if (maxHeuristic < loadHeuristic) {
        maxHeuristic = loadHeuristic;
      }
    }

    return Math.max(maxHeuristic, criticalPathHeuristic());
  }

  /**
   * Returns the load heuristic of a processor.
   *
   * @param processorIndex The index of the processor to calculate the load heuristic for
   * @return The load heuristic of the processor
   */
  private int getLoadHeuristic(int processorIndex) {
    return minTopLevels(processorIndex) + this.processorWeights[processorIndex]
        + minBottomLevels(processorIndex);
  }

  /**
   * Returns the minimum top level of a processor.
   * 
   * @param processorIndex The index of the processor to calculate the minimum top level for
   * @return The minimum top level of the processor
   */
  private int minTopLevels(int processorIndex) {
    Set<Task> processor = this.processors[processorIndex];
    return processor.stream().mapToInt(task -> {
      return task.getTopLevel();
    }).min().orElse(0);
  }

  /**
   * Returns the minimum bottom level of a processor.
   *
   * @param processorIndex The index of the processor to calculate the minimum bottom level for
   * @return The minimum bottom level of the processor
   */
  private int minBottomLevels(int processorIndex) {
    Set<Task> processor = this.processors[processorIndex];
    return processor.stream().mapToInt(task -> {
      return task.getBottomLevel() - task.getWeight();
    }).min().orElse(0);
  }

  /**
   * Returns the critical path heuristic of this allocation.
   * 
   * @return The critical path heuristic of this allocation
   */
  private int criticalPathHeuristic() {
    int maxCriticalPath = 0;
    for (Set<Task> tasks : processors) {
      if (tasks == null) {
        break;
      }
      for (Task task : tasks) {
        int criticalPath = task.getBottomLevel() + task.getTopLevel();
        if (criticalPath > maxCriticalPath) {
          maxCriticalPath = criticalPath;
        }
      }
    }
    return maxCriticalPath;
  }

}
