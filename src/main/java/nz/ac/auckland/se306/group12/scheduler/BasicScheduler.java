package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.Processor;

public class BasicScheduler {

  private final TopologicalSorter topologicalSorter = new TopologicalSorter();

  /**
   * Returns a basic schedule for the given graph of tasks.
   * <p>
   *
   * @param graph          The graph representing the tasks to be scheduled
   * @param processorCount The number of processors to schedule the tasks on
   * @return A basic schedule for the given list of tasks
   */
  public List<Processor> getABasicSchedule(Graph graph, int processorCount) {
    final List<Node> tasks = this.topologicalSorter.getATopologicalOrder(graph);
    List<Processor> processors = new ArrayList<>();

    for (int processorIndex = 0; processorIndex < processorCount; processorIndex++) {
      processors.add(new Processor(processorIndex));
    }

    for (Node task : tasks) {
      this.scheduleTaskOnEarliestProcessor(task, processors);
    }
    return processors;
  }

  /**
   * Returns the processor with the earliest end time.
   *
   * @param processors The list of processors to search through
   * @return The processor with the earliest end time
   */
  public Processor getProcessorWithEarliestEndTime(List<Processor> processors) {
    Processor shortestProcessor = processors.get(0);

    for (int processorIndex = 1; processorIndex < processors.size(); processorIndex++) {
      Processor processor = processors.get(processorIndex);
      if (processor.getEndTime() < shortestProcessor.getEndTime()) {
        shortestProcessor = processor;
      }
    }

    return shortestProcessor;
  }

  /**
   * Schedules the given task on the processor that gives it the earliest start time. This factors
   * in the intercommunication cost if the task has dependencies and is scheduled on a different
   * processor to them.
   *
   * @param task       The task to schedule
   * @param processors The list of processors to schedule on
   */
  private void scheduleTaskOnEarliestProcessor(Node task, List<Processor> processors) {
    Set<Edge> incomingEdges = task.getIncomingEdges();

    Processor earliestProcessor = processors.get(0);
    int earliestStartTime = Integer.MAX_VALUE;

    if (incomingEdges.isEmpty()) {
      earliestProcessor = this.getProcessorWithEarliestEndTime(processors);
      earliestStartTime = earliestProcessor.getEndTime();
    } else {
      IntercommunicationCosts communicationCosts = this.determineIntercommunicationCosts(
          incomingEdges, processors);

      for (int processorIndex = 0; processorIndex < processors.size(); processorIndex++) {
        int processorEndTime = processors.get(processorIndex).getEndTime();

        // If it's scheduled on the same processor as the latest dependency, we don't need to consider
        // the intercommunication cost, and so we use the second-latest start time. The
        // processor end time will always be >= the latest dependency on that processor.
        int dependentStartTime = processorIndex == communicationCosts.latestProcessorIndex()
            ? communicationCosts.secondLatestStartTime() : communicationCosts.latestStartTime();

        int earliestProcessorStartTime = Math.max(processorEndTime, dependentStartTime);
        if (earliestProcessorStartTime < earliestStartTime) {
          earliestStartTime = earliestProcessorStartTime;
          earliestProcessor = processors.get(processorIndex);
        }
      }
    }

    task.setStartTime(earliestStartTime);
    earliestProcessor.addTask(task);
  }

  /**
   * Determines the intercommunication costs for the given incoming edges and processors. We only
   * need to find the latest and second-latest start times while factoring in the communication cost
   * as if the task is scheduled on the same processor as the latest start time we switch to the
   * second-latest start time (As the intercommunication cost doesn't apply when scheduled on the
   * same processor). In all other cases, we use the latest start time.
   *
   * @param incomingEdges The edges connecting the dependencies of the task
   * @param processors    The list of processors that can be scheduled on
   * @return The intercommunication-factored latest and second-latest start times
   */
  public IntercommunicationCosts determineIntercommunicationCosts(
      Set<Edge> incomingEdges,
      List<Processor> processors
  ) {
    int secondLatestStartTime = Integer.MIN_VALUE;
    Node latestDependency = null;
    int latestStartTime = Integer.MIN_VALUE;

    // Determine the latest and second-latest dependencies assuming the task is scheduled on a different
    // processor and therefore, the communication cost must be considered. The latest dependency is
    // also stored so that we can determine which processor it is scheduled on.
    for (Edge incomingEdge : incomingEdges) {
      final Node dependency = incomingEdge.getSource();
      int dependentStartTime = dependency.getEndTime() + incomingEdge.getWeight();

      if (dependentStartTime > latestStartTime) {
        secondLatestStartTime = latestStartTime;
        latestDependency = dependency;
        latestStartTime = dependentStartTime;
      } else if (dependentStartTime > secondLatestStartTime) {
        secondLatestStartTime = dependentStartTime;
      }
    }

    int latestProcessorIndex = this.getParentProcessor(latestDependency, processors)
        .getProcessorIndex();

    return new IntercommunicationCosts(
        latestProcessorIndex, latestStartTime, secondLatestStartTime);
  }

  /**
   * Returns the parent processor of the given parent task.
   *
   * @param parentTask The parent task to find the parent processor of
   * @param processors The list of processors to search through
   * @return The parent processor of the given parent task
   */
  public Processor getParentProcessor(Node parentTask, List<Processor> processors) {
    for (Processor processor : processors) {
      if (processor.getScheduledTasks().contains(parentTask)) {
        return processor;
      }
    }
    return null;
  }

  private record IntercommunicationCosts(int latestProcessorIndex,
                                         int latestStartTime,
                                         int secondLatestStartTime) {

  }

}
