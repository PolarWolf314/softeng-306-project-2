package nz.ac.auckland.se306.group12.io;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

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
    Map<String, Node> nodes = new HashMap<>();
    Set<Edge> edges = new HashSet<>();

    for (GraphNode graphNode : parser.getNodes().values()) {
      long weight = Long.parseLong(graphNode.getAttributes().get("Weight").toString());
      Node node = new Node(graphNode.getId(), weight);
      nodes.put(node.getLabel(), node);
    }

    for (GraphEdge graphEdge : parser.getEdges().values()) {
      long weight = Long.parseLong(graphEdge.getAttributes().get("Weight").toString());
      Node source = nodes.get(graphEdge.getNode1().getId());
      Node destination = nodes.get(graphEdge.getNode2().getId());

      Edge edge = new Edge(source, destination, weight);
      source.getOutgoingEdges().add(edge);
      destination.getIncomingEdges().add(edge);
      edges.add(edge);
    }

    return new Graph(nodes, edges);
  }

  /**
   * Serialises the given scheduled tasks into a dot graph and either writes it to the given output
   * file specified in the {@link CommandLineArguments} or to stdout if the <code>-s</code> flag was
   * set. The scheduled tasks is a list of processor, each processor having a list of
   * {@link ScheduledTask ScheduledTasks} on it.
   *
   * @param arguments      The parsed commandline arguments
   * @param scheduledTasks The scheduled tasks to serialise
   * @throws IOException If an error occurs while writing to the file
   */
  public void writeDotGraph(
      final CommandLineArguments arguments,
      final List<List<ScheduledTask>> scheduledTasks
  ) throws IOException {
    final StringBuilder builder = new StringBuilder();
    final String digraphName = FileIO.withoutDotExtension(arguments.outputDotGraph().getName());
    builder.append("digraph ")
        .append(digraphName)
        .append(" {")
        .append(NEW_LINE);

    for (int processorIndex = 0; processorIndex < scheduledTasks.size(); processorIndex++) {
      final List<ScheduledTask> processorTasks = scheduledTasks.get(processorIndex);
      for (final ScheduledTask scheduledTask : processorTasks) {
        builder.append(scheduledTask.getNode().getLabel())
            .append(" [Weight=")
            .append(scheduledTask.getNode().getWeight())
            .append(",Start=")
            .append(scheduledTask.getStartTime())
            .append(",Processor=")
            .append(processorIndex + 1) // Processors are 1-indexed
            .append("]")
            .append(NEW_LINE);

        for (final Edge outgoingEdge : scheduledTask.getNode().getOutgoingEdges()) {
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

    if (arguments.writeToStdOut()) {
      System.out.println(builder);
    } else {
      FileIO.writeToFile(builder.toString(), arguments.outputDotGraph());
    }
  }
}
