// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http;

import io.clientcore.core.http.models.HttpRequest;

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
