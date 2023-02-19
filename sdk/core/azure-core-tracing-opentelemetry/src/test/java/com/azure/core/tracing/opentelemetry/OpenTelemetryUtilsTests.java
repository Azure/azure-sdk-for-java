// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link OpenTelemetryUtils}.
 */
public class OpenTelemetryUtilsTests {
    private static final String TEST_MESSAGE = "A random message.";

    public static Stream<Arguments> setErrorWithMessage() {
        return Stream.of(
            Arguments.of(TEST_MESSAGE, StatusCode.ERROR, TEST_MESSAGE),
            Arguments.of(OpenTelemetryUtils.SUCCESS_STATUS_MESSAGE, StatusCode.OK, null),
            Arguments.of(OpenTelemetryUtils.ERROR_STATUS_MESSAGE, StatusCode.ERROR, null)
        );
    }

    /**
     * Verifies the span when there is no error passed in.  The contents of the error message are checked.
     */
    @MethodSource
    @ParameterizedTest
    public void setErrorWithMessage(String errorMessage, StatusCode expectedStatus, String expectedDescription) {
        // Arrange
        final Span span = mock(Span.class);

        // Act
        OpenTelemetryUtils.setError(span, errorMessage, null);

        // Assert
        if (expectedDescription != null) {
            verify(span).setStatus(expectedStatus, expectedDescription);
        } else {
            verify(span).setStatus(expectedStatus);
        }
    }

    /**
     * Verify the throwable is set in the span and its message is used.
     */
    @Test
    public void setErrorThrowable() {
        // Arrange
        final Span span = mock(Span.class);
        final Throwable throwable = new TestException();

        // Act
        OpenTelemetryUtils.setError(span, TEST_MESSAGE, throwable);

        // Assert
        verify(span).recordException(throwable);
        verify(span).setStatus(StatusCode.ERROR, TEST_MESSAGE);
    }

    /**
     * Verify that the span is not modified when no message or error is passed in.
     */
    @Test
    public void setErrorNoThrowableOrMessage() {
        // Arrange
        final Span span = mock(Span.class);

        // Act
        OpenTelemetryUtils.setError(span, null, null);

        // Assert
        verifyNoInteractions(span);
    }

    private static final class TestException extends Throwable {
        static final String ERROR_MESSAGE = "Test exception error message.";

        private TestException() {
            super(ERROR_MESSAGE);
        }
    }
}
