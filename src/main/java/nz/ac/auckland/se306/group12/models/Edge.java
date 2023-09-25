package nz.ac.auckland.se306.group12.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Edge class represents a dependences between two tasks in a schedule
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Edge {

    private final Node source;
    private final Node destination;
    @EqualsAndHashCode.Exclude
    private final long weight;

}
