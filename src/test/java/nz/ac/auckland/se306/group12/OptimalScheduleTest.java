package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.scheduler.AStarScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests functionality of the model classes used for the graph classes
 */
class OptimalScheduleTest {

  private final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Returns an output schedule from specified
   *
   * @param graph          {@link Graph} to produce a schedule out of
   * @param processorCount number of processors that the schedule contains
   * @return schedule output by the scheduler
   */
  private Schedule getOutputSchedule(Graph graph, int processorCount) {
    Scheduler scheduler = new AStarScheduler();
    return scheduler.schedule(graph, processorCount);
  }

  @Test
  void Test2ProcNodes_7_OutTree() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_7_OutTree.dot");
    Schedule schedule = this.getOutputSchedule(graph, 2);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(28, schedule.getLatestEndTime());
  }


  @Test
  void Test4ProcNodes_7_OutTree() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_7_OutTree.dot");
    Schedule schedule = this.getOutputSchedule(graph, 4);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(22, schedule.getLatestEndTime());
  }

  @Test
  void Test2ProcNodes_8_Random() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_8_Random.dot");
    Schedule schedule = this.getOutputSchedule(graph, 2);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(581, schedule.getLatestEndTime());
  }

  @Test
  void Test4ProcNodes_8_Random() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_8_Random.dot");
    Schedule schedule = this.getOutputSchedule(graph, 4);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(581, schedule.getLatestEndTime());
  }

  @Test
  void Test2ProcNodes_9_SeriesParallel() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_9_SeriesParallel.dot");
    Schedule schedule = this.getOutputSchedule(graph, 2);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(55, schedule.getLatestEndTime());
  }

  @Test
  void Test4ProcNodes_9_SeriesParallel() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_9_SeriesParallel.dot");
    Schedule schedule = this.getOutputSchedule(graph, 4);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(55, schedule.getLatestEndTime());
  }

  @Test
  void Test2ProcNodes_10_Random() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_10_Random.dot");
    Schedule schedule = this.getOutputSchedule(graph, 2);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(50, schedule.getLatestEndTime());
  }

  @Test
  void Test4ProcNodes_10_Random() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_10_Random.dot");
    Schedule schedule = this.getOutputSchedule(graph, 4);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(50, schedule.getLatestEndTime());
  }

  @Test
  void Test2ProcNodes_11_OutTree() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    Schedule schedule = this.getOutputSchedule(graph, 2);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(350, schedule.getLatestEndTime());
  }

  @Test
  void Test4ProcNodes_11_OutTree() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    Schedule schedule = this.getOutputSchedule(graph, 4);
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.assertValidSchedule(schedule, graph);
    Assertions.assertEquals(227, schedule.getLatestEndTime());
  }
}