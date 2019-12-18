// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Enum which represent logging levels used in Azure SDKs.
 */
public enum LogLevel {
    /**
     * Indicates that log level is at verbose level.
     */
    VERBOSE(1, "verbose"),

    /**
     * Indicates that log level is at information level.
     */
    INFORMATIONAL(2, "info", "information", "informational"),

    /**
     * Indicates that log level is at warning level.
     */
    WARNING(3, "warn", "warning"),

    /**
     * Indicates that log level is at error level.
     */
    ERROR(4, "err", "error");

    private final int numericValue;
    private final Set<String> allowedLogLevelVariables;

    LogLevel(int numericValue, String... allowedLogLevelVariables) {
        this.numericValue = numericValue;
        this.allowedLogLevelVariables = new HashSet<>(Arrays.asList(allowedLogLevelVariables));
    }

    /**
     * Converts the log level into a numeric representation used for comparisons.
     *
     * @return The numeric representation of the log level.
     */
    public int toNumeric() {
        return numericValue;
    }

    /**
     * Converts the log level into a numeric representation used for comparisons.
     *
     * @return The numeric representation of the log level.
     */
    public Set<String> getAllowedLogLevels() {
        return allowedLogLevelVariables;
    }

    /**
     * Convert log level value to LogLevel.
     *
     * @param logLevelVal The log level value which needs to convert
     * @return The LogLevel Enum.
     */
    public static LogLevel getLogLevel(String logLevelVal) {
        return Arrays.stream(LogLevel.values()).filter(logLevel ->
            isValueMatch(logLevel, logLevelVal)).findFirst().orElse(null);
    }

    /**
     * Check if the log level value matches the LogLevel enum.
     */
    private static boolean isValueMatch(LogLevel logLevel, String logLevelVal) {
        return String.valueOf(logLevel.toNumeric()).equals(logLevelVal)
            || logLevel.getAllowedLogLevels().contains(logLevelVal.toLowerCase(Locale.ROOT));
    }

}
