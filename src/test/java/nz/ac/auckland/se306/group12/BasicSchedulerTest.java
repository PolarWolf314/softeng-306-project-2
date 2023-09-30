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

  final TopologicalSorter sorter = new TopologicalSorter();
  final BasicScheduler scheduler = new BasicScheduler();

  Processor findProcessor(Map<Processor, Integer> cpu, Node node) {
    List<Processor> processCore = cpu.keySet()
        .stream()
        .filter(processor -> processor.getScheduledTasks().contains(node))
        .toList();

    Assertions.assertEquals(1, processCore.size(), "Task exists in multiple processors");
    return processCore.get(0);
  }

  /**
   * Checks if a given list of tasks is in a proper order
   *
   * @param schedule to be checked
   * @return boolean of whether or not it is valid
   */
  boolean checkValidOrder(List<Node> schedule) {
    Set<Node> completedTasks = new HashSet<>();

    for (Node task : schedule) {
      completedTasks.add(task);
      boolean parentsComplete = task.getIncomingEdges()
          .stream()
          .allMatch(edge -> completedTasks.contains(edge.getSource()));
      boolean completedBeforeChildrenStart = task.getOutgoingEdges()
          .stream()
          .allMatch(edge -> edge.getDestination().getStartTime()
              >= task.getStartTime() + task.getWeight());

      if (!(parentsComplete && completedBeforeChildrenStart)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks that a graph's schedule is valid.
   *
   * @param graph        to be checked
   * @param numProcesses amount of processors
   */
  void validateSchedule(Graph graph, int numProcesses) {
    List<Node> tasks = this.sorter.getATopologicalOrder(graph);

    // Initialise my computer
    Map<Processor, Integer> processors = new HashMap<>();
    List<Processor> cores = this.scheduler.getABasicSchedule(tasks, numProcesses);

    for (Processor core : cores) {
      processors.put(core, 0);
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
      Processor processCore = findProcessor(processors, node);

      for (Edge edge : node.getIncomingEdges()) {
        Node parent = edge.getSource();
        int swapTime = 0;
        Processor processSource = findProcessor(processors, parent);
        if (!processCore.equals(processSource)) {
          swapTime = edge.getWeight();
        }
        System.out.println(node.getStartTime());
        Assertions.assertTrue(
            node.getStartTime() >= parent.getStartTime() + parent.getWeight() + swapTime,
            String.format("Schedule ordering is not valid: %s", node.getLabel()));
      }

      int currentCore = processors.get(processCore);
      Assertions.assertTrue(currentCore <= node.getStartTime(),
          String.format("Task %s overlaps with another task on processor %d", node.getLabel(),
              currentCore));
      processors.put(processCore, node.getStartTime() + node.getWeight());
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
    validateSchedule(graph, 2);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testDisjointGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    validateSchedule(graph, processors);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testLongCommunication(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_long_communication_time.dot");
    validateSchedule(graph, processors);
  }

  /**
   * Test with graph that has multiple sources
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testAnnoyingGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    validateSchedule(graph, processors);
  }

  /**
   * Test with graph with a large path
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3})
  void testMultiplePaths(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    validateSchedule(graph, processors);
  }
}
