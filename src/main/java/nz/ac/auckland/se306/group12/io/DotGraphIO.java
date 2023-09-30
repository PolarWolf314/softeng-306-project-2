package nz.ac.auckland.se306.group12.io;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

// TODO: Might be nice to create a model for the scheduled tasks?
public class DotGraphIO {

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Reads a dot graph from the given file and parses it into a {@link Graph} object. The graph is
   * expected to be in a valid format, with each node and edge having a <code>Weight</code>
   * attribute
   *
   * @param inputDotGraph The file to read the dot graph from
   * @return A {@link Graph} object representing the parsed dot graph
   * @throws IOException If an error occurs while reading the file
   */
  public Graph readDotGraph(final File inputDotGraph) throws IOException {
    GraphParser parser = new GraphParser(new FileInputStream(inputDotGraph));
    Graph graph = new Graph();

    graph.setName(parser.getGraphId());

    for (GraphNode graphNode : parser.getNodes().values()) {
      int weight = Integer.parseInt(graphNode.getAttributes().get("Weight").toString());
      graph.addNode(graphNode.getId(), weight);
    }

    for (GraphEdge graphEdge : parser.getEdges().values()) {
      int weight = Integer.parseInt(graphEdge.getAttributes().get("Weight").toString());
      graph.addEdge(graphEdge.getNode1().getId(), graphEdge.getNode2().getId(), weight);
    }

    return graph;
  }

  /**
   * Serialises the given scheduled tasks into a dot graph and either writes it to the given output
   * file specified in the {@link CommandLineArguments} or to stdout if the <code>-s</code> flag was
   * set. The scheduled tasks is a list of processor, each processor having a list of
   * {@link Node ScheduledTasks} on it.
   *
   * @param arguments The parsed commandline arguments
   * @param nodes     The scheduled tasks to serialise
   * @throws IOException If an error occurs while writing to the file
   */
  public void writeDotGraph(
      final CommandLineArguments arguments,
      final List<List<Node>> nodes
  ) throws IOException {
    final String digraphName = FileIO.withoutDotExtension(arguments.outputDotGraph().getName());

    // We surround the name with "..." to allow for characters such as '-' in the name
    String output = this.toDotString(digraphName, nodes);

    if (arguments.writeToStdOut()) {
      System.out.println(output);
    } else {
      FileIO.writeToFile(output, arguments.outputDotGraph());
    }
  }

  /**
   * Takes a Topological order (in the form of a list of nodes) and outputs the corresponding dot
   * file in the console. This function is used for the testing of the TopologicalSorter.
   *
   * @param graphName String name of the graph
   * @param graph     list of nodes to create a graph with
   */
  public void writeOrderToConsole(String graphName, List<Node> graph) {
    StringBuilder builder = new StringBuilder();
    builder.append("digraph ")
        .append(graphName)
        .append(" {")
        .append(NEW_LINE);

    for (Node node : graph) {
      builder.append(node.getLabel())
          .append(" [Weight=")
          .append(node.getWeight())
          .append("]")
          .append(NEW_LINE);

      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        builder.append(outgoingEdge.getSource().getLabel())
            .append(" -> ")
            .append(outgoingEdge.getDestination().getLabel())
            .append(" [Weight=")
            .append(outgoingEdge.getWeight())
            .append("]")
            .append(NEW_LINE);
      }
    }

    builder.append("}");

    System.out.println(builder);
  }

  /**
   * Same as writeDotGraph, used for testing.
   *
   * @param nodes The scheduled tasks to serialise
   */
  public void writeOutputDotGraphToConsole(String digraphName, final List<List<Node>> nodes) {
    System.out.println(toDotString(digraphName, nodes));
  }


  /**
   * Generates a dot graph string out of a schedule.
   *
   * @param digraphName name of the digraph
   * @param schedule    the schedule of the digraph
   * @return the digraph in string form, in a .dot format
   */
  public String toDotString(String digraphName, List<List<Node>> schedule) {
    final StringBuilder builder = new StringBuilder();
    builder.append("digraph ")
        .append(digraphName)
        .append(" {")
        .append(NEW_LINE);

    for (int processorIndex = 0; processorIndex < schedule.size(); processorIndex++) {
      final List<Node> processorTasks = schedule.get(processorIndex);
      for (final Node node : processorTasks) {
        builder.append(node.getLabel())
            .append(" [Weight=")
            .append(node.getWeight())
            .append(",Start=")
            .append(node.getStartTime())
            .append(",Processor=")
            .append(processorIndex + 1) // Processors are 1-indexed
            .append("]")
            .append(NEW_LINE);

        for (final Edge outgoingEdge : node.getOutgoingEdges()) {
          builder.append(outgoingEdge.getSource().getLabel())
              .append(" -> ")
              .append(outgoingEdge.getDestination().getLabel())
              .append(" [Weight=")
              .append(outgoingEdge.getWeight())
              .append("]")
              .append(NEW_LINE);
        }
      }
    }

    builder.append("}");
    return builder.toString();
  }
}
