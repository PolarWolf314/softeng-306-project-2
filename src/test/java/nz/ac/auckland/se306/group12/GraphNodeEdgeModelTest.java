package nz.ac.auckland.se306.group12;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

/*
 * Tests functionality of the model classes used for the graph classes
 */
class GraphNodeEdgeModelTest {

  /**
   * Tests node equality checks for the same name and weight
   */
  @Test
  public void testNodeEquality() {
    Node node1 = new Node("a", 1);
    Node node2 = new Node("a", 1);
    Node node3 = new Node("a", 2);

    Assertions.assertEquals(node1, node2);
    Assertions.assertNotEquals(node1, node3);
    Assertions.assertNotEquals(node2, node3);
  }

  /**
   * Test edge equality checks for the same source, destination and weight
   */
  @Test
  public void testEdgeEquality() {
    Node node1 = new Node("a", 1);
    Node node2 = new Node("b", 1);
    Node node3 = new Node("c", 1);
    Edge edge1 = new Edge(node1, node2, 1);
    Edge edge2 = new Edge(node1, node2, 1);
    Edge edge3 = new Edge(node1, node2, 2);
    Edge edge4 = new Edge(node1, node3, 1);

    Assertions.assertEquals(edge1, edge2);
    Assertions.assertNotEquals(edge1, edge3);
    Assertions.assertNotEquals(edge2, edge3);
    Assertions.assertNotEquals(edge1, edge4);
  }

  /**
   * Test empty graphs are equal
   */
  @Test
  public void testEmptyGraph() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    Assertions.assertEquals(graph1, graph2);
  }

  /**
   * Test graph with same nodes is equal
   */
  @Test
  public void testEqualGraphWithNode() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addNode("a", 1);
    graph2.addNode("a", 1);
    Assertions.assertEquals(graph1, graph2);
  }

  /**
   * Test graph with different node is not equal
   */
  @Test
  public void testUnequalGraphWithNode() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addNode("a", 1);
    graph2.addNode("b", 1);

    Assertions.assertNotEquals(graph1, graph2);
  }

  /**
   * Test graph with same node and edge is equal
   */
  @Test
  public void testEqualGraphWithNodesAndEdge() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addNode("a", 1);
    graph2.addNode("a", 1);

    graph1.addNode("b", 2);
    graph2.addNode("b", 2);

    graph1.addEdge("a", "b", 1);
    graph2.addEdge("a", "b", 1);

    Assertions.assertEquals(graph1, graph2);
  }

  /**
   * Test graph with same node and edge is not equal
   */
  @Test
  public void testUnequalGraphWithNodesAndEdge() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addNode("a", 1);
    graph2.addNode("a", 1);

    graph1.addNode("b", 2);
    graph2.addNode("b", 2);

    graph1.addNode("c", 2);
    graph2.addNode("c", 2);

    graph1.addEdge("a", "b", 1);
    graph2.addEdge("a", "c", 1);

    Assertions.assertNotEquals(graph1, graph2);
  }

}
