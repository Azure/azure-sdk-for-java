// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.models.ResponseError;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    @MethodSource("deserializeOffsetDateTimeSupplier")
    public void deserializeJson(String responseWithError, String expectedCode, String expectedMessage) throws IOException {
        ResponseError deserialize = MAPPER.deserialize(responseWithError, ResponseError.class, SerializerEncoding.JSON);
        assertEquals(expectedCode, deserialize.getCode());
        assertEquals(expectedMessage, deserialize.getMessage());
    }

    @Test
    public void deserializeResponseErrorMissingRequiredProperty() {
        // code is a required property and exception should be thrown if it's missing
        String missingCodeWithErrorWrapper = "{\"error\": {\"message\": \"Invalid syntax\"}}";
        Assertions.assertThrows(MismatchedInputException.class, () -> MAPPER.deserialize(missingCodeWithErrorWrapper, ResponseError.class, SerializerEncoding.JSON));

        String missingCodeWithoutErrorWrapper = "{\"message\": \"Invalid syntax\"}";
        Assertions.assertThrows(MismatchedInputException.class, () -> MAPPER.deserialize(missingCodeWithoutErrorWrapper, ResponseError.class, SerializerEncoding.JSON));

        // message is a required property and exception should be thrown if it's missing
        String missingMessageWithErrorWrapper = "{\"error\": {\"code\": \"BAD_QUERY_FORMAT\"}}";
        Assertions.assertThrows(MismatchedInputException.class, () -> MAPPER.deserialize(missingMessageWithErrorWrapper, ResponseError.class, SerializerEncoding.JSON));

        String missingMessageWithoutErrorWrapper = "{\"code\": \"BAD_QUERY_FORMAT\"}";
        Assertions.assertThrows(MismatchedInputException.class, () -> MAPPER.deserialize(missingMessageWithoutErrorWrapper, ResponseError.class, SerializerEncoding.JSON));
    }

    private static Stream<Arguments> deserializeOffsetDateTimeSupplier() {

        return Stream.of(
            Arguments.of("{\"error\": {\"code\": \"BAD_QUERY_FORMAT\", \"message\": \"Invalid syntax\"}}", "BAD_QUERY_FORMAT", "Invalid syntax"),
            Arguments.of("{\"code\": \"BAD_QUERY_FORMAT\", \"message\": \"Invalid syntax\"}", "BAD_QUERY_FORMAT", "Invalid syntax"),
            Arguments.of("{\"name\": \"foo\", \"error\": {\"code\": \"BAD_QUERY_FORMAT\", \"message\": \"Invalid syntax\"}}", "BAD_QUERY_FORMAT", "Invalid syntax"),
            Arguments.of("{\"name\": \"foo\", \"code\": \"BAD_QUERY_FORMAT\", \"message\": \"Invalid syntax\"}", "BAD_QUERY_FORMAT", "Invalid syntax")
        );
    }

}
