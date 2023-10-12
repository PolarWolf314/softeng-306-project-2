package nz.ac.auckland.se306.group12.models;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * ScheduledTask class represents a tasks in a schedule that has a start time and a processor
 * assigned to it.
 */
@Getter
@AllArgsConstructor
@ToString
public class ScheduledTask {

  private final int startTime;
  private final int endTime;
  private final int processorIndex;

  @Override
  public int hashCode() {
    return Objects.hash(startTime, processorIndex);
  }
}
