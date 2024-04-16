// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.util.ClientLogger;

/**
 * This class is used to access internal methods on {@link HttpRequest}.
 */
public final class HttpRequestAccessHelper {
    private static HttpRequestAccessor accessor;

    /**
     * Type defining the methods to access the internal methods on {@link HttpRequest}.
     */
    public interface HttpRequestAccessor {
        /**
         * Gets the number of times the request has been retried.
         *
         * @param httpRequest The {@link HttpRequest} to set the retry count of.
         *
         * @return The {@link HttpRequest} retry count.
         */
        int getRetryCount(HttpRequest httpRequest);

        /**
         * Sets the number of times the request has been retried.
         *
         * @param httpRequest The {@link HttpRequest} to set the retry count of.
         * @param retryCount The number of times the request has been retried.
         *
         * @return The modified {@link HttpRequest}.
         */
        HttpRequest setRetryCount(HttpRequest httpRequest, int retryCount);

        /**
         * Gets the {@link ClientLogger} used to log the request and response.
         *
         * @param httpRequest The {@link HttpRequest} to get the {@link ClientLogger} of.
         *
         * @return The {@link ClientLogger} used to log the request and response.
         */
        ClientLogger getLogger(HttpRequest httpRequest);
        /**
         *
         * Sets the {@link ClientLogger} used to log the request and response.
         *
         * @param httpRequest The {@link HttpRequest} to set the {@link ClientLogger} of.
         * @param requestLogger The {@link ClientLogger} used to log the request and response.
         *
         * @return The modified {@link HttpRequest}.
         */
        HttpRequest setLogger(HttpRequest httpRequest, ClientLogger requestLogger);
    }

    /**
     * Gets the number of times the request has been retried.
     *
     * @param httpRequest The {@link HttpRequest} to set the retry count of.
     *
     * @return The {@link HttpRequest} retry count.
     */
    public static int getRetryCount(HttpRequest httpRequest) {
        return accessor.getRetryCount(httpRequest);
    }

    /**
     * Sets the number of times the request has been retried.
     *
     * @param httpRequest The {@link HttpRequest} to set the retry count of.
     * @param retryCount The number of times the request has been retried.
     *
     * @return The updated {@link HttpRequest} object.
     */
    public static HttpRequest setRetryCount(HttpRequest httpRequest, int retryCount) {
        return accessor.setRetryCount(httpRequest, retryCount);
    }

    /**
     * Gets the {@link ClientLogger} used to log the request and response.
     *
     * @return The {@link ClientLogger} used to log the request and response.
     */
    public static ClientLogger getLogger(HttpRequest httpRequest) {
        return accessor.getLogger(httpRequest);
    }

    /**
     * Sets the {@link ClientLogger} used to log the request and response.
     *
     * @param logger The {@link ClientLogger} used to log the request and response.
     *
     * @return The updated {@link HttpRequest} object.
     */
    public static HttpRequest setLogger(HttpRequest httpRequest, ClientLogger logger) {
        return accessor.setLogger(httpRequest, logger);
    }

    /**
     * Sets the {@link HttpRequestAccessor}.
     *
     * @param accessor The {@link HttpRequestAccessor}.
     */
    public static void setAccessor(HttpRequestAccessor accessor) {
        HttpRequestAccessHelper.accessor = accessor;
    }

    private HttpRequestAccessHelper() {
    }
}
