package nz.ac.auckland.se306.group12;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopologicalSortTest {

  /**
   * Checks that a given topological order is valid.
   *
   * @param topologicalOrder List of nodes to be checked
   * @return whether the order is correct
   */
  boolean checkNodeOrdering(List<Node> topologicalOrder) {
    Set<Node> visitedNodes = new HashSet<>();

    for (Node node : topologicalOrder) {
      visitedNodes.add(node);
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        Node destinationNode = outgoingEdge.getDestination();
        if (!visitedNodes.contains(destinationNode)) {
          return false;
        }
      }
    }

    return true;
  }

  @Test
  void test_trivial_graph() {
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test1.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();
      List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

      Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size());
      Assertions.assertTrue(checkNodeOrdering(topologicalOrder));


    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_disjoint_graph() {
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_disjoint_graphs.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();
      List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

      Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size());
      Assertions.assertTrue(checkNodeOrdering(topologicalOrder));

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_annoying_graph() {
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_annoying.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();
      List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

      Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size());
      Assertions.assertTrue(checkNodeOrdering(topologicalOrder));

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_multiple_paths() {
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_unintuitive_shortest_path.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();
      List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

      dotGraphIO.writeOrderToDotGraph("intuitive", topologicalOrder);

      Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size());
      Assertions.assertTrue(checkNodeOrdering(topologicalOrder));

      Assertions.assertTrue(true);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

}
