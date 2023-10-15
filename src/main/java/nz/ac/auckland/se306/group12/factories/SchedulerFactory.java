package nz.ac.auckland.se306.group12.factories;

import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.scheduler.AStarScheduler;
import nz.ac.auckland.se306.group12.scheduler.DfsAOScheduler;
import nz.ac.auckland.se306.group12.scheduler.DfsScheduler;
import nz.ac.auckland.se306.group12.scheduler.ParallelDfsAOScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;

public class SchedulerFactory {

  /**
   * Returns a scheduler depending on if the user wants to visualise the search or not since we do
   * not properly support visualising A* or AO
   *
   * @param arguments The parsed commandline arguments
   * @return A scheduler depending on if the user wants to visualise the search or not
   */
  public Scheduler getScheduler(CommandLineArguments arguments) {
    // We only support visualization using DFS
    if (arguments.visualiseSearch()) {
      return new DfsScheduler(arguments.parallelisationProcessorCount());
    }

    switch (arguments.algorithm().toLowerCase()) {
      case "astar" -> {
        return new AStarScheduler();
      }
      case "dfs" -> {
        return new DfsScheduler(arguments.parallelisationProcessorCount());
      }
      case "ao" -> {
        return arguments.parallelisationProcessorCount() > 1
            ? new ParallelDfsAOScheduler(arguments.parallelisationProcessorCount())
            : new DfsAOScheduler();
      }
      default -> throw new IllegalArgumentException(
          "Invalid algorithm. " + arguments.algorithm() + " is not a valid algorithm.");
    }

  }

}
