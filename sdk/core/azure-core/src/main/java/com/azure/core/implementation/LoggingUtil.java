// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class LoggingUtil {
    public enum LogLevel {
        /**
         * Indicates that log level is at verbose level.
         */
        VERBOSE(1),

        /**
         * Indicates that log level is at information level.
         */
        INFORMATIONAL(2),

        /**
         * Indicates that log level is at warning level.
         */
        WARNING(3),

        /**
         * Indicates that log level is at error level.
         */
        ERROR(4),

        /**
         * Indicates that logging is disabled.
         */
        DISABLED(5);

        private final int numericValue;

        LogLevel(int numericValue) {
            this.numericValue = numericValue;
        }

        public int toNumeric() {
            return numericValue;
        }
    }

    private static final Map<Integer, LogLevel> LOG_LEVEL_MAPPER = Arrays.stream(LogLevel.values())
        .collect(Collectors.toMap(LogLevel::toNumeric, logLevel -> logLevel));

    /**
     * Retrieve the environment logging level which is used to determine if and what we are allowed to log.
     *
     * <p>The value returned from this method should be used throughout a single logging event as it may change during
     * the logging operation, this will help prevent difficult to debug timing issues.</p>
     *
     * @return Environment logging level if set, otherwise {@link LogLevel#DISABLED}.
     */
    public static LogLevel getEnvironmentLoggingLevel() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL,
            loadedValue -> LOG_LEVEL_MAPPER.getOrDefault(Integer.parseInt(loadedValue), LogLevel.DISABLED));
    }

    // Private constructor
    private LoggingUtil() {
    }
}
