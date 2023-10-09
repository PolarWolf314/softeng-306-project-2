package nz.ac.auckland.se306.group12.models;

public enum SchedulerStatus {

  /**
   * The scheduler is currently idle and has not started scheduling.
   */
  IDLE,

  /**
   * The scheduler is currently running and is in the process of finding a valid schedule.
   */
  SCHEDULING,

  /**
   * The scheduler has finished scheduling and has found a valid schedule.
   */
  SCHEDULED

}
