package nz.ac.auckland.se306.group12.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIO {

  /**
   * Writes the specified contents to the given file. If the file does not already exist then it
   * will attempt to create it. If the file already exists then it will be overwritten.
   *
   * @param contents The contents to write to the file
   * @param file     The file to write the contents to
   * @return Whether the contents were successfully written to the file
   */
  public static boolean writeToFile(final String contents, final File file) {
    try {
      if (!file.exists()) {
        if (!file.createNewFile()) {
          return false;
        }
      }

      try (final FileOutputStream outputStream = new FileOutputStream(file)) {
        outputStream.write(contents.getBytes());
      }
    } catch (final IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
