package nz.ac.auckland.se306.group12.scheduler;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

@Getter
public class AStarScheduler implements Scheduler {

  private long searchedCount;
  private long prunedCount;
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Queue<Schedule> priorityQueue = new PriorityQueue<>();

  @Override
  public Schedule getBestSchedule() {
    return this.priorityQueue.peek();
  }

  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    Set<String> closed = new HashSet<>();
    this.priorityQueue.clear();
    this.status = SchedulerStatus.SCHEDULING;

    this.priorityQueue.add(new Schedule(taskGraph, processorCount));

    // DFS iteration (no optimisations)
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
        for (int i = 0; i < latestStartTimes.length; i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);
          Schedule newSchedule = currentSchedule.extendWithTask(newScheduledTask, task);
          String stringHash = newSchedule.generateStringHash();

          if (!closed.contains(stringHash)) {
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
