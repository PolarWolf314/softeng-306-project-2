package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Edge class represents a dependence between two tasks in a schedule
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Edge {

  private final Task source;
  private final Task destination;
  private final int weight;

  /**
   * A custom <code>toString</code> method so that we can include the labels of the nodes without
   * getting stuck in an infinite loop.
   *
   * @return A string representation of the edge
   */
  @Override
  public String toString() {
    return String.format("Edge(%s -[%s]-> %s)",
        this.source.getLabel(), this.weight, this.destination.getLabel());
  }

}
