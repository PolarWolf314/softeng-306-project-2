package nz.ac.auckland.se306.group12.visualizer;

import java.util.Arrays;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

/**
 * A terminal-based class for visualising parallel schedules on systems with multiple homogenous
 * processors.
 */
public class TerminalVisualizer implements Visualizer {

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Used to indicate processor idle time in the 2D matrix representing the Gantt chart of a
   * {@link Schedule}.
   *
   * @see TerminalVisualizer#scheduleToGantt(Schedule, boolean)
   */
  private static final int PROCESSOR_IDLE = -1;

  /**
   * The task graph whose schedules (partial or complete) are to be visualised.
   */
  private final Graph taskGraph;

  /**
   * This visualiser’s output is just a massive string. This is where the heavy lifting gets done.
   * Initial capacity of 2000 is actually conservative, but already miles more appropriate than the
   * default 16.
   */
  private final StringBuilder sb = new StringBuilder(2000);

  public TerminalVisualizer(Graph taskGraph) {
    this.taskGraph = taskGraph;
  }

  /**
   * Renders the given {@link Schedule}, and prints it to system out.
   * <p>
   * The output produced is intended for a window of width 80 characters. If the terminal window
   * display the visualiser output is any narrower, soft wrapping will break its comprehensibility.
   * Wider windows will simply not fill the available width.
   * <p>
   * If given a schedule for more than 10 processors, the output will be wider than 80 characters.
   * For now, at least, just widen the terminal window to make the output look acceptable. A future
   * release may dynamically adapt to different window widths.
   */
  @Override
  public void visualize(Schedule schedule) {
    this.addDivider(); // Top border
    sb.append(NEW_LINE);

    this.populateStatusBar();
    sb.append(NEW_LINE);

    this.drawGanttChart(schedule);
    sb.append(NEW_LINE);

    this.addDivider(); // Bottom border

    System.out.println(sb);
  }

  /**
   * Takes a {@link Schedule}, and creates a Gantt Chart representation of it in plaintext (with
   * some in-band formatting using ANSI control sequences). The plaintext chart is then appended to
   * this visualiser's string builder to be drawing (printing) in
   * {@link TerminalVisualizer#visualize(Schedule)}.
   *
   * @param schedule The schedule to be rendered graphically (or... terminally?).
   */
  private void drawGanttChart(Schedule schedule) {
    // Chart header
    for (int processorIndex = 1; processorIndex <= schedule.getProcessorEndTimes().length;
        processorIndex++) {
      sb.append(String.format("P%-7s", processorIndex));
    }
    sb.append(NEW_LINE);

    // Chart body
    int[][] verticalGantt = this.scheduleToGantt(schedule);

    boolean[] taskRenderStarted = new boolean[schedule.getScheduledTaskCount()];
    for (int[] unitTime : verticalGantt) {
      for (int activeTaskIndex : unitTime) {

        if (activeTaskIndex == PROCESSOR_IDLE) {
          // Processor idling
          sb.append(new AnsiEscapeSequenceBuilder().background(7))
              .append("       "); // 7 spaces (chart columns are 7ch long, excl. padding)
        } else {
          sb.append(new AnsiEscapeSequenceBuilder().bold()
                  .foreground(AnsiColor.EIGHT_BIT_COLOR_CUBE[5][5][5])
                  .background(AnsiColor.EIGHT_BIT_COLOR_CUBE[activeTaskIndex % 6][0][4]))
              .append(taskRenderStarted[activeTaskIndex]
                  ? "       "
                  : String.format(" %-5.5s ", taskGraph.getTask(activeTaskIndex).getLabel()));

          taskRenderStarted[activeTaskIndex] = true;
        }

        sb.append(new AnsiEscapeSequenceBuilder().reset()).append(" "); // Padding between columns
      }

      sb.append(NEW_LINE);
    }
  }

  private void populateStatusBar() {
    sb.append(new AnsiEscapeSequenceBuilder().bold()
            .foreground(255, 255, 255)
            .background(255, 95, 135))
        .append(String.format("%-14s", " SCHEDULED ")) // TODO: Re-architect this
        .append(new AnsiEscapeSequenceBuilder().normalIntensity()
            .foreground(52, 52, 52)
            .background(190, 190, 190))
        .append(String.format(" %-64s ", taskGraph.getName()))
        .append(new AnsiEscapeSequenceBuilder().reset())
        .append(NEW_LINE);
  }

  /**
   * Adds a solid horizontal line, 80 characters wide, to the output.
   */
  private void addDivider() {
    // Note: 8-bit fallback colour is `AnsiColor.EIGHT_BIT_COLOR_CUBE[2][0][5]`
    sb.append(new AnsiEscapeSequenceBuilder().foreground(125, 86, 243))
        .append("─".repeat(80))
        .append(new AnsiEscapeSequenceBuilder().reset())
        .append(NEW_LINE);
  }

  /**
   * "Flattens" a {@link Schedule} into a 2D matrix which may be interpreted as a
   * <strong>vertical</strong> Gantt chart. Each column represents a processor, and each row
   * represents one unit time in the schedule. (Time advances <em>down</em> the columns.)
   * <p>
   * The element in position (t, P) of the matrix is the index of the task that is active on
   * processor P at time t.
   * <p>
   * A negative value (such as {@code PROCESSOR_IDLE}) indicates that processor is idle at that
   * moment in the schedule. The behaviour when an element has a value greater than the index of any
   * task in the task graph is undefined.
   *
   * @param schedule The schedule to be "flattened" into a Gantt matrix.
   * @return The 2D Gantt matrix representation of the given schedule.
   * @see Schedule
   * @see ScheduledTask
   */
  private int[][] scheduleToGantt(Schedule schedule) {
    int processorCount = schedule.getProcessorEndTimes().length;
    int makespan = schedule.getLatestEndTime();

    // Initialise Gantt matrix where all cores idle for the entire makespan
    int[][] scheduleMatrix = new int[makespan][processorCount];
    for (int[] row : scheduleMatrix) {
      Arrays.fill(row, PROCESSOR_IDLE);
    }

    ScheduledTask[] tasks = schedule.getScheduledTasks();
    for (int i = 0, taskCount = schedule.getScheduledTaskCount(); i < taskCount; i++) {
      ScheduledTask task = tasks[i];
      int processorIndex = task.getProcessorIndex();
      for (int time = task.getStartTime(); time < task.getEndTime(); time++) {
        // "Vertical" Gantt matrix, so columns represent processors
        scheduleMatrix[time][processorIndex] = i;
      }
    }

    return scheduleMatrix;
  }

}
