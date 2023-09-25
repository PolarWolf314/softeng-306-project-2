package nz.ac.auckland.se306.group12.models;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/*
 * Graph class represents a graph of tasks and their dependences to create a schedule
 */
@Getter
@ToString
@AllArgsConstructor
public class Graph {

  private final Set<Node> nodes;
  private final Set<Edge> edges;
}
