package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduleWithAnEmptyProcessor;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;
import nz.ac.auckland.se306.group12.models.datastructures.MaxSizeHashMap;

public class DfsScheduler implements Scheduler {

  /**
   * After a little bit of trial and error, this seems to be a decent balance between being able to
   * store a lot of schedules in the closed set and not running out of memory. This is subject to
   * change as we do more testing.
   */
  private static final int MAX_CLOSED_SET_SIZE = 1 << 18; // 262144

  private int currentMinMakespan = Integer.MAX_VALUE;
  @Getter
  private long searchedCount = 0;
  @Getter
  private long prunedCount = 0;
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;

  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.resetScheduler();
    this.status = SchedulerStatus.SCHEDULING;
    Deque<Schedule> stack = new ArrayDeque<>();
    Map<String, Boolean> closed = new MaxSizeHashMap<>(
        MAX_CLOSED_SET_SIZE, Scheduler.INITIAL_CLOSED_SET_CAPACITY);

    stack.add(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));

    while (!stack.isEmpty()) {
      Schedule currentSchedule = stack.pop();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan) {
        this.prunedCount++;
        continue;
      }

      this.searchedCount++;

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        this.currentMinMakespan = currentSchedule.getLatestEndTime();
        this.bestSchedule = currentSchedule;
        continue;
      }

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : currentSchedule.getReadyTasks()) {
        int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
        for (int i = 0; i < currentSchedule.getAllocableProcessors(); i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          Schedule newSchedule = currentSchedule.extendWithTask(newScheduledTask, task);

          // No need to add the schedule to the closed set at this point as if we find this schedule
          // again it'll get pruned at this point again anyway, which saves memory.
          if (newSchedule.getEstimatedMakespan() >= this.currentMinMakespan) {
            this.prunedCount++;
            continue;
          }

          String stringHash = newSchedule.generateUniqueString();

          if (closed.containsKey(stringHash)) {
            this.prunedCount++;
            continue;
          }

          stack.push(newSchedule);
          closed.put(stringHash, Boolean.TRUE);
        }
      }
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule;
  }

  /**
   * Resets the scheduler to its initial state so that it can be used to schedule a new graph.
   */
  private void resetScheduler() {
    this.searchedCount = 0;
    this.prunedCount = 0;
    this.bestSchedule = null;
    this.currentMinMakespan = Integer.MAX_VALUE;
  }

}
