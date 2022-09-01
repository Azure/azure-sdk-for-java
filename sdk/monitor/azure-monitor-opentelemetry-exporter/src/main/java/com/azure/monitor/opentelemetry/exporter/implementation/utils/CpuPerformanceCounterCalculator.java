/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.CPU_METRIC_ERROR;

public final class CpuPerformanceCounterCalculator {

  private static final Logger logger =
      LoggerFactory.getLogger(CpuPerformanceCounterCalculator.class);

  private static final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

  private long prevUpTime;
  private long prevProcessCpuTime;

  private ObjectName osBean;

  @SuppressWarnings("try")
  // this is not normalized by number of cores, so can be 800% with 8 cores
  @Nullable
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
