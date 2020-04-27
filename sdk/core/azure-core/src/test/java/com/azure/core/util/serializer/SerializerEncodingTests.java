// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link SerializerEncoding}.
 */
public class SerializerEncodingTests {
    /**
     * Tests extracting a {@link SerializerEncoding} from a header set returns the expected encoding.
     */
    @ParameterizedTest
    @MethodSource("fromHeadersSupplier")
    public void fromHeaders(HttpHeaders headers, SerializerEncoding expected) {
        assertEquals(expected, SerializerEncoding.fromHeaders(headers));
    }

    private static Stream<Arguments> fromHeadersSupplier() {
        Function<String, HttpHeaders> headersSupplier = value -> new HttpHeaders().put("Content-Type", value);

        return Stream.of(
            Arguments.arguments(new HttpHeaders(), SerializerEncoding.JSON),
            Arguments.arguments(headersSupplier.apply("junkType"), SerializerEncoding.JSON),
            Arguments.arguments(headersSupplier.apply("application/xml"), SerializerEncoding.XML),
            Arguments.arguments(headersSupplier.apply("text/xml"), SerializerEncoding.XML),
            Arguments.arguments(headersSupplier.apply("text/plain"), SerializerEncoding.TEXT),
            Arguments.arguments(headersSupplier.apply("application/json"), SerializerEncoding.JSON),
            Arguments.arguments(headersSupplier.apply("text/xml; Charset=UTF-8"), SerializerEncoding.XML),
            Arguments.arguments(headersSupplier.apply("text/plain; Charset=UTF-8"), SerializerEncoding.TEXT),
            Arguments.arguments(headersSupplier.apply("application/json; Charset=UTF-8"), SerializerEncoding.JSON)
        );
    }

    /**
     * Tests that extracting a {@link SerializerEncoding} from a null header set will throw an error.
     */
    @Test
    public void fromHeadersNullHeadersThrows() {
        assertThrows(NullPointerException.class, () -> SerializerEncoding.fromHeaders(null));
    }
}
