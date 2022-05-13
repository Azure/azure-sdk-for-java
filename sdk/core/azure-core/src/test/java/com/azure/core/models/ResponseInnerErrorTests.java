// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.json.DefaultJsonReader;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonWriter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link ResponseInnerError}.
 */
public class ResponseInnerErrorTests {
    @ParameterizedTest
    @MethodSource("toJsonSupplier")
    public void toJsonJsonWriter(ResponseInnerError innerError, String expectedJson) {
        AccessibleByteArrayOutputStream os = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(os);
        innerError.toJson(writer);

        assertEquals(expectedJson, os.toString(StandardCharsets.UTF_8));
    }

    private static Stream<Arguments> toJsonSupplier() {
        return Stream.of(
            Arguments.of(new ResponseInnerError().setCode("error code"),
                "{\"code\":\"error code\"}"),

            Arguments.of(new ResponseInnerError().setCode("error code")
                    .setInnerError(new ResponseInnerError().setCode("sub-error code")),
                "{\"code\":\"error code\",\"innererror\":{\"code\":\"sub-error code\"}}")
        );
    }

    @ParameterizedTest
    @MethodSource("fromJsonSupplier")
    public void fromJson(String json, ResponseInnerError expectedInnerError) {
        ResponseInnerError actualError = ResponseInnerError.fromJson(DefaultJsonReader.fromString(json));

        if (actualError == null) {
            assertNull(expectedInnerError, "Expected ResponseInnerError to be null.");
        }

        while (actualError != null) {
            assertEquals(expectedInnerError.getCode(), actualError.getCode());
            if (expectedInnerError.getInnerError() != null) {
                assertNotNull(actualError.getInnerError(), "Expected ResponseInnerError contained an inner error.");
                expectedInnerError = expectedInnerError.getInnerError();
                actualError = actualError.getInnerError();
            } else {
                assertNull(actualError.getInnerError(), "Expected ResponseInnerError didn't contain an inner error.");
                actualError = null; // Terminate the loop.
            }
        }
    }

    private static Stream<Arguments> fromJsonSupplier() {
        return Stream.of(
            // A "null" JSON string should indicate that the object is at the root and null.
            Arguments.of("null", null),

            // Non-null ResponseInnerError with null inner error.
            Arguments.of("{\"code\":\"error code\",\"innererror\":null}",
                new ResponseInnerError().setCode("error code")),

            // Non-null ResponseInnerError with a non-null inner error.
            Arguments.of("{\"code\":\"error code\",\"innererror\":{\"code\":\"sub-error code\",\"innererror\":null}}",
                new ResponseInnerError().setCode("error code")
                    .setInnerError(new ResponseInnerError().setCode("sub-error code")))
        );
    }
}
