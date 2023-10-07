package nz.ac.auckland.se306.group12.visualizer;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;

public class TuiVisualizer implements Visualizer {

  private static final String NEW_LINE = System.getProperty("line.separator");

  private final Graph taskGraph;
  private final StringBuilder sb = new StringBuilder(800);

  public TuiVisualizer(Graph taskGraph) {
    this.taskGraph = taskGraph;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void visualize(Schedule schedule) {

    // Status bar
    this.populateStatusBar();
    sb.append(NEW_LINE);

    // Gantt Chart Header
    for (int processorIndex = 1; processorIndex <= schedule.getProcessorEndTimes().length;
        processorIndex++) {
      sb.append(String.format("P%-7s", processorIndex));
    }

    sb.append(NEW_LINE);

    System.out.println(sb);
  }

  private void populateStatusBar() {
    sb.append(new AnsiEscapeSequenceBuilder().bold()
            .foreground(255, 255, 255)
            .background(255, 95, 135))
        .append(String.format("%-14s", " ✔ SCHEDULED ")) // TODO: Re-architect this
        .append(new AnsiEscapeSequenceBuilder().normalIntensity()
            .foreground(52, 52, 52)
            .background(190, 190, 190))
        .append(String.format(" %-64s ", taskGraph.getName()))
        .append(new AnsiEscapeSequenceBuilder().reset())
        .append(NEW_LINE);
  }

  private char[][] flatten(Schedule schedule) {
    return null;
  }

}
