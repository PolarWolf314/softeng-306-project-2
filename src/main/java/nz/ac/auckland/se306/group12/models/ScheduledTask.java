package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ScheduledTask class represents a tasks in a schedule that has a start time and a processor
 * assigned to it.
 */
@Deprecated
@Getter
@AllArgsConstructor
public class ScheduledTask {

  private final Node node;
  private final int startTime;
  private final int processor;

}
