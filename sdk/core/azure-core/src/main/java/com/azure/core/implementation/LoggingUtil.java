// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains utility methods useful for logging.
 */
public final class LoggingUtil {
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
        String environmentLogLevel = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL);

        return CoreUtils.isNullOrEmpty(environmentLogLevel)
            ? LogLevel.DISABLED
            : LOG_LEVEL_MAPPER.getOrDefault(Integer.parseInt(environmentLogLevel), LogLevel.DISABLED);
    }

    // Private constructor
    private LoggingUtil() {
    }
}
