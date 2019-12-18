// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Enum which represent logging levels used in Azure SDKs.
 */
public enum LogLevel {
    /**
     * Indicates that log level is at verbose level.
     */
    VERBOSE("1", "verbose"),

    /**
     * Indicates that log level is at information level.
     */
    INFORMATIONAL("2", "info", "information", "informational"),

    /**
     * Indicates that log level is at warning level.
     */
    WARNING("3", "warn", "warning"),

    /**
     * Indicates that log level is at error level.
     */
    ERROR("4", "err", "error");

    private final String[] allowedLogLevelVariables;

    LogLevel(String... allowedLogLevelVariables) {
        this.allowedLogLevelVariables = allowedLogLevelVariables;
    }

    /**
     * Converts the log level into a numeric representation used for comparisons.
     *
     * @return The numeric representation of the log level.
     */
    public int getLogLevel() {
        return Integer.parseInt(allowedLogLevelVariables[0]);
    }

    /**
     * Converts the log level into string representations used for comparisons.
     *
     * @return The string representations of the log level.
     */
    private String[] getAllowedLogLevels() {
        return allowedLogLevelVariables;
    }

    private static final Map<String, LogLevel> LOG_LEVEL_STRING_MAPPER = Arrays.stream(LogLevel.values())
        .flatMap(logLevel -> Arrays.stream(logLevel.getAllowedLogLevels()).map(v -> Tuples.of(v, logLevel)))
        .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));

    /**
     * Converts the passed log level string to the corresponding {@link LogLevel}.
     *
     * @param logLevelVal The log level value which needs to convert
     * @return The LogLevel Enum.
     */
    public static LogLevel fromString(String logLevelVal) {
        return LOG_LEVEL_STRING_MAPPER.get(logLevelVal);
    }
}
