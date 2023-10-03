package nz.ac.auckland.se306.group12;

import java.io.IOException;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());

      // TODO: Fix to use the correct scheduler
//      Scheduler scheduler = new BasicScheduler();
//
//      List<Processor> schedule = scheduler.schedule(graph, arguments.processorCount());
//      List<List<Task>> scheduledTasks = schedule.stream()
//          .map(Processor::getScheduledTasks)
//          .toList();
//
//      dotGraphIO.writeDotGraph(arguments, scheduledTasks);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
