package nz.ac.auckland.se306.group12;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.scheduler.BasicScheduler;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BasicSchedulerTest {

  TopologicalSorter sorter = new TopologicalSorter();
  BasicScheduler scheduler = new BasicScheduler();

  boolean checkValidOrder(List<Node> schedule) {
    Set<Node> completedTasks = new HashSet<>();

    for (Node task : schedule) {
      completedTasks.add(task);
      for (Edge edge : task.getIncomingEdges()) {
        if (!completedTasks.contains(edge.getSource())) {
          return false;
        }
      }
      for (Edge edge : task.getOutgoingEdges()) {
        if (edge.getDestination().getStartTime() > task.getStartTime() + task.getWeight()) {
          return false;
        }
      }
    }

    return true;
  }

  void checkForValidSchedule(Graph graph, int processors) {
    List<Node> tasks = sorter.getATopologicalOrder(graph);

    // Initialise my computer
    Map<Processor, Integer> cpu = new HashMap<>();
    List<Processor> cores = scheduler.getABasicSchedule(tasks, processors);

    for (int i = 0; i < cores.size(); i++) {
      cpu.put(cores.get(i), 0);
    }

    // Make sure schedule order is valid
    List<Node> schedule = TestUtil.scheduleToListNodes(
            cores).stream()
        .flatMap(List::stream)
        .sorted(Comparator.comparingInt(Node::getStartTime))
        .toList();
    Assertions.assertEquals(tasks.size(), schedule.size());
    Assertions.assertTrue(checkValidOrder(schedule));

    DotGraphIO io = new DotGraphIO();
    io.writeOutputDotGraphToConsole("test", TestUtil.scheduleToListNodes(cores));

    // Run the schedule
    for (Node node : schedule) {
      List<Processor> processCore = cpu.keySet()
          .stream()
          .filter(processor -> processor.getScheduledTasks().contains(node))
          .toList();

      Assertions.assertEquals(1, processCore.size(), "Task exists in multiple processors");

      int currentValue = cpu.get(processCore.get(0));

      Assertions.assertTrue(currentValue <= node.getStartTime(),
          "Tasks are overlapping in the CPU");

      cpu.put(processCore.get(0), currentValue + node.getWeight());
    }
  }


  /**
   * Test with an invalid schedule
   */
  @Test
  void testInvalidSchedule() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    List<Node> schedule = new ArrayList<>();
    schedule.add(graph.getNodes().get("A"));
    schedule.add(graph.getNodes().get("C"));
    Assertions.assertFalse(checkValidOrder(schedule));
  }

  /**
   * Test with an invalid schedule
   */
  @Test
  void testInvalidScheduleAgain() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    List<Node> schedule = new ArrayList<>();
    schedule.add(graph.getNodes().get("A"));
    schedule.add(graph.getNodes().get("B"));
    schedule.add(graph.getNodes().get("C"));
    schedule.add(graph.getNodes().get("D"));
    schedule.add(graph.getNodes().get("F"));
    schedule.add(graph.getNodes().get("J"));
    Assertions.assertFalse(checkValidOrder(schedule));
  }

  /**
   * Test with a weird but valid schedule
   */
  @Test
  void testConcussionSchedule() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    List<Node> schedule = new ArrayList<>();
    schedule.add(graph.getNodes().get("A"));
    schedule.add(graph.getNodes().get("B"));
    schedule.add(graph.getNodes().get("C"));
    schedule.add(graph.getNodes().get("E"));
    schedule.add(graph.getNodes().get("G"));
    schedule.add(graph.getNodes().get("J"));
    schedule.add(graph.getNodes().get("D"));
    schedule.add(graph.getNodes().get("F"));
    schedule.add(graph.getNodes().get("H"));
    schedule.add(graph.getNodes().get("I"));
    Assertions.assertTrue(checkValidOrder(schedule));
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
   * Test for disconnected graph
   */
  @Test
  void testLongCommunication() {
    Graph graph = TestUtil.loadGraph("./graphs/test_long_communication_time.dot");
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
