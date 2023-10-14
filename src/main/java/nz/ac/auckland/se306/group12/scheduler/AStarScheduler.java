package nz.ac.auckland.se306.group12.scheduler;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduleWithAnEmptyProcessor;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

@Getter
public class AStarScheduler implements Scheduler {

  private long searchedCount;
  private long prunedCount;
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Queue<Schedule> priorityQueue = new PriorityQueue<>();

  /**
   * The current best schedule is the first schedule in the priority queue as it is ordered by
   * lowest estimated makespan.
   *
   * @inheritDoc
   */
  @Override
  public Schedule getBestSchedule() {
    return this.priorityQueue.peek();
  }

  /**
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    Set<String> closed = new HashSet<>();
    this.priorityQueue.clear();
    this.status = SchedulerStatus.SCHEDULING;

    this.priorityQueue.add(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));

    while (!this.priorityQueue.isEmpty()) {
      Schedule currentSchedule = this.priorityQueue.peek();
      this.searchedCount++;

      // Check if current schedule is complete. The first complete schedule is the best schedule
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        this.status = SchedulerStatus.SCHEDULED;
        return currentSchedule;
      }

      // Only remove it now so that if we found the best schedule we can still access it through peeking
      this.priorityQueue.poll();

      // Check to find if any tasks can be scheduled and schedule them
      for (Task task : currentSchedule.getReadyTasks()) {
        int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
        for (int i = 0; i < currentSchedule.getAllocableProcessorCount(); i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          Schedule newSchedule = currentSchedule.extendWithTask(newScheduledTask, task);
          String stringHash = newSchedule.generateUniqueString();

          if (closed.contains(stringHash)) {
            this.prunedCount++;
          } else {
            this.priorityQueue.add(newSchedule);
            closed.add(stringHash);
          }
        }
      }
    }

    this.status = SchedulerStatus.SCHEDULED;
    throw new IllegalStateException("No optimal schedule found");
  }

}
