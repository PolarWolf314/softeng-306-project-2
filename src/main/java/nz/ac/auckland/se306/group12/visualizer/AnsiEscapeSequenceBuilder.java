package nz.ac.auckland.se306.group12.visualizer;

import nz.ac.auckland.se306.group12.exceptions.InvalidColorException;

public class AnsiEscapeSequenceBuilder {

  private static final String CONTROL_SEQUENCE_INTRODUCER = "\033[";
  private static final String CONTROL_SEQUENCE_DELIMITER = "m";
  private static final String SEPARATOR = ";";

  private final StringBuilder stringBuilder = new StringBuilder();

  public AnsiEscapeSequenceBuilder reset() {
    stringBuilder.append("0").append(SEPARATOR);
    return this;
  }

  /**
   * ECMA-48 stipulates that code {@code 21} should doubly underline subsequent characters, but in
   * "several" terminals, it instead disables bold intensity.
   */
  public AnsiEscapeSequenceBuilder bold() {
    stringBuilder.append("1").append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder underline(boolean enable) {
    stringBuilder.append(enable ? "4" : "24").append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder normalIntensity() {
    stringBuilder.append("21").append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder foreground(int colorCode8Bit) {
    if (colorCode8Bit < 0 || colorCode8Bit > 255) {
      throw new InvalidColorException("%d is not a valid colour code in 256-colour mode.");
    }
    stringBuilder.append("38").append(SEPARATOR)
        .append(colorCode8Bit).append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder foreground(int r, int g, int b) {
    if (r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255) {
      throw new InvalidColorException("rgb(%d %d %d) is not a valid colour.");
    }
    stringBuilder.append("38").append(SEPARATOR)
        .append(r).append(SEPARATOR)
        .append(g).append(SEPARATOR)
        .append(b).append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder background(int colorCode8Bit) {
    if (colorCode8Bit < 0 || colorCode8Bit > 255) {
      throw new InvalidColorException("%d is not a valid colour code in 256-colour mode.");
    }
    stringBuilder.append("48").append(SEPARATOR)
        .append(colorCode8Bit).append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder background(int r, int g, int b) {
    stringBuilder.append("48").append(SEPARATOR)
        .append(r).append(SEPARATOR)
        .append(g).append(SEPARATOR)
        .append(b).append(SEPARATOR);
    return this;
  }

  public String build() {
    stringBuilder.insert(0, CONTROL_SEQUENCE_INTRODUCER);
    stringBuilder.append(CONTROL_SEQUENCE_DELIMITER);
    return stringBuilder.toString();
  }

}
