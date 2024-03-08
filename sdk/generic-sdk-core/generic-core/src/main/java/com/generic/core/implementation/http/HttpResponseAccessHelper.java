// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http;

import com.generic.core.http.models.HttpResponse;

/**
 * This class is used to access internal methods on {@link HttpResponse}.
 */
public final class HttpResponseAccessHelper {
    private static HttpResponseAccessor accessor;

    /**
     * Type defining the methods to access the internal methods on {@link HttpResponse}.
     */
    public interface HttpResponseAccessor {
        /**
         * Sets a {@link HttpResponse}'s value.
         *
         * @param httpResponse The {@link HttpResponse} to set the value of.
         *
         * @return The modified {@link HttpResponse}.
         */
        HttpResponse<?> setValue(HttpResponse<?> httpResponse, Object value);
    }

    /**
     * Gets the raw header map from {@link HttpResponse}.
     *
     * @param httpResponse The {@link HttpResponse} to get the raw header map from.
     * @return The raw header map.
     */
    public static HttpResponse<?> setValue(HttpResponse<?> httpResponse, Object value) {
        return accessor.setValue(httpResponse, value);
    }

    /**
     * Sets the {@link HttpResponseAccessor}.
     *
     * @param accessor The {@link HttpResponseAccessor}.
     */
    public static void setAccessor(HttpResponseAccessor accessor) {
        HttpResponseAccessHelper.accessor = accessor;
    }

    private HttpResponseAccessHelper() {
    }
}
