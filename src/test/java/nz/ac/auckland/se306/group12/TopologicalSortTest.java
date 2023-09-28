package nz.ac.auckland.se306.group12;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopologicalSortTest {

  private final TopologicalSorter sorter = new TopologicalSorter();

  /**
   * Checks if a topological order returned by a graph is valid by iterating through each node in
   * order to ensure each subsequent node is in a lower order.
   *
   * @param graph to be checked
   */
  void checkGraphTopologicalOrder(Graph graph) {
    Set<Node> visitedNodes = new HashSet<>();
    List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

    Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size(),
        "Order's size is wrong");

    for (Node node : topologicalOrder) {
      visitedNodes.add(node);
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        Node destinationNode = outgoingEdge.getDestination();
        // Check for any parent nodes that have already been visited
        if (visitedNodes.contains(destinationNode)) {
          Assertions.fail("Topological order is not correct");
        }
      }
    }
  }

  /**
   * Test for trivial graph
   */
  @Test
  void test_trivial_graph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void test_disjoint_graph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void test_annoying_graph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void test_multiple_paths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    checkGraphTopologicalOrder(graph);
  }
}
