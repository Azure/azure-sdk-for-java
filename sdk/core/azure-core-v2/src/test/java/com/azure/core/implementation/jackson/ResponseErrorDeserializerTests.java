// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.models.ResponseError;
import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ResponseErrorDeserializer}.
 */
public class ResponseErrorDeserializerTests {
    private static final JacksonAdapter MAPPER = new JacksonAdapter();

    @ParameterizedTest
    @MethodSource("deserializeResponseErrorSupplier")
    @Execution(ExecutionMode.SAME_THREAD)
    public void deserializeJson(String responseWithError, String expectedCode, String expectedMessage)
        throws IOException {
        ResponseError deserialize = MAPPER.deserialize(responseWithError, ResponseError.class, SerializerEncoding.JSON);
        assertEquals(expectedCode, deserialize.getCode());
        assertEquals(expectedMessage, deserialize.getMessage());
    }

    private static Stream<Arguments> deserializeResponseErrorSupplier() {
        return Stream.of(
            Arguments.of("{\"error\":{\"code\":\"BAD_QUERY_FORMAT\",\"message\":\"Invalid syntax\"}}",
                "BAD_QUERY_FORMAT", "Invalid syntax"),
            Arguments.of("{\"code\":\"BAD_QUERY_FORMAT\",\"message\":\"Invalid syntax\"}", "BAD_QUERY_FORMAT",
                "Invalid syntax"),
            Arguments.of("{\"name\":\"foo\",\"error\":{\"code\":\"BAD_QUERY_FORMAT\",\"message\":\"Invalid syntax\"}}",
                "BAD_QUERY_FORMAT", "Invalid syntax"),
            Arguments.of("{\"name\":\"foo\",\"code\":\"BAD_QUERY_FORMAT\",\"message\":\"Invalid syntax\"}",
                "BAD_QUERY_FORMAT", "Invalid syntax"));
    }
}
