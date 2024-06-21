// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;

import java.util.List;
import java.util.Locale;
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

        /**
         * Adds a header value to the backing map in {@link HttpHeaders}.
         * <p>
         * This bypasses using {@link HttpHeaders#add(String, String)} which uses {@link String#toLowerCase(Locale)},
         * which may be slower than options available by implementing HTTP stacks (such as Netty which has an ASCII
         * string class which has optimizations around lowercasing due to ASCII constraints).
         *
         * @param headers The {@link HttpHeaders} to add the header to.
         * @param formattedName The lower-cased header name.
         * @param name The original header name.
         * @param value The header value.
         */
        void addInternal(HttpHeaders headers, String formattedName, String name, String value);

        /**
         * Sets a header value to the backing map in {@link HttpHeaders}.
         * <p>
         * This bypasses using {@link HttpHeaders#set(String, List)} which uses {@link String#toLowerCase(Locale)},
         * which may be slower than options available by implementing HTTP stacks (such as JDK HttpClient where all
         * response header names are already lowercased).
         *
         * @param headers The {@link HttpHeaders} to set the header to.
         * @param formattedName The lower-cased header name.
         * @param name The original header name.
         * @param values The header values.
         */
        void setInternal(HttpHeaders headers, String formattedName, String name, List<String> values);
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
     * Adds a header value to the backing map in {@link HttpHeaders}.
     * <p>
     * This bypasses using {@link HttpHeaders#add(String, String)} which uses {@link String#toLowerCase(Locale)},
     * which may be slower than options available by implementing HTTP stacks (such as Netty which has an ASCII
     * string class which has optimizations around lowercasing due to ASCII constraints).
     *
     * @param headers The {@link HttpHeaders} to add the header to.
     * @param formattedName The lower-cased header name.
     * @param name The original header name.
     * @param value The header value.
     */
    public static void addInternal(HttpHeaders headers, String formattedName, String name, String value) {
        accessor.addInternal(headers, formattedName, name, value);
    }

    /**
     * Sets a header value to the backing map in {@link HttpHeaders}.
     * <p>
     * This bypasses using {@link HttpHeaders#set(String, List)} which uses {@link String#toLowerCase(Locale)},
     * which may be slower than options available by implementing HTTP stacks (such as JDK HttpClient where all
     * response header names are already lowercased).
     *
     * @param headers The {@link HttpHeaders} to set the header to.
     * @param formattedName The lower-cased header name.
     * @param name The original header name.
     * @param values The header values.
     */
    public static void setInternal(HttpHeaders headers, String formattedName, String name, List<String> values) {
        accessor.setInternal(headers, formattedName, name, values);
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
