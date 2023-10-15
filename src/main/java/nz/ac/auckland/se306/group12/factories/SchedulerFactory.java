package nz.ac.auckland.se306.group12.factories;

import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.scheduler.AStarScheduler;
import nz.ac.auckland.se306.group12.scheduler.DfsScheduler;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;

public class SchedulerFactory {

  /**
   * Returns a scheduler for the given algorithm
   *
   * @param arguments The command line arguments that this was run with
   * @return A scheduler for the given algorithm
   */
  public Scheduler getScheduler(CommandLineArguments arguments) {
    // We don't currently support parallelisation of the other schedulers
    if (arguments.parallelisationProcessorCount() > 1) {
      return new DfsScheduler(arguments.parallelisationProcessorCount());
    }

    switch (arguments.algorithm().toLowerCase()) {
      case "astar" -> {
        return new AStarScheduler();
      }
      case "dfs" -> {
        return new DfsScheduler();
      }
      default -> throw new IllegalArgumentException(
          "Invalid algorithm. " + arguments.algorithm() + " is not a valid algorithm.");
    }
  }

}
