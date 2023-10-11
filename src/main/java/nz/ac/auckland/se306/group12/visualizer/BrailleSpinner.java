package nz.ac.auckland.se306.group12.visualizer;

public class BrailleSpinner implements AsciiSpinner {

  private static final String[] FRAMES = {"⢎⡰", "⢎⡡", "⢎⡑", "⢎⠱", "⠎⡱", "⢊⡱", "⢌⡱", "⢆⡱"};
  private int frameIndex = 0;

  @Override
  public String nextFrame() {
    return FRAMES[frameIndex++];
  }

}
