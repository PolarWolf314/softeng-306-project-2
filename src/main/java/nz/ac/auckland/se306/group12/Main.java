package nz.ac.auckland.se306.group12;

import java.io.IOException;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.factories.SchedulerFactory;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import nz.ac.auckland.se306.group12.visualizer.TerminalVisualizer;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());
      Scheduler scheduler = new SchedulerFactory().getScheduler(arguments.algorithm());

      if (arguments.visualiseSearch()) {
        new TerminalVisualizer(graph, scheduler, arguments.parallelisationProcessorCount());
      }

      Schedule schedule = scheduler.schedule(graph, arguments.processorCount());

      dotGraphIO.writeDotGraph(arguments, schedule, graph);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
