package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * Tests functionality of the model classes used for the graph classes
 */
class GraphTaskEdgeModelTest {

  /**
   * Tests node equality checks for the same name and weight
   */
  @Test
  public void testNodeEquality() {
    Task task1 = new Task("a", 1, 0);
    Task task2 = new Task("a", 1, 1);
    Task task3 = new Task("a", 2, 2);

    Assertions.assertEquals(task1, task2);
    Assertions.assertNotEquals(task1, task3);
    Assertions.assertNotEquals(task2, task3);
  }

  /**
   * Test edge equality checks for the same source, destination and weight
   */
  @Test
  public void testEdgeEquality() {
    Task task1 = new Task("a", 1, 0);
    Task task2 = new Task("b", 1, 1);
    Task task3 = new Task("c", 1, 2);
    Edge edge1 = new Edge(task1, task2, 1);
    Edge edge2 = new Edge(task1, task2, 1);
    Edge edge3 = new Edge(task1, task2, 2);
    Edge edge4 = new Edge(task1, task3, 1);

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

    graph1.addTask("a", 1);
    graph2.addTask("a", 1);
    Assertions.assertEquals(graph1, graph2);
  }

  /**
   * Test graph with different node is not equal
   */
  @Test
  public void testUnequalGraphWithNode() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addTask("a", 1);
    graph2.addTask("b", 1);

    Assertions.assertNotEquals(graph1, graph2);
  }

  /**
   * Test graph with same node and edge is equal
   */
  @Test
  public void testEqualGraphWithNodesAndEdge() {
    Graph graph1 = new Graph();
    Graph graph2 = new Graph();

    graph1.addTask("a", 1);
    graph2.addTask("a", 1);

    graph1.addTask("b", 2);
    graph2.addTask("b", 2);

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

    graph1.addTask("a", 1);
    graph2.addTask("a", 1);

    graph1.addTask("b", 2);
    graph2.addTask("b", 2);

    graph1.addTask("c", 2);
    graph2.addTask("c", 2);

    graph1.addEdge("a", "b", 1);
    graph2.addEdge("a", "c", 1);

    Assertions.assertNotEquals(graph1, graph2);
  }

}
