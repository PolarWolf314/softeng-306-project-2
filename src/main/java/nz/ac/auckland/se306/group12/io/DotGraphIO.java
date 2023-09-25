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

// TODO: Use graph model classes when created
public class DotGraphIO {

  private static final String NEW_LINE = System.getProperty("line.separator");

  public Graph readDotGraph(final File inputDotGraph) {
    System.out.println(inputDotGraph.getPath());
    try {
      final GraphParser parser = new GraphParser(new FileInputStream(inputDotGraph));

      Map<String, Node> nodeSet = new HashMap<>();
      Set<Edge> edgeSet = new HashSet<>();

      final Map<String, GraphNode> nodes = parser.getNodes();
      final Map<String, GraphEdge> edges = parser.getEdges();

      for (final GraphNode node : nodes.values()) {
        final long weight = Long.parseLong(node.getAttributes().get("Weight").toString());
        Node currentNode = new Node(node.getId(), weight);
        nodeSet.put(node.getId(), currentNode);
      }

      for (final GraphEdge edge : edges.values()) {
        final long weight = Long.parseLong(edge.getAttributes().get("Weight").toString());
        Node node1 = nodeSet.get(edge.getNode1().getId());
        Node node2 = nodeSet.get(edge.getNode2().getId());
        Edge currentEdge = new Edge(node1, node2, weight);
        edgeSet.add(currentEdge);
      }

      Set<Node> resultNodes = new HashSet<>(nodeSet.values());
      return new Graph(resultNodes, edgeSet);
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void writeDotGraph(
      final CommandLineArguments arguments,
      final List<List<ScheduledTask>> scheduledTasks
  ) {
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
            .append(processorIndex + 1)
            .append("]")
            .append(NEW_LINE);

        for (final Edge outgoingEdge : scheduledTask.getNode().getChildren()) {
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
      return;
    }

    FileIO.writeToFile(builder.toString(), arguments.outputDotGraph());
  }
}
