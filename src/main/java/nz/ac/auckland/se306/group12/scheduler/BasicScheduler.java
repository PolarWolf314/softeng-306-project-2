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
    // Step 3: Find the processor with the earliest finish time
    // Step 4: Find the finish time of the parent processor
    // Step 5: Find out the lower of these two -> Step 4 + Communication Time or Step 5
    // Step 6: Assign the task to the processor with the lower finish time

    List<Processor> processors = new ArrayList<>();

    for (int i = 0; i < numberOfProcessors; i++) {
      processors.add(new Processor(new ArrayList<>()));
    }

    for (Node task : tasks) {
      // find the parent tasks of the current task
      List<Node> parentTasks = task.getOutgoingEdges().stream()
          .map(Edge::getDestination)
          .toList();

      // if there are no parents, add to the cheapest processor
      if (parentTasks.isEmpty()) {
        int smallestCumulativeStartTime = Integer.MAX_VALUE;
        Processor cheapestProcessor = processors.get(0);
        for (Processor processor : processors) {
          if (processor.getCumulativeStartTime() < smallestCumulativeStartTime) {
            smallestCumulativeStartTime = processor.getCumulativeStartTime();
            cheapestProcessor = processor;
          }
        }
        cheapestProcessor.addTask(task);
        // set the start time of the task to the cumulative start time of the processor
        task.setStartTime(cheapestProcessor.getCumulativeStartTime());
      }

      // otherwise, find the parent with the highest start time

    }
    return null;
  }

}
