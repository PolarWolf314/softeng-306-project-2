package nz.ac.auckland.se306.group12;

import java.io.File;
import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.models.Task;
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
   * Takes in a schedule and turns it into a list of tasks for each processor
   *
   * @param schedule to be converted
   * @return List of tasks for each processor
   */
  public static List<List<Task>> scheduleToListNodes(List<Processor> schedule) {
    return schedule.stream().map(Processor::getScheduledTasks).toList();
  }
}
