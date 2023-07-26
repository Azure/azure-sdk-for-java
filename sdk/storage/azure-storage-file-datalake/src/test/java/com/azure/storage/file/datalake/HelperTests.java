// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.FileRange;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HelperTests {
    @ParameterizedTest
    @MethodSource("fileRangeSupplier")
    public void fileRange(int offset, Long count, String result) {
        if (count == null) {
            assertEquals(result, new FileRange(offset).toHeaderValue());
        } else {
            assertEquals(result, new FileRange(offset, count).toHeaderValue());
        }
    }

    private static Stream<Arguments> fileRangeSupplier() {
        return Stream.of(
            // offset | count | result
            Arguments.of(0, null, null),
            Arguments.of(0, 5L, "bytes=0-4"),
            Arguments.of(5, 10L, "bytes=5-14")
        );
    }

    @ParameterizedTest
    @CsvSource({"-1,5", "0,-1"})
    public void fileRangeIA(int offset, long count) {
        assertThrows(IllegalArgumentException.class, () -> new FileRange(offset, count));
    }

    private static Stream<Arguments> fileRangeIASupplier() {
        return Stream.of(
            // offset | count
            Arguments.of(-1, 5L),
            Arguments.of(0, -1L)
        );
    }
}
