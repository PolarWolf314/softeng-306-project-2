package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import nz.ac.auckland.se306.group12.models.Edge;
import nz.ac.auckland.se306.group12.models.Node;
import nz.ac.auckland.se306.group12.models.Processor;

public class BasicScheduler {

  /**
   * Returns a basic schedule for the given list of tasks.
   *
   * @param tasks              The list of tasks to schedule in topological order
   * @param numberOfProcessors The number of processors to schedule the tasks on
   * @return A basic schedule for the given list of tasks
   */
  public List<Processor> getABasicSchedule(List<Node> tasks, int numberOfProcessors) {
    // Step 1: Create a list of processors
    // Step 2: Find the parent task of the current task with the highest finish time
    // Step 3: Find the processor with the earliest finish time.
    // Step 4: Find the finish time of the parent processor
    // Step 5: Find out the lower of these two ->
    //      Step 3 (or parent task finish time, whichever is higher) + Communication Time or Step 4
    // Step 6: Assign the task to the processor with the lower finish time

    List<Processor> processors = new ArrayList<>();

    for (int i = 0; i < numberOfProcessors; i++) {
      processors.add(new Processor(i));
    }

    for (Node task : tasks) {
      // find the parent tasks of the current task
      List<Edge> parentEdges = task.getIncomingEdges().stream().toList();

      // if there are no parents, add to the cheapest processor
      if (parentEdges.isEmpty()) {
        Processor shortestProcessor = this.getProcessorWithEarliestEndTime(processors);
        task.setStartTime(shortestProcessor.getEndTime());
        shortestProcessor.addTask(task);
        continue;
      }

      Edge parentEdge = this.getParentWithHighestFinishTime(parentEdges);

      // find the processor with the lowest cumulative start time
      Processor cheapestProcessor = this.getProcessorWithEarliestEndTime(processors);

      // find the parent processor of the parent task
      Processor parentProcessor = this.getParentProcessor(parentEdge.getSource(), processors);

      // find the finish time of the parent processor
      int parentProcessorFinishTime = parentProcessor.getEndTime();

      // find the finish time of the parent task
      int parentTaskFinishTime =
          parentEdge.getSource().getStartTime() + parentEdge.getSource().getWeight();

      // find the communication cost
      int communicationCost = parentEdge.getWeight();

      if (cheapestProcessor.getEndTime() >= parentTaskFinishTime) {
        if (cheapestProcessor.getEndTime() + communicationCost < parentProcessorFinishTime) {
          task.setStartTime(cheapestProcessor.getEndTime() + communicationCost);
          cheapestProcessor.addTask(task);
        } else {
          task.setStartTime(parentTaskFinishTime);
          parentProcessor.addTask(task);
        }
      } else {
        if (parentTaskFinishTime + communicationCost < parentProcessorFinishTime) {
          task.setStartTime(parentTaskFinishTime + communicationCost);
          cheapestProcessor.addTask(task);
        } else {
          task.setStartTime(parentTaskFinishTime);
          parentProcessor.addTask(task);
        }
      }
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

  private void scheduleTaskOnEarliestProcessor(Node task, List<Processor> processors) {
    int secondLatestParentEndTime = Integer.MIN_VALUE;
    Node latestParent = null;
    int latestParentEndTime = Integer.MIN_VALUE;

    // Determine the latest and second-latest parents assuming the task is scheduled on a different
    // processor and therefore, the communication cost must be considered. The latest parent node is
    // also stored so that we can determine which processor it is scheduled on.
    for (Edge parentEdge : task.getIncomingEdges()) {
      final Node parent = parentEdge.getSource();
      int parentEndTime = parent.getEndTime() + parentEdge.getWeight();

      if (parentEndTime > latestParentEndTime) {
        secondLatestParentEndTime = latestParentEndTime;
        latestParent = parent;
        latestParentEndTime = parentEndTime;
      } else if (parentEndTime > secondLatestParentEndTime) {
        secondLatestParentEndTime = parentEndTime;
      }
    }

    int latestParentProcessorIndex = this.getParentProcessor(latestParent, processors)
        .getProcessorIndex();

    int earliestStartTime = Integer.MAX_VALUE;
    Processor earliestProcessor = processors.get(0);

    for (int processorIndex = 0; processorIndex < processors.size(); processorIndex++) {
      int processorEndTime = processors.get(processorIndex).getEndTime();

      // If it's scheduled on the same processor as the latest parent, we don't need to consider
      // the intercommunication cost, and so we use the second-latest parent end time. The
      // processor end time will always be >= the latest parent on that processor.
      int parentEndTime = processorIndex == latestParentProcessorIndex
          ? secondLatestParentEndTime : latestParentEndTime;

      int earliestProcessorStartTime = Math.max(processorEndTime, parentEndTime);
      if (earliestProcessorStartTime < earliestStartTime) {
        earliestStartTime = earliestProcessorStartTime;
        earliestProcessor = processors.get(processorIndex);
      }
    }

    task.setStartTime(earliestStartTime);
    earliestProcessor.addTask(task);
  }

  /**
   * Returns the parent task with the highest finish time.
   *
   * @param parentEdges the list of parent edges to search through
   * @return The parent task with the highest finish time
   */
  public Edge getParentWithHighestFinishTime(List<Edge> parentEdges) {
    int highestFinishTime = Integer.MIN_VALUE;
    Edge parentEdgeWithHighestFinishTime = parentEdges.get(0);
    for (Edge parentEdge : parentEdges) {
      if (parentEdge.getSource().getStartTime() + parentEdge.getSource().getWeight()
          > highestFinishTime) {
        highestFinishTime = parentEdge.getSource().getStartTime()
            + parentEdge.getSource().getWeight();
        parentEdgeWithHighestFinishTime = parentEdge;
      }
    }
    return parentEdgeWithHighestFinishTime;
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

}
