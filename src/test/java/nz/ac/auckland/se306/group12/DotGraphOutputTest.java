package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DotGraphOutputTest {

  private final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Test that the dot graph output is correct for a single processor when given a linear graph (a
   * graph with no branches)
   */
  @Test
  public void testSingleProcessorLinearGraphTest() {
    String expectedDotGraph = """
        digraph "test1-output" {
        A [Weight=2,Start=0,Processor=1];
        A -> B [Weight=1];
        B [Weight=3,Start=2,Processor=1];
        B -> C [Weight=3];
        C [Weight=1,Start=5,Processor=1];
        C -> D [Weight=2];
        D [Weight=4,Start=6,Processor=1];
        D -> E [Weight=1];
        E [Weight=2,Start=10,Processor=1];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addNode("A", 2);
    actualGraph.addNode("B", 3);
    actualGraph.addNode("C", 1);
    actualGraph.addNode("D", 4);
    actualGraph.addNode("E", 2);

    actualGraph.addEdge("A", "B", 1);
    actualGraph.addEdge("B", "C", 3);
    actualGraph.addEdge("C", "D", 2);
    actualGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(5, 6, 0);
    ScheduledTask taskD = new ScheduledTask(6, 10, 0);
    ScheduledTask taskE = new ScheduledTask(10, 12, 0);

    Schedule actualSchedule = new Schedule(5, 1);
    actualSchedule = actualSchedule.extendWithTask(taskA, 0);
    actualSchedule = actualSchedule.extendWithTask(taskB, 1);
    actualSchedule = actualSchedule.extendWithTask(taskC, 2);
    actualSchedule = actualSchedule.extendWithTask(taskD, 3);
    actualSchedule = actualSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test1-output", actualSchedule,
        actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for multiple processors when given a linear graph (a
   * graph with no branches)
   */
  @Test
  public void testMultiProcessorLinearGraphTest() {
    String expectedDotGraph = """
        digraph "test2-output" {
        A [Weight=2,Start=0,Processor=1];
        A -> B [Weight=1];
        B [Weight=3,Start=2,Processor=1];
        B -> C [Weight=3];
        C [Weight=1,Start=8,Processor=2];
        C -> D [Weight=2];
        D [Weight=4,Start=11,Processor=3];
        D -> E [Weight=1];
        E [Weight=2,Start=16,Processor=4];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addNode("A", 2);
    actualGraph.addNode("B", 3);
    actualGraph.addNode("C", 1);
    actualGraph.addNode("D", 4);
    actualGraph.addNode("E", 2);

    actualGraph.addEdge("A", "B", 1);
    actualGraph.addEdge("B", "C", 3);
    actualGraph.addEdge("C", "D", 2);
    actualGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(8, 9, 1);
    ScheduledTask taskD = new ScheduledTask(11, 15, 2);
    ScheduledTask taskE = new ScheduledTask(16, 18, 3);

    Schedule actualSchedule = new Schedule(5, 4);
    actualSchedule = actualSchedule.extendWithTask(taskA, 0);
    actualSchedule = actualSchedule.extendWithTask(taskB, 1);
    actualSchedule = actualSchedule.extendWithTask(taskC, 2);
    actualSchedule = actualSchedule.extendWithTask(taskD, 3);
    actualSchedule = actualSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test2-output", actualSchedule, actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for a single processor when given a graph with no
   * edges
   */
  @Test
  public void testSingleProcessorIsolatedTasksGraphTest() {
    String expectedDotGraph = """
        digraph "test3-output" {
        A [Weight=2,Start=0,Processor=1];
        B [Weight=3,Start=2,Processor=1];
        C [Weight=1,Start=5,Processor=1];
        D [Weight=4,Start=6,Processor=1];
        E [Weight=2,Start=10,Processor=1];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addNode("A", 2);
    actualGraph.addNode("B", 3);
    actualGraph.addNode("C", 1);
    actualGraph.addNode("D", 4);
    actualGraph.addNode("E", 2);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(5, 6, 0);
    ScheduledTask taskD = new ScheduledTask(6, 10, 0);
    ScheduledTask taskE = new ScheduledTask(10, 12, 0);

    Schedule actualSchedule = new Schedule(5, 1);
    actualSchedule = actualSchedule.extendWithTask(taskA, 0);
    actualSchedule = actualSchedule.extendWithTask(taskB, 1);
    actualSchedule = actualSchedule.extendWithTask(taskC, 2);
    actualSchedule = actualSchedule.extendWithTask(taskD, 3);
    actualSchedule = actualSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test3-output", actualSchedule,
        actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for multiple processors when given a graph with no
   * edges
   */
  @Test
  public void testMultiProcessorIsolatedTasksGraphTest() {
    String expectedDotGraph = """
        digraph "test4-output" {
        A [Weight=2,Start=0,Processor=1];
        B [Weight=3,Start=2,Processor=1];
        C [Weight=1,Start=8,Processor=2];
        D [Weight=4,Start=11,Processor=3];
        E [Weight=2,Start=16,Processor=4];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addNode("A", 2);
    actualGraph.addNode("B", 3);
    actualGraph.addNode("C", 1);
    actualGraph.addNode("D", 4);
    actualGraph.addNode("E", 2);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(8, 9, 1);
    ScheduledTask taskD = new ScheduledTask(11, 15, 2);
    ScheduledTask taskE = new ScheduledTask(16, 18, 3);

    Schedule actualSchedule = new Schedule(5, 4);
    actualSchedule = actualSchedule.extendWithTask(taskA, 0);
    actualSchedule = actualSchedule.extendWithTask(taskB, 1);
    actualSchedule = actualSchedule.extendWithTask(taskC, 2);
    actualSchedule = actualSchedule.extendWithTask(taskD, 3);
    actualSchedule = actualSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test4-output", actualSchedule, actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }
}
