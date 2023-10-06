package nz.ac.auckland.se306.group12;

import java.io.File;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;
import org.junit.jupiter.api.Assertions;

public class DotGraphOutputTest {

  private final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Check that the two graphs are equal including the edges and nodes and the incoming and outgoing
   * edges of all the nodes
   *
   * @param expectedGraph The expected graph
   * @param parsedGraph   The parsed graph
   */
  private void checkGraphEquality(Graph expectedGraph, Graph parsedGraph) {
    Assertions.assertEquals(expectedGraph, parsedGraph);

    for (Task task : expectedGraph.getTasks()) {
      Task parsedTask = parsedGraph.getTasks().get(task.getIndex());
      Assertions.assertEquals(task.getIncomingEdges(), parsedTask.getIncomingEdges());
      Assertions.assertEquals(task.getOutgoingEdges(), parsedTask.getOutgoingEdges());
    }
  }

  private void runTestWithFile(Graph expectedGraph, String pathFromProjectRoot) {
    try {
      Graph graph = this.dotGraphIO.readDotGraph(new File(pathFromProjectRoot));
      this.checkGraphEquality(expectedGraph, graph);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
