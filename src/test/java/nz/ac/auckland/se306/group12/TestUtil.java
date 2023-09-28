package nz.ac.auckland.se306.group12;

import java.io.File;
import java.io.IOException;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Graph;
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
      Assertions.fail("File not found.");
      return null;
    }
  }
}
