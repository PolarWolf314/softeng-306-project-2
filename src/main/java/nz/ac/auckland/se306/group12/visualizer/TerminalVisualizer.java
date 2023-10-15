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
import nz.ac.auckland.se306.group12.monitors.ResourceMonitor;
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
   * The number of logical processors available for execution. The {@link #scheduler} may or may not
   * use all of them.
   */
  private final int systemProcessorCount = ResourceMonitor.getLogicalProcessorCount();

  /**
   * Responsible for re-rendering the visualisation on a regular basis, based on the
   * {@link #scheduler}'s best-so-far schedule.
   */
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
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

  /**
   * The task graph whose schedules (partial or complete) are to be visualised.
   */
  private final Graph taskGraph;
  /**
   * The {@link Scheduler} whose progress to visualise.
   */
  private final Scheduler scheduler;
  /**
   * Whether {@link #scheduler} is using parallel execution.
   */
  private final boolean executionIsParallel;

  private final LocalDateTime startTime;

  private int terminalWidth = 80;
  /**
   * Used to adapt the visualiser output to the terminal window height. If
   * {@link #terminalHeightManager} cannot detect the window height, the fallback value 24 is used.
   */
  private int terminalHeight = 24;
  private final int resourceChartBodyHeight = (this.systemProcessorCount + 1) / 2;
  /**
   * The available space for the schedule's Gantt chart to fill, after accounting for UI chrome and
   * other content, such as the system resource usage graph and statistic chips. This "maximum" is
   * slightly conservative.
   */
  private int ganttChartMaxBodyHeight;
  private int peakCpuUsagePercentage = 0;
  private long peakMemoryUsageMiB = 0;


  /**
   * This visualiser’s output is just a massive string. This is where the heavy lifting gets done.
   * This initial capacity is actually conservative, but already miles more appropriate than the
   * default 16.
   */
  private final StringBuilder sb = new StringBuilder(1200);
  private final AsciiSpinner spinner = new BrailleSpinner();

  /**
   * If the visualiser could run instantaneously, then this field would be redundant. However, if
   * methods in this class independently access {@link #scheduler}, then they may receive different
   * data. This results in a single visualisation "snapshot" showing internally inconsistent
   * information.
   */
  private SchedulerStatus schedulerStatus;
  /**
   * The schedule to be rendered graphically (or... terminally?). Kept as a field and updated with
   * each visualisation cycle for the same reason as {@link #schedulerStatus}.
   */
  private Schedule schedule;


  /**
   * Instantiates and <strong>immediately begins running</strong> a visualiser.
   * <p>
   * The rate at which {@link #visualize()} may be run is inextricably coupled to the delay used by
   * {@link ResourceMonitor#getProcessorCpuLoad()}, because it will take at least that long to
   * return its value.  If the period is too short, the executor will have a backlog, which renders
   * the visualiser ineffective.  Ignoring that annoying method, {@link #visualize()} should
   * comfortably take somewhere in the order of 100 ms to run.
   *
   * @param taskGraph               The task graph whose schedules (partial and/or complete) are to
   *                                be visualised.
   * @param scheduler               The {@link Scheduler} whose progress to visualise.
   * @param executionProcessorCount The number of threads the {@link Scheduler} is using to execute
   *                                (not the number of processors for which it is scheduling
   *                                tasks).
   */
  public TerminalVisualizer(Graph taskGraph, Scheduler scheduler, int executionProcessorCount) {
    this.taskGraph = taskGraph;
    this.scheduler = scheduler;
    this.executionIsParallel = executionProcessorCount > 1;
    this.startTime = LocalDateTime.now();

    // No need to keep the returned ScheduledFuture; the ScheduledExecutorService shutdown takes
    // care of cancelling this future
    //
    // Period must be greater than delay in call to `getProcessorCpuLoad()` for some asinine reason.
    // See Javadoc immediately above.
    this.executor.scheduleAtFixedRate(this::visualize,
        0,
        500,
        TimeUnit.MILLISECONDS);
  }

  /**
   * Renders the given {@link Schedule}, and prints it to system out.
   * <p>
   * The visualiser tries to scale gracefully to the width of the terminal window (if the width can
   * be detected). Nevertheless, at narrow widths (about 3 times the number of processors), any soft
   * wrapping done by the terminal will break comprehensibility of the Gantt chart.
   */
  private void visualize() {
    this.eraseDisplay();

    // Get latest data
    this.schedulerStatus = scheduler.getStatus();
    this.schedule = scheduler.getBestSchedule();
    this.updateDimensions();

    // Prepare frame
    this.drawHorizontalRule();
    sb.append(NEW_LINE);
    this.drawStatusBar();
    sb.append(NEW_LINE);
    if (schedule == null) {
      this.drawLoadingGraphic();
    } else {
//      if (schedulerStatus != SchedulerStatus.SCHEDULED) {
      this.drawSystemResourceUsage();
      sb.append(NEW_LINE);
//      }
      this.drawGanttChart();
      sb.append(NEW_LINE);
      this.drawStatistics();
    }
    sb.append(NEW_LINE);
    this.drawHorizontalRule();
    sb.deleteCharAt(sb.length() - 1); // Trim trailing newline

    System.out.println(sb); // Render frame

    if (schedulerStatus == SchedulerStatus.SCHEDULED) {
      this.executor.shutdown();
    }
    this.clearStringBuilder();
  }

  /**
   * Attempts to get and store the width of the terminal window where this visualiser is rendering
   * its output.
   * <p>
   * Likely to work on Unix-like operating systems (Linux and macOS), but Windows support may be
   * hit-or-miss. If the terminal width cannot be determined, the fallback (initial) value will be
   * used.
   */
  private void updateDimensions() {
    int width = terminalWidthManager.getTerminalWidth();
    if (width > 0) {
      terminalWidth = width - 1; // -1 for wiggle room
    }

    int height = terminalHeightManager.getTerminalHeight();
    if (height > 0) {
      terminalHeight = height;
    }

    this.ganttChartMaxBodyHeight = getGanttChartMaxBodyHeight();
  }

  /**
   * Determines the number of lines in the terminal which may be used by the {@link #schedule}'s
   * Gantt chart, after accounting for space reserved for other components such as UI chrome, the
   * system resource usage chart, and statistics chips. Axis labels are <strong>not</strong>
   * considered part of the Gantt chart's body.
   *
   * @return The maximum number of lines the Gantt chart body may occupy.
   */
  private int getGanttChartMaxBodyHeight() {
    return this.terminalHeight - this.resourceChartBodyHeight - 16;
  }

  /**
   * <pre>
   *  _                    _ _
   * | |    ___   __ _  __| (_)_ __   __ _
   * | |   / _ \ / _` |/ _` | | '_ \ / _` |
   * | |__| (_) | (_| | (_| | | | | | (_| |_ _ _
   * |_____\___/ \__,_|\__,_|_|_| |_|\__, (_|_|_)
   *                                 |___/
   * </pre>
   */
  private void drawLoadingGraphic() {
    final String format = "%" + (44 + (terminalWidth - 44) / 2) + "s%n";
    //                    ^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Centre alignment
    //                     44 is the length of the `Loading...` word art
    sb.append(new AnsiSgrSequenceBuilder().faint())
        .append(String.format(format, " _                    _ _                   "))
        .append(String.format(format, "| |    ___   __ _  __| (_)_ __   __ _       "))
        .append(String.format(format, "| |   / _ \\ / _` |/ _` | | '_ \\ / _` |      "))
        .append(String.format(format, "| |__| (_) | (_| | (_| | | | | | (_| |_ _ _ "))
        .append(String.format(format, "|_____\\___/ \\__,_|\\__,_|_|_| |_|\\__, (_|_|_)"))
        .append(String.format(format, "                                |___/       "))
        .append(AnsiSgrSequenceBuilder.RESET);
  }

  /**
   * <pre>
   * +------------------+--------------------------------------+---------------+
   * | SCHEDULER STATUS |              GRAPH NAME              |   STOPWATCH   |
   * | width 14 (fixed) |         fill remaining space         | min. width 11 |
   * +------------------+--------------------------------------+---------------+
   * </pre>
   * (Hideous ASCII characters because Windows can't handle box-drawing characters smh.)
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
            .background(AnsiColor.COLOR_CUBE_8_BIT[4][4][5]) // 189 lilac-ish
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[0][0][1])) // 18 blue-black
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

  private void drawSystemResourceUsage() {
    final String backgroundSgrSequence = new AnsiSgrSequenceBuilder()
        .background(252).toString();
    final String foregroundSgrSequence = new AnsiSgrSequenceBuilder()
        .background(AnsiColor.COLOR_CUBE_8_BIT[1][1][5]).toString();

    final int columnWidth = (this.terminalWidth - 1) / 2; // -1 for padding between columns

    // Get CPU usage
    final double[] coreLoads = ResourceMonitor.getProcessorCpuLoad();
    final int cpuLoadPercentage = (int) Math.round(ResourceMonitor.getSystemCpuLoad() * 100);

    // Get memory usage
    final long memoryUsageMiB = ResourceMonitor.getMemoryUsageMiB();
    final long maxMemoryMiB = ResourceMonitor.getMaxMemoryMiB();

    // Update peak usage stats
    if (cpuLoadPercentage > this.peakCpuUsagePercentage) {
      this.peakCpuUsagePercentage = cpuLoadPercentage;
    }
    if (memoryUsageMiB > this.peakMemoryUsageMiB) {
      this.peakMemoryUsageMiB = memoryUsageMiB;
    }

    // CPU usage chart
    sb.append(new AnsiSgrSequenceBuilder().bold())
        .append("vCPUs ")
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(cpuLoadPercentage)
        .append("% ")
        .append(new AnsiSgrSequenceBuilder().faint())
        .append("(peak ")
        .append(this.peakCpuUsagePercentage)
        .append("%)")
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);
    for (int i = 0; i < this.resourceChartBodyHeight; i++) {
      // Left column
      int cpuNumber = i + 1;
      String cpuLabel = String.format("%2d ", cpuNumber);
      int coreUsageQuantized = (int) Math.round((columnWidth - cpuLabel.length()) * coreLoads[i]);
      sb.append(cpuLabel)
          .append(foregroundSgrSequence)
          .append(" ".repeat(coreUsageQuantized))
          .append(backgroundSgrSequence)
          .append(" ".repeat((columnWidth - 3) - coreUsageQuantized))
          .append(AnsiSgrSequenceBuilder.RESET);

      sb.append(terminalWidth % 2 == 0 ? "  " : " "); // Padding between columns

      // Right column
      cpuNumber = i + this.resourceChartBodyHeight + 1;
      if (cpuNumber > this.systemProcessorCount) {
        break; // Odd number of CPUs
      }
      cpuLabel = String.format("%2d ", cpuNumber);
      coreUsageQuantized = (int) Math.round(
          (columnWidth - cpuLabel.length()) * coreLoads[cpuNumber - 1]);
      sb.append(cpuLabel)
          .append(foregroundSgrSequence)
          .append(" ".repeat(coreUsageQuantized))
          .append(backgroundSgrSequence)
          .append(" ".repeat((columnWidth - 3) - coreUsageQuantized))
          .append(AnsiSgrSequenceBuilder.RESET);

      sb.append(NEW_LINE);
    }

    sb.append(NEW_LINE);

    // Memory usage chart
    sb.append(new AnsiSgrSequenceBuilder().bold())
        .append("Memory ")
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(memoryUsageMiB)
        .append('/')
        .append(maxMemoryMiB)
        .append(" MiB ")
        .append(new AnsiSgrSequenceBuilder().faint())
        .append("(peak ")
        .append(this.peakMemoryUsageMiB)
        .append(" MiB)")
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);

    final float memoryUsageRatio = (float) memoryUsageMiB / maxMemoryMiB;
    final int memoryUsageQuantized = Math.round(this.terminalWidth * memoryUsageRatio);

    sb.append(foregroundSgrSequence)
        .append(" ".repeat(memoryUsageQuantized))
        .append(backgroundSgrSequence)
        .append(" ".repeat(this.terminalWidth - memoryUsageQuantized))
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);
  }

  /**
   * Takes a {@link Schedule}, and creates a Gantt Chart representation of it in plaintext (with
   * some in-band formatting using ANSI control sequences). The plaintext chart is then appended to
   * this visualiser's string builder to be drawing (printing) in
   * {@link TerminalVisualizer#visualize()}.
   */
  private void drawGanttChart() {
    // Clamp the width of each column to between 2ch and 15ch (excl. 1ch gap between columns)
    // +1 in the denominator to accommodate labels along the time axis
    int columnWidth = Math.max(3,
        Math.min(terminalWidth / (this.schedule.getProcessorCount() + 1), 16)) - 1;
    String columnSlice = " ".repeat(columnWidth);

    int timeLabelWidth = Math.min(7, columnWidth);

    // Horizontal axis labels: processors
    int processorCount = this.schedule.getProcessorCount();
    for (int processorIndex = 1; processorIndex <= processorCount; processorIndex++) {
      sb.append(String.format("P%-" + (columnWidth) + "d", processorIndex));
    }

    // Horizontal axis label: time
    sb.append(String.format("%" + (timeLabelWidth - 1) + "s", "time")).append(NEW_LINE);

    // Chart body
    int[][] verticalGantt = scheduleToGantt(this.schedule);
    int makespan = verticalGantt.length;

    int scale = Math.max(1, makespan / this.ganttChartMaxBodyHeight);

    boolean[] taskRenderStarted = new boolean[this.schedule.getScheduledTaskCount()];
    int rowNumber = 0;
    for (int time = 0; time < makespan; time += scale) {
      // Slight abuse of term, this "time slice" always has duration 1
      int[] timeSlice = verticalGantt[time];
      for (int activeTaskIndex : timeSlice) {
        if (activeTaskIndex == PROCESSOR_IDLE) {
          // Processor idling
          sb.append(new AnsiSgrSequenceBuilder().background(252)) // #c6c6c6 grey
              .append(columnSlice);
        } else {
          sb.append(new AnsiSgrSequenceBuilder().bold()
                  .foreground(AnsiColor.COLOR_CUBE_8_BIT[5][5][5]) // White
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
      sb.append(++rowNumber % 3 == 0
          ? String.format("%s%" + timeLabelWidth + "d%s",
          new AnsiSgrSequenceBuilder().faint().underline(),
          time,
          AnsiSgrSequenceBuilder.RESET)
          : columnSlice);

      sb.append(NEW_LINE);
    }
  }

  private void drawStatistics() {
    // Determine lengths (widths) of each chip
    final String searchCountChip = String.format("  %,d searched  ", scheduler.getSearchedCount());
    final String pruneCountChip = String.format("  %,d pruned  ", scheduler.getPrunedCount());
    final String makespanChip = String.format("  %,d  ", schedule.getLatestEndTime());

    sb.append("Schedules "); // Length 10

    // Search count chip
    sb.append(new AnsiSgrSequenceBuilder()
            .background(AnsiColor.COLOR_CUBE_8_BIT[3][4][5]) // 153 light blue
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[0][1][2])) // 23 dark blue
        .append(searchCountChip)
        .append(AnsiSgrSequenceBuilder.RESET);

    // Padding
    sb.append(' '); // Length 1

    // Prune count chip
    sb.append(new AnsiSgrSequenceBuilder()
            .background(AnsiColor.COLOR_CUBE_8_BIT[5][4][1]) // 221 pale gold
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[2][1][0])) // 94 dark orange
        .append(pruneCountChip)
        .append(AnsiSgrSequenceBuilder.RESET);

    // Padding - fill space between schedule chips and makespan chip
    // Note: Not using String.format() here for right-alignment because formatting control sequences
    //       make string lengths unpredictable
    sb.append(" ".repeat(terminalWidth - 20
        - searchCountChip.length()
        - pruneCountChip.length()
        - makespanChip.length()));

    // Makespan chip
    sb.append(new AnsiSgrSequenceBuilder().bold())
        .append("Makespan ") // Length 9
        .append(new AnsiSgrSequenceBuilder()
            .background(238) // #444 grey
            .foreground(255)) // #eee grey
        .append(makespanChip)
        .append(AnsiSgrSequenceBuilder.RESET);

    sb.append(NEW_LINE);
  }

  /**
   * Adds a solid horizontal line to the output.
   */
  private void drawHorizontalRule() {
    sb.append(new AnsiSgrSequenceBuilder()
            .foreground(AnsiColor.COLOR_CUBE_8_BIT[1][1][5])) // 63 lavender-ish
        .append("─".repeat(terminalWidth))
        .append(AnsiSgrSequenceBuilder.RESET)
        .append(NEW_LINE);
  }

  /**
   * Appends in-band instruction (ANSI CSI sequence) to erase the display. Moves the cursor to the
   * upper left of the terminal and erases the entire window.
   */
  private void eraseDisplay() {
    sb.append("\033[H\033[2J");
  }

  /**
   * Clears the workhorse string builder, {@link #sb},  preserving its capacity. (The next "frame"
   * is likely to be rendered with a similar string length.)
   */
  private void clearStringBuilder() {
    int len = sb.length();
    sb.setLength(0);
    sb.setLength(len);
  }

  /**
   * "Flattens" a {@link Schedule} into a 2D matrix which may be interpreted as a
   * <strong>vertical</strong> Gantt chart. Each column represents a processor, and each row
   * represents one unit time in the schedule. (Time advances <em>down</em> the columns.)
   * <p>
   * The element in position (<var>t</var>, <var>P</var>) of the matrix is the index of the task
   * that is active on processor <var>P</var> at time <var>t</var>.
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
  private static int[][] scheduleToGantt(Schedule schedule) {
    if (schedule == null) {
      return new int[][]{};
    }

    // Initialise Gantt matrix where all cores idle for the entire makespan
    int[][] scheduleMatrix = new int[schedule.getLatestEndTime()][schedule.getProcessorCount()];
    for (int[] row : scheduleMatrix) {
      Arrays.fill(row, PROCESSOR_IDLE);
    }

    // "Fill in" Gantt matrix with active tasks based on start times, execution times
    ScheduledTask[] tasks = schedule.getScheduledTasks();
    for (int i = 0, taskCount = schedule.getScheduledTaskCount(); i < taskCount; i++) {
      ScheduledTask task = tasks[i];
      for (int time = task.getStartTime(), end = task.getEndTime(); time < end; time++) {
        scheduleMatrix[time][task.getProcessorIndex()] = i;
      }
    }

    return scheduleMatrix;
  }

}
