package nz.ac.auckland.se306.group12.visualizer.util;

import nz.ac.auckland.se306.group12.exceptions.InvalidColorException;

/**
 * A helper class for building ANSI escape sequences (or control sequences) to be used as in-band
 * formatting signals when outputting to console. In particular, this class creates sequences using
 * <a
 * href="https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_(Select_Graphic_Rendition)_parameters"
 * >Select Graphic Rendition</a> (SGA) parameters.
 * <p>
 * All methods in this builder class (except {@link AnsiSgrSequenceBuilder#toString()}) return the
 * object on which it was called (i.e. {@code this}), so calls may be chained.
 */
public class AnsiSgrSequenceBuilder {

  private static final String CONTROL_SEQUENCE_INTRODUCER = "\033[";
  private static final String CONTROL_SEQUENCE_DELIMITER = "m";
  private static final String SEPARATOR = ";";

  public static final String RESET = new AnsiSgrSequenceBuilder().toString();
  public static final String SET_BOLD = new AnsiSgrSequenceBuilder().bold().toString();
  public static final String SET_FAINT = new AnsiSgrSequenceBuilder().faint().toString();
  public static final String SET_NORMAL_INTENSITY = new AnsiSgrSequenceBuilder().normalIntensity()
      .toString();

  private final StringBuilder stringBuilder = new StringBuilder();

  /**
   * Resets (disables) all display attributes.
   */
  public AnsiSgrSequenceBuilder reset() {
    stringBuilder.append("0").append(SEPARATOR);
    return this;
  }

  /**
   * Sets bold, or increased intensity. Rendered as a colour change in some terminals.
   */
  public AnsiSgrSequenceBuilder bold() {
    stringBuilder.append("1").append(SEPARATOR);
    return this;
  }

  /**
   * Sets faint, or decreased intensity. Some terminals may render this as a different font weight,
   * such as light or bold.
   */
  public AnsiSgrSequenceBuilder faint() {
    stringBuilder.append("2").append(SEPARATOR);
    return this;
  }

  /**
   * Sets single underline.
   */
  public AnsiSgrSequenceBuilder underline() {
    return this.underline(true);
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
  public AnsiSgrSequenceBuilder underline(boolean enable) {
    stringBuilder.append(enable ? "4" : "24").append(SEPARATOR);
    return this;
  }

  /**
   * Sets normal intensity. That is, neither bold nor faint.
   */
  public AnsiSgrSequenceBuilder normalIntensity() {
    stringBuilder.append("22").append(SEPARATOR);
    return this;
  }

  /**
   * Sets no underline. That is, neither singly nor doubly underlined.
   */
  public AnsiSgrSequenceBuilder noUnderline() {
    return this.underline(false);
  }

  /**
   * Sets foreground colour with 256-colour mode, based on the pre-defined lookup table (available
   * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">on Wikipedia</a>).
   */
  public AnsiSgrSequenceBuilder foreground(int colorCode8Bit) {
    if (colorCode8Bit < 0 || colorCode8Bit > 255) {
      throw new InvalidColorException("SGR code %d is not a valid colour code in 256-colour mode.");
    }
    stringBuilder.append("38").append(SEPARATOR)
        .append("5").append(SEPARATOR)
        .append(colorCode8Bit).append(SEPARATOR);
    return this;
  }

  /**
   * Resets foreground colour to default. This is terminal-implementation-dependent, and may vary
   * with user configuration.
   */
  public AnsiSgrSequenceBuilder defaultForeground() {
    stringBuilder.append("39").append(SEPARATOR);
    return this;
  }

  /**
   * Sets RGB foreground colour with 24-bit "true colour" mode.
   */
  public AnsiSgrSequenceBuilder foreground(int r, int g, int b) {
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

  /**
   * Sets background colour with 256-colour mode, based on the pre-defined lookup table (available
   * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">on Wikipedia</a>).
   */
  public AnsiSgrSequenceBuilder background(int colorCode8Bit) {
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
  public AnsiSgrSequenceBuilder background(int r, int g, int b) {
    stringBuilder.append("48").append(SEPARATOR)
        .append("2").append(SEPARATOR)
        .append(r).append(SEPARATOR)
        .append(g).append(SEPARATOR)
        .append(b).append(SEPARATOR);
    return this;
  }


  /**
   * Resets background colour to default. This is terminal-implementation-dependent, and may vary
   * with user configuration.
   */
  public AnsiSgrSequenceBuilder defaultBackground() {
    stringBuilder.append("49").append(SEPARATOR);
    return this;
  }

  /**
   * Delimits the control sequence in proper delimiters ({@code ESC[}...{@code m}) and returns it in
   * {@link String} form.
   * <p>
   * If called without any of the "optional" methods defined above, its behaviour is equivalent to
   * {@code this.reset().toString()}. This the control sequence with no parameters (that is,
   * {@code ESC[m}) is equivalent to that with the reset code {@code ESC[0m}.
   * <p>
   * This method takes the place of what would've been the `{@code build()} method, whose behaviour
   * would've been identical. This takes advantage of times when Java automatically calls
   * {@link Object#toString()}, so the programmer need not always end the call chain with
   * {@code .build()}.
   *
   * @return The delimited ANSI control sequence, as a string.
   */
  @Override
  public String toString() {
    // Remove trailing separator
    if (!stringBuilder.isEmpty()) {
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }

    // Delimit control sequence
    stringBuilder.insert(0, CONTROL_SEQUENCE_INTRODUCER);
    stringBuilder.append(CONTROL_SEQUENCE_DELIMITER);

    return stringBuilder.toString();
  }

}
