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
import nz.ac.auckland.se306.group12.models.datastructures.TaskSet;
import nz.ac.auckland.se306.group12.scheduler.TopologicalSorter;

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
  @Exclude
  private final TopologicalSorter topologicalSorter = new TopologicalSorter();

  /**
   * This is the total weight of all the tasks in the graph. It is used as part of the underestimate
   * when pruning possible partial schedules.
   */
  @Getter
  private int totalTaskWeights = 0;

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
    this.totalTaskWeights += weight;
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

  /**
   * This method finds the top and bottom level for every task, using a topological order. The
   * bottom level is the maximum distance from the task to a sink task (task without children). The
   * top level is the maximum distance from the task to a source task excluding its own weight. Both
   * of these do not consider transfer times.
   */
  public void setTopAndBottomLevels() {
    List<Task> reverseTopologicalOrder = this.topologicalSorter.getAReverseTopologicalOrder(this);

    for (Task task : reverseTopologicalOrder) {
      int maxChildBottomLevel = task.getOutgoingEdges()
          .stream()
          .mapToInt(edge -> edge.getDestination().getBottomLevel())
          .max()
          .orElse(0);
      task.setBottomLevel(task.getWeight() + maxChildBottomLevel);
    }

    for (int index = reverseTopologicalOrder.size() - 1; index >= 0; index--) {
      Task task = reverseTopologicalOrder.get(index);

      int maxParentTopLevel = task.getIncomingEdges()
          .stream()
          .mapToInt(edge -> {
            Task parentTask = edge.getSource();
            // The parents top level doesn't include its own weight, but we need to add it for the
            // child's top level
            return parentTask.getTopLevel() + parentTask.getWeight();
          })
          .max()
          .orElse(0);

      task.setTopLevel(maxParentTopLevel);
    }
  }

  /**
   * A set of all the source tasks in the graph. A source task is one that has no incoming edges.
   *
   * @return A set of all the source tasks in the graph
   */
  public Set<Task> getSourceTasks() {
    return this.tasks.stream()
        .filter(Task::isSource)
        .collect(TaskSet.collect(this));
  }

}
