package nz.ac.auckland.se306.group12.scheduler;

import java.util.List;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Processor;

public interface Scheduler {

  /**
   * Finds a valid schedule for the given graph of tasks on the specified number of processors. A
   * valid schedule will ensure that the start time of any task is after the end time of all it's
   * dependences and that, if scheduled on a different processor, after the end time + transfer time
   * of its dependence. No two tasks can be scheduled on the same processor at the same time.
   *
   * @param graph          The graph representing the tasks to be scheduled
   * @param processorCount The number of processors to schedule the tasks on
   * @return A valid schedule for the given graph of tasks on the processors
   */
  List<Processor> schedule(Graph graph, int processorCount);
}
