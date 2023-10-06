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
   * @param schedule The list of tasks to be checked
   */
  public static void assertValidOrder(List<ScheduledTask> scheduledTasks, List<Task> tasks) {
    Set<Task> completedTasks = new HashSet<>();

    for (int i = 0; i < scheduledTasks.size(); i++) {
      ScheduledTask scheduledTask = scheduledTasks.get(i);
      Task task = tasks.get(i);
      completedTasks.add(task);
      boolean parentsComplete = task.getIncomingEdges()
          .stream()
          .allMatch(edge -> completedTasks.contains(edge.getSource()));
      boolean completedBeforeChildrenStart = task.getOutgoingEdges()
          .stream()
          .allMatch(edge -> scheduledTasks.get(tasks.indexOf(edge.getDestination())).getStartTime()
              >= scheduledTask.getEndTime());

      Assertions.assertTrue(parentsComplete,
          String.format("Invalid order: Dependents of task %s not met", task.getLabel()));

      Assertions.assertTrue(completedBeforeChildrenStart,
          String.format("Invalid order: Dependents of task %s start before this task completes",
              task.getLabel()));
    }
  }

  /**
   * Checks that the resulting schedule from the given graph is valid.
   *
   * @param schedule The {@link Schedule} representing the tasks to be scheduled
   */
  public static void validateSchedule(Schedule schedule, Graph graph) {

    Map<Integer, Integer> processors = new HashMap<>();

    for (int i = 0; i < schedule.getProcessorEndTimes().length; i++) {
      processors.put(i, 0);
    }
    List<Task> unorderedTasks = graph.getTasks();
    ScheduledTask[] unorderedScheduledTasks = schedule.getScheduledTasks();

    Map<Task, ScheduledTask> taskMapper = new HashMap<>();
    for (int i = 0; i < unorderedTasks.size(); i++) {
      taskMapper.put(unorderedTasks.get(i), unorderedScheduledTasks[i]);
    }

    // Sorts tasks and scheduledTasks by the startTime
    List<Task> tasks = taskMapper.entrySet()
        .stream()
        .sorted(Comparator
            .comparingInt(entry -> entry.getValue().getStartTime()))
        .map(Entry::getKey)
        .toList();

    List<ScheduledTask> scheduledTasks = taskMapper.values()
        .stream()
        .sorted(Comparator
            .comparingInt(ScheduledTask::getStartTime))
        .toList();

    Assertions.assertEquals(graph.getTasks().size(), scheduledTasks.size(),
        String.format(
            "Graph has order %d, but %d tasks have been scheduled",
            graph.getTasks().size(), scheduledTasks.size()));

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

      int currentCoreValue = processors.get(processorCore);

      Assertions.assertTrue(currentCoreValue <= scheduledTask.getStartTime(),
          String.format("Invalid Schedule: Task %s overlaps with another task on processor %d",
              task.getLabel(), processorCore));

      processors.put(processorCore, scheduledTask.getEndTime());
    }
  }

}
