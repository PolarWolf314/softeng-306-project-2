package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduleWithAnEmptyProcessor;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsScheduler implements Scheduler {

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
    this.status = SchedulerStatus.SCHEDULING;
    Set<String> closed = new HashSet<>();
    Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());

    queue.add(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));

    // DFS iteration (no optimisations)
    while (!queue.isEmpty()) {
      Schedule currentSchedule = queue.remove();

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
        for (int i = 0; i < currentSchedule.getLoopCount(); i++) {
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

          String stringHash = newSchedule.generateStringHash();

          if (!closed.contains(stringHash)) {
            queue.add(newSchedule);
            closed.add(stringHash);
          } else {
            this.prunedCount++;
          }
        }
      }
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule;
  }

}
