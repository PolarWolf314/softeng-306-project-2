package nz.ac.auckland.se306.group12;

import java.io.IOException;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.scheduler.DfsScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import nz.ac.auckland.se306.group12.visualizer.TuiVisualizer;
import nz.ac.auckland.se306.group12.visualizer.Visualizer;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());

      Scheduler scheduler = new DfsScheduler();
      Schedule schedule = scheduler.schedule(graph, arguments.processorCount());

      if (arguments.visualiseSearch()) {
        Visualizer visualizer = new TuiVisualizer(graph);
        visualizer.visualize(schedule);
      }

      System.out.println(schedule.getLatestEndTime());
      dotGraphIO.writeDotGraph(arguments, schedule, graph);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
