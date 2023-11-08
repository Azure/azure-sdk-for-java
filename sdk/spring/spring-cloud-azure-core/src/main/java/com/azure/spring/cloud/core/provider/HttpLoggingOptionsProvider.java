// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider;

import com.azure.core.http.policy.HttpLogDetailLevel;

import java.util.Set;

/**
 * Interface to be implemented by classes that wish to describe logging options in http-based client sdks options.
 *
 * For example, if you want to log the http request or response, you could set the level to
 * {@link HttpLogDetailLevel#BASIC} or some other levels.
 */
public interface HttpLoggingOptionsProvider {

    /**
     * Get the http logging details.
     * @return the http logging.
     */
    HttpLoggingOptions getLogging();

    /**
     * Interface to be implemented by classes that wish to describe logging options in http-based client sdks options.
     *≤
     * For example, if you want to log the http request or response, you could set the level to
     * {@link HttpLogDetailLevel#BASIC} or some other levels.
     */
    interface HttpLoggingOptions {

        /**
         * Gets the level of detail to log on HTTP messages.
         * @return the http log detail level.
         */
        HttpLogDetailLevel getLevel();

        /**
         * Gets the allowlist headers that should be logged.
         * @return The list of allowlist headers.
         */
        Set<String> getAllowedHeaderNames();

        /**
         * Gets the allowlist query parameters.
         * @return The list of allowlist query parameters.
         */
        Set<String> getAllowedQueryParamNames();

        /**
         * Gets flag to allow pretty printing of message bodies.
         * @return whether to pretty print the message bodies.
         */
        Boolean getPrettyPrintBody();

    }


}
