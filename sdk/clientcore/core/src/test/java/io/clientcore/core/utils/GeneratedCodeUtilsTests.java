// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.MockHttpResponse;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonSerializer;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link GeneratedCodeUtils}.
 */
public class GeneratedCodeUtilsTests {
    /**
     * Test that appendQueryParams correctly appends multi-value query parameters with the specified delimiter.
     */
    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testAppendQueryParams(String uri, String key, List<?> value, String expected) {
        UriBuilder uriBuilder = UriBuilder.parse(uri);
        GeneratedCodeUtils.addQueryParameter(uriBuilder, key, false, value, false);
        assertEquals(expected, uriBuilder.toString(),
            "The URL should be correctly updated with the multi-value query parameter.");
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            // Test cases with no query string
            Arguments.of("https://example.com", "api-version", Collections.singletonList("1.0"),
                "https://example.com?api-version=1.0"),
            Arguments.of("https://example.com", "api-version", Arrays.asList("1.0", "2.0"),
                "https://example.com?api-version=1.0&api-version=2.0"),  // List value with comma delimiter

            // Test cases with existing query string
            Arguments.of("https://example.com?existingParam=value", "api-version", Collections.singletonList("1.0"),
                "https://example.com?existingParam=value&api-version=1.0"),
            Arguments.of("https://example.com?existingParam=value", "api-version", Arrays.asList("1.0", "2.0"),
                "https://example.com?existingParam=value&api-version=1.0&api-version=2.0"),

            // Test cases with empty URL
            Arguments.of("", "api-version", Collections.singletonList("1.0"), "?api-version=1.0"),

            // Test case with a non-empty map and one of the keys having a null value
            Arguments.of("https://example.com", "api-version", null, "https://example.com"));
    }

    @Test
    void testAppendNullQueryParam() {
        UriBuilder uriBuilder = UriBuilder.parse("https://example.com");
        String key = "name";
        String expected = "https://example.com";
        // Null value for parameter
        GeneratedCodeUtils.addQueryParameter(uriBuilder, key, false, null, false);
        assertEquals(expected, uriBuilder.toString(), "The URL should be correctly updated with the query parameter.");
    }

    @Test
    void handleUnexpectedResponseOctetStream() {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream")
            .set(HttpHeaderName.CONTENT_LENGTH, "1024");
        Response<BinaryData> response = new MockHttpResponse(null, 500, headers, BinaryData.fromBytes(new byte[1024]));
        JsonSerializer serializer = JsonSerializer.getInstance();

        Map<Integer, ParameterizedType> exceptionTypeMap = new HashMap<>();
        Exception ex = assertThrows(RuntimeException.class,
            () -> GeneratedCodeUtils.handleUnexpectedResponse(500, response, serializer, null, null, exceptionTypeMap));
        assertTrue(ex.getMessage().contains("(1024-byte body)"));
    }

    @Test
    void handleUnexpectedResponseEmptyBody() {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        Response<BinaryData> response = new MockHttpResponse(null, 404, headers, BinaryData.fromBytes(new byte[0]));
        JsonSerializer serializer = JsonSerializer.getInstance();

        Map<Integer, ParameterizedType> exceptionTypeMap = new HashMap<>();
        Exception ex = assertThrows(RuntimeException.class,
            () -> GeneratedCodeUtils.handleUnexpectedResponse(404, response, serializer, null, null, exceptionTypeMap));
        assertTrue(ex.getMessage().contains("(empty body)"));
    }

    @Test
    void handleUnexpectedResponseJsonBody() {
        String json = "{\"error\":\"not found\"}";
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        Response<BinaryData> response = new MockHttpResponse(null, 400, headers, BinaryData.fromString(json));
        JsonSerializer serializer = JsonSerializer.getInstance();

        Map<Integer, ParameterizedType> exceptionTypeMap = new HashMap<>();
        Exception ex = assertThrows(RuntimeException.class,
            () -> GeneratedCodeUtils.handleUnexpectedResponse(400, response, serializer, null, null, exceptionTypeMap));
        assertTrue(ex.getMessage().contains(json));
        assertTrue(ex.getMessage().contains("\"")); // Should be quoted
    }

    @Test
    void handleUnexpectedResponseUsesTypeMapping() {
        // Arrange
        String json = "{\"error\":\"forbidden\"}";
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        Response<BinaryData> response = new MockHttpResponse(null, 403, headers, BinaryData.fromString(json));

        ParameterizedType mappedType = CoreUtils.createParameterizedType(String.class);
        Map<Integer, ParameterizedType> exceptionTypeMap = new HashMap<>();
        exceptionTypeMap.put(403, mappedType);

        // Use a holder to capture the type passed to the serializer
        final java.lang.reflect.Type[] capturedType = new java.lang.reflect.Type[1];
        JsonSerializer serializer = new TestJsonSerializer(capturedType);

        Exception ex = assertThrows(RuntimeException.class,
            () -> GeneratedCodeUtils.handleUnexpectedResponse(403, response, serializer, null, null, exceptionTypeMap));
        assertTrue(ex.getMessage().contains(json));
        assertTrue(
            (capturedType[0] instanceof Class && capturedType[0].equals(String.class))
                || (capturedType[0] instanceof ParameterizedType
                    && ((ParameterizedType) capturedType[0]).getRawType().equals(String.class)),
            "Should use String.class as the mapped type for status 403");
    }

    @Test
    void handleUnexpectedResponseUsesDefaultErrorBodyType() {
        // Arrange
        String json = "{\"error\":\"default\"}";
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        Response<BinaryData> response = new MockHttpResponse(null, 418, headers, BinaryData.fromString(json));

        ParameterizedType defaultType = CoreUtils.createParameterizedType(String.class);
        final java.lang.reflect.Type[] capturedType = new java.lang.reflect.Type[1];
        JsonSerializer serializer = new TestJsonSerializer(capturedType);

        Exception ex = assertThrows(RuntimeException.class,
            () -> GeneratedCodeUtils.handleUnexpectedResponse(418, response, serializer, null, defaultType, null));
        assertTrue(ex.getMessage().contains(json));
        assertTrue(
            (capturedType[0] instanceof Class && capturedType[0].equals(String.class))
                || (capturedType[0] instanceof ParameterizedType
                    && ((ParameterizedType) capturedType[0]).getRawType().equals(String.class)),
            "Should use String.class as the default error body type for unmapped status 418");
    }

    static class TestJsonSerializer extends JsonSerializer {
        private final java.lang.reflect.Type[] capturedType;

        TestJsonSerializer(java.lang.reflect.Type[] capturedType) {
            this.capturedType = capturedType;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T deserializeFromBytes(byte[] bytes, java.lang.reflect.Type type) {
            capturedType[0] = type;
            return (T) "customType";
        }
    }
}
