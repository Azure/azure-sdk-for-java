// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.instrumentation.logging;

import java.util.HashMap;
import java.util.Locale;

/**
 * Enum which represent logging levels used.
 */
public enum LogLevel {
    /**
     * Indicates that there no log level is set.
     */
    NOTSET(0, "0", "notSet"),

    /**
     * Indicates that the log level is at verbose level.
     */
    VERBOSE(1, "1", "verbose", "debug"),

    /**
     * Indicates that the log level is at information level.
     */
    INFORMATIONAL(2, "2", "info", "information", "informational"),

    /**
     * Indicates that the log level is at warning level.
     */
    WARNING(3, "3", "warn", "warning"),

    /**
     * Indicates that the log level is at error level.
     */
    ERROR(4, "4", "err", "error");

    private final int numericValue;
    private final String[] allowedLogLevelVariables;
    private static final HashMap<String, io.clientcore.core.instrumentation.logging.LogLevel> LOG_LEVEL_STRING_MAPPER
        = new HashMap<>();
    private final String caseSensitive;

    static {
        for (io.clientcore.core.instrumentation.logging.LogLevel logLevel : io.clientcore.core.instrumentation.logging.LogLevel
            .values()) {
            for (String val : logLevel.allowedLogLevelVariables) {
                LOG_LEVEL_STRING_MAPPER.put(val, logLevel);
            }
        }
    }

    LogLevel(int numericValue, String... allowedLogLevelVariables) {
        this.numericValue = numericValue;
        this.allowedLogLevelVariables = allowedLogLevelVariables;
        this.caseSensitive = allowedLogLevelVariables[0];
    }

    /**
     * Converts the log level into a numeric representation used for comparisons.
     *
     * @return The numeric representation of the log level.
     */
    private int getLevelCode() {
        return numericValue;
    }

    /**
     * Compares the passed log level with the configured log level and returns true if the passed log level is greater
     *
     * @param level The log level to compare.
     * @param configuredLevel The configured log level.
     * @return True if the passed log level is greater or equal to the configured log level, false otherwise.
     */
    public static boolean isGreaterOrEqual(io.clientcore.core.instrumentation.logging.LogLevel level,
        io.clientcore.core.instrumentation.logging.LogLevel configuredLevel) {
        return level.getLevelCode() >= configuredLevel.getLevelCode();
    }

    /**
     * Converts the passed log level string to the corresponding {@link io.clientcore.core.instrumentation.logging.LogLevel}.
     *
     * @param logLevelVal The log level value which needs to convert
     * @return The LogLevel Enum if pass in the valid string.
     * The valid strings for {@link io.clientcore.core.instrumentation.logging.LogLevel} are:
     * <ul>
     * <li>VERBOSE: "verbose", "debug"</li>
     * <li>INFO: "info", "information", "informational"</li>
     * <li>WARNING: "warn", "warning"</li>
     * <li>ERROR: "err", "error"</li>
     * </ul>
     * Returns NOT_SET if null is passed in.
     * @throws IllegalArgumentException if the log level value is invalid.
     */
    public static io.clientcore.core.instrumentation.logging.LogLevel fromString(String logLevelVal) {
        if (logLevelVal == null) {
            return io.clientcore.core.instrumentation.logging.LogLevel.NOTSET;
        }
        String caseInsensitiveLogLevel = logLevelVal.toLowerCase(Locale.ROOT);
        if (!LOG_LEVEL_STRING_MAPPER.containsKey(caseInsensitiveLogLevel)) {
            throw new IllegalArgumentException(
                "We currently do not support the log level you set. LogLevel: " + logLevelVal);
        }
        return LOG_LEVEL_STRING_MAPPER.get(caseInsensitiveLogLevel);
    }

    /**
     * Converts the log level to a string representation.
     *
     * @return The string representation of the log level.
     */
    public String toString() {
        return caseSensitive;
    }
}
