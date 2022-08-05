// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests instantiating {@link GsonJsonReader} and {@link GsonJsonWriter}.
 */
public class GsonJsonInstantiationTests {
    @ParameterizedTest
    @MethodSource("throwsNullPointerExceptionSupplier")
    public void throwsNullPointerException(Executable executable) {
        assertThrows(NullPointerException.class, executable);
    }

    @SuppressWarnings("resource")
    private static Stream<Executable> throwsNullPointerExceptionSupplier() {
        return Stream.of(
            () -> GsonJsonReader.fromBytes(null),
            () -> GsonJsonReader.fromString(null),
            () -> GsonJsonReader.fromStream(null),

            () -> GsonJsonWriter.toStream(null)
        );
    }
}
