package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

  public Set<Task> getParentTasks() {
    return incomingEdges.stream().map(Edge::getSource).collect(Collectors.toUnmodifiableSet());
  }

  public Set<Task> getChildTasks() {
    return outgoingEdges.stream().map(Edge::getDestination).collect(Collectors.toUnmodifiableSet());
  }

}
