package nz.ac.auckland.se306.group12.visualizer;

import java.util.Arrays;
import net.sourceforge.argparse4j.internal.TerminalWidth;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

/**
 * A class for visualising parallel schedules on systems with multiple homogenous processors in a
 * Terminal UI.
 */
public class TerminalVisualizer implements Visualizer {

  private static final String NEW_LINE = System.getProperty("line.separator");

  /**
   * Used to indicate processor idle time in the 2D matrix representing the Gantt chart of a
   * {@link Schedule}.
   *
   * @see TerminalVisualizer#scheduleToGantt(Schedule)
   */
  private static final int PROCESSOR_IDLE = -1;

  /**
   * Used to detect the width (in characters) of the visualiser's output terminal window.
   *
   * @see #terminalWidth
   */
  private final TerminalWidth terminalWidthManager = new TerminalWidth();

  /**
   * Used to adapt the visualiser output to the terminal window width. If
   * {@link #terminalWidthManager} cannot detect the window width, the fallback value 80 is used.
   */
  private int terminalWidth = 80;

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
   * The visualiser tries to scale gracefully to the width of the terminal window (if the width can
   * be detected). Nevertheless, at narrow widths (about 3 times the number of processors), any soft
   * wrapping done by the terminal will break comprehensibility of the Gantt chart.
   */
  @Override
  public void visualize(Schedule schedule) {
    eraseDisplay();
    updateTerminalWidth();

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
   * Attempts to get and store the width of the terminal window where this visualiser is rendering
   * its output.
   * <p>
   * Likely to work on Unix-like operating systems (Linux and macOS), but Windows support may be
   * hit-or-miss. If the terminal width cannot be determined, the fallback (initial) value will be
   * used.
   */
  private void updateTerminalWidth() {
    int width = terminalWidthManager.getTerminalWidth();
    if (width > 0) {
      terminalWidth = width - 2; // -2 for wiggle room
    }
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
    // Clamp the width of each column to between 2ch and 15ch (excl. 1ch gap between columns)
    // +1 in the denominator to accommodate labels along the time axis
    int columnWidth = Math.max(2, Math.min(terminalWidth / (schedule.getProcessorCount() + 1), 15));
    String columnSlice = " ".repeat(columnWidth);

    int timeLabelWidth = Math.max(6, columnWidth);

    // Horizontal axis labels: processors
    for (int processorIndex = 1; processorIndex <= schedule.getProcessorCount(); processorIndex++) {
      sb.append(String.format("P%-" + columnWidth + "d", processorIndex));
    }

    // Horizontal axis label: time
    sb.append(String.format("%" + (timeLabelWidth - 1) + "s", "time")).append(NEW_LINE);

    // Chart body
    int[][] verticalGantt = this.scheduleToGantt(schedule);

    boolean[] taskRenderStarted = new boolean[schedule.getScheduledTaskCount()];
    for (int time = 0; time < verticalGantt.length; time++) {

      // Slight abuse of term, this "time slice" always has duration 1
      int[] timeSlice = verticalGantt[time];
      for (int activeTaskIndex : timeSlice) {
        if (activeTaskIndex == PROCESSOR_IDLE) {
          // Processor idling
          sb.append(new AnsiSgrSequenceBuilder().background(7))
              .append(columnSlice);
        } else {
          sb.append(new AnsiSgrSequenceBuilder().bold()
                  .foreground(AnsiColor.COLOR_CUBE_8_BIT[5][5][5])
                  .background(AnsiColor.COLOR_CUBE_8_BIT[activeTaskIndex % 6][0][4]))
              .append(taskRenderStarted[activeTaskIndex]
                  ? columnSlice
                  : String.format(" %-" + (columnWidth - 2) + "." + (columnWidth - 2) + "s ",
                      taskGraph.getTask(activeTaskIndex).getLabel()));

          taskRenderStarted[activeTaskIndex] = true;
        }

        sb.append(new AnsiSgrSequenceBuilder().reset()).append(" "); // Padding between columns
      }

      sb.deleteCharAt(sb.length() - 1); // Trim trailing padding

      // Vertical axis label: time
      // Not enforcing maximum length in case of long makespans (this may cause text wrap issues in
      // narrow terminals)
      sb.append(time % 5 == 4
          ? String.format("%s%" + timeLabelWidth + "d%s",
          new AnsiSgrSequenceBuilder().faint().underline(),
          time + 1,
          new AnsiSgrSequenceBuilder().reset())
          : columnSlice);

      sb.append(NEW_LINE);
    }
  }

  private void populateStatusBar() {
    sb.append(new AnsiSgrSequenceBuilder().bold()
            .foreground(255, 255, 255)
            .background(255, 95, 135))
        .append(
            String.format("%-14.14s", " SCHEDULED ")) // TODO: Re-architect to support live-updating
        .append(new AnsiSgrSequenceBuilder().normalIntensity()
            .foreground(52, 52, 52)
            .background(190, 190, 190))
        .append(String.format(" %-" + (terminalWidth - 16) + "." + (terminalWidth - 16) + "s ",
            taskGraph.getName()))
        .append(new AnsiSgrSequenceBuilder().reset())
        .append(NEW_LINE);
  }

  /**
   * Adds a solid horizontal line to the output.
   */
  private void addDivider() {
    // Note: 8-bit fallback colour is `AnsiColor.EIGHT_BIT_COLOR_CUBE[2][0][5]`
    sb.append(new AnsiSgrSequenceBuilder().foreground(125, 86, 243))
        .append("─".repeat(terminalWidth))
        .append(new AnsiSgrSequenceBuilder().reset())
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
    // Initialise Gantt matrix where all cores idle for the entire makespan
    int[][] scheduleMatrix = new int[schedule.getLatestEndTime()][schedule.getProcessorCount()];
    for (int[] row : scheduleMatrix) {
      Arrays.fill(row, PROCESSOR_IDLE);
    }

    // "Fill in" Gantt matrix with active tasks based on start times, execution times
    ScheduledTask[] tasks = schedule.getScheduledTasks();
    for (int i = 0, taskCount = schedule.getScheduledTaskCount(); i < taskCount; i++) {
      ScheduledTask task = tasks[i];
      for (int time = task.getStartTime(); time < task.getEndTime(); time++) {
        scheduleMatrix[time][task.getProcessorIndex()] = i;
      }
    }

    return scheduleMatrix;
  }

  /**
   * Moves the cursor to upper left and clears the entire screen.
   */
  private void eraseDisplay() {
    System.out.print("\033[H\033[2J");
  }

}
