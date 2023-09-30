package nz.ac.auckland.se306.group12;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.scheduler.BasicScheduler;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        if (edge.getDestination().getStartTime() < task.getStartTime() + task.getWeight()) {
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

    for (Processor core : cores) {
      cpu.put(core, 0);
    }

    // Make sure schedule order is valid
    List<Node> schedule = TestUtil.scheduleToListNodes(
            cores).stream()
        .flatMap(List::stream)
        .sorted(Comparator.comparingInt(Node::getStartTime))
        .toList();
    Assertions.assertEquals(graph.getNodes().size(), schedule.size());
    Assertions.assertTrue(checkValidOrder(schedule));

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
   * Test for trivial graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    checkForValidSchedule(graph, 2);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testDisjointGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    checkForValidSchedule(graph, processors);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testLongCommunication(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_long_communication_time.dot");
    checkForValidSchedule(graph, processors);
  }

  /**
   * Test with graph that has multiple sources
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testAnnoyingGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    checkForValidSchedule(graph, processors);
  }

  /**
   * Test with graph with a large path
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testMultiplePaths(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    checkForValidSchedule(graph, processors);
  }
}
