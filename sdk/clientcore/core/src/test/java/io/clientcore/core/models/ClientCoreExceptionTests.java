// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientCoreExceptionTests {
    @Test
    public void wrapIOException() {
        IOException exception = new IOException("Test exception");
        ClientCoreException clientCoreException = ClientCoreException.from(exception);
        assertInstanceOf(IOException.class, clientCoreException.getCause());
        assertEquals(exception.getMessage(), clientCoreException.getMessage());
        assertTrue(clientCoreException.isRetryable());
    }

    @Test
    public void wrapWithMessage() {
        IOException exception = new IOException("Test exception");
        ClientCoreException clientCoreException = ClientCoreException.from("A better message", exception);
        assertInstanceOf(IOException.class, clientCoreException.getCause());
        assertEquals("A better message", clientCoreException.getMessage());
    }

    @Test
    public void wrapWithMessageAndRetryable() {
        IOException exception = new IOException("Test exception");
        ClientCoreException clientCoreException = ClientCoreException.from("A better message", exception, false);
        assertInstanceOf(IOException.class, clientCoreException.getCause());
        assertEquals("A better message", clientCoreException.getMessage());
        assertFalse(clientCoreException.isRetryable());
    }

    @Test
    public void wrapWithNoCause() {
        ClientCoreException clientCoreException = ClientCoreException.from("A better message", null);
        assertEquals("A better message", clientCoreException.getMessage());
        assertNull(clientCoreException.getCause());
        assertTrue(clientCoreException.isRetryable());
    }

    @Test
    public void wrapClientCoreException() {
        ClientCoreException exception = ClientCoreException.from(new IOException("Test exception"));
        assertSame(ClientCoreException.from(exception), exception);
    }

    @Test
    public void wrapClientCoreExceptionWithMessage() {
        ClientCoreException exception = ClientCoreException.from(new IOException("Test exception"));
        ClientCoreException withNewMessage = ClientCoreException.from("A better message", exception);
        assertNotSame(exception, withNewMessage);
        assertEquals("A better message", withNewMessage.getMessage());
        assertSame(exception.getCause(), withNewMessage.getCause());
    }

    @Test
    public void wrapClientCoreExceptionNonRetryable() {
        ClientCoreException exception = ClientCoreException.from(new IOException("Test exception"));
        ClientCoreException nonRetryable = ClientCoreException.from(exception, false);
        assertNotSame(exception, nonRetryable);
        assertEquals(exception.getMessage(), nonRetryable.getMessage());
        assertSame(exception.getCause(), nonRetryable.getCause());
        assertFalse(nonRetryable.isRetryable());
    }

    @Test
    public void uncheckedIOException() {
        IOException ioException = new IOException("Test exception");
        UncheckedIOException uncheckedIOException = new UncheckedIOException(ioException);
        ClientCoreException exception = ClientCoreException.from(uncheckedIOException);
        assertSame(ioException, exception.getCause());
        assertEquals(uncheckedIOException.getMessage(), exception.getMessage());
        assertTrue(exception.isRetryable());
    }

    @Test
    public void uncheckedIOExceptionWithMessage() {
        IOException ioException = new IOException("Test exception");
        UncheckedIOException uncheckedIOException = new UncheckedIOException("A better message", ioException);
        ClientCoreException exception = ClientCoreException.from(uncheckedIOException, false);
        assertSame(ioException, exception.getCause());
        assertEquals(uncheckedIOException.getMessage(), exception.getMessage());
        assertFalse(exception.isRetryable());
    }

    @ParameterizedTest
    @MethodSource("retryableCauses")
    public void testRetryable(Exception ex, boolean isRetryable) {
        assertEquals(isRetryable, ClientCoreException.from(ex).isRetryable());
    }

    public static Stream<Arguments> retryableCauses() {
        return Stream.of(Arguments.of(new IOException("Test exception"), true),
            Arguments.of(new FileNotFoundException("Test exception"), false),
            Arguments.of(new UncheckedIOException("Test exception", new IOException("Test exception")), true),
            Arguments.of(ClientCoreException.from("Test exception", new FileNotFoundException(), true), true),
            Arguments.of(ClientCoreException.from(new IOException("Test exception"), false), false));
    }

    @Test
    public void testEmpty() {
        // does not throw
        ClientCoreException clientCoreException = ClientCoreException.from(null, null);
        assertNull(clientCoreException.getCause());
        assertNull(clientCoreException.getMessage());
        assertTrue(clientCoreException.isRetryable());
    }

    @Test
    public void testNoMessage() {
        Exception exception = new Exception();
        ClientCoreException clientCoreException = ClientCoreException.from(exception);
        assertNotNull(exception);
        assertNull(clientCoreException.getMessage());
        assertTrue(clientCoreException.isRetryable());
    }

}
