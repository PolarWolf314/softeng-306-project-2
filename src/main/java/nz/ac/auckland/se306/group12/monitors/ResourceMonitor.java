package nz.ac.auckland.se306.group12.monitors;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ResourceMonitor {

  private static final OperatingSystemMXBean OS_BEAN = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  private static final CentralProcessor PROCESSOR = new SystemInfo().getHardware().getProcessor();
  private static final Runtime RUNTIME = Runtime.getRuntime();

  /**
   * Get the number of logical CPUs available for processing. This value may be higher than physical
   * CPUs if hyperthreading is enabled.
   * <p>
   * On some operating systems with variable numbers of logical processors, may return a max value.
   *
   * @return The number of logical CPUs available.
   */
  public static int getLogicalProcessorCount() {
    return PROCESSOR.getLogicalProcessorCount();
  }

  /**
   * Get the number of physical CPUs/cores available for processing.
   * <p>
   * On some operating systems with variable numbers of physical processors available to the OS, may
   * return a max value.
   *
   * @return The number of physical CPUs available.
   */
  public static int getPhysicalProcessorCount() {
    return PROCESSOR.getPhysicalProcessorCount();
  }

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
    return PROCESSOR.getProcessorCpuLoad(310);
  }

  /**
   * Gets the "recent" CPU usage of the environment (across all threads/cores).
   *
   * @return CPU load as a double between 0.0 and 1.0 (inclusive).
   */
  public static double getSystemCpuLoad() {
    return OS_BEAN.getCpuLoad();
  }

  /**
   * @return The current memory usage in MiB.
   */
  public static long getMemoryUsageMiB() {
    return (RUNTIME.maxMemory() - RUNTIME.freeMemory()) >> 20; // 2^20 B in a MiB
  }

  /**
   * @return The maximum available memory in MiB.
   */
  public static long getMaxMemoryMiB() {
    return RUNTIME.maxMemory() >> 20; // 2^20 B in a MiB
  }

}
