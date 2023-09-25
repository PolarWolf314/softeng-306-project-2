package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Node class represents a task in a schedule
 */
@Getter
@RequiredArgsConstructor
public class Node {

    private final String label;
    private final long weight;
    private final Set<Edge> parents = new HashSet<>();
    private final Set<Edge> children = new HashSet<>();

}
