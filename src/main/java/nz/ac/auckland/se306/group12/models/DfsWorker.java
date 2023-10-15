package nz.ac.auckland.se306.group12.models;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DfsWorker {

  private final Deque<Schedule> stack = new LinkedBlockingDeque<>();

  public boolean hasWork() {
    return !stack.isEmpty();
  }

  public void give(Schedule schedule) {
    this.stack.push(schedule);
  }

  public Schedule steal() {
    return this.stack.pollFirst();
  }

}
