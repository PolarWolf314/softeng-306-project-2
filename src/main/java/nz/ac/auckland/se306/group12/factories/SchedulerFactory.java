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
   * @param algorithm The algorithm to return a scheduler for
   * @return A scheduler for the given algorithm
   */
  public Scheduler getScheduler(String algorithm) {
    algorithm = algorithm.toLowerCase();

    switch (algorithm) {
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
    if (arguments.visualiseSearch()) {
      return new DfsScheduler();
    } else {
      return this.getScheduler(arguments.algorithm());
    }
  }
}
