// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class RetryTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void defaultRetryPolicy() {
        // Arrange
        final Retry retry = Retry.getDefaultRetry();
        final AmqpException exception = new AmqpException(true, ErrorCondition.SERVER_BUSY_ERROR, "error message", errorContext);
        final Duration remainingTime = Duration.ofSeconds(60);

        // Act
        retry.incrementRetryCount();
        final Duration firstRetryInterval = retry.getNextRetryInterval(exception, remainingTime);
        assertNotNull(firstRetryInterval);

        retry.incrementRetryCount();
        final Duration leftoverTime = remainingTime.minus(firstRetryInterval);
        final Duration secondRetryInterval = retry.getNextRetryInterval(exception, leftoverTime);

        // Assert
        assertNotNull(secondRetryInterval);
        assertTrue(secondRetryInterval.toNanos() > firstRetryInterval.toNanos());
    }

    /**
     * Verifies we can increment the retry count.
     */
    @Test
    public void canIncrementRetryCount() {
        Retry retry = Retry.getDefaultRetry();
        assertEquals(0, retry.getRetryCount());
        assertEquals(0, retry.incrementRetryCount());

        assertEquals(1, retry.getRetryCount());
        assertEquals(1, retry.incrementRetryCount());

        assertEquals(2, retry.getRetryCount());
        assertEquals(2, retry.incrementRetryCount());

        retry.resetRetryInterval();

        assertEquals(0, retry.getRetryCount());
        assertEquals(0, retry.incrementRetryCount());

        assertEquals(1, retry.getRetryCount());
    }

    @Test
    public void isRetriableException() {
        final Exception exception = new AmqpException(true, "error message", errorContext);
        assertTrue(Retry.isRetriableException(exception));
    }

    @Test
    public void notRetriableException() {
        final Exception invalidException = new RuntimeException("invalid exception");
        assertFalse(Retry.isRetriableException(invalidException));
    }

    @Test
    public void notRetriableExceptionNotTransient() {
        final Exception invalidException = new AmqpException(false, "Some test exception", errorContext);
        assertFalse(Retry.isRetriableException(invalidException));
    }

    /**
     * Verifies that using no retry policy does not allow us to retry a failed request.
     */
    @Test
    public void noRetryPolicy() {
        // Arrange
        final Retry noRetry = Retry.getNoRetry();
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration remainingTime = Duration.ofSeconds(60);

        // Act
        final Duration nextRetryInterval = noRetry.getNextRetryInterval(exception, remainingTime);
        int retryCount = noRetry.incrementRetryCount();

        // Assert
        assertEquals(0, retryCount);
        assertNull(nextRetryInterval);
    }

    /**
     * Verifies that if we exceed the number of allowed retry attempts, the next retry interval, even if there is time
     * remaining, is null.
     */
    @Test
    public void excessMaxRetry() {
        // Arrange
        final Retry retry = Retry.getDefaultRetry();
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration sixtySec = Duration.ofSeconds(60);

        // Simulates that we've tried to retry the max number of requests this allows.
        for (int i = 0; i < retry.getMaxRetryCount(); i++) {
            retry.incrementRetryCount();
        }

        // Act
        final Duration nextRetryInterval = retry.getNextRetryInterval(exception, sixtySec);

        // Assert
        assertEquals(Retry.DEFAULT_MAX_RETRY_COUNT, retry.getRetryCount());
        assertNull(nextRetryInterval);
    }
}
