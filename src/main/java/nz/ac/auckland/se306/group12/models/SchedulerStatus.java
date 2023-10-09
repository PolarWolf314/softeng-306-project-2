package nz.ac.auckland.se306.group12.models;

public enum SchedulerStatus {

  /**
   * The scheduler is currently idle and has either not started scheduling or has finished.
   */
  IDLE,

  /**
   * The scheduler is currently running and is in the process of finding an optimal schedule.
   */
  RUNNING

}
