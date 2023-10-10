package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BottomLevelTest {

  /**
   * Creates a string representation of the top and bottom levels of tasks in the given graph.
   * <p>
   * This method calculates the top and bottom levels of tasks in the input graph using the
   * {@link Graph#setTopAndBottomLevels()} method of the graph. It then constructs a string that
   * includes the name of the graph and these two levels formatted as task label followed by their
   * top and bottom level.
   *
   * @param graph The graph for which the bottom levels of tasks are to be calculated and
   *              displayed.
   * @return A formatted string containing the name of the graph and the bottom levels of its tasks.
   */
  String createTopAndBottomLevelsString(Graph graph) {
    graph.setTopAndBottomLevels();
    StringBuilder output = new StringBuilder();
    output.append(graph.getName()).append("\n");
    for (Task task : graph.getTasks()) {
      output.append(task.getLabel())
          .append(": ")
          .append(task.getTopLevel())
          .append(", ")
          .append(task.getBottomLevel())
          .append("\n");
    }
    return output.toString();
  }

  /**
   * Test for trivial graph
   */
  @Test
  void testTrivialGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test1.dot");
    String expected = """
        TestGraph1_Variation1
        A: 0, 12
        B: 2, 10
        C: 5, 7
        D: 6, 6
        E: 10, 2
        """;
    Assertions.assertEquals(expected, createTopAndBottomLevelsString(graph));
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    String expected = """
        DisjointGraphs
        A: 0, 9
        B: 2, 7
        C: 2, 5
        D: 5, 4
        E: 0, 5
        F: 2, 3
        """;
    Assertions.assertEquals(expected, createTopAndBottomLevelsString(graph));
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    String expected = """
        Test_Annoying
        A: 0, 17
        B: 0, 18
        C: 3, 15
        D: 3, 9
        E: 4, 14
        F: 7, 5
        G: 6, 12
        H: 10, 2
        I: 10, 2
        J: 11, 7
        """;
    Assertions.assertEquals(expected, createTopAndBottomLevelsString(graph));
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    String expected = """
        WeirdPath
        A: 0, 12
        B: 2, 8
        C: 5, 5
        D: 6, 4
        E: 2, 10
        """;
    Assertions.assertEquals(expected, createTopAndBottomLevelsString(graph));
  }

  /**
   * Test with pain and suffering
   */
  @Test
  void testNodes11() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    String expected = """
        "OutTree-Balanced-MaxBf-3_Nodes_11_CCR_0.1_WeightType_Random"
        0: 0, 220
        1: 50, 170
        10: 150, 20
        2: 50, 170
        3: 50, 120
        4: 120, 40
        5: 120, 20
        6: 120, 100
        7: 140, 80
        8: 140, 50
        9: 140, 20
        """;
    Assertions.assertEquals(expected, createTopAndBottomLevelsString(graph));
  }
}
