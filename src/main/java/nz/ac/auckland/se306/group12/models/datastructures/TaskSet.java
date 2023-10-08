package nz.ac.auckland.se306.group12.models.datastructures;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;

/**
 * A set of {@link Task tasks} that is stored in a bitmap. This is used to increase performance of
 * operations like {@link #contains(Object)}, {@link #add(Task)} and {@link #remove(Object)}.
 * <p>
 * As this uses an integer for the bitmap, it is limited to 32 tasks, or a maximum index of
 * {@link #MAX_TASK_INDEX}. Attempting to add a task with an invalid index will cause an
 * {@link IllegalArgumentException} to be thrown.
 * <p>
 * Additionally, operations like {@link #containsAll(Collection)}, {@link #addAll(Collection)},
 * {@link #removeAll(Collection)}, {@link #retainAll(Collection)} have been optimised when being
 * used with another {@link TaskSet} as it leverages bitwise operations on the two bitmaps, which
 * makes them significantly faster than the default implementations.
 */
@RequiredArgsConstructor
public class TaskSet implements Set<Task> {

  /**
   * As we are using an int to store the bitmap, we can only store up to 32 tasks, or a maximum
   * index of 31 as integers are 32 bits long.
   */
  public static final int MAX_TASK_INDEX = 31;

  private final Graph taskGraph;
  private int taskBitMap = 0;
  private int taskCount = 0;

  /**
   * Creates a new {@link TaskSet} from a {@link Set} that is actually an instance of TaskSet. This
   * is simply a convenience method to avoid having to cast the set to a TaskSet, allowing
   * abstraction to be maintained. Attempting to use this constructor with a set that is not an
   * instance of TaskSet will cause an {@link IllegalArgumentException} to be thrown.
   *
   * @param existingTaskSet The existing {@link TaskSet} to create a new TaskSet from
   * @throws IllegalArgumentException If the existingTaskSet is not an instance of {@link TaskSet}
   */
  public TaskSet(Set<Task> existingTaskSet) {
    if (!(existingTaskSet instanceof TaskSet taskSet)) {
      throw new IllegalArgumentException("TaskSet can only be constructed from another TaskSet");
    }

    this.taskBitMap = taskSet.taskBitMap;
    this.taskCount = taskSet.taskCount;
    this.taskGraph = taskSet.taskGraph;
  }

  /**
   * Creates a new {@link TaskSet} from an existing TaskSet.
   *
   * @param existingTaskSet The existing {@link TaskSet} to create a new TaskSet from
   */
  public TaskSet(TaskSet existingTaskSet) {
    this.taskBitMap = existingTaskSet.taskBitMap;
    this.taskCount = existingTaskSet.taskCount;
    this.taskGraph = existingTaskSet.taskGraph;
  }

  /**
   * Updates the taskBitMap to the new value and recalculates the new taskCount. This should only be
   * used when making large changes to the TaskSet as the cost of recalculating the taskCount is not
   * justified when only adding/removing a single task. If the taskBitMap is not changed, this will
   * not change anything or recalculate the taskCount.
   *
   * @param newTaskBitMap The new taskBitMap
   * @return {@code true} if the taskBitMap was changed, {@code false} otherwise
   */
  private boolean setTaskBitMap(int newTaskBitMap) {
    if (this.taskBitMap == newTaskBitMap) {
      return false;
    }

    this.taskBitMap = newTaskBitMap;
    this.taskCount = Integer.bitCount(this.taskBitMap);
    return true;
  }

  /**
   * Checks that the index of the {@link Task} is within the allowed bounds of 0 to
   * {@link #MAX_TASK_INDEX}. If the index is not valid an {@link IllegalArgumentException} is
   * thrown.
   *
   * @param task The task to check the index of
   * @throws IllegalArgumentException If the index of the task is not valid
   */
  private void assertValidTaskIndex(Task task) {
    if (task.getIndex() < 0 || task.getIndex() > MAX_TASK_INDEX) {
      throw new IllegalArgumentException(String.format(
          "Task index %d is outside the range of supported indices (0 to %d) for TaskSet",
          task.getIndex(),
          MAX_TASK_INDEX));
    }
  }


  /**
   * Checks that this TaskSet contains a {@link Task} with the given index.
   *
   * @param taskIndex The index of the task to check for
   * @return {@code true} if the task index is contained, {@code false} otherwise
   */
  private boolean containsTaskIndex(int taskIndex) {
    // Check that there is a 1 bit at the taskIndex
    return (this.taskBitMap & (1 << taskIndex)) != 0;
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
      // We have to check that the index of the task is valid to prevent it overflowing the int
      this.assertValidTaskIndex(task);
      return this.containsTaskIndex(task.getIndex());
    }
    return false;
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

    // We know the index of the task is valid because of the contains check
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

    // We know that the object is a task and that the index is valid because of the contains check
    Task task = (Task) object;
    this.taskBitMap &= ~(1 << task.getIndex());
    this.taskCount--;
    return true;
  }

  /**
   * If the collection is an {@link TaskSet} this will use bitwise operations to check if all the
   * tasks are contained, which is significantly faster than the default implementation. Otherwise,
   * it will iterate through the collection and check that each task is contained.
   *
   * @inheritDoc
   */
  @Override
  public boolean containsAll(Collection<?> collection) {
    if (collection instanceof TaskSet otherTaskSet) {
      return (this.taskBitMap & otherTaskSet.taskBitMap) == otherTaskSet.taskBitMap;
    }

    for (Object object : collection) {
      if (!this.contains(object)) {
        return false;
      }
    }

    return true;
  }

  /**
   * If the collection is an {@link TaskSet} this will use bitwise operations to add all the tasks
   * at once, which is significantly faster than the default implementation. Otherwise, it will
   * iterate through the collection and add each task individually.
   *
   * @inheritDoc
   */
  @Override
  public boolean addAll(Collection<? extends Task> collection) {
    if (collection instanceof TaskSet otherTaskSet) {
      // Combine the bitmaps of the two TaskSets
      return this.setTaskBitMap(this.taskBitMap | otherTaskSet.taskBitMap);
    }

    int oldTaskCount = this.taskCount;
    for (Task task : collection) {
      this.add(task);
    }
    return this.taskCount != oldTaskCount;
  }

  /**
   * If the collection is an {@link TaskSet} this will use bitwise operations retain only the tasks
   * also in the other collection all at once, which is significantly faster than the default
   * implementation. Otherwise, it will iterate through all the tasks in this set and remove each
   * task individually if it's not contained in the collection.
   *
   * @inheritDoc
   */
  @Override
  public boolean retainAll(Collection<?> collection) {
    if (collection instanceof TaskSet otherTaskSet) {
      // Remove the bits of this TaskSet that are not in the other TaskSet
      return this.setTaskBitMap(this.taskBitMap & otherTaskSet.taskBitMap);
    }
    return this.removeIf(task -> !collection.contains(task));
  }

  /**
   * If the collection is an {@link TaskSet} this will use bitwise operations to remove all the
   * tasks at once, which is significantly faster than the default implementation. Otherwise, it
   * will iterate through the tasks in the collection and remove them individually.
   *
   * @inheritDoc
   */
  @Override
  public boolean removeAll(Collection<?> collection) {
    if (collection instanceof TaskSet otherTaskSet) {
      // Remove the bits of the other TaskSet from this TaskSet
      return this.setTaskBitMap(this.taskBitMap & ~otherTaskSet.taskBitMap);
    }

    // It's likely faster to iterate through the other collection than to iterate through this one
    int oldTaskCount = this.taskCount;
    for (Object object : collection) {
      this.remove(object);
    }
    return this.taskCount != oldTaskCount;
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
