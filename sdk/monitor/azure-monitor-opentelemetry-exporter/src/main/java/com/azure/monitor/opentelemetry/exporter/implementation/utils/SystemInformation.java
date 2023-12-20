// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Locale;

public class SystemInformation {

    private static final Logger logger = LoggerFactory.getLogger(SystemInformation.class);

    private static final String DEFAULT_PROCESS_NAME = "Java_Process";

    private static final boolean WINDOWS;
    private static final boolean LINUX;

    static {
        String osName = System.getProperty("os.name");
        String osNameLower = osName == null ? null : osName.toLowerCase(Locale.ROOT);
        WINDOWS = osNameLower != null && osNameLower.startsWith("windows");
        LINUX = osNameLower != null && osNameLower.startsWith("linux");
    }

    private static final String processId = initializeProcessId();

    public static String getProcessId() {
        return processId;
    }

    public static boolean isWindows() {
        return WINDOWS;
    }

    public static boolean isLinux() {
        return LINUX;
    }

    /**
     * JVMs are not required to publish this value/bean and some processes may not have permission to
     * access it.
     */
    private static String initializeProcessId() {
        String rawName = ManagementFactory.getRuntimeMXBean().getName();
        if (!Strings.isNullOrEmpty(rawName)) {
            int i = rawName.indexOf("@");
            if (i != -1) {
                String processIdAsString = rawName.substring(0, i);
                try {
                    Integer.parseInt(processIdAsString);
                    return processIdAsString;
                } catch (RuntimeException e) {
                    logger.error("Failed to fetch process id: '{}'", e.toString());
                    logger.error("Failed to parse PID as number: '{}'", e.toString());
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        logger.error("Could not extract PID from runtime name: '" + rawName + "'");
        // Default
        return DEFAULT_PROCESS_NAME;
    }

    private SystemInformation() {
    }
}
