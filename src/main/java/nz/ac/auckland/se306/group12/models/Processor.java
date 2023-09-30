package nz.ac.auckland.se306.group12.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Process class represents a process in a schedule that has a cumulative start time dependent on
 * the list of scheduled tasks, and the list of scheduled tasks.
 */
@Getter
@ToString
@RequiredArgsConstructor
public class Processor {


  private final List<Node> scheduledTasks = new ArrayList<>();
  @Getter
  private final int processorIndex;
  private int endTime = 0;
  @Setter
  private int finalCost = 0;

  public void addTask(Node task) {
    this.endTime = task.getEndTime();
    this.scheduledTasks.add(task);
  }
}
