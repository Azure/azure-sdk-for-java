// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;

/**
 * Helper class to access private methods of {@link HttpHeaders} across package boundaries.
 */
public final class HttpHeadersHelper {
    private static HttpHeadersAccessor accessor;

    /**
     * Type defining the methods to access the private methods of an {@link HttpHeaders} instance.
     */
    public interface HttpHeadersAccessor {
        /**
         * Sets the HTTP header {@code name} with the specified {@code value} without formatting the {@code name}.
         *
         * @param headers The {@link HttpHeaders} having an HTTP header set.
         * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
         * @param name The HTTP header name.
         * @param value The HTTP header value.
         * @return The updated {@code headers} object.
         */
        HttpHeaders setNoKeyFormat(HttpHeaders headers, String formattedName, String name, String value);

        /**
         * Gets the HTTP header value for the formatted HTTP header name.
         *
         * @param headers The {@link HttpHeaders} having the header value retrieved.
         * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
         * @return The header for the key, or null if it wasn't present.
         */
        HttpHeader getNoKeyFormat(HttpHeaders headers, String formattedName);

        /**
         * Gets the HTTP header value for the formatted HTTP header name.
         *
         * @param headers The {@link HttpHeaders} having the header value retrieved.
         * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
         * @return The header value for the key, or null if it wasn't present.
         */
        String getValueNoKeyFormat(HttpHeaders headers, String formattedName);
    }

    /**
     * The method called from {@link HttpHeaders} to set its accessor.
     *
     * @param httpHeadersAccessor The accessor.
     */
    public static void setAccessor(final HttpHeadersAccessor httpHeadersAccessor) {
        accessor = httpHeadersAccessor;
    }

    /**
     * Sets the HTTP header {@code name} with the specified {@code value} without formatting the {@code name}.
     *
     * @param headers The {@link HttpHeaders} having an HTTP header set.
     * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
     * @param name The HTTP header name.
     * @param value The HTTP header value.
     * @return The updated {@code headers} object.
     */
    public static HttpHeaders setNoKeyFormat(HttpHeaders headers, String formattedName, String name, String value) {
        if (headers.getClass() != HttpHeaders.class) {
            return headers.set(name, value);
        }

        return accessor.setNoKeyFormat(headers, formattedName, name, value);
    }

    /**
     * Gets the HTTP header value for the formatted HTTP header name.
     *
     * @param headers The {@link HttpHeaders} having the header value retrieved.
     * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
     * @return The header for the key, or null if it wasn't present.
     */
    public static HttpHeader getNoKeyFormat(HttpHeaders headers, String formattedName) {
        if (headers.getClass() != HttpHeaders.class) {
            return headers.get(formattedName);
        }

        return accessor.getNoKeyFormat(headers, formattedName);
    }

    /**
     * Gets the HTTP header value for the formatted HTTP header name.
     *
     * @param headers The {@link HttpHeaders} having the header value retrieved.
     * @param formattedName The {@link HttpHeaders} formatted HTTP header name.
     * @return The header value for the key, or null if it wasn't present.
     */
    public static String getValueNoKeyFormat(HttpHeaders headers, String formattedName) {
        if (headers.getClass() != HttpHeaders.class) {
            return headers.getValue(formattedName);
        }

        return accessor.getValueNoKeyFormat(headers, formattedName);
    }
}
