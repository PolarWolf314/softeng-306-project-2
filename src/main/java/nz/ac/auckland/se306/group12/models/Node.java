package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Node class represents a task in a schedule
 */
@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {

  @EqualsAndHashCode.Include
  private final String label;
  @EqualsAndHashCode.Include
  private final long weight;
  private final Set<Edge> incomingEdges = new HashSet<>();
  private final Set<Edge> children = new HashSet<>();

}
