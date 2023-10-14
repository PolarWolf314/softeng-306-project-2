package nz.ac.auckland.se306.group12.scheduler;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;
import nz.ac.auckland.se306.group12.models.ScheduleWithAnEmptyProcessor;
import nz.ac.auckland.se306.group12.models.ScheduledTask;
import nz.ac.auckland.se306.group12.models.SchedulerStatus;
import nz.ac.auckland.se306.group12.models.Task;

public class DfsScheduler implements Scheduler {

  private final AtomicReference<Schedule> bestSchedule = new AtomicReference<>();
  private AtomicLong searchedCount = new AtomicLong(0);
  private AtomicLong prunedCount = new AtomicLong(0);
  private AtomicInteger currentMinMakespan = new AtomicInteger(Integer.MAX_VALUE);
  @Getter
  private SchedulerStatus status = SchedulerStatus.IDLE;
  private Set<Thread> threads = ConcurrentHashMap.newKeySet();
  private AtomicInteger threadCount = new AtomicInteger(1);
  private int syncThreshold = 4;
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
    this.threadNum = 1;

    Set<String> closed = new HashSet<>();
    Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
    queue.add(new ScheduleWithAnEmptyProcessor(taskGraph, processorCount));

    branchAndBound(taskGraph, queue, closed);

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

  /**
   * Performs branch and bound algorithm on a given graph.
   *
   * @param taskGraph graph to perform branch and bound on
   * @param queue     current queue of the branch and bound instance
   */
  private void branchAndBound(Graph taskGraph, Queue<Schedule> queue,
      Set<String> closed) {
    // DFS iteration (no optimisations)
    int syncCounter = 0;
    int localMinMakespan = this.currentMinMakespan.get();
    ;
    while (!queue.isEmpty()) {
      Schedule currentSchedule = queue.remove();
      syncCounter++;

      if (syncCounter == this.syncThreshold) {
        localMinMakespan = this.currentMinMakespan.get();
        syncCounter = 0;
      }

      // Prune if current schedule is worse than current best
      if (currentSchedule.getEstimatedMakespan() >= localMinMakespan) {
        this.prunedCount.incrementAndGet();
        continue;
      }

      this.searchedCount.incrementAndGet();

      // Check if current schedule is complete
      if (currentSchedule.getScheduledTaskCount() == taskGraph.taskCount()) {
        localMinMakespan = currentSchedule.getLatestEndTime();
        if (localMinMakespan < this.currentMinMakespan.get()) {
          this.currentMinMakespan = new AtomicInteger(localMinMakespan);
        }
        this.bestSchedule.set(currentSchedule);
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
          if (newSchedule.getEstimatedMakespan() >= localMinMakespan) {
            this.prunedCount.incrementAndGet();
            continue;
          }

          String stringHash = newSchedule.generateUniqueString();

          if (closed.contains(stringHash)) {
            this.prunedCount.incrementAndGet();
            continue;
          }

          int currentThread = this.threadCount.getAndIncrement();
          if (currentThread < this.threadNum) {
            Thread thread = new Thread(() -> {
              Queue<Schedule> currentQueue = Collections.asLifoQueue(new ArrayDeque<>());
              currentQueue.add(newSchedule);
              closed.add(stringHash);
              branchAndBound(taskGraph, currentQueue, closed);
            });
            this.threads.add(thread);
            thread.start();
          } else {
            this.threadCount.decrementAndGet();
            queue.add(newSchedule);
            closed.add(stringHash);
          }

        }
      }
    }

  }


}
