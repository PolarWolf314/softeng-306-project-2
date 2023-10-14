package nz.ac.auckland.se306.group12.models;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class DfsWorker {

  private final Queue<Schedule> queue = Collections.asLifoQueue(new ArrayDeque<>());
  @Getter
  private boolean hasWork = false;

  public void give(Schedule schedule) {
    this.queue.add(schedule);
    this.hasWork = true;
  }

  public Schedule steal() {
    if (queue.size() == 1) {
      this.hasWork = false;
    }
    return this.queue.remove();
  }
  
}
