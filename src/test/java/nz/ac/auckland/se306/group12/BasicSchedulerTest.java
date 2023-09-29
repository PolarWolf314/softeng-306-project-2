package nz.ac.auckland.se306.group12;

import java.util.List;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.scheduler.BasicScheduler;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Test;

public class BasicSchedulerTest {

  TopologicalSorter sorter = new TopologicalSorter();
  BasicScheduler scheduler = new BasicScheduler();

  void checkForValidSchedule(Graph graph, int processsors) {
    List<Node> tasks = sorter.getATopologicalOrder(graph);
    List<Processor> schedule = scheduler.getABasicSchedule(tasks, processsors);
    System.out.println(schedule);
  }

  /**
   * Test for trivial graph
   */
  @Test
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    checkForValidSchedule(graph, 2);
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    checkForValidSchedule(graph, 2);
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    checkForValidSchedule(graph, 2);
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    checkForValidSchedule(graph, 2);
  }
}
