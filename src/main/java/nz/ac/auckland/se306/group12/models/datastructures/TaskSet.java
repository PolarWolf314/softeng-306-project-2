package nz.ac.auckland.se306.group12.models.datastructures;

import java.util.Iterator;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;

@NoArgsConstructor
public class TaskSet {

  /**
   * As we are using an int to store the bitmap, we can only store up to 32 tasks, or a maximum
   * index of 31 as integers are 32 bits long.
   */
  private static final int MAX_TASK_INDEX = 31;
  private int taskBitMap = 0;
  private int taskCount = 0;

  public TaskSet(TaskSet existingTaskSet) {
    this.taskBitMap = existingTaskSet.taskBitMap;
    this.taskCount = existingTaskSet.taskCount;
  }


  public int size() {
    return this.taskCount;
  }

  public boolean isEmpty() {
    return this.taskCount == 0;
  }

  public boolean contains(Task task) {
    return this.containsTaskIndex(task.getIndex());
  }

  public boolean containsTaskIndex(int taskIndex) {
    return (this.taskBitMap & (1 << taskIndex)) != 0;
  }

  public boolean add(Task task) {
    boolean isContained = !this.contains(task);
    if (isContained) {
      return false;
    }

    if (task.getIndex() > MAX_TASK_INDEX) {
      throw new IllegalArgumentException(
          String.format("Task index %d exceeds maximum allowed %d for TaskSet", task.getIndex(),
              MAX_TASK_INDEX));
    }

    this.taskBitMap |= (1 << task.getIndex());
    this.taskCount++;
    return true;
  }

  public boolean remove(Task task) {
    if (!this.contains(task)) {
      return false;
    }

    this.taskBitMap &= ~(1 << task.getIndex());
    this.taskCount--;
    return true;
  }

  public void clear() {
    this.taskCount = 0;
    this.taskBitMap = 0;
  }

  public Iterable<Task> toIterable(Graph graph) {
    return new IterableTaskSet(graph);
  }

  @RequiredArgsConstructor
  public class IterableTaskSet implements Iterable<Task> {

    private final Graph taskGraph;

    @Override
    public Iterator<Task> iterator() {
      return new Iterator<>() {
        private int currentTaskIndex = 0;
        private int currentTaskCount = TaskSet.this.taskCount;

        @Override
        public boolean hasNext() {
          return this.currentTaskCount != 0;
        }

        @Override
        public Task next() {
          while (this.currentTaskIndex <= MAX_TASK_INDEX) {
            if (TaskSet.this.containsTaskIndex(this.currentTaskIndex)) {
              this.currentTaskCount--;
              return IterableTaskSet.this.taskGraph.getTask(this.currentTaskIndex++);
            }
            this.currentTaskIndex++;
          }

          // This should theoretically never happen because of the check in hasNext()
          return null;
        }
      };
    }
  }
}
