/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

/**
 * Optional properties that can be used when creating a HttpPipeline.
 */
public final class HttpPipelineOptions {
    /**
     * The Logger that has been assigned to the HttpPipeline.
     */
    private final HttpPipelineLogger logger;

    /**
     * Create a new HttpPipelinePolicyOptions object.
     *
     * @param logger The logger that has been assigned to the HttpPipeline
     */
    public HttpPipelineOptions(HttpPipelineLogger logger) {
        this.logger = logger;
    }

    /**
     * Get whether or not a log with the provided log level should be logged.
     *
     * @param logLevel The log level of the log that will be logged
     * @return Whether or not a log with the provided log level should be logged
     */
    public boolean shouldLog(HttpPipelineLogLevel logLevel) {
        boolean result = false;

        if (logger != null && logLevel != null && logLevel != HttpPipelineLogLevel.OFF) {
            final HttpPipelineLogLevel minimumLogLevel = logger.minimumLogLevel();
            if (minimumLogLevel != null) {
                result = logLevel.ordinal() <= minimumLogLevel.ordinal();
            }
        }

        return result;
    }

    /**
     * Attempt to log the provided message to the provided logger. If no logger was provided or if
     * the log level does not meat the logger's threshold, then nothing will be logged.
     *
     * @param logLevel The log level of this log.
     * @param message The message of this log.
     * @param formattedMessageArguments The formatted arguments to apply to the message.
     */
    public void log(HttpPipelineLogLevel logLevel, String message, Object... formattedMessageArguments) {
        if (logger != null) {
            logger.log(logLevel, message, formattedMessageArguments);
        }
    }
}