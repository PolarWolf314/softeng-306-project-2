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
   * Checks if a topological order returned by a graph is valid.
   *
   * @param graph to be checked
   */
  void checkGraphTopologicalOrder(Graph graph) {
    TopologicalSorter sorter = new TopologicalSorter();
    Set<Node> visitedNodes = new HashSet<>();
    List<Node> topologicalOrder = sorter.getATopologicalOrder(graph);

    Assertions.assertEquals(graph.getNodes().size(), topologicalOrder.size());

    for (Node node : topologicalOrder) {
      visitedNodes.add(node);
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        Node destinationNode = outgoingEdge.getDestination();
        if (!visitedNodes.contains(destinationNode)) {
          Assertions.fail("Topological order contains a cycle");
        }
      }
    }
  }

  @Test
  void test_trivial_graph() {
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test1.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      checkGraphTopologicalOrder(graph);

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

      checkGraphTopologicalOrder(graph);

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

      checkGraphTopologicalOrder(graph);

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

      checkGraphTopologicalOrder(graph);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

}
