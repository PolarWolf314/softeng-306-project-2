package nz.ac.auckland.se306.group12.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import nz.ac.auckland.se306.group12.exceptions.DanglingEdgeException;
import nz.ac.auckland.se306.group12.exceptions.IllegalEdgeWeightException;

/**
 * Represents a graph of tasks and their dependences to create a schedule
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Graph {

  @Getter
  @Exclude
  private final String name;

  @Getter
  private final List<Task> tasks = new ArrayList<>();
  @Getter
  private final Set<Edge> edges = new HashSet<>();
  @Exclude
  private final Map<String, Integer> taskIndexMap = new HashMap<>();

  public Graph() {
    // Default name, for when the graph name doesn't matter
    this.name = "Graph";
  }

  /**
   * @return The number of tasks in this task graph. (In other words, the order of this graph.)
   */
  public int taskCount() {
    return this.tasks.size();
  }

  /**
   * Add and edge to the graph and also update the nodes incoming and outgoing edges
   *
   * @param source      The source node label
   * @param destination The destination node label
   * @param weight      The weight of the edge, representing communication cost
   */
  public void addEdge(String source, String destination, int weight) {
    Task sourceTask = this.getTask(source);
    if (sourceTask == null) {
      throw new DanglingEdgeException(String.format(
          "Cannot add edge (%s, %s) because node %s doesn't exist in the task graph.",
          source,
          destination,
          source)
      );
    }

    Task destinationTask = this.getTask(destination);
    if (destinationTask == null) {
      throw new DanglingEdgeException(String.format(
          "Cannot add edge (%s, %s) because node %s doesn't exist in the task graph.",
          source,
          destination,
          destination)
      );
    }

    if (weight < 0) {
      throw new IllegalEdgeWeightException("Edge weights must be non-negative.");
    }

    Edge edge = new Edge(sourceTask, destinationTask, weight);

    destinationTask.getIncomingEdges().add(edge);
    sourceTask.getOutgoingEdges().add(edge);
    this.edges.add(edge);
  }

  /**
   * Adds a task to the graph with the given label and weight.
   *
   * @param taskLabel The task's label
   * @param weight    The task's weight, representing the execution time of the task
   */
  public void addTask(String taskLabel, int weight) {
    int index = this.tasks.size();
    this.tasks.add(new Task(taskLabel, weight, index));
    this.taskIndexMap.put(taskLabel, index);
  }

  /**
   * Retrieves a task from the graph by its label. If no task with that label exists in the graph,
   * null is returned.
   *
   * @param label The label of the task to retrieve
   * @return The task with the given label, or null if no such task exists
   */
  public Task getTask(String label) {
    if (!this.taskIndexMap.containsKey(label)) {
      return null;
    }

    int index = this.taskIndexMap.get(label);
    return this.getTask(index);
  }

  /**
   * Retrieves a task from the graph by its index.
   *
   * @param index The index of the task to retrieve
   * @return The task with the given index
   * @throws IndexOutOfBoundsException If there is no task with the given index
   */
  public Task getTask(int index) {
    return this.tasks.get(index);
  }

  public void setBottomLevels() {
    for (Task task : this.getTasks()) {
      System.out.println(task.getLabel() + ": " + task.findBottomLevel());
    }
  }
}
