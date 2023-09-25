package nz.ac.auckland.se306.group12.parser;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

// TODO: Use graph model classes when created
public class DotGraphIO {

  public Object readDotGraph(final File inputDotGraph) {
    System.out.println(inputDotGraph.getPath());
    try {
      final GraphParser parser = new GraphParser(new FileInputStream(inputDotGraph));

      final Map<String, GraphNode> nodes = parser.getNodes();
      final Map<String, GraphEdge> edges = parser.getEdges();

      for (final GraphNode node : nodes.values()) {
        System.out.println(node);
      }

      for (final GraphEdge edge : edges.values()) {
        System.out.println(edge);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void writeDotGraph(final File outputDotGraph, final Object dotGraph) {

  }
}
