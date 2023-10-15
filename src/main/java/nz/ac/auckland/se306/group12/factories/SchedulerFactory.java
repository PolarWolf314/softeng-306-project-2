package nz.ac.auckland.se306.group12.factories;

import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.scheduler.AStarScheduler;
import nz.ac.auckland.se306.group12.scheduler.DfsAOScheduler;
import nz.ac.auckland.se306.group12.scheduler.DfsScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;

public class SchedulerFactory {

  /**
   * Returns a scheduler for the given algorithm
   *
   * @param algorithm The command line arguments that this was run with
   * @return A scheduler for the given algorithm
   */
  public Scheduler getScheduler(String algorithm) {

    switch (algorithm.toLowerCase()) {
      case "astar" -> {
        return new AStarScheduler();
      }
      case "dfs" -> {
        return new DfsScheduler();
      }
      case "ao" -> {
        return new DfsAOScheduler();
      }
      default -> throw new IllegalArgumentException(
          "Invalid algorithm. " + algorithm + " is not a valid algorithm.");
    }
  }

  /**
   * Returns a scheduler depending on if the user wants to visualise the search or not since we do
   * not properly support visualising A* or AO
   *
   * @param arguments The parsed commandline arguments
   * @return A scheduler depending on if the user wants to visualise the search or not
   */
  public Scheduler getScheduler(CommandLineArguments arguments) {
    // We only support parallelization and visualization using DFS
    if (arguments.visualiseSearch() || arguments.parallelisationProcessorCount() > 1) {
      return new DfsScheduler(arguments.parallelisationProcessorCount());
    } else {
      return this.getScheduler(arguments.algorithm());
    }
  }
}
