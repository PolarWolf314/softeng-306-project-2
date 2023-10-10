package nz.ac.auckland.se306.group12.visualizer;

public class AnsiColor {

  /* 3-bit colours */

  public static final int NORMAL_FG_BLACK = 30;
  public static final int NORMAL_FG_RED = 31;
  public static final int NORMAL_FG_GREEN = 32;
  public static final int NORMAL_FG_YELLOW = 33;
  public static final int NORMAL_FG_BLUE = 34;
  public static final int NORMAL_FG_MAGENTA = 35;
  public static final int NORMAL_FG_CYAN = 36;
  public static final int NORMAL_FG_WHITE = 37;

  public static final int NORMAL_BG_BLACK = 40;
  public static final int NORMAL_BG_RED = 41;
  public static final int NORMAL_BG_GREEN = 42;
  public static final int NORMAL_BG_YELLOW = 43;
  public static final int NORMAL_BG_BLUE = 44;
  public static final int NORMAL_BG_MAGENTA = 45;
  public static final int NORMAL_BG_CYAN = 46;
  public static final int NORMAL_BG_WHITE = 47;

  /* 4-bit colours */

  public static final int BRIGHT_FG_BLACK = 90;
  public static final int BRIGHT_FG_RED = 91;
  public static final int BRIGHT_FG_GREEN = 92;
  public static final int BRIGHT_FG_YELLOW = 93;
  public static final int BRIGHT_FG_BLUE = 94;
  public static final int BRIGHT_FG_MAGENTA = 95;
  public static final int BRIGHT_FG_CYAN = 96;
  public static final int BRIGHT_FG_WHITE = 97;

  public static final int BRIGHT_BG_BLACK = 100;
  public static final int BRIGHT_BG_RED = 101;
  public static final int BRIGHT_BG_GREEN = 102;
  public static final int BRIGHT_BG_YELLOW = 103;
  public static final int BRIGHT_BG_BLUE = 104;
  public static final int BRIGHT_BG_MAGENTA = 105;
  public static final int BRIGHT_BG_CYAN = 106;
  public static final int BRIGHT_BG_WHITE = 107;

  /* 8-bit colours */

  /**
   * The Select Graphic Rendition (SGR) colour codes from {@code 16} to {@code 231} (inclusive) make
   * a 6×6×6 RGB colour cube. This 3D matrix makes them accessible with R, G, and B values between 0
   * and 5 (not unlike the 0-255 we're used with the "normal" RGB colour model).
   * <p>
   * Codes {@code 0} through {@code 15} (inclusive) are equivalent to the normal and bright ANSI
   * colours, though note that their colour codes are not interchangeable with those defined above.
   * <p>
   * The remaining 24 codes are greyscale colours ranging from #080808 (code {@code 232}) to #eeeeee
   * (code {@code 255}).
   * <p>
   * The full lookup table is available <a
   * href="https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit">on Wikipedia</a>.
   * <p>
   * Ideally, this would be an immutable object. (Or, at least a private object whose values are
   * accessed via a read-only method.) However, the priority is to minimise performance overhead, so
   * using an array, despite elements being mutable.
   */
  public static final int[][][] COLOR_CUBE_8_BIT = new int[6][6][6];

  static {
    int sgrColorCode = 16;
    for (int r = 0; r < 6; r++) {
      for (int g = 0; g < 6; g++) {
        for (int b = 0; b < 6; b++) {
          COLOR_CUBE_8_BIT[r][g][b] = sgrColorCode++;
        }
      }
    }
  }

  /**
   * Only static fields and methods in this class, so it doesn't make sense to allow instantiation.
   */
  private AnsiColor() {
  }

}
