package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.exceptions.IllegalGraphException;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Task;

public class TopologicalSorter {

  /**
   * Uses finishing times from DFS to obtain <em>a</em> valid topological order, given a directed
   * acyclic graph.
   * <p>
   * Although a DAG may have multiple valid topological orderings, the behaviour of this
   * implementation is intentionally deterministic to aid unit testing. Given the same graph, it
   * will return the same topological order.
   *
   * @param graph The dependence graph of tasks (a DAG) for which a topological order is to be
   *              found.
   * @return A list of the {@link Task}s from the input graph, in a topological order.
   * @throws IllegalGraphException If given a cyclic digraph.
   */
  public List<Task> getATopologicalOrder(Graph graph) {
    List<Task> topologicalOrder = this.getAReverseTopologicalOrder(graph);
    Collections.reverse(topologicalOrder);
    return topologicalOrder;
  }

  /**
   * Computes a reverse topological order of tasks in a directed acyclic graph (DAG).
   * <p>
   * This method finds a reverse topological order of tasks in the given dependence graph, which is
   * assumed to be a directed acyclic graph (DAG). A reverse topological order represents an
   * ordering of tasks such that for every directed edge (u, v), task 'u' appears before task 'v' in
   * the order.
   *
   * @param graph The dependence graph of tasks (a DAG) for which a topological order is to be
   *              found.
   * @return A list of the {@link Task}s from the input graph, in a topological order.
   * @throws IllegalGraphException If given a cyclic digraph.
   */
  public List<Task> getAReverseTopologicalOrder(Graph graph) {
    Set<Task> visited = new HashSet<>(graph.taskCount());
    List<Task> list = new ArrayList<>(graph.taskCount());

    // Iterate through all the nodes in the graph and call the recursive helper function
    for (Task task : graph.getTasks()) {
      if (!visited.contains(task)) {
        topologicalSortUtil(task, visited, list);
      }
    }

    return list;
  }


  /**
   * Recursive helper function for {@link #getATopologicalOrder(Graph)}.
   * <p>
   * This ensures that the children of the input node are added to the topological list before the
   * node itself.
   *
   * @param task    The node to be added to the topological list.
   * @param visited A set of nodes that have already been visited.
   * @param list    The list of tasks in a topological order.
   */
  private void topologicalSortUtil(Task task, Set<Task> visited, List<Task> list) {
    visited.add(task);
    // Recursively call this function for all the children that haven't been visited yet
    for (Edge edge : task.getOutgoingEdges()) {
      Task destination = edge.getDestination();
      if (!visited.contains(destination)) {
        topologicalSortUtil(destination, visited, list);
      }
    }
    list.add(task);
  }

}
