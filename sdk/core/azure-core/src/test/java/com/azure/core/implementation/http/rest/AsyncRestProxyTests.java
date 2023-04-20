// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
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
}
