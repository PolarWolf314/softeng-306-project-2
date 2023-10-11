package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;

public class DfsScheduler implements Scheduler {

  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);

  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  @Getter
  private Schedule bestSchedule = null;


  @Override
  public long getSearchedCount() {
    return this.searchedCount.getOpaque();
  }

  @Override
  public long getPrunedCount() {
    return this.prunedCount.getOpaque();
  }


  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.SCHEDULING;

    Set<Thread> threads = new HashSet<>();
    Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());

    queue.add(new Schedule(taskGraph, processorCount));

    branchAndBound(taskGraph, queue);

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule;
  }

  private void branchAndBound(Graph taskGraph, Queue<Schedule> queue) {
    // DFS iteration (no optimisations)
    while (!queue.isEmpty()) {
      Schedule currentSchedule = queue.remove();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan.get()) {
        this.prunedCount.incrementAndGet();
        continue;
      }

      this.searchedCount.incrementAndGet();

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        this.currentMinMakespan = new AtomicInteger(currentSchedule.getLatestEndTime());
        this.bestSchedule = currentSchedule;
        continue;
      }

      currentSchedule.extendSchedule(queue);
    }

  }

}
