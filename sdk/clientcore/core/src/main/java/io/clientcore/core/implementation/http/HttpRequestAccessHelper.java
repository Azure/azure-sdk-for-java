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
         * Gets the number of times the request has been attempted. It's 0 during the first attempt
         * and increments after attempt is made.
         *
         * @param httpRequest The {@link HttpRequest} to set the try count of.
         *
         * @return The {@link HttpRequest} try count.
         */
        int getTryCount(HttpRequest httpRequest);

        /**
         * Sets the number of times the request has been attempted. It's 0 during the first attempt
         * and increments after attempt is made.
         *
         * @param httpRequest The {@link HttpRequest} to set the try count of.
         * @param tryCount The number of times the request has been attempted.
         *
         * @return The modified {@link HttpRequest}.
         */
        HttpRequest setTryCount(HttpRequest httpRequest, int tryCount);
    }

    /**
     * Gets the number of times the request has already been retried. It's 0 during the first attempt
     * and increments after attempt is made.
     *
     * @param httpRequest The {@link HttpRequest} to set the try count of.
     *
     * @return The {@link HttpRequest} try count.
     */
    public static int getTryCount(HttpRequest httpRequest) {
        return accessor.getTryCount(httpRequest);
    }

    /**
     * Sets the number of times the request has been attempted. It's 0 during the first attempt
     * and increments after attempt is made.
     *
     * @param httpRequest The {@link HttpRequest} to set the try count of.
     * @param tryCount The number of times the request has been retried.
     *
     * @return The updated {@link HttpRequest} object.
     */
    public static HttpRequest setTryCount(HttpRequest httpRequest, int tryCount) {
        return accessor.setTryCount(httpRequest, tryCount);
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
