package nz.ac.auckland.se306.group12.visualizer;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.internal.TerminalWidth;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.scheduler.Scheduler;
import nz.ac.auckland.se306.group12.visualizer.util.AnsiColor;
import nz.ac.auckland.se306.group12.visualizer.util.AnsiSgrSequenceBuilder;
import nz.ac.auckland.se306.group12.visualizer.util.AsciiSpinner;
import nz.ac.auckland.se306.group12.visualizer.util.BrailleSpinner;
import nz.ac.auckland.se306.group12.visualizer.util.TerminalHeight;

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
   */
  private final TerminalWidth terminalWidthManager = new TerminalWidth();
  /**
   * Used to detect the height (in lines) of the visualiser's output terminal window.
   */
  private final TerminalHeight terminalHeightManager = new TerminalHeight();
  /**
   * Used to adapt the visualiser output to the terminal window width. If
   * {@link #terminalWidthManager} cannot detect the window width, the fallback value 80 is used.
   */
  private int terminalWidth = 80;
  /**
   * Used to adapt the visualiser output to the terminal window height. If
   * {@link #terminalHeightManager} cannot detect the window height, the fallback value 24 is used.
   */
  private int terminalHeight = 24;

  /**
   * The task graph whose schedules (partial or complete) are to be visualised.
   */
  private final Graph taskGraph;
  /**
   * The {@link Scheduler} whose progress to visualise.
   */
  private final Scheduler scheduler;
  /**
   * If the visualiser could run instantaneously, then this field would be redundant. However, if
   * individual methods in this class each call {@link Scheduler#getStatus()} separately, then we
   * may run into the scenario where the scheduler is {@link SchedulerStatus#SCHEDULING} at the
   * start of a visualisation cycle, but has {@link SchedulerStatus#SCHEDULED} midway through the
   * cycle. This results in a single visualisation "snapshot" apparently showing conflicting
   * information, where part of the output says that the scheduler is running and another says that
   * it has finished.
   */
  private SchedulerStatus schedulerStatus;

  /**
   * Responsible for re-rendering the visualisation * on a regular basis, based on the
   * {@link #scheduler}'s best-so-far schedule.
   */
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final LocalDateTime startTime;

  /**
   * This visualiser’s output is just a massive string. This is where the heavy lifting gets done.
   * Initial capacity of 1000 is actually conservative, but already miles more appropriate than the
   * default 16.
   */
  private final StringBuilder sb = new StringBuilder(1000);
  private final AsciiSpinner spinner = new BrailleSpinner();

  /**
   * Instantiates and <strong>immediately begins running</strong> a visualiser.
   *
   * @param taskGraph The task graph whose schedules (partial and/or complete) are to be
   *                  visualised.
   * @param scheduler The {@link Scheduler} whose progress to visualise.
   */
  public TerminalVisualizer(Graph taskGraph, Scheduler scheduler) {
    this.taskGraph = taskGraph;
    this.scheduler = scheduler;

    // No need to keep the returned ScheduledFuture; the ScheduledExecutorService shutdown takes
    // care of cancelling this future
    this.executor.scheduleAtFixedRate(this::visualize,
        0,
        500,
        TimeUnit.MILLISECONDS);
    this.startTime = LocalDateTime.now();
  }

  /**
   * Renders the given {@link Schedule}, and prints it to system out.
   * <p>
   * The visualiser tries to scale gracefully to the width of the terminal window (if the width can
   * be detected). Nevertheless, at narrow widths (about 3 times the number of processors), any soft
   * wrapping done by the terminal will break comprehensibility of the Gantt chart.
   */
  private void visualize() {
    this.schedulerStatus = scheduler.getStatus();
    updateTerminalDimensions();

    this.addDivider(); // Top border
    sb.append(NEW_LINE);

    this.drawStatusBar();
    sb.append(NEW_LINE);

    this.drawStatistics();
    sb.append(NEW_LINE);

    this.drawGanttChart(scheduler.getBestSchedule());
    sb.append(NEW_LINE);

    this.addDivider(); // Bottom border

    eraseDisplay();
    System.out.println(sb);

    if (schedulerStatus == SchedulerStatus.SCHEDULED) {
      this.executor.shutdown();
    }

    // Clear the string builder
    int len = sb.length();
    sb.setLength(0);
    sb.setLength(len);
  }

  /**
   * Attempts to get and store the width of the terminal window where this visualiser is rendering
   * its output.
   * <p>
   * Likely to work on Unix-like operating systems (Linux and macOS), but Windows support may be
   * hit-or-miss. If the terminal width cannot be determined, the fallback (initial) value will be
   * used.
   */
  private void updateTerminalDimensions() {
    int width = terminalWidthManager.getTerminalWidth();
    if (width > 0) {
      terminalWidth = width - 1; // -1 for wiggle room
    }

    int height = terminalHeightManager.getTerminalHeight();
    if (height > 0) {
      terminalHeight = height;
    }
  }

  /**
   * Takes a {@link Schedule}, and creates a Gantt Chart representation of it in plaintext (with
   * some in-band formatting using ANSI control sequences). The plaintext chart is then appended to
   * this visualiser's string builder to be drawing (printing) in
   * {@link TerminalVisualizer#visualize()}.
   *
   * @param schedule The schedule to be rendered graphically (or... terminally?).
   */
  private void drawGanttChart(Schedule schedule) {
    // Clamp the width of each column to between 2ch and 15ch (excl. 1ch gap between columns)
    // +1 in the denominator to accommodate labels along the time axis
    int columnWidth = Math.max(2, Math.min(terminalWidth / (schedule.getProcessorCount() + 1), 15));
    String columnSlice = " ".repeat(columnWidth);

    int timeLabelWidth = Math.min(6, columnWidth);

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

        sb.append(AnsiSgrSequenceBuilder.RESET).append(" "); // Padding between columns
      }

      sb.deleteCharAt(sb.length() - 1); // Trim trailing padding

      // Vertical axis label: time
      // Not enforcing maximum length in case of long makespans (this may cause text wrap issues in
      // narrow terminals)
      sb.append(time % 5 == 4
          ? String.format("%s%" + timeLabelWidth + "d%s",
          new AnsiSgrSequenceBuilder().faint().underline(),
          time + 1,
          AnsiSgrSequenceBuilder.RESET)
          : columnSlice);

      sb.append(NEW_LINE);
    }
  }

  /**
   * <pre>
   * ┌──────────────────┬──────────────────────────────────────┬───────────────┐
   * │ SCHEDULER STATUS │              GRAPH NAME              │   STOPWATCH   │
   * │ width 14 (fixed) │         fill remaining space         │ min. width 11 │
   * └──────────────────┴──────────────────────────────────────┴───────────────┘
   * </pre>
   */
  private void drawStatusBar() {
    // Scheduler status
    sb.append(new AnsiSgrSequenceBuilder().bold()
        .foreground(AnsiColor.COLOR_CUBE_8_BIT[5][5][5]));

    switch (schedulerStatus) {
      case IDLE -> {
        sb.append(new AnsiSgrSequenceBuilder()
                .background(AnsiColor.COLOR_CUBE_8_BIT[5][2][0])) // 208 orange
            .append(' ')
            .append(spinner.nextFrame());
      }
      case SCHEDULING -> {
        sb.append(new AnsiSgrSequenceBuilder()
                .background(AnsiColor.COLOR_CUBE_8_BIT[5][1][2])) // 204 magenta
            .append(' ')
            .append(spinner.nextFrame());
      }
      case SCHEDULED -> {
        sb.append(new AnsiSgrSequenceBuilder()
                .background(AnsiColor.COLOR_CUBE_8_BIT[0][3][0])) // 34 green
            .append(' ')
            .append(spinner.doneFrame());
      }
    }

    sb.append(String.format(" %-10.10s ", schedulerStatus));

    // Prepare stopwatch label (so length is determined)
    long elapsedSeconds = SECONDS.between(startTime, LocalDateTime.now());
    String stopwatchLabel = elapsedSeconds < 60
        ? String.format(" %7.7ss ", elapsedSeconds)
        : String.format(" %2dm %2.2ss ", elapsedSeconds / 60, elapsedSeconds % 60);
    int stopwatchLength = stopwatchLabel.length();

    // Graph name
    sb.append(new AnsiSgrSequenceBuilder().normalIntensity()
            .foreground(52, 52, 52)
            .background(190, 190, 190))
        .append(String.format(" %-"
                + (terminalWidth - 16 - stopwatchLength)
                + "."
                + (terminalWidth - 16 - stopwatchLength)
                + "s ",
            taskGraph.getName()));

    // Elapsed time (stopwatch)
    sb.append(new AnsiSgrSequenceBuilder()
            .background(AnsiColor.COLOR_CUBE_8_BIT[1][1][5]) // 63 lavender-ish
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[5][5][5])) // 231 white
        .append(stopwatchLabel);

    sb.append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);
  }

  private void drawStatistics() {
    // Heading
    sb.append("Possible schedules: ");

    // Search count token
    sb.append(new AnsiSgrSequenceBuilder()
            .background(AnsiColor.COLOR_CUBE_8_BIT[3][4][5]) // 153 light blue
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[0][1][2])) // 23 dark blue
        .append(String.format("  %,d searched  ", scheduler.getPrunedCount()))
        .append(AnsiSgrSequenceBuilder.RESET);

    // Padding
    sb.append(' ');

    // Prune count token
    sb.append(new AnsiSgrSequenceBuilder()
            .background(AnsiColor.COLOR_CUBE_8_BIT[5][4][1]) // 221 pale gold
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[2][1][0])) // 94 dark orange
        .append(String.format("  %,d pruned  ", scheduler.getPrunedCount()))
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);
  }

  /**
   * Adds a solid horizontal line to the output.
   */
  private void addDivider() {
    // Note: 8-bit fallback colour is `AnsiColor.EIGHT_BIT_COLOR_CUBE[2][0][5]`
    sb.append(new AnsiSgrSequenceBuilder().foreground(125, 86, 243))
        .append("─".repeat(terminalWidth))
        .append(AnsiSgrSequenceBuilder.RESET)
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
   * Clears the entire terminal window. Equivalent to:
   * <pre> {@code
   *   cursorToStart();
   *   eraseToEnd();
   * }</pre>
   */
  private void eraseDisplay() {
    System.out.print("\033[H\033[2J");
  }

  /**
   * Move cursor to upper left of the terminal window.
   */
  private void cursorToStart() {
    System.out.print("\033[H");
  }

  /**
   * Clears the terminal from the cursor position to the end of the window.
   */
  private void eraseToEnd() {
    System.out.print("\033[J");
  }

  /**
   * Clears the current line. Cursor position does not change.
   */
  private void eraseLine() {
    System.out.print("\033[2K"); // Cursor to start
  }

}
