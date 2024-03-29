package nz.ac.auckland.se306.group12.scheduler;

import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;

public interface Scheduler {

  /**
   * We are going to be storing a lot of schedules in our closed set, so we want to initially create
   * it to be quite large so that we don't have to resize it too often. {@link java.util.HashMap}
   * requires this to be a power of two (Although it's not clear if this requirement also applied to
   * {@link java.util.LinkedHashMap}).
   * <p>
   * While the choice of number is slightly arbitrary, it is immensely more appropriate than the
   * default initial capacity of 16.
   */
  int INITIAL_CLOSED_SET_CAPACITY = 1 << 13; // 8192

  /**
   * Returns the number of partial schedules that have been searched so far by the scheduler. A
   * partial schedule is considered searched if it is not pruned and is therefore a potential
   * candidate for an optimal schedule.
   *
   * @return The number of searched partial schedules
   */
  long getSearchedCount();

  /**
   * Returns the number of partial schedules that have been pruned so far by the scheduler. A
   * partial schedule is considered pruned if it is not a potential candidate for a valid schedule
   * and is not explored any further.
   *
   * @return The number of pruned partial schedules
   */
  long getPrunedCount();

  /**
   * Returns the best schedule found so far by the scheduler. This can be null if no schedules have
   * been explored yet and so any usage of this method must check that the return value is not
   * null.
   *
   * @return The current best schedule found by the scheduler
   */
  Schedule getBestSchedule();

  /**
   * Returns the current status of the scheduler.
   *
   * @return The current status of the scheduler
   * @see SchedulerStatus
   */
  SchedulerStatus getStatus();

  /**
   * Finds a valid schedule for the given graph of tasks on the specified number of processors. A
   * valid schedule will ensure that the start time of any task is after the end time of all it's
   * dependences and that, if scheduled on a different processor, after the end time + transfer time
   * of its dependence. No two tasks can be scheduled on the same processor at the same time.
   *
   * @param taskGraph      The {@link Graph} representing the tasks to be scheduled
   * @param processorCount The number of processors to schedule the tasks on
   * @return A valid {@link Schedule} for the given graph of tasks on the processors
   */
  Schedule schedule(Graph taskGraph, int processorCount);

  /**
   * @return The full, human-readable name of this scheduler, in sentence case.
   */
  String getHumanReadableName();

}
