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
   * Test that the dot graph output is correct for a single processor
   */
  @Test
  public void testSingleProcessorTest() {
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

    Graph testGraph = new Graph();
    testGraph.addNode("A", 2);
    testGraph.addNode("B", 3);
    testGraph.addNode("C", 1);
    testGraph.addNode("D", 4);
    testGraph.addNode("E", 2);

    testGraph.addEdge("A", "B", 1);
    testGraph.addEdge("B", "C", 3);
    testGraph.addEdge("C", "D", 2);
    testGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(5, 6, 0);
    ScheduledTask taskD = new ScheduledTask(6, 10, 0);
    ScheduledTask taskE = new ScheduledTask(10, 12, 0);

    Schedule testSchedule = new Schedule(5, 1);
    testSchedule = testSchedule.extendWithTask(taskA, 0);
    testSchedule = testSchedule.extendWithTask(taskB, 1);
    testSchedule = testSchedule.extendWithTask(taskC, 2);
    testSchedule = testSchedule.extendWithTask(taskD, 3);
    testSchedule = testSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test1-output", testSchedule, testGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for multiple processors
   */
  @Test
  public void testMultiProcessorTest() {
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

    Graph testGraph = new Graph();
    testGraph.addNode("A", 2);
    testGraph.addNode("B", 3);
    testGraph.addNode("C", 1);
    testGraph.addNode("D", 4);
    testGraph.addNode("E", 2);

    testGraph.addEdge("A", "B", 1);
    testGraph.addEdge("B", "C", 3);
    testGraph.addEdge("C", "D", 2);
    testGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(8, 9, 1);
    ScheduledTask taskD = new ScheduledTask(11, 15, 2);
    ScheduledTask taskE = new ScheduledTask(16, 18, 3);

    Schedule testSchedule = new Schedule(5, 4);
    testSchedule = testSchedule.extendWithTask(taskA, 0);
    testSchedule = testSchedule.extendWithTask(taskB, 1);
    testSchedule = testSchedule.extendWithTask(taskC, 2);
    testSchedule = testSchedule.extendWithTask(taskD, 3);
    testSchedule = testSchedule.extendWithTask(taskE, 4);

    String actualDotGraph = dotGraphIO.toDotString("test2-output", testSchedule, testGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }
}
