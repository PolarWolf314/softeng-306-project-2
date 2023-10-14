package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AOScheduledTask class represents a tasks in a AO schedule that has a
 * start time and a processor and childTask assigned to it.
 */
@Getter
@Setter
@ToString
public class AOScheduledTask extends ScheduledTask {

  // next task index
  private int next = -1;

  public AOScheduledTask(int startTime, int endTime, int processorIndex) {
    super(startTime, endTime, processorIndex);
  }

  public AOScheduledTask(AOScheduledTask scheduledTask) {
    super(scheduledTask.startTime, scheduledTask.endTime, scheduledTask.processorIndex);
    this.next = scheduledTask.next;
  }

}
