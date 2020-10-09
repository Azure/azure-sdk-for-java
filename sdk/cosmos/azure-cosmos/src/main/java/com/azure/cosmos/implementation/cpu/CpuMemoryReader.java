// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

public class CpuMemoryReader {
    private final static Logger logger = LoggerFactory.getLogger(CpuMemoryReader.class);
    private final com.sun.management.OperatingSystemMXBean operatingSystemMXBean;

    public CpuMemoryReader() {
        java.lang.management.OperatingSystemMXBean mxBean = null;
        try {
            mxBean =
                ManagementFactory.getOperatingSystemMXBean();
        } catch (Throwable t) {
            logger.error("failed to initialized CpuMemoryReader", t);
        }

        this.operatingSystemMXBean = tryGetAs(mxBean,
            com.sun.management.OperatingSystemMXBean.class);
    }

    public float getSystemWideCpuUsage() {
        try {
            if (operatingSystemMXBean != null) {
                float val = (float) operatingSystemMXBean.getSystemCpuLoad();
                if (val > 0) {
                    return val;
                }
            }

            return Float.NaN;
        } catch (Throwable t) {
            logger.error("Failed to get System CPU", t);
            return Float.NaN;
        }
    }

    public long getSystemWideMemoryUsage() {
        try {
            long totalMemory = Runtime.getRuntime().totalMemory() / (1024*1024);
            long freeMemory = Runtime.getRuntime().freeMemory() / (1024*1024);
            long maxMemory = Runtime.getRuntime().maxMemory() / (1024*1024);
            return maxMemory -(totalMemory - freeMemory);
        } catch (Throwable t) {
            logger.error("Failed to get System memory", t);
            return 0;
        }
    }

    private <T> T tryGetAs(java.lang.management.OperatingSystemMXBean mxBean, Class<T> classType) {
        try {
            return Utils.as(mxBean, classType);
        } catch (Throwable t) {
            logger.error("failed to initialized CpuMemoryReader as type {}", classType.getName(), t);
            return null;
        }
    }
}
