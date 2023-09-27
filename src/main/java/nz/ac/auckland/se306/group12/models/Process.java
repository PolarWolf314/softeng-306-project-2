package nz.ac.auckland.se306.group12.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Process class represents a process in a schedule that has a cumulative start time dependent on
 * the list of scheduled tasks, and the list of scheduled tasks.
 */
@Getter
@AllArgsConstructor
public class Process {

  private final long id;
  private final long cumulativeStartTime;
  private final List<ScheduledTask> scheduledTasks;
}
