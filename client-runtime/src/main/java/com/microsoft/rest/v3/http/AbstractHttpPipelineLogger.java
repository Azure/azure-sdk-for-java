/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http;

/**
 * An abstract Logger for HttpPipeline RequestPolicies that contains functionality that is
 * common to Loggers.
 */
public abstract class AbstractHttpPipelineLogger implements HttpPipelineLogger {
    private HttpPipelineLogLevel minimumLogLevel = HttpPipelineLogLevel.INFO;

    /**
     * Set the minimum log level that this logger should log. Anything with a higher log level
     * should be ignored.
     * @param minimumLogLevel The minimum log level to set.
     * @return This Logger.
     */
    public AbstractHttpPipelineLogger withMinimumLogLevel(HttpPipelineLogLevel minimumLogLevel) {
        this.minimumLogLevel = minimumLogLevel;
        return this;
    }

    @Override
    public HttpPipelineLogLevel minimumLogLevel() {
        return minimumLogLevel;
    }

    protected static String format(String message, Object... formattedMessageArguments) {
        if (formattedMessageArguments != null && formattedMessageArguments.length >= 1) {
            message = String.format(message, formattedMessageArguments);
        }
        return message;
    }
}