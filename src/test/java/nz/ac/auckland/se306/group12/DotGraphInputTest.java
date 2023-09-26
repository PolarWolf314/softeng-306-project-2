package nz.ac.auckland.se306.group12;

import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

class DotGraphInputTest {

  /**
   * Check that the two graphs are equal including the edges and nodes and the incoming and outgoing
   * edges of all the nodes
   * 
   * @param expectedGraph The expected graph
   * @param parsedGraph The parsed graph
   */
  private void checkGraphEquality(Graph expectedGraph, Graph parsedGraph) {
    Assertions.assertEquals(expectedGraph, parsedGraph);

    for (Node node : expectedGraph.getNodes().values()) {
      Node parsedNode = parsedGraph.getNodes().get(node.getLabel());
      Assertions.assertEquals(node.getIncomingEdges(), parsedNode.getIncomingEdges());
      Assertions.assertEquals(node.getOutgoingEdges(), parsedNode.getOutgoingEdges());
    }
  }

  private void runTestWithFile(Graph expectedGraph, String pathFromProjectRoot) {
    DotGraphIO dotGraphIO = new DotGraphIO();
    try {
      Graph graph = dotGraphIO.readDotGraph(new File(pathFromProjectRoot));
      checkGraphEquality(expectedGraph, graph);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Test that test1.dot is parsed correctly
   */
  @Test
  public void test1Test() {
    Graph expectedGraph = new Graph();

    expectedGraph.addNode("A", 2);
    expectedGraph.addNode("B", 3);
    expectedGraph.addNode("C", 1);
    expectedGraph.addNode("D", 4);
    expectedGraph.addNode("E", 2);

    expectedGraph.addEdge("A", "B", 1);
    expectedGraph.addEdge("B", "C", 3);
    expectedGraph.addEdge("C", "D", 2);
    expectedGraph.addEdge("D", "E", 1);

    runTestWithFile(expectedGraph, "./graphs/test1.dot");
  }

  /**
   * Test that test2.dot is parsed correctly
   */
  @Test
  public void test2FileTest() {
    Graph expectedGraph = new Graph();

    expectedGraph.addNode("A", 2);
    expectedGraph.addNode("B", 3);
    expectedGraph.addNode("C", 1);
    expectedGraph.addNode("D", 4);
    expectedGraph.addNode("E", 2);
    expectedGraph.addNode("F", 3);

    expectedGraph.addEdge("A", "B", 1);
    expectedGraph.addEdge("B", "C", 2);
    expectedGraph.addEdge("C", "D", 3);
    expectedGraph.addEdge("D", "E", 1);
    expectedGraph.addEdge("E", "F", 2);

    runTestWithFile(expectedGraph, "./graphs/test2.dot");
  }

  /**
   * Test that test_annoying.dot is parsed correctly
   */
  @Test
  public void testAnnoyingTest() {
    Graph expectedGraph = new Graph();

    expectedGraph.addNode("A", 2);
    expectedGraph.addNode("B", 3);
    expectedGraph.addNode("C", 1);
    expectedGraph.addNode("D", 4);
    expectedGraph.addNode("E", 2);
    expectedGraph.addNode("F", 3);
    expectedGraph.addNode("G", 5);
    expectedGraph.addNode("H", 2);
    expectedGraph.addNode("I", 2);
    expectedGraph.addNode("J", 7);

    expectedGraph.addEdge("A", "C", 1);
    expectedGraph.addEdge("B", "C", 2);
    expectedGraph.addEdge("B", "D", 3);
    expectedGraph.addEdge("D", "F", 1);
    expectedGraph.addEdge("C", "F", 2);
    expectedGraph.addEdge("C", "E", 3);
    expectedGraph.addEdge("E", "G", 1);
    expectedGraph.addEdge("G", "J", 2);
    expectedGraph.addEdge("F", "H", 1);
    expectedGraph.addEdge("F", "I", 2);

    runTestWithFile(expectedGraph, "./graphs/test_annoying.dot");
  }

  /**
   * Test that test_multiple_parents.dot is parsed correctly
   */
  @Test
  public void testMultipleParentsTest() {
    Graph expectedGraph = new Graph();

    expectedGraph.addNode("A", 2);
    expectedGraph.addNode("B", 3);
    expectedGraph.addNode("C", 1);
    expectedGraph.addNode("D", 4);
    expectedGraph.addNode("E", 2);

    expectedGraph.addEdge("A", "B", 1);
    expectedGraph.addEdge("A", "C", 3);
    expectedGraph.addEdge("B", "D", 2);
    expectedGraph.addEdge("C", "D", 1);
    expectedGraph.addEdge("D", "E", 5);

    runTestWithFile(expectedGraph, "./graphs/test_multiple_parents.dot");
  }

}
