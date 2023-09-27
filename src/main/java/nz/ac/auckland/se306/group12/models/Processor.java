package nz.ac.auckland.se306.group12.models;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Process class represents a process in a schedule that has a cumulative start time dependent on
 * the list of scheduled tasks, and the list of scheduled tasks.
 */
@Getter
@RequiredArgsConstructor
public class Processor {

  private final List<Node> scheduledTasks;
  private int cumulativeStartTime = 0;

  public void addTask(Node task) {
    cumulativeStartTime += task.getWeight();
    scheduledTasks.add(task);
  }
}
