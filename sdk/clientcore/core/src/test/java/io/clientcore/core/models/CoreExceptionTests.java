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

public class CoreExceptionTests {
    @Test
    public void wrapIOException() {
        IOException exception = new IOException("Test exception");
        CoreException coreException = CoreException.from(exception);
        assertInstanceOf(IOException.class, coreException.getCause());
        assertEquals(exception.getMessage(), coreException.getMessage());
        assertTrue(coreException.isRetryable());
    }

    @Test
    public void wrapWithMessage() {
        IOException exception = new IOException("Test exception");
        CoreException coreException = CoreException.from("A better message", exception);
        assertInstanceOf(IOException.class, coreException.getCause());
        assertEquals("A better message", coreException.getMessage());
    }

    @Test
    public void wrapWithMessageAndRetryable() {
        IOException exception = new IOException("Test exception");
        CoreException coreException = CoreException.from("A better message", exception, false);
        assertInstanceOf(IOException.class, coreException.getCause());
        assertEquals("A better message", coreException.getMessage());
        assertFalse(coreException.isRetryable());
    }

    @Test
    public void wrapWithNoCause() {
        CoreException coreException = CoreException.from("A better message", null);
        assertEquals("A better message", coreException.getMessage());
        assertNull(coreException.getCause());
        assertTrue(coreException.isRetryable());
    }

    @Test
    public void wrapCoreException() {
        CoreException exception = CoreException.from(new IOException("Test exception"));
        assertSame(CoreException.from(exception), exception);
    }

    @Test
    public void wrapCoreExceptionWithMessage() {
        CoreException exception = CoreException.from(new IOException("Test exception"));
        CoreException withNewMessage = CoreException.from("A better message", exception);
        assertNotSame(exception, withNewMessage);
        assertEquals("A better message", withNewMessage.getMessage());
        assertSame(exception.getCause(), withNewMessage.getCause());
    }

    @Test
    public void wrapCoreExceptionNonRetryable() {
        CoreException exception = CoreException.from(new IOException("Test exception"));
        CoreException nonRetryable = CoreException.from(null, exception, false);
        assertNotSame(exception, nonRetryable);
        assertEquals(exception.getMessage(), nonRetryable.getMessage());
        assertSame(exception.getCause(), nonRetryable.getCause());
        assertFalse(nonRetryable.isRetryable());
    }

    @Test
    public void uncheckedIOException() {
        IOException ioException = new IOException("Test exception");
        UncheckedIOException uncheckedIOException = new UncheckedIOException(ioException);
        CoreException exception = CoreException.from(uncheckedIOException);
        assertSame(ioException, exception.getCause());
        assertEquals(uncheckedIOException.getMessage(), exception.getMessage());
        assertTrue(exception.isRetryable());
    }

    @Test
    public void uncheckedIOExceptionWithMessage() {
        IOException ioException = new IOException("Test exception");
        UncheckedIOException uncheckedIOException = new UncheckedIOException("A better message", ioException);
        CoreException exception = CoreException.from(null, uncheckedIOException, false);
        assertSame(ioException, exception.getCause());
        assertEquals(uncheckedIOException.getMessage(), exception.getMessage());
        assertFalse(exception.isRetryable());
    }

    @ParameterizedTest
    @MethodSource("retryableCauses")
    public void testRetryable(Exception ex, boolean isRetryable) {
        assertEquals(isRetryable, CoreException.from(ex).isRetryable());
    }

    public static Stream<Arguments> retryableCauses() {
        return Stream.of(Arguments.of(new IOException("Test exception"), true),
            Arguments.of(new FileNotFoundException("Test exception"), false),
            Arguments.of(new UncheckedIOException("Test exception", new IOException("Test exception")), true),
            Arguments.of(CoreException.from("Test exception", new FileNotFoundException(), true), true),
            Arguments.of(CoreException.from(null, new IOException("Test exception"), false), false));
    }

    @Test
    public void testEmpty() {
        // does not throw
        CoreException coreException = CoreException.from(null, null);
        assertNull(coreException.getCause());
        assertNull(coreException.getMessage());
        assertTrue(coreException.isRetryable());
    }

    @Test
    public void testNoMessage() {
        Exception exception = new Exception();
        CoreException coreException = CoreException.from(exception);
        assertNotNull(exception);
        assertNull(coreException.getMessage());
        assertTrue(coreException.isRetryable());
    }

}
