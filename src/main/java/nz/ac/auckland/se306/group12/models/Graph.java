package nz.ac.auckland.se306.group12.models;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

/*
 * Graph class represents a graph of tasks and their dependences to create a schedule
 */
@Getter
public class Graph {

    private final Set<Node> nodes = new HashSet<>();
    private final Set<Edge> edges = new HashSet<>();

}
