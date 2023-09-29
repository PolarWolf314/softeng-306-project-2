package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.scheduler.BasicScheduler;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Test;

public class BasicSchedulerTest {

  TopologicalSorter sorter = new TopologicalSorter();
  BasicScheduler scheduler = new BasicScheduler();

  /**
   * Test for trivial graph
   */
  @Test
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    sorter.getATopologicalOrder(graph);
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    sorter.getATopologicalOrder(graph);
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    sorter.getATopologicalOrder(graph);
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    sorter.getATopologicalOrder(graph);
  }
}
