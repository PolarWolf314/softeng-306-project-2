package nz.ac.auckland.se306.group12;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.Task;
import org.junit.jupiter.api.Assertions;

public class ScheduleValidator {

  private final DotGraphIO dotGraphIO = new DotGraphIO();

  /**
   * Asserts that the given list of tasks is in a valid order. If not, this will cause the unit test
   * to fail.
   *
   * @param scheduledTasks list of scheduledTasks in order of start time
   * @param tasks          list of tasks from original graph in order of start time
   */
  public static void assertValidOrder(List<ScheduledTask> scheduledTasks, List<Task> tasks) {
    Set<Task> completedTasks = new HashSet<>();

    for (int i = 0; i < scheduledTasks.size(); i++) {
      ScheduledTask scheduledTask = scheduledTasks.get(i);
      Task task = tasks.get(i);
      completedTasks.add(task);
      boolean isReady = task.getIncomingEdges()
          .stream()
          .allMatch(edge -> completedTasks.contains(edge.getSource()));
      boolean isFinishedBeforeChildren = task.getOutgoingEdges()
          .stream()
          .allMatch(edge -> scheduledTasks.get(tasks.indexOf(edge.getDestination())).getStartTime()
              >= scheduledTask.getEndTime());

      Assertions.assertTrue(isReady,
          String.format("Invalid order: Dependents of task %s not met", task.getLabel()));

      Assertions.assertTrue(isFinishedBeforeChildren,
          String.format("Invalid order: Dependents of task %s start before this task completes",
              task.getLabel()));
    }
  }

  /**
   * Checks that the resulting schedule from the given graph is valid.
   *
   * @param schedule  The {@link Schedule} representing the tasks to be scheduled
   * @param taskGraph The {@link Graph} representing the original graph of the schedule
   */
  public static void assertValidSchedule(Schedule schedule, Graph taskGraph) {

    int[] processors = new int[schedule.getProcessorEndTimes().length];

    List<Task> unorderedTasks = taskGraph.getTasks();
    ScheduledTask[] unorderedScheduledTasks = schedule.getScheduledTasks();

    Map<Task, ScheduledTask> taskMapper = new HashMap<>();
    for (int i = 0; i < unorderedTasks.size(); i++) {
      taskMapper.put(unorderedTasks.get(i), unorderedScheduledTasks[i]);
    }

    // Sorts tasks and scheduledTasks by the startTime
    final List<Task> tasks = taskMapper.entrySet()
        .stream()
        .sorted(Comparator
            .comparingInt(entry -> entry.getValue().getStartTime()))
        .map(Entry::getKey)
        .toList();

    final List<ScheduledTask> scheduledTasks = taskMapper.values()
        .stream()
        .sorted(Comparator
            .comparingInt(ScheduledTask::getStartTime))
        .toList();

    Assertions.assertEquals(taskGraph.getTasks().size(), scheduledTasks.size(),
        String.format(
            "Graph has order %d, but %d tasks have been scheduled",
            taskGraph.getTasks().size(), scheduledTasks.size()));

    // Validate the schedule's order
    assertValidOrder(scheduledTasks, tasks);

    // Run the schedule
    for (int i = 0; i < scheduledTasks.size(); i++) {
      ScheduledTask scheduledTask = scheduledTasks.get(i);
      Task task = tasks.get(i);
      int processorCore = scheduledTask.getProcessorIndex();

      for (Edge edge : task.getIncomingEdges()) {
        Task parent = edge.getSource();
        ScheduledTask scheduledParent = scheduledTasks.get(tasks.indexOf(parent));

        int processorSource = scheduledParent.getProcessorIndex();
        int transferTime = processorCore == processorSource ? 0 : edge.getWeight();

        Assertions.assertTrue(
            scheduledTask.getStartTime() >= scheduledParent.getEndTime() + transferTime,
            String.format(
                "Invalid Schedule: Task %s starts before parent %s completes, at start %d",
                task.getLabel(), parent.getLabel(), scheduledTask.getStartTime()));
      }

      int currentCoreValue = processors[processorCore];

      Assertions.assertTrue(currentCoreValue <= scheduledTask.getStartTime(),
          String.format("Invalid Schedule: Task %s overlaps with another task on processor %d",
              task.getLabel(), processorCore));

      processors[processorCore] = scheduledTask.getEndTime();
    }
  }
}
