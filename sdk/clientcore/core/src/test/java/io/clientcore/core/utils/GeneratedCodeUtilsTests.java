// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link GeneratedCodeUtils}.
 */
public class GeneratedCodeUtilsTests {
    /**
     * Test that appendQueryParams correctly appends multi-value query parameters with the specified delimiter.
     */
    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testAppendQueryParams(String uri, String key, List<?> value, String expected) {
        UriBuilder uriBuilder = UriBuilder.parse(uri);
        GeneratedCodeUtils.addQueryParameter(uriBuilder, key, false, value, false);
        assertEquals(expected, uriBuilder.toString(),
            "The URL should be correctly updated with the multi-value query parameter.");
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            // Test cases with no query string
            Arguments.of("https://example.com", "api-version", Collections.singletonList("1.0"),
                "https://example.com?api-version=1.0"),
            Arguments.of("https://example.com", "api-version", Arrays.asList("1.0", "2.0"),
                "https://example.com?api-version=1.0&api-version=2.0"),  // List value with comma delimiter

            // Test cases with existing query string
            Arguments.of("https://example.com?existingParam=value", "api-version", Collections.singletonList("1.0"),
                "https://example.com?existingParam=value&api-version=1.0"),
            Arguments.of("https://example.com?existingParam=value", "api-version", Arrays.asList("1.0", "2.0"),
                "https://example.com?existingParam=value&api-version=1.0&api-version=2.0"),

            // Test cases with empty URL
            Arguments.of("", "api-version", Collections.singletonList("1.0"), "?api-version=1.0"),

            // Test case with a non-empty map and one of the keys having a null value
            Arguments.of("https://example.com", "api-version", null, "https://example.com"));
    }

    @Test
    void testAppendNullQueryParam() {
        UriBuilder uriBuilder = UriBuilder.parse("https://example.com");
        String key = "name";
        String expected = "https://example.com";
        // Null value for parameter
        GeneratedCodeUtils.addQueryParameter(uriBuilder, key, false, null, false);
        assertEquals(expected, uriBuilder.toString(), "The URL should be correctly updated with the query parameter.");
    }
}
