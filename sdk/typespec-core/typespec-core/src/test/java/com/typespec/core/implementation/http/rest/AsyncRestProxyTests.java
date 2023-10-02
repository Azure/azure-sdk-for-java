// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.annotation.Get;
import com.typespec.core.annotation.Head;
import com.typespec.core.annotation.Host;
import com.typespec.core.annotation.ServiceInterface;
import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.util.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.typespec.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link AsyncRestProxy}
 */
public class AsyncRestProxyTests {

    private SwaggerInterfaceParser swaggerInterfaceParser;



    @Host("https://example.com")
    @ServiceInterface(name = "async-rest-proxy-tests")
    private interface MockService {
        @Head("headBoolean")
        boolean headBoolean();

        @Get("getByteArray")
        byte[] getByteArray();

        @Get("getInputStream")
        InputStream getInputStream();

        @Get("getStreamResponse")
        Flux<ByteBuffer> getStreamResponse();
    }

    @BeforeEach
    public void beforeEach() {
        swaggerInterfaceParser = new SwaggerInterfaceParser(MockService.class);
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
    public void handleBodyReturnTypeBoolean(Type returnType, int statusCode, Boolean expectedValue)
        throws NoSuchMethodException {

        // Arrange
        SwaggerMethodParser methodParser = swaggerInterfaceParser
            .getMethodParser(MockService.class.getDeclaredMethod("headBoolean"));
        HttpResponse httpResponse = new MockHttpResponse(null, statusCode);

        // Act
        StepVerifier.create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser,
                returnType))
            .assertNext(value -> {
                assertTrue(value instanceof Boolean);
                assertEquals(expectedValue, value);
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for decoding boolean return values.
     */
    @Test
    public void handleBodyReturnTypeByte() throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser = swaggerInterfaceParser
            .getMethodParser(MockService.class.getDeclaredMethod("getByteArray"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = byte[].class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, new HttpHeaders(), expectedBytes);

        // Act
        StepVerifier.create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser,
                returnType))
            .assertNext(value -> {
                assertTrue(value instanceof byte[]);
                assertArraysEqual(expectedBytes, (byte[]) value);
            })
            .expectComplete()
            .verify();
    }


    /**
     * Validates scenario for decoding input stream.
     */
    @Test
    public void handleBodyReturnTypeInputStream() throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser = swaggerInterfaceParser
            .getMethodParser(MockService.class.getDeclaredMethod("getInputStream"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = InputStream.class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, new HttpHeaders(), expectedBytes);

        // Act
        StepVerifier.create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser,
                returnType))
            .assertNext(value -> {
                assertTrue(value instanceof InputStream);

                try {
                    InputStream inputStream = (InputStream) value;
                    final int available = inputStream.available();

                    assertEquals(expectedBytes.length, available);

                    final byte[] actualBytes = new byte[available];
                    final int read = inputStream.read(actualBytes);

                    assertEquals(available, read, "Should have read same number of bytes available.");
                    assertArraysEqual(expectedBytes, actualBytes);
                } catch (IOException e) {
                    fail("Should not have thrown an error.", e);
                }
            })
            .expectComplete()
            .verify();
    }

    /**
     * Validates scenario for a response with different content type headers that maps to a
     * replayable BinaryData if the content type is not a stream.
     */
    @ParameterizedTest
    @MethodSource("getResponseHeaderAndReplayability")
    public void handleBodyReturnTypeStream(HttpHeaders headers, boolean isReplayable) throws NoSuchMethodException {
        // Arrange
        SwaggerMethodParser methodParser = swaggerInterfaceParser
            .getMethodParser(MockService.class.getDeclaredMethod("getStreamResponse"));

        final byte[] expectedBytes = "hello".getBytes(StandardCharsets.UTF_8);
        final Type returnType = BinaryData.class;

        HttpResponse httpResponse = new MockHttpResponse(null, 200, headers, expectedBytes);

        // Act
        StepVerifier.create(AsyncRestProxy.handleBodyReturnType(httpResponse, ignored -> null, methodParser,
                returnType))
            .assertNext(value -> {
                assertTrue(value instanceof BinaryData);
                BinaryData binaryData = (BinaryData) value;
                assertEquals(isReplayable, binaryData.isReplayable());
                byte[] actualBytes = binaryData.toBytes();
                assertEquals(expectedBytes.length, actualBytes.length);
                assertArraysEqual(expectedBytes, actualBytes);
            })
            .expectComplete()
            .verify();
    }

    public static Stream<Arguments> getResponseHeaderAndReplayability() {
        return Stream.of(
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM), true),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream"), false),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON), true),
            Arguments.of(new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"), true)
        );
    }
}
