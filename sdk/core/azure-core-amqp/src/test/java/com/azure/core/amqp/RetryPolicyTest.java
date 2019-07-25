// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class RetryPolicyTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void defaultRetryPolicy() {
        // Arrange
        final RetryPolicy retry = RetryPolicy.getDefaultRetry();
        final AmqpException exception = new AmqpException(true, ErrorCondition.SERVER_BUSY_ERROR, "error message", errorContext);
        final Duration remainingTime = Duration.ofSeconds(60);

        // Act
        retry.incrementRetryCount();
        final Duration firstRetryInterval = retry.getNextRetryInterval(exception, remainingTime);
        Assert.assertNotNull(firstRetryInterval);

        retry.incrementRetryCount();
        final Duration leftoverTime = remainingTime.minus(firstRetryInterval);
        final Duration secondRetryInterval = retry.getNextRetryInterval(exception, leftoverTime);

        // Assert
        Assert.assertNotNull(secondRetryInterval);
        Assert.assertTrue(secondRetryInterval.toNanos() > firstRetryInterval.toNanos());
    }

    /**
     * Verifies we can increment the retry count.
     */
    @Test
    public void canIncrementRetryCount() {
        RetryPolicy retry = RetryPolicy.getDefaultRetry();
        Assert.assertEquals(0, retry.getRetryCount());
        Assert.assertEquals(0, retry.incrementRetryCount());

        Assert.assertEquals(1, retry.getRetryCount());
        Assert.assertEquals(1, retry.incrementRetryCount());

        Assert.assertEquals(2, retry.getRetryCount());
        Assert.assertEquals(2, retry.incrementRetryCount());

        retry.resetRetryInterval();

        Assert.assertEquals(0, retry.getRetryCount());
        Assert.assertEquals(0, retry.incrementRetryCount());

        Assert.assertEquals(1, retry.getRetryCount());
    }

    @Test
    public void isRetriableException() {
        final Exception exception = new AmqpException(true, "error message", errorContext);
        Assert.assertTrue(RetryPolicy.isRetriableException(exception));
    }

    @Test
    public void notRetriableException() {
        final Exception invalidException = new RuntimeException("invalid exception");
        Assert.assertFalse(RetryPolicy.isRetriableException(invalidException));
    }

    @Test
    public void notRetriableExceptionNotTransient() {
        final Exception invalidException = new AmqpException(false, "Some test exception", errorContext);
        Assert.assertFalse(RetryPolicy.isRetriableException(invalidException));
    }

    /**
     * Verifies that using no retry policy does not allow us to retry a failed request.
     */
    @Test
    public void noRetryPolicy() {
        // Arrange
        final RetryPolicy noRetry = RetryPolicy.getNoRetry();
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration remainingTime = Duration.ofSeconds(60);

        // Act
        final Duration nextRetryInterval = noRetry.getNextRetryInterval(exception, remainingTime);
        int retryCount = noRetry.incrementRetryCount();

        // Assert
        Assert.assertEquals(0, retryCount);
        Assert.assertNull(nextRetryInterval);
    }

    /**
     * Verifies that if we exceed the number of allowed retry attempts, the next retry interval, even if there is time
     * remaining, is null.
     */
    @Test
    public void excessMaxRetry() {
        // Arrange
        final RetryPolicy retry = RetryPolicy.getDefaultRetry();
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration sixtySec = Duration.ofSeconds(60);

        // Simulates that we've tried to retry the max number of requests this allows.
        for (int i = 0; i < retry.getMaxRetryCount(); i++) {
            retry.incrementRetryCount();
        }

        // Act
        final Duration nextRetryInterval = retry.getNextRetryInterval(exception, sixtySec);

        // Assert
        Assert.assertEquals(RetryPolicy.DEFAULT_MAX_RETRY_COUNT, retry.getRetryCount());
        Assert.assertNull(nextRetryInterval);
    }
}
