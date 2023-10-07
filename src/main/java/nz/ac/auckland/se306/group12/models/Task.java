package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Node class represents a task in a schedule
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task {

  /**
   * The name of this task. Unique within a task graph.
   */
  @EqualsAndHashCode.Include
  private final String label;

  /**
   * The execution time of this task. Factored into equality checking for testing purposes.
   */
  @EqualsAndHashCode.Include
  private final int weight;

  private final Set<Edge> incomingEdges = new HashSet<>();

  private final Set<Edge> outgoingEdges = new HashSet<>();

  private final int index;

  @Setter
  private int bottomLevel = 0;

  /**
   * This method should not be used in performance sensitive areas as it recreates the entire
   * incomingEdges as a new set
   *
   * @return the set of tasks that are parents of this task
   */

  public Set<Task> getParentTasks() {
    return this.incomingEdges
        .stream()
        .map(Edge::getSource)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * This method should not be used in performance sensitive areas as it recreates the entire
   * outgoingEdges as a new set
   *
   * @return the set of tasks that are children of this task
   */
  public Set<Task> getChildTasks() {
    return this.outgoingEdges
        .stream()
        .map(Edge::getDestination)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * @return {@code true} if this task has no parent tasks (dependences), {@code false} otherwise.
   */
  public boolean isSource() {
    return this.incomingEdges.isEmpty();
  }

  /**
   * @return {@code true} if this task has no child tasks (dependents), {@code false} otherwise.
   */
  public boolean isSink() {
    return this.outgoingEdges.isEmpty();
  }

  /**
   * This method finds the bottom level from the current task. The bottom level is the maximum
   * distance from the task to a sink task (task without children)
   *
   * @return the bottom level of a particular task.
   */
  public int findBottomLevel() {
    int cost = 0;
    int max = 0;

    Stack<Task> taskStack = new Stack<>();
    Set<Task> visited = new HashSet<>();

    // Root task (current)
    taskStack.push(this);

    while (!taskStack.isEmpty()) {
      Task current = taskStack.peek();
      Set<Task> children = current.getChildTasks();

      // Arbitrary push an unvisited task on stack
      for (Task task : current.getChildTasks()) {
        if (!visited.contains(task)) {
          taskStack.push(task);
          break;
        }
      }

      // Check if current task has already been calculated
      if (!children.isEmpty() && visited.containsAll(current.getChildTasks())) {
        visited.add(taskStack.pop());
        cost -= current.weight;
        continue;
      } else {
        if (!visited.contains(current)) {
          cost += current.weight;
          visited.add(current);
        }
        if (cost > max) {
          max = cost;
        }
      }

      // Account for sink tasks
      if (children.isEmpty()) {
        visited.add(taskStack.pop());
        cost -= current.weight;
      }
    }
    return max;
  }
}
