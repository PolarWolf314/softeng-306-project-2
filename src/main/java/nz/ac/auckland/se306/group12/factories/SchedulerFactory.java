package nz.ac.auckland.se306.group12.factories;

import nz.ac.auckland.se306.group12.scheduler.AStarScheduler;
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
        System.out.println("A* Scheduler not implemented");
        System.exit(1);
        return new AStarScheduler();
      }
      case "dfs" -> {
        return new DfsScheduler();
      }
      default -> throw new IllegalArgumentException(
          "Invalid algorithm. " + algorithm + " is not a valid algorithm.");
    }
  }
}
