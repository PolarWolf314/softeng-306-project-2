package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    Set<Node> visited = new HashSet<>(graph.getNodes().size());
    List<Node> list = new ArrayList<>();

    // Iterate through all the nodes in the graph and call the recursive helper function
    for (Node node : graph.getNodes().values()) {
      if (!visited.contains(node)) {
        TopologicalSortUtil(node, visited, list);
      }
    }

    // Reverse output list to get a topological orderings (alternatively enqueue could be used)
    Collections.reverse(list);

    return list;
  }

  /**
   * Recursive helper function for {@link #getATopologicalOrder(Graph)}.
   * <p>
   * This ensures that the children of the input node are added to the topological list
   * before the node itself.
   *
   * @param node    The node to be added to the topological list.
   * @param visited A set of nodes that have already been visited.
   * @param list    The list of nodes in a topological order.
   */
  private void TopologicalSortUtil(Node node, Set<Node> visited, List<Node> list) {
    visited.add(node);
    // Recursively call this function for all the children that haven't been visited yet
    for (Edge edge : node.getOutgoingEdges()) {
      Node destination = edge.getDestination();
      if (!visited.contains(destination)) {
        TopologicalSortUtil(destination, visited, list);
      }
    }
    list.add(node);
  }

}
