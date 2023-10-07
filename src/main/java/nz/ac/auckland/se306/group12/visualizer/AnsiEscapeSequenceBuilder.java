package nz.ac.auckland.se306.group12.visualizer;

import nz.ac.auckland.se306.group12.exceptions.InvalidColorException;

public class AnsiEscapeSequenceBuilder {

  private static final String CONTROL_SEQUENCE_INTRODUCER = "\033[";
  private static final String CONTROL_SEQUENCE_DELIMITER = "m";
  private static final String SEPARATOR = ";";

  private final StringBuilder stringBuilder = new StringBuilder();

  /**
   * Resets (disables) all display attributes.
   */
  public AnsiEscapeSequenceBuilder reset() {
    stringBuilder.append("0").append(SEPARATOR);
    return this;
  }

  /**
   * Sets bold, or increased intensity. Rendered as a colour change in some terminals.
   */
  public AnsiEscapeSequenceBuilder bold() {
    stringBuilder.append("1").append(SEPARATOR);
    return this;
  }

  /**
   * Set single underline, or disable underline (single or double).
   * <p>
   * Further styling is supported by Kitty, VTE, mintty, iTerm2 and Konsole, but this class does not
   * take advantage of their style extensions.
   * <p>
   * SGR code {@code 21} may be used to set double underline in ECMA-48 compliant terminals.
   * However, "several" terminals <a
   * href="https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_(Select_Graphic_Rendition)_parameters">reportedly</a>
   * instead use this code to disable bold (or increased intensity). Hence, this class intentionally
   * discourages use of code {@code 21} by not providing a method for it.
   *
   * @param enable {@code true} to enable underline; {@code false} to disable underline.
   */
  public AnsiEscapeSequenceBuilder underline(boolean enable) {
    stringBuilder.append(enable ? "4" : "24").append(SEPARATOR);
    return this;
  }

  /**
   * Sets normal intensity. That is, neither bold nor faint.
   */
  public AnsiEscapeSequenceBuilder normalIntensity() {
    stringBuilder.append("22").append(SEPARATOR);
    return this;
  }

  /**
   * Sets no underline. That is, neither singly nor doubly underlined.
   */
  public AnsiEscapeSequenceBuilder noUnderline() {
    return this.underline(false);
  }

  /**
   * Sets foreground colour with 256-colour mode, based on the pre-defined lookup table (available
   * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">on Wikipedia</a>).
   */
  public AnsiEscapeSequenceBuilder foreground(int colorCode8Bit) {
    if (colorCode8Bit < 0 || colorCode8Bit > 255) {
      throw new InvalidColorException("SGR code %d is not a valid colour code in 256-colour mode.");
    }
    stringBuilder.append("38").append(SEPARATOR)
        .append("5").append(SEPARATOR)
        .append(colorCode8Bit).append(SEPARATOR);
    return this;
  }

  /**
   * Sets RGB foreground colour with 24-bit "true colour" mode.
   */
  public AnsiEscapeSequenceBuilder foreground(int r, int g, int b) {
    if (r < 0 || g < 0 || b < 0 || r > 255 || g > 255 || b > 255) {
      throw new InvalidColorException("rgb(%d %d %d) is not a valid colour.");
    }
    stringBuilder.append("38").append(SEPARATOR)
        .append("2").append(SEPARATOR)
        .append(r).append(SEPARATOR)
        .append(g).append(SEPARATOR)
        .append(b).append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder defaultForeground() {
    stringBuilder.append("39").append(SEPARATOR);
    return this;
  }

  /**
   * Sets background colour with 256-colour mode, based on the pre-defined lookup table (available
   * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">on Wikipedia</a>).
   */
  public AnsiEscapeSequenceBuilder background(int colorCode8Bit) {
    if (colorCode8Bit < 0 || colorCode8Bit > 255) {
      throw new InvalidColorException("SGR code %d is not a valid colour code in 256-colour mode.");
    }
    stringBuilder.append("48").append(SEPARATOR)
        .append("5").append(SEPARATOR)
        .append(colorCode8Bit).append(SEPARATOR);
    return this;
  }

  /**
   * Sets RGB background colour with 24-bit "true colour" mode.
   */
  public AnsiEscapeSequenceBuilder background(int r, int g, int b) {
    stringBuilder.append("48").append(SEPARATOR)
        .append("2").append(SEPARATOR)
        .append(r).append(SEPARATOR)
        .append(g).append(SEPARATOR)
        .append(b).append(SEPARATOR);
    return this;
  }

  public AnsiEscapeSequenceBuilder defaultBackground() {
    stringBuilder.append("49").append(SEPARATOR);
    return this;
  }

  @Override
  public String toString() {
    // Remove trailing separator
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);

    // Delimit control sequence
    stringBuilder.insert(0, CONTROL_SEQUENCE_INTRODUCER);
    stringBuilder.append(CONTROL_SEQUENCE_DELIMITER);

    return stringBuilder.toString();
  }

}
