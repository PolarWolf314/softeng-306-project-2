package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.exceptions.IllegalGraphException;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

public class TopologicalSorter {

  /**
   * Uses finishing times from DFS to obtain <em>a</em> valid topological order, given a directed
   * acyclic graph.
   * <p>
   * Although a DAG may have multiple valid topological orderings, the behaviour of this
   * implementation is intentionally deterministic to aid unit testing. Given the same graph, it
   * will return the same topological order.
   *
   * @param graph The dependence graph of tasks (a DAG) for which a topological order is to be
   *              found.
   * @return A list of the {@link Node}s from the input graph, in a topological order.
   * @throws IllegalGraphException If given a cyclic digraph.
   */
  public List<Node> getATopologicalOrder(Graph graph) {
    // Find a source from which to start DFS traversal
    Node startNode = graph.getNodes()
        .values()
        .stream()
        .filter(node -> node.getIncomingEdges().isEmpty())
        .findFirst() // `findAny()` would be valid, but we need determinism for unit testing
        .orElse(null); // Input graph not acyclic

    if (startNode == null) {
      throw new IllegalGraphException(
          "Input digraph has a cycle. No topological order to be found.");
    }

    // Algorithm relies on insertion order into this set, hence `LinkedHashSet`
    Set<Node> discoveredNodes = new LinkedHashSet<>(graph.getNodes().size());
    Deque<Node> stack = new ArrayDeque<>();
    stack.push(startNode);
    while (!stack.isEmpty()) {
      Node node = stack.pop();
      if (!discoveredNodes.contains(node)) {
        discoveredNodes.add(node);
        node.getOutgoingEdges().stream().map(Edge::getDestination).forEach(stack::push);
      }
    }

    // Reverse of insertion order into `discoveredNodes` is a valid topological order
    LinkedList<Node> topologicalOrder = new LinkedList<>();
    discoveredNodes.forEach(topologicalOrder::addFirst);

    return new ArrayList<>(topologicalOrder);
  }

}
