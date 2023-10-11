package nz.ac.auckland.se306.group12.visualizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Returns the height, in lines, of the command line terminal from which this program was started.
 * <p>
 * Currently works on Linux and macOS.
 * <p>
 * Returns {@code -1} if the height cannot be determined for some reason.
 * <p>
 * Adapted from <a href="https://github.com/argparse4j/argparse4j">argparse4j</a>'s
 * {@link net.sourceforge.argparse4j.internal.TerminalWidth} class, which also explains the mildly
 * awkward class name.
 * <p>
 * <h2>License</h2>
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <a href="http://www.apache.org/licenses/LICENSE-2.0">apache.org</a>.
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public class TerminalHeight {

  private static final int UNKNOWN_HEIGHT = -1;

  public int getTerminalHeight() {
    String height = System.getenv("LINES");
    if (height != null) {
      try {
        return Integer.parseInt(height);
      } catch (NumberFormatException e) {
        return UNKNOWN_HEIGHT;
      }
    }

    try {
      return getTerminalHeight2();
    } catch (IOException e) {
      return UNKNOWN_HEIGHT;
    }
  }

  // see
  // http://grokbase.com/t/gg/clojure/127qwgscvc/how-do-you-determine-terminal-console-width-in-%60lein-repl%60
  private int getTerminalHeight2() throws IOException {
    String osName = System.getProperty("os.name");
    boolean isOSX = osName.startsWith("Mac OS X");
    boolean isLinux = osName.startsWith("Linux") || osName.startsWith("LINUX");
    if (!isLinux && !isOSX) {
      return UNKNOWN_HEIGHT; // actually, this might also work on Solaris but this hasn't been tested
    }
    ProcessBuilder builder = new ProcessBuilder(whichSh().toString(),
        "-c", "stty -a < /dev/tty");
    builder.redirectErrorStream(true);
    Process process = builder.start();
    InputStream in = process.getInputStream();
    ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
    try {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) >= 0) {
        resultBytes.write(buf, 0, len);
      }
    } finally {
      in.close();
    }

    String result = new String(resultBytes.toByteArray());

    try {
      if (process.waitFor() != 0) {
        return UNKNOWN_HEIGHT;
      }
    } catch (InterruptedException e) {
      return UNKNOWN_HEIGHT;
    }

    String pattern;
    if (isOSX) {
      // Extract rows from a line such as this:
      // speed 9600 baud; 39 rows; 80 columns;
      pattern = "(\\d+) rows";
    } else {
      // Extract rows from a line such as this:
      // speed 9600 baud; rows 50; columns 83; line = 0;
      pattern = "rows (\\d+)";
    }
    Matcher m = Pattern.compile(pattern).matcher(result);
    if (!m.find()) {
      return UNKNOWN_HEIGHT;
    }
    result = m.group(1);

    try {
      return Integer.parseInt(result);
    } catch (NumberFormatException e) {
      return UNKNOWN_HEIGHT;
    }
  }

  private File whichSh() throws IOException {
    String path = System.getenv("PATH");
    if (path != null) {
      for (String dir : path.split(Pattern.quote(File.pathSeparator))) {
        File command = new File(dir.trim(), "sh");
        if (command.canExecute()) {
          return command.getAbsoluteFile();
        }
      }
    }
    throw new IOException("No command 'sh' on path " + path);
  }

}
