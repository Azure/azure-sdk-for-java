// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AsyncRestProxy}
 */
public class AsyncRestProxyTests {

    @Mock
    private SerializerAdapter serializerAdapter;
    @Mock
    private HttpPipeline httpPipeline;
    @Mock
    private SwaggerMethodParser methodParser;
    @Mock
    private SwaggerInterfaceParser interfaceParser;
    @Mock
    private HttpResponseDecoder.HttpDecodedResponse decodedResponse;
    @Mock
    private HttpResponse httpResponse;

    private AsyncRestProxy asyncRestProxy;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        asyncRestProxy = new AsyncRestProxy(httpPipeline, serializerAdapter, interfaceParser);
    }

    @AfterEach
    public void afterEach() throws Exception {
        mocksCloseable.close();
    }

    public static Stream<Arguments> handleBodyReturnTypeBoolean() {
        return Stream.of(
            Arguments.of(boolean.class, 200, true),
            Arguments.of(Boolean.class, 404, false)
        );
    }

    /**
     * Validates scenario for decoding boolean return values.
     */
    @ParameterizedTest
    @MethodSource
    public void handleBodyReturnTypeBoolean(Type returnType, int statusCode, Boolean expectedValue) {

        // Arrange
        when(decodedResponse.getSourceResponse()).thenReturn(httpResponse);

        when(httpResponse.getStatusCode()).thenReturn(statusCode);

        when(methodParser.getHttpMethod()).thenReturn(HttpMethod.HEAD);
        when(methodParser.getReturnValueWireType()).thenReturn(null);

        // Act
        StepVerifier.create(asyncRestProxy.handleBodyReturnType(decodedResponse, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof Boolean);
                assertEquals(value, expectedValue);
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for decoding boolean return values.
     */
    @Test
    public void handleBodyReturnTypeByte() {
        // Arrange
        final String expected = "hello";
        final byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        final Type returnType = byte[].class;
        when(decodedResponse.getSourceResponse()).thenReturn(httpResponse);

        when(httpResponse.getStatusCode()).thenReturn(200);
        when(httpResponse.getBodyAsByteArray()).thenReturn(Mono.just(expectedBytes));

        when(methodParser.getHttpMethod()).thenReturn(HttpMethod.GET);
        when(methodParser.getReturnValueWireType()).thenReturn(null);

        // Act
        StepVerifier.create(asyncRestProxy.handleBodyReturnType(decodedResponse, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof byte[]);
                assertArrayEquals((byte[]) value, expectedBytes);
            })
            .expectComplete()
            .verify();
    }


    /**
     * Validates scenario for decoding input stream.
     */
    @Test
    public void handleBodyReturnTypeInputStream() {
        // Arrange
        final String expected = "hello";
        final byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(expectedBytes);
        final Type returnType = InputStream.class;
        when(decodedResponse.getSourceResponse()).thenReturn(httpResponse);

        when(httpResponse.getStatusCode()).thenReturn(200);
        when(httpResponse.getBodyAsInputStream()).thenReturn(Mono.just(byteArrayInputStream));

        when(methodParser.getHttpMethod()).thenReturn(HttpMethod.GET);
        when(methodParser.getReturnValueWireType()).thenReturn(null);

        // Act
        StepVerifier.create(asyncRestProxy.handleBodyReturnType(decodedResponse, methodParser, returnType))
            .assertNext(value -> {
                assertTrue(value instanceof InputStream);

                try {
                    InputStream inputStream = (InputStream) value;
                    final int available = inputStream.available();

                    assertEquals(expectedBytes.length, available);

                    final byte[] actualBytes = new byte[available];
                    final int read = inputStream.read(actualBytes);

                    assertEquals(available, read, "Should have read same number of bytes available.");
                    assertArrayEquals(expectedBytes, actualBytes);
                } catch (IOException e) {
                    fail("Should not have thrown an error.", e);
                }
            })
            .expectComplete()
            .verify();
    }
}
