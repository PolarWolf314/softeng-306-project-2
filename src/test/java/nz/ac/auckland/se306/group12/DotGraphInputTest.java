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
   * Add and edge to the graph and also update the nodes incoming and outgoing edges
   *
   * @param graph The graph to add the edge to
   * @param source The source node label
   * @param destination The destination node label
   * @param weight The weight of the edge
   */
  private void addEdgeToGraph(Graph graph, String source, String destination, int weight) {
    Node sourceNode = graph.getNodes().get(source);
    Node destinationNode = graph.getNodes().get(destination);

    Edge edge = new Edge(sourceNode, destinationNode, weight);

    destinationNode.getIncomingEdges().add(edge);
    sourceNode.getOutgoingEdges().add(edge);
    graph.getEdges().add(edge);
  }

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

    addEdgeToGraph(expectedGraph, "A", "B", 1);
    addEdgeToGraph(expectedGraph, "B", "C", 3);
    addEdgeToGraph(expectedGraph, "C", "D", 2);
    addEdgeToGraph(expectedGraph, "D", "E", 1);

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

    addEdgeToGraph(expectedGraph, "A", "B", 1);
    addEdgeToGraph(expectedGraph, "B", "C", 2);
    addEdgeToGraph(expectedGraph, "C", "D", 3);
    addEdgeToGraph(expectedGraph, "D", "E", 1);
    addEdgeToGraph(expectedGraph, "E", "F", 2);

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

    addEdgeToGraph(expectedGraph, "A", "C", 1);
    addEdgeToGraph(expectedGraph, "B", "C", 2);
    addEdgeToGraph(expectedGraph, "B", "D", 3);
    addEdgeToGraph(expectedGraph, "D", "F", 1);
    addEdgeToGraph(expectedGraph, "C", "F", 2);
    addEdgeToGraph(expectedGraph, "C", "E", 3);
    addEdgeToGraph(expectedGraph, "E", "G", 1);
    addEdgeToGraph(expectedGraph, "G", "J", 2);
    addEdgeToGraph(expectedGraph, "F", "H", 1);
    addEdgeToGraph(expectedGraph, "F", "I", 2);

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

    addEdgeToGraph(expectedGraph, "A", "B", 1);
    addEdgeToGraph(expectedGraph, "A", "C", 3);
    addEdgeToGraph(expectedGraph, "B", "D", 2);
    addEdgeToGraph(expectedGraph, "C", "D", 1);
    addEdgeToGraph(expectedGraph, "D", "E", 5);

    runTestWithFile(expectedGraph, "./graphs/test_multiple_parents.dot");
  }

}
