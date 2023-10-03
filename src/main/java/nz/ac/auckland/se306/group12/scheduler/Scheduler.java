package nz.ac.auckland.se306.group12.scheduler;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;

public interface Scheduler {

  /**
   * Finds a valid schedule for the given graph of tasks on the specified number of processors. A
   * valid schedule will ensure that the start time of any task is after the end time of all it's
   * dependences and that, if scheduled on a different processor, after the end time + transfer time
   * of its dependence. No two tasks can be scheduled on the same processor at the same time.
   *
   * @param graph          The {@link Graph} representing the tasks to be scheduled
   * @param processorCount The number of processors to schedule the tasks on
   * @return A valid {@link Schedule} for the given graph of tasks on the processors
   */
  Schedule schedule(Graph graph, int processorCount);

}
