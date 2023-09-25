package nz.ac.auckland.se306.group12;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Edge;
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

  private void runTestWithFile(Graph expectedGraph, String pathFromRoot) {
    DotGraphIO dotGraphIO = new DotGraphIO();
    try {
      Graph graph = dotGraphIO.readDotGraph(new File(pathFromRoot));
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
    HashMap<String, Node> nodes = new HashMap<>();
    HashSet<Edge> edges = new HashSet<>();
    Graph expectedGraph = new Graph(nodes, edges);

    nodes.put("A", new Node("A", 2));
    nodes.put("B", new Node("B", 3));
    nodes.put("C", new Node("C", 1));
    nodes.put("D", new Node("D", 4));
    nodes.put("E", new Node("E", 2));

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
    HashMap<String, Node> nodes = new HashMap<>();
    HashSet<Edge> edges = new HashSet<>();
    Graph expectedGraph = new Graph(nodes, edges);

    nodes.put("A", new Node("A", 2));
    nodes.put("B", new Node("B", 3));
    nodes.put("C", new Node("C", 1));
    nodes.put("D", new Node("D", 4));
    nodes.put("E", new Node("E", 2));
    nodes.put("F", new Node("F", 3));

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
    HashMap<String, Node> nodes = new HashMap<>();
    HashSet<Edge> edges = new HashSet<>();
    Graph expectedGraph = new Graph(nodes, edges);

    nodes.put("A", new Node("A", 2));
    nodes.put("B", new Node("B", 3));
    nodes.put("C", new Node("C", 1));
    nodes.put("D", new Node("D", 4));
    nodes.put("E", new Node("E", 2));
    nodes.put("F", new Node("F", 3));
    nodes.put("G", new Node("G", 5));
    nodes.put("H", new Node("H", 2));
    nodes.put("I", new Node("I", 2));
    nodes.put("J", new Node("J", 7));

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
    HashMap<String, Node> nodes = new HashMap<>();
    HashSet<Edge> edges = new HashSet<>();
    Graph expectedGraph = new Graph(nodes, edges);

    nodes.put("A", new Node("A", 2));
    nodes.put("B", new Node("B", 3));
    nodes.put("C", new Node("C", 1));
    nodes.put("D", new Node("D", 4));
    nodes.put("E", new Node("E", 2));

    expectedGraph.addEdge("A", "B", 1);
    expectedGraph.addEdge("A", "C", 3);
    expectedGraph.addEdge("B", "D", 2);
    expectedGraph.addEdge("C", "D", 1);
    expectedGraph.addEdge("D", "E", 5);

    runTestWithFile(expectedGraph, "./graphs/test_multiple_parents.dot");
  }

}
