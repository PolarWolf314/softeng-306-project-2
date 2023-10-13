package nz.ac.auckland.se306.group12.monitors;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class SystemInfo {

  private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

  /**
   * Get current CPU load
   *
   * @return CPU load as a double between 0 and 1
   */
  public static double getCPULoad() {
    return osBean.getCpuLoad();
  }

  /**
   * Get current memory usage
   *
   * @return memory usage in KB
   */
  public static double getMemoryUsage() {
    return (double) (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1024;
  }
}