/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

/**
 * Uses the decorator pattern to add custom behavior when an HTTP request is made.
 * e.g. add header, user agent, timeout, retry, etc.
 *
 */
public interface RequestPolicy {
    /**
     * Sends an HTTP request as an asynchronous operation.
     *
     * @param request The HTTP request message to send.
     * @return The io.reactivex.Single instance representing the asynchronous operation.
     */
    Single<HttpResponse> sendAsync(HttpRequest request);

    /**
     * Factory to create a RequestPolicy. RequestPolicies are instantiated per-request
     * so that they can contain instance state specific to that request/response exchange,
     * for example, the number of retries attempted so far in a counter.
     */
    interface Factory {
        /**
         * Creates RequestPolicy.
         *
         * @param next the next RequestPolicy in the request-response pipeline.
         * @return the RequestPolicy
         */
        RequestPolicy create(RequestPolicy next, Options options);
    }

    /**
     * Optional properties that can be used when creating a RequestPolicy.
     */
    final class Options {
        /**
         * The Logger that has been assigned to the HttpPipeline.
         */
        private final HttpPipeline.Logger logger;

        /**
         * Create a new RequestPolicy.Options object.
         * @param logger The logger that has been assigned to the HttpPipeline.
         */
        public Options(HttpPipeline.Logger logger) {
            this.logger = logger;
        }

        /**
         * Get whether or not a log with the provided log level should be logged.
         * @param logLevel The log level of the log that will be logged.
         * @return Whether or not a log with the provided log level should be logged.
         */
        public boolean shouldLog(HttpPipeline.LogLevel logLevel) {
            boolean result = false;

            if (logger != null && logLevel != null && logLevel != HttpPipeline.LogLevel.OFF) {
                final HttpPipeline.LogLevel minimumLogLevel = logger.minimumLogLevel();
                if (minimumLogLevel != null) {
                    result = logLevel.ordinal() <= minimumLogLevel.ordinal();
                }
            }

            return result;
        }

        /**
         * Attempt to log the provided message to the provided logger. If no logger was provided or if
         * the log level does not meat the logger's threshold, then nothing will be logged.
         * @param logLevel The log level of this log.
         * @param message The message of this log.
         * @param formattedMessageArguments The formatted arguments to apply to the message.
         */
        public void log(HttpPipeline.LogLevel logLevel, String message, Object... formattedMessageArguments) {
            if (logger != null) {
                logger.log(logLevel, message, formattedMessageArguments);
            }
        }
    }
}
