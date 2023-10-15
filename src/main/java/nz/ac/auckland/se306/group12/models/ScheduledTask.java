package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ScheduledTask class represents a tasks in a schedule that has a start time and a processor
 * assigned to it.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class ScheduledTask {

  private final int startTime;
  private final int endTime;
  private final int processorIndex;

  public ScheduledTask(ScheduledTask scheduledTask) {
    this.startTime = scheduledTask.getStartTime();
    this.endTime = scheduledTask.getEndTime();
    this.processorIndex = scheduledTask.getProcessorIndex();
  }

}
