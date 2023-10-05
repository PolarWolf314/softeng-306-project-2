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
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.Task;

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
  public Graph readDotGraph(File inputDotGraph) throws IOException {
    GraphParser parser = new GraphParser(new FileInputStream(inputDotGraph));
    Graph graph = new Graph(parser.getGraphId());

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
   * set.
   *
   * @param arguments The parsed commandline arguments
   * @param schedule  The scheduled tasks to serialise
   * @param graph     The precedence graph from which `schedule` was generated
   * @throws IOException If an error occurs while writing to the file
   */
  public void writeDotGraph(CommandLineArguments arguments, Schedule schedule, Graph graph)
      throws IOException {
    final String digraphName = FileIO.withoutDotExtension(arguments.outputDotGraph().getName());

    String output = this.toDotString(digraphName, schedule, graph);

    if (arguments.writeToStdOut()) {
      System.out.println(output);
    } else {
      FileIO.writeToFile(output, arguments.outputDotGraph());
    }
  }

  /**
   * Same as writeDotGraph, used for testing.
   *
   * @param graphName The name of the graph
   * @param schedule  The scheduled tasks to serialise
   * @param graph     The precedence graph from which `schedule` was generated
   */
  public void writeOutputDotGraphToConsole(String graphName, Schedule schedule, Graph graph) {
    System.out.println(this.toDotString(graphName, schedule, graph));
  }

  /**
   * Generates a dot graph string out of a schedule.
   *
   * @param digraphName name of the digraph
   * @param schedule    the schedule of the digraph
   * @return the digraph in string form, in a .dot format
   */
  public String toDotString(String digraphName, Schedule schedule, Graph graph) {
    StringBuilder builder = new StringBuilder();

    // We surround the name with "..." to allow for characters such as '-' in the name

    builder.append("digraph \"")
        .append(digraphName)
        .append("\" {")
        .append(NEW_LINE);

    List<Task> tasks = graph.getTasks();
    ScheduledTask[] scheduledTasks = schedule.getScheduledTasks();
    for (int i = 0; i < scheduledTasks.length; i++) {
      Task task = tasks.get(i);
      ScheduledTask scheduledTask = scheduledTasks[i];
      builder.append(task.getLabel())
          .append(" [Weight=")
          .append(task.getWeight())
          .append(",Start=")
          .append(scheduledTask.getStartTime())
          .append(",Processor=")
          .append(scheduledTask.getProcessorIndex() + 1) // Processors are 1-indexed
          .append("];")
          .append(NEW_LINE);

      for (Edge outgoingEdge : tasks.get(i).getOutgoingEdges()) {
        builder.append(outgoingEdge.getSource().getLabel())
            .append(" -> ")
            .append(outgoingEdge.getDestination().getLabel())
            .append(" [Weight=")
            .append(outgoingEdge.getWeight())
            .append("];")
            .append(NEW_LINE);
      }
    }

    builder.append("}");
    return builder.toString();
  }

}
