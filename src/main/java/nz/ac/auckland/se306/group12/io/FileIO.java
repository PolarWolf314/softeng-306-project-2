package nz.ac.auckland.se306.group12.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileIO {

  /**
   * Writes the specified contents to the given file. If the file does not already exist then it
   * will attempt to create it. If the file already exists then it will be overwritten. If there is
   * any error while writing to the file then an {@link IOException} will be thrown.
   *
   * @param contents The contents to write to the file
   * @param file     The file to write the contents to
   * @throws IOException If an error occurs while writing to the file
   */
  public static void writeToFile(final String contents, final File file) throws IOException {
    if (!file.exists()) {
      if (!file.createNewFile()) {
        throw new IOException("Failed to create file: " + file.getPath());
      }
    }

    try (final FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(contents.getBytes());
    }
  }

  /**
   * Adds the <code>.dot</code> file extension to the filename if it doesn't already have it. If the
   * filename is <code>null</code> then <code>null</code> is returned.
   *
   * @param filename The filename to add the extension to
   * @return The filename with the <code>.dot</code> extension
   */
  public static String withDotExtension(String filename) {
    if (filename == null) {
      return null;
    }

    if (!filename.endsWith(".dot")) {
      filename += ".dot";
    }
    return filename;
  }

  /**
   * Removes the <code>.dot</code> file extension from the filename if it has it. If the filename
   * is
   * <code>null</code> then <code>null</code> is returned.
   *
   * @param filename The filename to remove the extension from
   * @return The filename without the <code>.dot</code> extension
   */
  public static String withoutDotExtension(String filename) {
    if (filename == null) {
      return null;
    }

    if (filename.endsWith(".dot")) {
      filename = filename.substring(0, filename.length() - 4);
    }
    return filename;
  }

}
