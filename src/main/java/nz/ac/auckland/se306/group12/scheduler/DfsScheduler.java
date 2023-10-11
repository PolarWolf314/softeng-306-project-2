package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsScheduler implements Scheduler {

  private final AtomicLong searchedCount = new AtomicLong(0);
  private final AtomicLong prunedCount = new AtomicLong(0);
  private final AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Set<Thread> threads = ConcurrentHashMap.newKeySet();
  private AtomicInteger threadCount = new AtomicInteger(1);
  private int threadNum = 1;


  @Override
  public long getSearchedCount() {
    return this.searchedCount.get();
  }

  @Override
  public long getPrunedCount() {
    return this.prunedCount.get();
  }

  @Override
  public Schedule getBestSchedule() {
    return this.bestSchedule.get();
  }


  /*
   * @inheritDoc
   */
  @Override
  public Schedule schedule(Graph taskGraph, int processorCount) {
    this.status = SchedulerStatus.SCHEDULING;
    this.threadNum = 4;

    Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
    queue.add(new Schedule(taskGraph, processorCount));

    branchAndBound(taskGraph, queue);

    for (Thread thread : this.threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule.get();
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
        this.bestSchedule.set(currentSchedule);
        continue;
      }

      for (Task task : currentSchedule.getReadyTasks()) {
        int[] latestStartTimes = currentSchedule.getLatestStartTimesOf(task);
        for (int i = 0; i < latestStartTimes.length; i++) {
          // Ensure that it either schedules by latest time or after the last task on the processor
          int startTime = Math.max(latestStartTimes[i], currentSchedule.getProcessorEndTimes()[i]);
          int endTime = startTime + task.getWeight();
          ScheduledTask newScheduledTask = new ScheduledTask(startTime, endTime, i);

          if (this.threadCount.getAndIncrement() < this.threadNum) {
            Thread thread = new Thread(() -> {
              Queue<Schedule> currentQueue = Collections.asLifoQueue(new ArrayDeque<>());
              currentQueue.add(currentSchedule.extendWithTask(newScheduledTask, task));
              branchAndBound(taskGraph, currentQueue);
            });
            this.threads.add(thread);
            thread.start();
          } else {
            queue.add(currentSchedule.extendWithTask(newScheduledTask, task));
          }

        }
      }
    }
  }


}
