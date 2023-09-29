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

  void checkForValidSchedule(Graph graph) {
    List<Node> tasks = sorter.getATopologicalOrder(graph);
    System.out.println(tasks);
    List<Processor> schedule = scheduler.getABasicSchedule(tasks, 2);
    System.out.println(schedule);
  }

  /**
   * Test for trivial graph
   */
  @Test
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    checkForValidSchedule(graph);
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    checkForValidSchedule(graph);
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    checkForValidSchedule(graph);
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    checkForValidSchedule(graph);
  }
}
