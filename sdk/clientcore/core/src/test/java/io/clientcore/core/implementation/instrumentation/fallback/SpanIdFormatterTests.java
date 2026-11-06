// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpanIdFormatterTests {
    @ParameterizedTest
    @MethodSource("spanIds")
    public void formatsFixedWidthLowercaseIds(long value, String expected) {
        assertEquals(expected, SpanIdFormatter.format(value));
    }

    private static Stream<Arguments> spanIds() {
        return Stream.of(Arguments.of(0L, "0000000000000000"), Arguments.of(1L, "0000000000000001"),
            Arguments.of(0x1234L, "0000000000001234"), Arguments.of(-1L, "ffffffffffffffff"),
            Arguments.of(Long.MIN_VALUE, "8000000000000000"), Arguments.of(Long.MAX_VALUE, "7fffffffffffffff"));
    }

    @Test
    public void generatedIdsKeepTheirExistingFormat() {
        String spanId = RandomIdUtils.generateSpanId();
        String traceId = RandomIdUtils.generateTraceId();
        assertTrue(spanId.matches("[0-9a-f]{16}"));
        assertTrue(traceId.matches("[0-9a-f]{32}"));
        assertNotEquals(RandomIdUtils.INVALID_SPAN_ID, spanId);
        assertNotEquals(RandomIdUtils.INVALID_TRACE_ID, traceId);
    }
}
