package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

public class TopologicalSorter {

  /**
   * Uses finishing times from DFS to obtain <em>a</em> valid topological order, given a directed
   * acyclic graph.
   */
  public List<Node> getSomeTopologicalOrder(Graph graph) {

    // Find a source from which to start DFS traversal
    Node startNode = graph.getNodes()
        .stream()
        .filter(node -> node.getParents().isEmpty())
        .findFirst() // `findAny()` would be valid, but we need determinism for unit testing
        .orElse(null); // Input graph not acyclic

    if (startNode == null) {
      throw new RuntimeException(
          "Input digraph has a cycle. No topological order to be found.");
    }
    
    Set<Node> discovered = new LinkedHashSet<>(graph.getNodes());
    Deque<Node> stack = new ArrayDeque<>();
    stack.push(startNode);
    while (!stack.isEmpty()) {
      Node node = stack.pop();
      if (!discovered.contains(node)) {
        discovered.add(node);
        node.getChildren().stream().map(Edge::getDestination).forEach(stack::push);
      }
    }

    // Insertion order into `discovered` is a valid topological order
    LinkedList<Node> topologicalOrder = new LinkedList<>();
    discovered.forEach(topologicalOrder::addFirst);

    return new ArrayList<>(topologicalOrder);
  }

}
