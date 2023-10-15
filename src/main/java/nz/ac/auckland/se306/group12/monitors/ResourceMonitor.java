package nz.ac.auckland.se306.group12.monitors;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import oshi.SystemInfo;

public class ResourceMonitor {

  private static final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

  /**
   * Gets the CPU load per processor core as an array of doubles.
   *
   * <h2>Quirk</h2>
   * This method doesn't work properly when the delay is less than 1000 :/ ^Aaron
   * <p>
   * Unreliable behaviour may be system-dependent. For me it fails below 300ms. ^Jasper
   *
   * @return An array of CPU loads as a double between 0 and 1
   */
  public static double[] getProcessorCpuLoad() {
    // Delay is in milliseconds
    return new SystemInfo().getHardware().getProcessor().getProcessorCpuLoad(310);
  }

  /**
   * Gets the current CPU load of the entire system
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

  public static long getMemoryUsageMiB() {
    final Runtime runtime = Runtime.getRuntime();
    return (runtime.maxMemory() - runtime.freeMemory()) >> 20; // 2^20 B in a MiB
  }

  /**
   * Get maximum memory available
   *
   * @return memory usage in KB
   */
  public static double getMaxMemory() {
    return (double) Runtime.getRuntime().maxMemory() / 1024;
  }

  public static long getMaxMemoryMiB() {
    return Runtime.getRuntime().maxMemory() >> 20; // 2^20 B in a MiB
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
