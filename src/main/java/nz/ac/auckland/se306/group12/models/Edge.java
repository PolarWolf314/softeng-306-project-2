package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Edge class represents a dependences between two tasks in a schedule
 */
@Getter
@AllArgsConstructor
public class Edge {

    private final Node source;
    private final Node destination;
    private final long weight;

}
