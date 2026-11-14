package dev.voidfps.bench;

import java.lang.management.ManagementFactory;

/** JDK-only process metrics (no Minecraft classes). */
public final class SystemSampler {
    private SystemSampler() {}

    /** Heap currently in use by this JVM, in MiB. */
    public static long heapUsedMiB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    /** Recent CPU load of this JVM process, 0..100, or -1 if the JVM cannot report it. */
    public static double processCpuPercent() {
        try {
            var os = ManagementFactory.getOperatingSystemMXBean();
            if (os instanceof com.sun.management.OperatingSystemMXBean sun) {
                double load = sun.getProcessCpuLoad();
                if (Double.isNaN(load) || load < 0) return -1;
                return load * 100.0;
            }
        } catch (Throwable ignored) {
            // metric is optional
        }
        return -1;
    }
}
