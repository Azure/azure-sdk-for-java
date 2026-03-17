// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Monitors process CPU load and provides a cool-down mechanism to wait
 * for CPU to settle after intensive operations (e.g., bulk data ingestion).
 *
 * <p>Uses the JVM-internal {@code getProcessCpuLoad()} method
 * (available on HotSpot / OpenJDK) for per-process CPU readings.
 * If not available, cool-down is skipped gracefully.</p>
 *
 * <p>This is an internal utility — cool-down runs automatically with
 * hardcoded defaults and is not user-configurable.</p>
 */
final class CpuMonitor {

    private static final Logger logger = LoggerFactory.getLogger(CpuMonitor.class);

    // Cool-down defaults (internal, not user-configurable)
    private static final long MAX_WAIT_MS = 5 * 60 * 1_000; // 5 minutes
    private static final double CPU_THRESHOLD_DELTA = 0.10;   // baseline + 10%
    private static final long POLL_INTERVAL_MS = 1_000;       // 1 second

    private CpuMonitor() {
    }

    /**
     * Captures the current process CPU load as a value between 0.0 and 1.0.
     * Returns -1.0 if the metric is unavailable.
     */
    static double captureProcessCpuLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            double load = ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad();
            logger.info("[CpuMonitor] Current process CPU load: {}", String.format("%.2f%%", load * 100));
            return load;
        }
        logger.warn("[CpuMonitor] com.sun.management.OperatingSystemMXBean not available; CPU monitoring disabled");
        return -1.0;
    }

    /**
     * Waits until the process CPU load drops to within {@link #CPU_THRESHOLD_DELTA}
     * of the given {@code baselineCpu}, or until {@link #MAX_WAIT_MS} elapses.
     *
     * <p>If the baseline is unavailable (negative), this method returns immediately.</p>
     *
     * @param baselineCpu the CPU load captured before the intensive operation (0.0–1.0)
     */
    static void awaitCoolDown(double baselineCpu) {
        if (baselineCpu < 0) {
            logger.info("[CpuMonitor] Baseline CPU unavailable; skipping cool-down");
            return;
        }

        double target = baselineCpu + CPU_THRESHOLD_DELTA;
        logger.info("[CpuMonitor] Cool-down started — baseline={}, target<={}",
            String.format("%.2f%%", baselineCpu * 100),
            String.format("%.2f%%", target * 100));

        long startTime = System.currentTimeMillis();
        long deadline = startTime + MAX_WAIT_MS;
        int sampleCount = 0;

        while (System.currentTimeMillis() < deadline) {
            double current = captureCurrentCpuLoadQuietly();
            sampleCount++;

            if (current < 0) {
                logger.info("[CpuMonitor] CPU metric unavailable during cool-down; aborting wait");
                return;
            }

            if (current <= target) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.info("[CpuMonitor] CPU settled to {} after {}ms ({} samples)",
                    String.format("%.2f%%", current * 100), elapsed, sampleCount);
                return;
            }

            logger.debug("[CpuMonitor] CPU at {} (target<={}), waiting...",
                String.format("%.2f%%", current * 100),
                String.format("%.2f%%", target * 100));

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("[CpuMonitor] Cool-down interrupted");
                return;
            }
        }

        // Timed out — log final CPU reading
        double finalCpu = captureCurrentCpuLoadQuietly();
        long elapsed = System.currentTimeMillis() - startTime;
        logger.warn("[CpuMonitor] Cool-down timed out after {}ms ({} samples). "
                + "Final CPU={}, baseline={}, target<={}",
            elapsed, sampleCount,
            String.format("%.2f%%", finalCpu * 100),
            String.format("%.2f%%", baselineCpu * 100),
            String.format("%.2f%%", target * 100));
    }

    /**
     * Internal helper — reads CPU without logging at INFO level to avoid log spam during polling.
     */
    private static double captureCurrentCpuLoadQuietly() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad();
        }
        return -1.0;
    }
}
