package nz.ac.auckland.se306.group12;

import java.util.List;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.parser.DotGraphIO;

public class Main {

  public static void main(final String[] args) {
    final CommandLineParser parser = new CommandLineParser();
    final DotGraphIO dotGraphIO = new DotGraphIO();

    final CommandLineArguments arguments = parser.parse(args);
    dotGraphIO.readDotGraph(arguments.inputDotGraph());

    final List<List<ScheduledTask>> scheduledTasks = List.of(
        List.of(new ScheduledTask(new Node("A", 1), 0, 0)),
        List.of(new ScheduledTask(new Node("B", 1), 0, 0))
    );

    dotGraphIO.writeDotGraph(arguments, scheduledTasks);
  }

}
