package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BottomLevelTest {

  String setBottoms(Graph graph) {
    graph.setBottomLevels();
    StringBuilder output = new StringBuilder();
    output.append(graph.getName())
        .append("\n");
    for (Task task : graph.getTasks()) {
      output.append(task.getLabel())
          .append(": ")
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
        A: 12
        B: 10
        C: 7
        D: 6
        E: 2
        """;
    Assertions.assertEquals(expected, setBottoms(graph));
  }

  /**
   * Test for disconnected graph
   */
  @Test
  void testDisjointGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_disjoint_graphs.dot");
    String expected = """
        DisjointGraphs
        A: 9
        B: 7
        C: 5
        D: 4
        E: 5
        F: 3
        """;
    Assertions.assertEquals(expected, setBottoms(graph));
  }

  /**
   * Test with graph that has multiple sources
   */
  @Test
  void testAnnoyingGraph() {
    Graph graph = TestUtil.loadGraph("./graphs/test_annoying.dot");
    String expected = """
        Test_Annoying
        A: 17
        B: 18
        C: 15
        D: 9
        E: 14
        F: 5
        G: 12
        H: 2
        I: 2
        J: 7
        """;
    Assertions.assertEquals(expected, setBottoms(graph));
  }

  /**
   * Test with graph with a large path
   */
  @Test
  void testMultiplePaths() {
    Graph graph = TestUtil.loadGraph("./graphs/test_unintuitive_shortest_path.dot");
    String expected = """
        WeirdPath
        A: 12
        B: 8
        C: 5
        D: 4
        E: 10
        """;
    Assertions.assertEquals(expected, setBottoms(graph));
  }

  /**
   * Test with pain and suffering
   */
  @Test
  void testNodes11() {
    Graph graph = TestUtil.loadGraph("./graphs/Nodes_11_OutTree.dot");
    String expected = """
        "OutTree-Balanced-MaxBf-3_Nodes_11_CCR_0.1_WeightType_Random"
        0: 220
        1: 170
        10: 20
        2: 170
        3: 120
        4: 40
        5: 20
        6: 100
        7: 80
        8: 50
        9: 20
        """;
    Assertions.assertEquals(expected, setBottoms(graph));
  }
}
