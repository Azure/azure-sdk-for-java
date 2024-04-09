// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;

import java.util.Map;

/**
 * This class is used to access internal methods on {@link HttpHeaders}.
 */
public final class HttpHeadersAccessHelper {
    private static HttpHeadersAccessor accessor;

    /**
     * Type defining the methods to access the internal methods on {@link HttpHeaders}.
     */
    public interface HttpHeadersAccessor {
        /**
         * Gets the raw header map from {@link HttpHeaders}.
         *
         * @param headers The {@link HttpHeaders} to get the raw header map from.
         * @return The raw header map.
         */
        Map<String, HttpHeader> getRawHeaderMap(HttpHeaders headers);
    }

    /**
     * Gets the raw header map from {@link HttpHeaders}.
     *
     * @param headers The {@link HttpHeaders} to get the raw header map from.
     * @return The raw header map.
     */
    public static Map<String, HttpHeader> getRawHeaderMap(HttpHeaders headers) {
        return accessor.getRawHeaderMap(headers);
    }

    /**
     * Sets the {@link HttpHeadersAccessor}.
     *
     * @param accessor The {@link HttpHeadersAccessor}.
     */
    public static void setAccessor(HttpHeadersAccessor accessor) {
        HttpHeadersAccessHelper.accessor = accessor;
    }

    private HttpHeadersAccessHelper() {
    }
}
