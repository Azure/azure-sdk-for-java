// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains utility methods for encoding query parameters, quoting header values,
 * and other helpers for code generation.
 */
public final class CodeGenUtils {
    private CodeGenUtils() {
    }

    /**
     * Encodes a query parameter value for use in generated code.
     * @param value The value to encode.
     * @return The Java code string for encoding the value.
     */
    public static String encodeQueryParamValue(String value) {
        return "UriEscapers.QUERY_ESCAPER.escape(" + value + ")";
    }

    /**
     * Applies the following quoting logic to a header value.
     * <p>
     * If the value starts and ends with a quote, returns as-is.
     * If it starts with a quote, appends a trailing quote.
     * If it ends with a quote, prepends a leading quote.
     * Otherwise, wraps the value in quotes.
     *
     * @param value The header value to quote.
     * @return The quoted header value as a String.
     */
    public static String quoteHeaderValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            return value;
        } else if (value.startsWith("\"")) {
            return value + "\"";
        } else if (value.endsWith("\"")) {
            return "\"" + value;
        } else {
            return "\"" + value + "\"";
        }
    }

    /**
     * Joins a list of values into a Java code array initializer.
     * @param values The values to join.
     * @param quote Whether to quote each value.
     * @return The Java code array initializer string.
     */
    public static String toJavaArrayInitializer(List<String> values, boolean quote) {
        return values.stream().map(v -> quote ? quoteHeaderValue(v) : v).collect(Collectors.joining(", "));
    }
}
