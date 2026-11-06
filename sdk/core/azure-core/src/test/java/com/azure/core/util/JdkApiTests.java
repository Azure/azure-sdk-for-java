// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdkApiTests {
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 16, 256, 1025 })
    public void hexadecimalFormattingPreservesAllByteValues(int length) {
        byte[] bytes = new byte[length];
        StringBuilder expected = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
            expected.append(String.format(Locale.ROOT, "%02x", bytes[i] & 0xff));
        }
        byte[] original = bytes.clone();

        assertEquals(expected.toString(), CoreUtils.bytesToHexString(bytes));
        assertArrayEquals(original, bytes);
    }

    @ParameterizedTest
    @NullSource
    public void nullHexadecimalInputIsPreserved(byte[] bytes) {
        assertNull(CoreUtils.bytesToHexString(bytes));
    }

    @ParameterizedTest
    @MethodSource("contentRanges")
    public void rangeParsingPreservesTrimmingAndSignedValues(String header, long expected) {
        assertEquals(expected, CoreUtils.extractSizeFromContentRange(header));
    }

    private static Stream<Arguments> contentRanges() {
        return Stream.of(Arguments.of("bytes 0-5/6", 6L), Arguments.of("bytes */ \t+0006\r\n", 6L),
            Arguments.of("bytes */\u0000*\u001f", -1L), Arguments.of("bytes */-5", -5L),
            Arguments.of("bytes */9223372036854775807", Long.MAX_VALUE),
            Arguments.of("bytes */-9223372036854775808", Long.MIN_VALUE),
            Arguments.of("bytes */\u0661\u0662\u0663", 123L));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "bytes */",
            "bytes */ \t",
            "bytes */9223372036854775808",
            "bytes */-9223372036854775809",
            "bytes */+",
            "bytes */12x",
            "bytes */**",
            "bytes */\u2003123",
            "bytes */\u00a0123" })
    public void invalidRangeSizesRemainNumberFormatErrors(String header) {
        assertThrows(NumberFormatException.class, () -> CoreUtils.extractSizeFromContentRange(header));
    }
}
