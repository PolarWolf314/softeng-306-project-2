package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayList;
import java.util.List;
import nz.ac.auckland.se306.group12.models.ScheduledTask;

public class BasicScheduler {

  /**
   * Returns a basic schedule for the given list of tasks.
   *
   * @param tasks              The list of tasks to schedule in topological order
   * @param numberOfProcessors The number of processors to schedule the tasks on
   * @return A basic schedule for the given list of tasks
   */
  public List<Process> getABasicSchedule(List<ScheduledTask> tasks, int numberOfProcessors) {
    // Step 1: Create a list of processors
    // Step 2: Find the parent task of the current task with the highest finish time
    // Step 3: Find the processor with the earliest finish time
    // Step 4: Find the finish time of the parent processor
    // Step 5: Find out the lower of these two -> Step 4 + Communication Time or Step 5
    // Step 6: Assign the task to the processor with the lower finish time

    List<Process> processors = new ArrayList<>(numberOfProcessors);
    return null;
  }

}
