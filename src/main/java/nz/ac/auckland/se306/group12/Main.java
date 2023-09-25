package nz.ac.auckland.se306.group12;

import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());
      System.out.println(graph);

      List<List<ScheduledTask>> scheduledTasks = List.of(
          List.of(new ScheduledTask(new Node("A", 1), 0, 0)),
          List.of(new ScheduledTask(new Node("B", 1), 0, 0))
      );

      dotGraphIO.writeDotGraph(arguments, scheduledTasks);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
