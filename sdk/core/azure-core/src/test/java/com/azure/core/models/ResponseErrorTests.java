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
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ResponseError}.
 */
public class ResponseErrorTests {
    @ParameterizedTest
    @MethodSource("toJsonSupplier")
    public void toJsonJsonWriter(ResponseError error, String expectedJson) {
        AccessibleByteArrayOutputStream os = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(os);
        error.toJson(writer);

        assertEquals(expectedJson, os.toString(StandardCharsets.UTF_8));
    }

    private static Stream<Arguments> toJsonSupplier() {
        return Stream.of(
            Arguments.of(new ResponseError("code", "message"), "{\"code\":\"code\",\"message\":\"message\"}"),

            Arguments.of(new ResponseError("code", "message").setTarget("target"),
                "{\"code\":\"code\",\"message\":\"message\",\"target\":\"target\"}"),

            Arguments.of(new ResponseError("code", "message")
                .setInnerError(new ResponseInnerError().setCode("error code")),
                "{\"code\":\"code\",\"message\":\"message\",\"innererror\":"
                    + "{\"code\":\"error code\"}}"),

            Arguments.of(new ResponseError("code", "message").setErrorDetails(new ArrayList<>()),
                "{\"code\":\"code\",\"message\":\"message\",\"details\":[]}"),

            Arguments.of(new ResponseError("code", "message")
                    .setErrorDetails(singletonList(new ResponseError("sub-code", "sub-message"))),
                "{\"code\":\"code\",\"message\":\"message\",\"details\":"
                    + "[{\"code\":\"sub-code\",\"message\":\"sub-message\"}]}"),

            Arguments.of(new ResponseError("code", "message")
                    .setErrorDetails(singletonList(new ResponseError("sub-code", "sub-message")
                        .setErrorDetails(singletonList(new ResponseError("sub-sub-code", "sub-sub-message"))))),
                "{\"code\":\"code\",\"message\":\"message\",\"details\":"
                    + "[{\"code\":\"sub-code\",\"message\":\"sub-message\",\"details\":"
                    + "[{\"code\":\"sub-sub-code\",\"message\":\"sub-sub-message\"}]}]}")
        );
    }

    @ParameterizedTest
    @MethodSource("fromJsonSupplier")
    public void fromJson(String json, ResponseError expectedError) {
        ResponseError actualError = ResponseError.fromJson(DefaultJsonReader.fromString(json));

        validateErrorChain(expectedError, actualError);
    }

    private static void validateErrorChain(ResponseError expected, ResponseError actual) {
        if (expected == null) {
            assertNull(actual, "Expected ResponseError to be null.");
            return;
        }

        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(expected.getTarget(), actual.getTarget());

        validateInnerErrorChain(expected.getInnerError(), actual.getInnerError());

        if (expected.getErrorDetails() == null) {
            assertNull(actual.getErrorDetails(), "Expected ResponseError to have null details.");
        } else {
            assertEquals(expected.getErrorDetails().size(), actual.getErrorDetails().size(),
                () -> String.format("Different amount of error details, expected: %d, actual: %d",
                    expected.getErrorDetails().size(), actual.getErrorDetails().size()));

            for (int i = 0; i < expected.getErrorDetails().size(); i++) {
                validateErrorChain(expected.getErrorDetails().get(i), actual.getErrorDetails().get(i));
            }
        }
    }

    private static void validateInnerErrorChain(ResponseInnerError expected, ResponseInnerError actual) {
        if (expected == null) {
            assertNull(actual, "Expected ResponseInnerError to be null.");
        }

        while (expected != null) {
            assertEquals(expected.getCode(), actual.getCode());
            if (expected.getInnerError() != null) {
                assertNotNull(actual.getInnerError(), "Expected ResponseInnerError contained an inner error.");
            } else {
                assertNull(actual.getInnerError(), "Expected ResponseInnerError didn't contain an inner error.");
            }

            expected = expected.getInnerError();
            actual = actual.getInnerError();
        }
    }

    private static Stream<Arguments> fromJsonSupplier() {
        return Stream.of(
            Arguments.of("null", null),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\"}", new ResponseError("code", "message")),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\",\"target\":\"target\"}",
                new ResponseError("code", "message").setTarget("target")),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\",\"innererror\":{\"code\":\"error code\"}}",
                new ResponseError("code", "message").setInnerError(new ResponseInnerError().setCode("error code"))),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\",\"details\":[]}",
                new ResponseError("code", "message").setErrorDetails(new ArrayList<>())),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\",\"details\":"
                + "[{\"code\":\"sub-code\",\"message\":\"sub-message\"}]}",
                new ResponseError("code", "message").setErrorDetails(
                    singletonList(new ResponseError("sub-code", "sub-message")))),

            Arguments.of("{\"code\":\"code\",\"message\":\"message\",\"details\":"
                + "[{\"code\":\"sub-code\",\"message\":\"sub-message\",\"details\":"
                + "[{\"code\":\"sub-sub-code\",\"message\":\"sub-sub-message\"}]}]}",
                new ResponseError("code", "message").setErrorDetails(
                    singletonList(new ResponseError("sub-code", "sub-message").setErrorDetails(
                        singletonList(new ResponseError("sub-sub-code", "sub-sub-message"))))))
        );
    }

    @ParameterizedTest
    @MethodSource("fromJsonThrowsErrorSupplier")
    public void fromJsonThrowsErrorSupplier(String json, Class<? extends Throwable> expectedErrorType,
        String expectedPartialMessageContent) {
        Throwable throwable = assertThrows(expectedErrorType,
            () -> ResponseError.fromJson(DefaultJsonReader.fromString(json)));

        assertTrue(throwable.getMessage().contains(expectedPartialMessageContent),
            () -> String.format("Expected exceptions '%s' to contain contents '%s'.",
                throwable.getMessage(), expectedPartialMessageContent));
    }

    private static Stream<Arguments> fromJsonThrowsErrorSupplier() {
        return Stream.of(
            Arguments.of("{}", IllegalStateException.class, "'code' and 'message'"),
            Arguments.of("{\"code\":\"code\"}", IllegalStateException.class, "'message'"),
            Arguments.of("{\"message\":\"message\"}", IllegalStateException.class, "'code'")
        );
    }
}
