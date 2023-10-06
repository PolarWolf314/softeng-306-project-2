package nz.ac.auckland.se306.group12;

import java.io.File;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.scheduler.DfsScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Tests functionality of the model classes used for the graph classes
 */
class OptimisedScheduleTest {

  private final DotGraphIO dotGraphIO = new DotGraphIO();

  private Schedule getOutputSchedule(String pathFromProjectRoot, int processorCount) {
    try {
      Graph graph = this.dotGraphIO.readDotGraph(new File(pathFromProjectRoot));

      Scheduler scheduler = new DfsScheduler();
      return scheduler.schedule(graph, processorCount);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }

  @Test
  public void Test2ProcNodes_7_OutTree() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_7_OutTree.dot", 2);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_7_OutTree.dot");
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 28);
  }


  @Test
  public void Test4ProcNodes_7_OutTree() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_7_OutTree.dot", 4);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_7_OutTree.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 22);
  }

  @Test
  public void Test2ProcNodes_8_Random() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_8_Random.dot", 2);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_8_Random.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 581);
  }

  @Test
  public void Test4ProcNodes_8_Random() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_8_Random.dot", 4);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_8_Random.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 581);
  }

  @Test
  public void Test2ProcNodes_9_SeriesParallel() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_9_SeriesParallel.dot", 2);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_9_SeriesParallel.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 55);
  }

  @Test
  public void Test4ProcNodes_9_SeriesParallel() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_9_SeriesParallel.dot", 4);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_9_SeriesParallel.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 55);
  }

  @Test
  public void Test2ProcNodes_10_Random() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_10_Random.dot", 2);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_10_Random.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 50);
  }

  @Test
  public void Test4ProcNodes_10_Random() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_10_Random.dot", 4);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_10_Random.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 50);
  }

  @Test
  public void Test2ProcNodes_11_OutTree() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_11_OutTree.dot", 2);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 350);
  }

  @Test
  public void Test4ProcNodes_11_OutTree() {
    Schedule schedule = this.getOutputSchedule("./graphs/Nodes_11_OutTree.dot", 4);
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    dotGraphIO.writeOutputDotGraphToConsole(graph.getName(), schedule, graph);
    ScheduleValidator.validateSchedule(schedule, graph);
    Assertions.assertEquals(schedule.getLatestEndTime(), 227);
  }

}