package nz.ac.auckland.se306.group12;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopologicalSortTest {

  private final TopologicalSorter sorter = new TopologicalSorter();
  private final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Checks if a topological order returned by a graph is valid by iterating through each node in
   * order to ensure each subsequent node is in a lower order.
   *
   * @param graph to be checked
   */
  void checkGraphTopologicalOrder(Graph graph) {
    Set<Task> visitedTasks = new HashSet<>();
    List<Task> topologicalOrder = sorter.getATopologicalOrder(graph);

    Assertions.assertEquals(graph.taskCount(), topologicalOrder.size(),
        "Number of nodes in the topological order and graph doesn't match.");

    for (Task task : topologicalOrder) {
      visitedTasks.add(task);
      for (Edge outgoingEdge : task.getOutgoingEdges()) {
        Task destinationTask = outgoingEdge.getDestination();
        // Check for any parent nodes that have already been visited
        if (visitedTasks.contains(destinationTask)) {
          Assertions.fail("Topological order is not correct");
        }
      }
    }
  }

  /**
   * Test for trivial graph
   */
  @Test
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    checkGraphTopologicalOrder(graph);
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    checkGraphTopologicalOrder(graph);
  }
}
