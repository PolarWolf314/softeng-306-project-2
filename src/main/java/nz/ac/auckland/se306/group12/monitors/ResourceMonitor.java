package nz.ac.auckland.se306.group12.monitors;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import oshi.SystemInfo;

public class ResourceMonitor {

  private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

  /**
   * Gets the CPU load per processor core as an array of doubles
   *
   * @return An array of CPU loads as a double between 0 and 1
   */
  public static double[] getProcessorCpuLoad() {
    SystemInfo systemInfo = new SystemInfo();
    // the delay is in milliseconds. This method doesn't work properly when the delay is less than 1000 :/
    return systemInfo.getHardware().getProcessor().getProcessorCpuLoad(1000);
  }

  /**
   * Get current CPU load
   *
   * @return CPU load as a double between 0 and 1
   */
  public static double getSystemCpuLoad() {
    return osBean.getCpuLoad();
  }

  /**
   * Get current memory usage
   *
   * @return memory usage in KB
   */
  public static double getMemoryUsage() {
    return getMaxMemory() - getFreeMemory();
  }

  /**
   * Get maximum memory available
   *
   * @return memory usage in KB
   */
  public static double getMaxMemory() {
    return (double) Runtime.getRuntime().maxMemory() / 1024;
  }

  /**
   * Get current available memory
   *
   * @return memory usage in KB
   */
  public static double getFreeMemory() {
    return (double) Runtime.getRuntime().freeMemory() / 1024;
  }

}

