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

/*
 * Graph class represents a graph of tasks and their dependences to create a schedule
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
   * Add and edge to the graph and also update the nodes incoming and outgoing edges
   *
   * @param source      The source node label
   * @param destination The destination node label
   * @param weight      The weight of the edge
   */
  public void addEdge(String source, String destination, int weight) {
    Task sourceTask = this.tasks.get(source);
    Task destinationTask = this.tasks.get(destination);

    Edge edge = new Edge(sourceTask, destinationTask, weight);

    destinationTask.getIncomingEdges().add(edge);
    sourceTask.getOutgoingEdges().add(edge);
    this.edges.add(edge);
  }

  /**
   * Adds a node to the graph with the given label and weight.
   *
   * @param node   The node label
   * @param weight The node weight
   */
  public void addNode(String node, int weight) {
    this.tasks.put(node, new Task(node, weight));
  }

  public List<Task> getTasks() {
    return new ArrayList<>(this.tasks.values());
  }
}
