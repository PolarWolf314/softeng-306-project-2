package nz.ac.auckland.se306.group12;

import java.io.File;
import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import org.junit.jupiter.api.Assertions;

public class TestUtil {

  private static final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Loads a graph from a file of a specified path for testing.
   *
   * @param path of the file to load
   * @return the graph object read from the file
   */
  public static Graph loadGraph(String path) {
    try {
      File file = new File(path);
      return dotGraphIO.readDotGraph(file);
    } catch (IOException e) {
      return Assertions.fail("File not found.");
    }
  }

  /**
   * Returns a list of all the optimal schedulers that should be used in the parameterized testing
   * of the scheduler in {@link OptimalSchedulerTest}.
   *
   * @return The schedulers to test
   */
  public static List<Scheduler> getOptimalSchedulers() {
    // TODO: Return instances of the optimal schedulers
    Scheduler placeholderScheduler = new Scheduler() {
      @Override
      public Schedule schedule(Graph graph, int processorCount) {
        return null;
      }
    };

    return List.of(placeholderScheduler);
  }

}
