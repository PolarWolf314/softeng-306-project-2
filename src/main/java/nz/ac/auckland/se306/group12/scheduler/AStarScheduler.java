package nz.ac.auckland.se306.group12.scheduler;

import lombok.Getter;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Schedule;

@Getter
public class AStarScheduler implements Scheduler {

  private long searchedCount;
  private long prunedCount;
  private Schedule bestSchedule;

  @Override
  public Schedule schedule(Graph graph, int processorCount) {
    // TODO Auto-generated method stub
    System.out.println("A* Scheduler not implemented");
    return null;
  }

}
