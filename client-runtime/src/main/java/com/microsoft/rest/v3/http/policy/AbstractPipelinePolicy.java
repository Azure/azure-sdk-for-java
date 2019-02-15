/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineOptions;

/**
 * An abstract HttpPipelinePolicy base-class.
 */
public abstract class AbstractPipelinePolicy implements HttpPipelinePolicy {
    private final HttpPipelineOptions options;

    /**
     * Creates AbstractRequestPolicy.
     *
     * @param options the options for this HttpPipelinePolicy.
     */
    protected AbstractPipelinePolicy(HttpPipelineOptions options) {
        this.options = options;
    }

    /**
     * @return the options.
     */
    protected HttpPipelineOptions options() {
        return options;
    }

    /**
     * Get whether or not a log with the provided log level should be logged.
     *
     * @param logLevel the log level of the log that will be logged.
     * @return whether or not a log with the provided log level should be logged.
     */
    public boolean shouldLog(HttpPipelineLogLevel logLevel) {
        return options != null && options.shouldLog(logLevel);
    }

    /**
     * Attempt to log the provided message to the provided logger. If no logger was provided or if
     * the log level does not meat the logger's threshold, then nothing will be logged.
     *
     * @param logLevel the log level of this log.
     * @param message the message of this log.
     * @param formattedMessageArguments The formatted arguments to apply to the message.
     */
    protected void log(HttpPipelineLogLevel logLevel, String message, Object... formattedMessageArguments) {
        if (options != null) {
            options.log(logLevel, message, formattedMessageArguments);
        }
    }
}