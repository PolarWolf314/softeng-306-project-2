package nz.ac.auckland.se306.group12;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BasicSchedulerTest {

  private final BasicScheduler scheduler = new BasicScheduler();

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

      if (!parentsComplete) {
        System.out.format("Invalid order: Dependences of task %s not met.%n", task.getLabel());
        return false;
      }
      if (!completedBeforeChildrenStart) {
        System.out.format(
            "Invalid order: Dependents of task %s start before this task completes.%n",
            task.getLabel());
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

    // Initialise my computer
    Map<Processor, Integer> processors = new HashMap<>();
    List<Processor> cores = this.scheduler.getABasicSchedule(graph, numProcesses);

    for (Processor core : cores) {
      processors.put(core, 0);
    }
    // Make sure schedule order is valid
    List<Node> schedule = TestUtil.scheduleToListNodes(
            cores).stream()
        .flatMap(List::stream)
        .sorted(Comparator.comparingInt(Node::getStartTime))
        .toList();

    System.out.println("Processors: " + numProcesses);
    DotGraphIO io = new DotGraphIO();
    io.writeOutputDotGraphToConsole(graph.getName(), TestUtil.scheduleToListNodes(cores));

    Assertions.assertEquals(graph.getNodes().size(), schedule.size(),
        String.format("Graph has order %d, but %d tasks have been scheduled.%n",
            graph.getNodes().size(), schedule.size()));
    Assertions.assertTrue(this.checkValidOrder(schedule),
        "Invalid Schedule: A task's dependencies were not met before execution");

    // Run the schedule
    for (Node node : schedule) {
      Processor processCore = this.findProcessor(processors, node);

      for (Edge edge : node.getIncomingEdges()) {
        Node parent = edge.getSource();
        int swapTime = 0;
        Processor processSource = this.findProcessor(processors, parent);
        if (!processCore.equals(processSource)) {
          swapTime = edge.getWeight();
        }
        Assertions.assertTrue(
            node.getStartTime() >= parent.getStartTime() + parent.getWeight() + swapTime,
            String.format(
                "Invalid Schedule: Task %s starts before parent %s completes, at start %d",
                node.getLabel(), parent.getLabel(), node.getStartTime()));
      }

      int currentCoreValue = processors.get(processCore);
      Assertions.assertTrue(currentCoreValue <= node.getStartTime(),
          String.format("Invalid Schedule: Task %s overlaps with another task on processor %d",
              node.getLabel(),
              processCore.getProcessorIndex()));
      processors.put(processCore, node.getStartTime() + node.getWeight());
    }
  }


  /**
   * Test for trivial graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    this.validateSchedule(graph, 2);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testDisjointGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test for disconnected graph
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testLongCommunication(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_long_communication_time.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with graph that has multiple sources
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testAnnoyingGraph(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with graph with a large path
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testMultiplePaths(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    this.validateSchedule(graph, processors);
  }


  /**
   * Test with OutTree test case
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testOutTree(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_7_OutTree.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with Random test case
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testRandom(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_8_Random.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with SeriesParallel test case
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testSeriesParallel(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_9_SeriesParallel.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with Random 10 test case
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testRandom10(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_10_Random.dot");
    this.validateSchedule(graph, processors);
  }

  /**
   * Test with Random 11 OutTree test case
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 10, 24})
  void testOutTree11(int processors) {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    this.validateSchedule(graph, processors);
  }

}
