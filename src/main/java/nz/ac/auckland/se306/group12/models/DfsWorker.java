package nz.ac.auckland.se306.group12.models;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class DfsWorker {

  private final Deque<Schedule> stack = new ArrayDeque<>();

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
