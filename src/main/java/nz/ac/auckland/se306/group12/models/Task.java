package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import nz.ac.auckland.se306.group12.models.datastructures.Indexable;

/**
 * Node class represents a task in a schedule
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Task implements Indexable {

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

}
