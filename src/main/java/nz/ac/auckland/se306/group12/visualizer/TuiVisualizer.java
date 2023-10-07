package nz.ac.auckland.se306.group12.visualizer;

import java.util.Arrays;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

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
    this.populateStatusBar();
    sb.append(NEW_LINE);

    this.updateGanttChart(schedule);
    sb.append(NEW_LINE);

    System.out.println(sb);
  }

  private void updateGanttChart(Schedule schedule) {
    // Chart header
    for (int processorIndex = 1; processorIndex <= schedule.getProcessorEndTimes().length;
        processorIndex++) {
      sb.append(String.format("P%-7s", processorIndex));
    }
    sb.append(NEW_LINE);

    // Chart body
    int[][] verticalGantt = this.scheduleToVerticalGantt(schedule);

    boolean[] taskRenderStarted = new boolean[schedule.getScheduledTaskCount()];
    for (int[] unitTime : verticalGantt) {
      for (int activeTaskIndex : unitTime) {

        if (activeTaskIndex == -1) {
          // Processor idling
          sb.append(new AnsiEscapeSequenceBuilder().background(7))
              .append("       "); // 7 spaces (chart columns are 7ch long, excl. padding)
        } else {
          sb.append(new AnsiEscapeSequenceBuilder()
                  .foreground(255, 255, 255)
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

  private int[][] scheduleToVerticalGantt(Schedule schedule) {
    return scheduleToGantt(schedule, true);
  }

  private int[][] scheduleToGantt(Schedule schedule, boolean transpose) {
    final int IDLE = -1;

    int processorCount = schedule.getProcessorEndTimes().length;
    int makespan = schedule.getLatestEndTime();

    // Initialise Gantt matrix where all cores idle for the entire makespan
    int[][] scheduleMatrix = transpose
        ? new int[makespan][processorCount]
        : new int[processorCount][makespan];
    for (int[] row : scheduleMatrix) {
      Arrays.fill(row, IDLE);
    }

    ScheduledTask[] tasks = schedule.getScheduledTasks();
    for (int i = 0, taskCount = schedule.getScheduledTaskCount(); i < taskCount; i++) {
      ScheduledTask task = tasks[i];
      int processorIndex = task.getProcessorIndex();
      for (int time = task.getStartTime(); time < task.getEndTime(); time++) {
        if (transpose) {
          scheduleMatrix[time][processorIndex] = i;
        } else {
          scheduleMatrix[processorIndex][time] = i;
        }
      }
    }

    return scheduleMatrix;
  }

}
