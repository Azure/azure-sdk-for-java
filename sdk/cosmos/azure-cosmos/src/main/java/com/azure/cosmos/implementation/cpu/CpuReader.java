// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

public class CpuReader {
    private final static Logger logger = LoggerFactory.getLogger(CpuReader.class);
    private final com.sun.management.OperatingSystemMXBean operatingSystemMXBean;

    public CpuReader() {
        java.lang.management.OperatingSystemMXBean mxBean = null;
        try {
            mxBean =
                ManagementFactory.getOperatingSystemMXBean();
        } catch (Throwable t) {
            logger.error("failed to initialized CpuReader", t);
        }

        this.operatingSystemMXBean = tryGetAs(mxBean,
            com.sun.management.OperatingSystemMXBean.class);
    }

    public double getSystemWideCpuUsage() {
        try {
            if (operatingSystemMXBean != null) {
                double val = operatingSystemMXBean.getSystemCpuLoad();
                if (val > 0) {
                    return val;
                }
            }

            return Double.NaN;
        } catch (Throwable t) {
            logger.error("Failed to get System CPU", t);
            return Double.NaN;
        }
    }

    private <T> T tryGetAs(java.lang.management.OperatingSystemMXBean mxBean, Class<T> classType) {
        try {
            return Utils.as(mxBean, classType);
        } catch (Throwable t) {
            logger.error("failed to initialized CpuReader as type {}", classType.getName(), t);
            return null;
        }
    }
}
