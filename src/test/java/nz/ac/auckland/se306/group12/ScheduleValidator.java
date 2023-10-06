package nz.ac.auckland.se306.group12;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private void assertValidOrder(Schedule schedule, Graph graph) {
    Set<Task> completedTasks = new HashSet<>();

    List<Task> tasks = graph.getTasks();

    ScheduledTask[] scheduledTasks = schedule.getScheduledTasks();

    for (int i = 0; i < scheduledTasks.length; i++) {
      ScheduledTask scheduledTask = scheduledTasks[i];
      Task task = tasks.get(i);
      completedTasks.add(task);
      boolean parentsComplete = task.getIncomingEdges()
          .stream()
          .allMatch(edge -> completedTasks.contains(edge.getSource()));
      boolean completedBeforeChildrenStart = task.getOutgoingEdges()
          .stream()
          .allMatch(edge -> scheduledTasks[tasks.indexOf(edge.getDestination())].getStartTime()
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
  private void validateSchedule(Schedule schedule, Graph graph) {

    Map<Integer, Integer> processors = new HashMap<>();

    for (int i = 0; i < schedule.getProcessorEndTimes().length; i++) {
      processors.put(i, 0);
    }
    // Make sure schedule order is valid
    List<ScheduledTask> listTasks = List.of(schedule.getScheduledTasks());

    Assertions.assertEquals(graph.getTasks().size(), listTasks.size(),
        String.format(
            "Graph has order %d, but %d tasks have been scheduled",
            graph.getTasks().size(), listTasks.size()));

    this.assertValidOrder(schedule, graph);

    // Run the schedule
    List<Task> tasks = graph.getTasks();

    ScheduledTask[] scheduledTasks = schedule.getScheduledTasks();

    for (int i = 0; i < scheduledTasks.length; i++) {
      ScheduledTask scheduledTask = scheduledTasks[i];
      Task task = tasks.get(i);
      int processCore = scheduledTask.getProcessorIndex();

      for (Edge edge : task.getIncomingEdges()) {
        Task parent = edge.getSource();
        ScheduledTask scheduledParent = scheduledTasks[tasks.indexOf(parent)];

        int processSource = scheduledParent.getProcessorIndex();
        int transferTime = processCore == processSource ? 0 : edge.getWeight();

        Assertions.assertTrue(
            scheduledTask.getStartTime() >= scheduledParent.getEndTime() + transferTime,
            String.format(
                "Invalid Schedule: Task %s starts before parent %s completes, at start %d",
                task.getLabel(), parent.getLabel(), scheduledTask.getStartTime()));
      }

      int currentCoreValue = processors.get(processCore);

      Assertions.assertTrue(currentCoreValue <= scheduledTask.getStartTime(),
          String.format("Invalid Schedule: Task %s overlaps with another task on processor %d",
              task.getLabel(), processCore));

      processors.put(processCore, scheduledTask.getEndTime());
    }
  }

}
