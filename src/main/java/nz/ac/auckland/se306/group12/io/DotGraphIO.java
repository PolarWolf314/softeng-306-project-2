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

// TODO: How to handle errors?
public class DotGraphIO {

  private static final String NEW_LINE = System.getProperty("line.separator");

  public Graph readDotGraph(final File inputDotGraph) {
    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(inputDotGraph);
    } catch (IOException e) {
      // TODO: How to handle this
      e.printStackTrace();
      return null;
    }

    final GraphParser parser = new GraphParser(inputStream);
    Map<String, Node> nodeMap = new HashMap<>();
    Set<Edge> edges = new HashSet<>();

    for (GraphNode graphNode : parser.getNodes().values()) {
      long weight = Long.parseLong(graphNode.getAttributes().get("Weight").toString());
      Node node = new Node(graphNode.getId(), weight);
      nodeMap.put(node.getLabel(), node);
    }

    for (GraphEdge graphEdge : parser.getEdges().values()) {
      long weight = Long.parseLong(graphEdge.getAttributes().get("Weight").toString());
      Node source = nodeMap.get(graphEdge.getNode1().getId());
      Node destination = nodeMap.get(graphEdge.getNode2().getId());

      Edge edge = new Edge(source, destination, weight);
      source.getChildren().add(edge);
      destination.getParents().add(edge);
      edges.add(edge);
    }

    Set<Node> nodes = new HashSet<>(nodeMap.values());
    return new Graph(nodes, edges);
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
