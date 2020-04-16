// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link PercentEscaper}.
 */
public class PercentEscaperTests {
    @ParameterizedTest
    @MethodSource("escapeSupplier")
    public void escape(PercentEscaper escaper, String original, String expected) {
        assertEquals(expected, escaper.escape(original));
    }

    private static Stream<Arguments> escapeSupplier() {
        PercentEscaper defaultEscaper = new PercentEscaper(null, false);

        return Stream.of(
            Arguments.arguments(defaultEscaper, "$", "%24"),
            Arguments.arguments(defaultEscaper, "¢", "%C2%A2"),
            Arguments.arguments(defaultEscaper, "ह", "%E0%A4%B9"),
            Arguments.arguments(defaultEscaper, "€", "%E2%82%AC"),
            Arguments.arguments(defaultEscaper, "한", "%ED%95%9C"),
            Arguments.arguments(defaultEscaper, "円", "%E5%86%86"),
            Arguments.arguments(defaultEscaper, "\uD800\uDF48", "%F0%90%8D%88"),
            Arguments.arguments(defaultEscaper, "日本語", "%E6%97%A5%E6%9C%AC%E8%AA%9E"),
            Arguments.arguments(defaultEscaper, " ", "%20"),
            Arguments.arguments(new PercentEscaper(null, true), " ", "+"),
            Arguments.arguments(new PercentEscaper("$", false), "$", "$")
        );
    }
}
