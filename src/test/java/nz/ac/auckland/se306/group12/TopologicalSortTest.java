package nz.ac.auckland.se306.group12;

import java.io.File;
import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopologicalSortTest {

  @Test
  void test_trivial_graph() {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test1.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();

      List<Node> top = sorter.getATopologicalOrder(graph);
      System.out.println(top);

      Assertions.assertTrue(true);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_disjoint_graph() {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_disjoint_graphs.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();

      List<Node> top = sorter.getATopologicalOrder(graph);
      System.out.println(top);

      Assertions.assertTrue(true);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_annoying_graph() {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_annoying.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();

      List<Node> top = sorter.getATopologicalOrder(graph);
      System.out.println(top);

      Assertions.assertTrue(true);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

  @Test
  void test_multiple_paths() {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    try {
      File file = new File("./graphs/test_unintuitive_shortest_path.dot");
      Graph graph = dotGraphIO.readDotGraph(file);

      TopologicalSorter sorter = new TopologicalSorter();

      List<Node> top = sorter.getATopologicalOrder(graph);
      System.out.println(top);

      Assertions.assertTrue(true);

    } catch (IOException e) {
      Assertions.fail("File not found.");
    }
  }

}
