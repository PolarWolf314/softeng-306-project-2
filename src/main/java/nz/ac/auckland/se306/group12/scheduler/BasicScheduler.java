package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Processor;
import nz.ac.auckland.se306.group12.models.Task;

public class BasicScheduler implements Scheduler {

  private final TopologicalSorter topologicalSorter = new TopologicalSorter();

  /**
   * @inheritDoc
   */
  @Override
  public List<Processor> schedule(Graph graph, int processorCount) {
    final List<Task> tasks = this.topologicalSorter.getATopologicalOrder(graph);
    List<Processor> processors = new ArrayList<>(processorCount);

    for (int processorIndex = 0; processorIndex < processorCount; processorIndex++) {
      processors.add(new Processor(processorIndex));
    }

    for (Task task : tasks) {
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
   * in the transfer time if the task has dependences and is scheduled on a different processor to
   * them.
   *
   * @param task       The task to schedule
   * @param processors The list of processors to schedule on
   */
  private void scheduleTaskOnEarliestProcessor(Task task, List<Processor> processors) {
    Set<Edge> incomingEdges = task.getIncomingEdges();

    // This is just a default value so that IntelliJ doesn't complain about potential null pointers.
    Processor earliestProcessor = processors.get(0);
    int earliestStartTime = Integer.MAX_VALUE;

    if (incomingEdges.isEmpty()) {
      earliestProcessor = this.getProcessorWithEarliestEndTime(processors);
      earliestStartTime = earliestProcessor.getEndTime();
    } else {
      TransferTimeFactoredStartTimes transferTimeFactoredStartTimes = this.determineTransferTimeFactoredStartTime(
          incomingEdges, processors);

      for (Processor processor : processors) {
        int earliestPossibleStartTime = this.determineEarliestPossibleStartTime(
            processor, transferTimeFactoredStartTimes);

        if (earliestPossibleStartTime < earliestStartTime) {
          earliestStartTime = earliestPossibleStartTime;
          earliestProcessor = processor;
        }
      }
    }

    task.setStartTime(earliestStartTime);
    earliestProcessor.addTask(task);
  }

  /**
   * Determines the earliest possible start time for a task on the given processor. If it's
   * scheduled on the same processor as the latest dependence, we don't need to consider the latest
   * transfer time, and so we use the second-latest transfer factored start time. The processor end
   * time will always be >= the latest dependence on that processor, so we don't need to worry about
   * disregarding the end time of the latest task.
   *
   * @param processor                      The processor to schedule the task on
   * @param transferTimeFactoredStartTimes The transfer time factored start times for the task
   * @return The earliest possible start time for the task on the given processor
   */
  private int determineEarliestPossibleStartTime(
      Processor processor,
      TransferTimeFactoredStartTimes transferTimeFactoredStartTimes
  ) {
    int processorEndTime = processor.getEndTime();

    int dependentStartTime =
        processor.getProcessorIndex() == transferTimeFactoredStartTimes.latestProcessorIndex()
            ? transferTimeFactoredStartTimes.secondLatestStartTime()
            : transferTimeFactoredStartTimes.latestStartTime();

    return Math.max(processorEndTime, dependentStartTime);
  }

  /**
   * Determines the transfer time factored start times for the given incoming edges and processors.
   * We only need to find the latest and second-latest start times while factoring in the transfer
   * time as if the task is scheduled on the same processor as the latest start time we switch to
   * the second-latest start time (As the transfer time doesn't apply when scheduled on the same
   * processor). In all other cases, we use the latest start time. Note this works because we ensure
   * these two times are on different processors. If there's only one parent then
   * {@link Integer#MIN_VALUE} is returned for the second-latest start time. This is fine as we'll
   * always take the max of this value and the processor end time, which will always be > than
   * this.
   *
   * @param incomingEdges The edges connecting the dependences of the task
   * @param processors    The list of processors that can be scheduled on
   * @return The transfer time factored latest and second-latest start times
   * @throws IllegalArgumentException If the task has no dependences
   */
  public TransferTimeFactoredStartTimes determineTransferTimeFactoredStartTime(
      Set<Edge> incomingEdges,
      List<Processor> processors
  ) {
    int secondLatestStartTime = 0;
    Processor latestProcessor = null;
    int latestStartTime = Integer.MIN_VALUE;

    // Determine the latest and second-latest dependences assuming the task is scheduled on a different
    // processor and therefore, the transfer time must be considered. The latest dependence is
    // also stored so that we can determine which processor it is scheduled on.
    for (Edge incomingEdge : incomingEdges) {
      Task dependence = incomingEdge.getSource();
      Processor dependenceProcessor = this.getParentProcessor(dependence, processors);

      int dependentStartTime = dependence.getEndTime() + incomingEdge.getWeight();

      if (dependentStartTime > latestStartTime) {
        if (!dependenceProcessor.equals(latestProcessor)) {
          secondLatestStartTime = latestStartTime;
        }

        latestProcessor = dependenceProcessor;
        latestStartTime = dependentStartTime;
      } else if (!dependenceProcessor.equals(latestProcessor)
          && dependentStartTime > secondLatestStartTime) {
        secondLatestStartTime = dependentStartTime;
      }
    }

    if (latestProcessor == null) {
      throw new IllegalArgumentException("The task must have at least one dependence");
    }

    return new TransferTimeFactoredStartTimes(
        latestProcessor.getProcessorIndex(), latestStartTime, secondLatestStartTime);
  }

  /**
   * Returns the parent processor of the given parent task.
   *
   * @param parentTask The parent task to find the parent processor of
   * @param processors The list of processors to search through
   * @return The parent processor of the given parent task
   */
  public Processor getParentProcessor(Task parentTask, List<Processor> processors) {
    for (Processor processor : processors) {
      if (processor.getScheduledTasks().contains(parentTask)) {
        return processor;
      }
    }
    return null;
  }

  private record TransferTimeFactoredStartTimes(int latestProcessorIndex,
                                                int latestStartTime,
                                                int secondLatestStartTime) {

  }

}
