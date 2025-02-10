// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.CPU_METRIC_ERROR;

public final class CpuPerformanceCounterCalculator {

    private static final Logger logger = LoggerFactory.getLogger(CpuPerformanceCounterCalculator.class);

    private static final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

    private long prevUpTime;
    private long prevProcessCpuTime;

    private ObjectName osBean;

    // this is not normalized by number of cores, so can be 800% with 8 cores
    @Nullable
    @SuppressWarnings("try")
    public Double getCpuPercentage() {
        try {
            long upTime = runtimeMxBean.getUptime();
            long processCpuTime = getProcessCpuTime();

            if (prevUpTime > 0L && upTime > prevUpTime) {
                long elapsedCpu = processCpuTime - prevProcessCpuTime;
                long elapsedTime = upTime - prevUpTime;
                prevUpTime = upTime;
                prevProcessCpuTime = processCpuTime;
                // if this looks weird, here's another way to write it:
                // (elapsedCpu / 1000000.0) / elapsedTime / 100.0
                return elapsedCpu / (elapsedTime * 10_000.0);
            }
            prevUpTime = upTime;
            prevProcessCpuTime = processCpuTime;
            return null;
        } catch (Exception e) {
            try (MDC.MDCCloseable ignored = CPU_METRIC_ERROR.makeActive()) {
                logger.error("Error in getProcessCPUUsage");
            }
            logger.trace("Error in getProcessCPUUsage", e);
            return null;
        }
    }

    private long getProcessCpuTime() throws Exception {
        MBeanServer bsvr = ManagementFactory.getPlatformMBeanServer();
        if (osBean == null) {
            osBean = ObjectName.getInstance(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        }
        return (Long) bsvr.getAttribute(osBean, "ProcessCpuTime");
    }
}
