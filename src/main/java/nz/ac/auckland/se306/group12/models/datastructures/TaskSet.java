package nz.ac.auckland.se306.group12.models.datastructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;

@RequiredArgsConstructor
public class TaskSet implements Set<Task> {

  /**
   * As we are using an int to store the bitmap, we can only store up to 32 tasks, or a maximum
   * index of 31 as integers are 32 bits long.
   */
  private static final int MAX_TASK_INDEX = 31;

  private final Graph taskGraph;
  private int taskBitMap = 0;
  private int taskCount = 0;

  public TaskSet(Set<Task> existingTaskSet) {
    if (!(existingTaskSet instanceof TaskSet taskSet)) {
      throw new IllegalArgumentException("TaskSet can only be constructed from another TaskSet");
    }

    this.taskBitMap = taskSet.taskBitMap;
    this.taskCount = taskSet.taskCount;
    this.taskGraph = taskSet.taskGraph;
  }

  public TaskSet(TaskSet existingTaskSet) {
    this.taskBitMap = existingTaskSet.taskBitMap;
    this.taskCount = existingTaskSet.taskCount;
    this.taskGraph = existingTaskSet.taskGraph;
  }

  /**
   * @inheritDoc
   */
  @Override
  public int size() {
    return this.taskCount;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isEmpty() {
    return this.taskCount == 0;
  }

  /**
   * If the object is not an instance of {@link Task} this will always return {@code false}.
   *
   * @inheritDoc
   */
  @Override
  public boolean contains(Object object) {
    if (object instanceof Task task) {
      return this.containsTaskIndex(task.getIndex());
    }
    return false;
  }

  /**
   * Checks that this TaskSet contains a {@link Task} with the given index.
   *
   * @param taskIndex The index of the task to check for
   * @return {@code true} if the task index is contained, {@code false} otherwise
   */
  public boolean containsTaskIndex(int taskIndex) {
    // Check that there is a 1 bit at the taskIndex
    return (this.taskBitMap & (1 << taskIndex)) != 0;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean add(Task task) {
    boolean isContained = this.contains(task);
    if (isContained) {
      return false;
    }

    if (task.getIndex() > MAX_TASK_INDEX) {
      throw new IllegalArgumentException(
          String.format("Task index %d exceeds maximum allowed %d for TaskSet", task.getIndex(),
              MAX_TASK_INDEX));
    }

    // Add a 1 bit to the taskBitMap at the index of the task
    this.taskBitMap |= (1 << task.getIndex());
    this.taskCount++;
    return true;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean remove(Object object) {
    if (!this.contains(object)) {
      return false;
    }

    // We know that the object is a task because of the contains check
    Task task = (Task) object;
    this.taskBitMap &= ~(1 << task.getIndex());
    this.taskCount--;
    return true;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean containsAll(Collection<?> collection) {
    for (Object object : collection) {
      if (!this.contains(object)) {
        return false;
      }
    }

    return true;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean addAll(Collection<? extends Task> collection) {
    int oldTaskCount = this.taskCount;
    for (Task task : collection) {
      this.add(task);
    }

    return this.taskCount != oldTaskCount;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean retainAll(Collection<?> collection) {
    return this.removeIf(task -> !collection.contains(task));
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean removeAll(Collection<?> collection) {
    return this.removeIf(collection::contains);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void clear() {
    this.taskCount = 0;
    this.taskBitMap = 0;
  }

  /**
   * @inheritDoc
   */
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
            return TaskSet.this.taskGraph.getTask(this.currentTaskIndex++);
          }
          this.currentTaskIndex++;
        }

        // This should theoretically never happen because of the check in hasNext()
        return null;
      }
    };
  }

  /**
   * This method has not been implemented as it is not required and I don't foresee any likely
   * usages of it in the future. This can be added later if required.
   *
   * @throws UnsupportedOperationException If this method is called
   */
  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException("toArray() is not supported on TaskSet");
  }

  /**
   * This method has not been implemented as it is not required and I don't foresee any likely
   * usages of it in the future. This can be added later if required.
   *
   * @throws UnsupportedOperationException If this method is called
   */
  @Override
  public <T> T[] toArray(T[] array) {
    throw new UnsupportedOperationException("toArray() is not supported on TaskSet");
  }

}
