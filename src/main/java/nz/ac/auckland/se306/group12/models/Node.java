package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Node class represents a task in a schedule
 */
@Getter
@AllArgsConstructor
public class Node {

    private final String label;
    private final long weight;
    private final Set<Edge> parents = new HashSet<>();
    private final Set<Edge> children = new HashSet<>();

}
