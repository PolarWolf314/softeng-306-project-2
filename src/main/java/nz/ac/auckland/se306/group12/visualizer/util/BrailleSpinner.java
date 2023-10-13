package nz.ac.auckland.se306.group12.visualizer.util;

public class BrailleSpinner implements AsciiSpinner {

  private static final String[] FRAMES = "⣷⣯⣟⡿⢿⣻⣽⣾".split("");
  private static final String DONE = "✔";
  private int frameIndex = -1;

  @Override
  public String nextFrame() {
    frameIndex = (frameIndex + 1) % FRAMES.length;
    return FRAMES[frameIndex];
  }

  @Override
  public String doneFrame() {
    return DONE;
  }

}
