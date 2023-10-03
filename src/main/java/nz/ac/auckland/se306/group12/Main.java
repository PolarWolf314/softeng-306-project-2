package nz.ac.auckland.se306.group12;

import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.models.Task;
import nz.ac.auckland.se306.group12.scheduler.BasicScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());

      Scheduler scheduler = new BasicScheduler();

      List<Processor> schedule = scheduler.schedule(graph, arguments.processorCount());
      List<List<Task>> scheduledTasks = schedule.stream()
          .map(Processor::getScheduledTasks)
          .toList();

      dotGraphIO.writeDotGraph(arguments, scheduledTasks);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
