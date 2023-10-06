package nz.ac.auckland.se306.group12.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import nz.ac.auckland.se306.group12.exceptions.DanglingEdgeException;
import nz.ac.auckland.se306.group12.exceptions.IllegalEdgeWeightException;

/**
 * Represents a graph of tasks and their dependences to create a schedule
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Graph {

  private final Map<String, Task> tasks = new LinkedHashMap<>();
  private final Set<Edge> edges = new HashSet<>();

  @Exclude
  private final String name;

  public Graph() {
    // Default name, for when the graph name doesn't matter
    this.name = "Graph";
  }

  /**
   * @return The tasks in this graph in a {@link List}
   */
  public List<Task> getTasks() {
    return new ArrayList<>(this.tasks.values());
  }

  /**
   * @return The number of tasks in this task graph. (In other words, the order of this graph.)
   */
  public int taskCount() {
    return this.tasks.size();
  }

  /**
   * Add and edge to the graph and also update the nodes incoming and outgoing edges
   *
   * @param source      The source node label
   * @param destination The destination node label
   * @param weight      The weight of the edge, representing communication cost
   */
  public void addEdge(String source, String destination, int weight) {
    Task sourceTask = this.tasks.get(source);
    if (sourceTask == null) {
      throw new DanglingEdgeException(String.format(
          "Cannot add edge (%s, %s) because node %s doesn't exist in the task graph.",
          source,
          destination,
          source)
      );
    }

    Task destinationTask = this.tasks.get(destination);
    if (destinationTask == null) {
      throw new DanglingEdgeException(String.format(
          "Cannot add edge (%s, %s) because node %s doesn't exist in the task graph.",
          source,
          destination,
          destination)
      );
    }

    if (weight < 0) {
      throw new IllegalEdgeWeightException("Edge weights must be non-negative.");
    }

    Edge edge = new Edge(sourceTask, destinationTask, weight);

    destinationTask.getIncomingEdges().add(edge);
    sourceTask.getOutgoingEdges().add(edge);
    this.edges.add(edge);
  }

  /**
   * Adds a node to the graph with the given label and weight.
   *
   * @param nodeLabel The task's label
   * @param weight    The node weight, representing the task's execution time
   */
  public void addNode(String nodeLabel, int weight) {
    this.tasks.put(nodeLabel, new Task(nodeLabel, weight, this.tasks.size()));
  }

}
