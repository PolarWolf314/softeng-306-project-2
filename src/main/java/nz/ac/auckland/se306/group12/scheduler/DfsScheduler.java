package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsScheduler implements Scheduler {

  private final AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
  @Getter
  private long searchedCount = 0;
  @Getter
  private long prunedCount = 0;
  private long[] searchCountArray;
  private long[] prunedCountArray;
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Map<Thread, Integer> threads = new ConcurrentHashMap<>();
  private AtomicInteger threadCount = new AtomicInteger(1);
  private int threadNum = 1;

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
    this.searchCountArray = new long[this.threadNum];
    this.prunedCountArray = new long[this.threadNum];

    Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
    queue.add(new Schedule(taskGraph, processorCount));

    branchAndBound(taskGraph, queue, 1);

    for (Thread thread : this.threads.keySet()) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    for (int i = 0; i < this.threadNum; i++) {
      this.prunedCount += this.prunedCountArray[i];
      this.searchedCount += searchCountArray[i];
    }

    this.status = SchedulerStatus.SCHEDULED;
    return this.bestSchedule.get();
  }

  /**
   * Performs branch and bound algorithm on a given graph.
   *
   * @param taskGraph graph to perform branch and bound on
   * @param queue     current queue of the branch and bound instance
   */
  private void branchAndBound(Graph taskGraph, Queue<Schedule> queue, int threadIndex) {
    // DFS iteration (no optimisations)
    while (!queue.isEmpty()) {
      Schedule currentSchedule = queue.remove();

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= this.currentMinMakespan.get()) {
        this.prunedCountArray[threadIndex]++;
        continue;
      }

      this.searchCountArray[threadIndex]++;

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

          int currentThread = this.threadCount.getAndIncrement();
          if (currentThread < this.threadNum) {
            Thread thread = new Thread(() -> {
              Queue<Schedule> currentQueue = Collections.asLifoQueue(new ArrayDeque<>());
              currentQueue.add(currentSchedule.extendWithTask(newScheduledTask, task));
              branchAndBound(taskGraph, currentQueue, currentThread);
            });
            this.threads.put(thread, currentThread);
            thread.start();
          } else {
            this.threadCount.decrementAndGet();
            queue.add(currentSchedule.extendWithTask(newScheduledTask, task));
          }

        }
      }
    }
  }


}
