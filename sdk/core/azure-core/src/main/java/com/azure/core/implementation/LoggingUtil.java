// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;

/**
 * This class contains utility methods useful for logging.
 */
public final class LoggingUtil {
    /**
     * Retrieve the environment logging level which is used to determine if and what we are allowed to log.
     *
     * <p>The value returned from this method should be used throughout a single logging event as it may change during
     * the logging operation, this will help prevent difficult to debug timing issues.</p>
     *
     * @return Environment logging level if set, otherwise {@link LogLevel#NOT_SET}.
     */
    public static LogLevel getEnvironmentLoggingLevel() {
        String environmentLogLevel = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL);

        return LogLevel.fromString(environmentLogLevel);
    }

    // Private constructor
    private LoggingUtil() {
    }
}
