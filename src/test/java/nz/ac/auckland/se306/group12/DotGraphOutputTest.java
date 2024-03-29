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
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);

    actualGraph.addEdge("A", "B", 1);
    actualGraph.addEdge("B", "C", 3);
    actualGraph.addEdge("C", "D", 2);
    actualGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(5, 6, 0);
    ScheduledTask taskD = new ScheduledTask(6, 10, 0);
    ScheduledTask taskE = new ScheduledTask(10, 12, 0);

    Schedule actualSchedule = new Schedule(actualGraph, 1);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));

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
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);

    actualGraph.addEdge("A", "B", 1);
    actualGraph.addEdge("B", "C", 3);
    actualGraph.addEdge("C", "D", 2);
    actualGraph.addEdge("D", "E", 1);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(8, 9, 1);
    ScheduledTask taskD = new ScheduledTask(11, 15, 2);
    ScheduledTask taskE = new ScheduledTask(16, 18, 3);

    Schedule actualSchedule = new Schedule(actualGraph, 4);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));

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
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(5, 6, 0);
    ScheduledTask taskD = new ScheduledTask(6, 10, 0);
    ScheduledTask taskE = new ScheduledTask(10, 12, 0);

    Schedule actualSchedule = new Schedule(actualGraph, 1);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));

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
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);

    ScheduledTask taskA = new ScheduledTask(0, 2, 0);
    ScheduledTask taskB = new ScheduledTask(2, 5, 0);
    ScheduledTask taskC = new ScheduledTask(8, 9, 1);
    ScheduledTask taskD = new ScheduledTask(11, 15, 2);
    ScheduledTask taskE = new ScheduledTask(16, 18, 3);

    Schedule actualSchedule = new Schedule(actualGraph, 4);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));

    String actualDotGraph = dotGraphIO.toDotString("test4-output", actualSchedule, actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for a single processor when given a complex graph
   * with branches
   */
  @Test
  public void testSingleProcessorComplexGraphTest() {
    String expectedDotGraph = """
        digraph "test5-output" {
        A [Weight=2,Start=7,Processor=1];
        A -> C [Weight=1];
        B [Weight=3,Start=0,Processor=1];
        B -> C [Weight=2];
        B -> D [Weight=3];
        C [Weight=1,Start=9,Processor=1];
        C -> F [Weight=2];
        C -> E [Weight=3];
        D [Weight=4,Start=3,Processor=1];
        D -> F [Weight=1];
        E [Weight=2,Start=17,Processor=1];
        E -> G [Weight=1];
        F [Weight=3,Start=10,Processor=1];
        F -> H [Weight=1];
        F -> I [Weight=2];
        G [Weight=5,Start=19,Processor=1];
        G -> J [Weight=2];
        H [Weight=2,Start=15,Processor=1];
        I [Weight=2,Start=13,Processor=1];
        J [Weight=7,Start=24,Processor=1];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);
    actualGraph.addTask("F", 3);
    actualGraph.addTask("G", 5);
    actualGraph.addTask("H", 2);
    actualGraph.addTask("I", 2);
    actualGraph.addTask("J", 7);

    actualGraph.addEdge("A", "C", 1);
    actualGraph.addEdge("B", "C", 2);
    actualGraph.addEdge("B", "D", 3);
    actualGraph.addEdge("C", "E", 3);
    actualGraph.addEdge("C", "F", 2);
    actualGraph.addEdge("D", "F", 1);
    actualGraph.addEdge("E", "G", 1);
    actualGraph.addEdge("F", "H", 1);
    actualGraph.addEdge("F", "I", 2);
    actualGraph.addEdge("G", "J", 2);

    ScheduledTask taskA = new ScheduledTask(7, 9, 0);
    ScheduledTask taskB = new ScheduledTask(0, 3, 0);
    ScheduledTask taskC = new ScheduledTask(9, 10, 0);
    ScheduledTask taskD = new ScheduledTask(3, 7, 0);
    ScheduledTask taskE = new ScheduledTask(17, 19, 0);
    ScheduledTask taskF = new ScheduledTask(10, 13, 0);
    ScheduledTask taskG = new ScheduledTask(19, 24, 0);
    ScheduledTask taskH = new ScheduledTask(15, 17, 0);
    ScheduledTask taskI = new ScheduledTask(13, 15, 0);
    ScheduledTask taskJ = new ScheduledTask(24, 31, 0);

    Schedule actualSchedule = new Schedule(actualGraph, 1);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));
    actualSchedule = actualSchedule.extendWithTask(taskF, actualGraph.getTask(5));
    actualSchedule = actualSchedule.extendWithTask(taskG, actualGraph.getTask(6));
    actualSchedule = actualSchedule.extendWithTask(taskH, actualGraph.getTask(7));
    actualSchedule = actualSchedule.extendWithTask(taskI, actualGraph.getTask(8));
    actualSchedule = actualSchedule.extendWithTask(taskJ, actualGraph.getTask(9));

    String actualDotGraph = dotGraphIO.toDotString("test5-output", actualSchedule,
        actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }

  /**
   * Test that the dot graph output is correct for multiple processors when given a complex graph
   * with branches
   */
  @Test
  public void testMultiProcessorComplexGraphTest() {
    String expectedDotGraph = """
        digraph "test6-output" {
        A [Weight=2,Start=0,Processor=2];
        A -> C [Weight=1];
        B [Weight=3,Start=0,Processor=4];
        B -> C [Weight=2];
        B -> D [Weight=3];
        C [Weight=1,Start=3,Processor=4];
        C -> F [Weight=2];
        C -> E [Weight=3];
        D [Weight=4,Start=6,Processor=3];
        D -> F [Weight=1];
        E [Weight=2,Start=4,Processor=4];
        E -> G [Weight=1];
        F [Weight=3,Start=10,Processor=3];
        F -> H [Weight=1];
        F -> I [Weight=2];
        G [Weight=5,Start=6,Processor=4];
        G -> J [Weight=2];
        H [Weight=2,Start=15,Processor=3];
        I [Weight=2,Start=13,Processor=3];
        J [Weight=7,Start=11,Processor=4];
        }
        """;

    Graph actualGraph = new Graph();
    actualGraph.addTask("A", 2);
    actualGraph.addTask("B", 3);
    actualGraph.addTask("C", 1);
    actualGraph.addTask("D", 4);
    actualGraph.addTask("E", 2);
    actualGraph.addTask("F", 3);
    actualGraph.addTask("G", 5);
    actualGraph.addTask("H", 2);
    actualGraph.addTask("I", 2);
    actualGraph.addTask("J", 7);

    actualGraph.addEdge("A", "C", 1);
    actualGraph.addEdge("B", "C", 2);
    actualGraph.addEdge("B", "D", 3);
    actualGraph.addEdge("C", "E", 3);
    actualGraph.addEdge("C", "F", 2);
    actualGraph.addEdge("D", "F", 1);
    actualGraph.addEdge("E", "G", 1);
    actualGraph.addEdge("F", "H", 1);
    actualGraph.addEdge("F", "I", 2);
    actualGraph.addEdge("G", "J", 2);

    ScheduledTask taskA = new ScheduledTask(0, 2, 1);
    ScheduledTask taskB = new ScheduledTask(0, 3, 3);
    ScheduledTask taskC = new ScheduledTask(3, 4, 3);
    ScheduledTask taskD = new ScheduledTask(6, 10, 2);
    ScheduledTask taskE = new ScheduledTask(4, 6, 3);
    ScheduledTask taskF = new ScheduledTask(10, 13, 2);
    ScheduledTask taskG = new ScheduledTask(6, 11, 3);
    ScheduledTask taskH = new ScheduledTask(15, 17, 2);
    ScheduledTask taskI = new ScheduledTask(13, 15, 2);
    ScheduledTask taskJ = new ScheduledTask(11, 18, 3);

    Schedule actualSchedule = new Schedule(actualGraph, 4);
    actualSchedule = actualSchedule.extendWithTask(taskA, actualGraph.getTask(0));
    actualSchedule = actualSchedule.extendWithTask(taskB, actualGraph.getTask(1));
    actualSchedule = actualSchedule.extendWithTask(taskC, actualGraph.getTask(2));
    actualSchedule = actualSchedule.extendWithTask(taskD, actualGraph.getTask(3));
    actualSchedule = actualSchedule.extendWithTask(taskE, actualGraph.getTask(4));
    actualSchedule = actualSchedule.extendWithTask(taskF, actualGraph.getTask(5));
    actualSchedule = actualSchedule.extendWithTask(taskG, actualGraph.getTask(6));
    actualSchedule = actualSchedule.extendWithTask(taskH, actualGraph.getTask(7));
    actualSchedule = actualSchedule.extendWithTask(taskI, actualGraph.getTask(8));
    actualSchedule = actualSchedule.extendWithTask(taskJ, actualGraph.getTask(9));

    String actualDotGraph = dotGraphIO.toDotString("test6-output", actualSchedule,
        actualGraph);

    Assertions.assertEquals(expectedDotGraph, actualDotGraph);
  }
}
