package nz.ac.auckland.se306.group12.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/*
 * Graph class represents a graph of tasks and their dependences to create a schedule
 */
@Getter
@ToString
@EqualsAndHashCode
public class Graph {

  private final Map<String, Node> nodes = new HashMap<>();
  private final Set<Edge> edges = new HashSet<>();

  /**
   * Add and edge to the graph and also update the nodes incoming and outgoing edges
   *
   * @param source      The source node label
   * @param destination The destination node label
   * @param weight      The weight of the edge
   */
  public void addEdge(String source, String destination, int weight) {
    Node sourceNode = this.nodes.get(source);
    Node destinationNode = this.nodes.get(destination);

    Edge edge = new Edge(sourceNode, destinationNode, weight);

    destinationNode.getIncomingEdges().add(edge);
    sourceNode.getOutgoingEdges().add(edge);
    this.edges.add(edge);
  }

  public void addNode(String node, int weight) {
    this.nodes.put(node, new Node(node, weight));
  }

}
