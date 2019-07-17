// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ExponentialRetryTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");
    private final AmqpException exception = new AmqpException(true, ErrorCondition.SERVER_BUSY_ERROR, "error message", errorContext);
    private final Duration minBackoff = Duration.ofSeconds(15);
    private final Duration maxBackoff = Duration.ofSeconds(45);
    private final int retryAttempts = 4;

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void retryDurationIncreases() {
        // Arrange
        final ExponentialRetry retry = new ExponentialRetry(minBackoff, maxBackoff, retryAttempts);
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
     * Verifies that we can clone the retry instance and it behaves the same as its original.
     */
    @Test
    public void retryCloneBehavesSame() {
        // Arrange
        final ExponentialRetry retry = new ExponentialRetry(minBackoff, maxBackoff, retryAttempts);
        final ExponentialRetry clone = (ExponentialRetry) retry.clone();

        final Duration remainingTime = Duration.ofSeconds(60);

        retry.incrementRetryCount();
        final Duration retryInterval = retry.getNextRetryInterval(exception, remainingTime);

        clone.incrementRetryCount();
        clone.incrementRetryCount();
        clone.incrementRetryCount();
        final Duration cloneRetryInterval = clone.getNextRetryInterval(exception, remainingTime);

        // Assert
        assertNotNull(retryInterval);
        assertNotNull(cloneRetryInterval);

        // The retry interval for the clone will be larger because we've incremented the retry count, so it should
        // calculate a longer waiting period.
        assertTrue(cloneRetryInterval.toNanos() > retryInterval.toNanos());
    }

    @Test
    public void retryClone() {
        // Arrange
        final ExponentialRetry retry = new ExponentialRetry(minBackoff, maxBackoff, retryAttempts);
        final ExponentialRetry clone = (ExponentialRetry) retry.clone();

        final Duration remainingTime = Duration.ofSeconds(60);

        retry.incrementRetryCount();
        final Duration retryInterval = retry.getNextRetryInterval(exception, remainingTime);

        clone.incrementRetryCount();
        final Duration cloneRetryInterval = clone.getNextRetryInterval(exception, remainingTime);

        // Assert
        assertNotSame(retry, clone);
        assertEquals(retry, clone);
        assertEquals(retry.hashCode(), clone.hashCode());

        assertNotNull(retryInterval);
        assertNotNull(cloneRetryInterval);
        assertEquals(retryInterval, cloneRetryInterval);
    }
}
