// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class RetryPolicyTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");
    private final int maxRetries = 10;
    private final Duration maxDelay = Duration.ofSeconds(120);
    private final Duration delay = Duration.ofSeconds(20);
    private final RetryOptions options = new RetryOptions()
        .maxRetries(maxRetries)
        .maxDelay(maxDelay)
        .delay(delay);

    /**
     * Verifies we retry on a retriable AmqpException.
     */
    @Test
    public void isRetriableException() {
        // Arrange
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration expected = Duration.ofSeconds(40);
        final int count = 2;
        final RetryPolicy policy = new MockRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(maxRetries, policy.getMaxRetries());
    }

    /**
     * Verifies that a timeout exception will allow for retries.
     */
    @Test
    public void isTimeoutException() {
        // Arrange
        final Exception exception = new TimeoutException("test-message-timeout");
        final Duration expected = Duration.ofSeconds(40);
        final int count = 2;
        final RetryPolicy policy = new MockRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assert.assertEquals(expected, actual);
    }

    /**
     * Verifies that null is returned if the exception is not an AmqpException.
     */
    @Test
    public void notRetriableException() {
        // Arrange
        final Exception invalidException = new RuntimeException("invalid exception");
        final Duration expected = Duration.ofSeconds(40);
        final int count = 2;
        final RetryPolicy policy = new MockRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(invalidException, count);

        // Assert
        Assert.assertNull(actual);
    }

    /**
     * Verifies that null is returned if the AmqpException is not transient.
     */
    @Test
    public void notRetriableExceptionNotTransient() {
        // Arrange
        final Exception invalidException = new AmqpException(false, "Some test exception", errorContext);
        final Duration expected = Duration.ofSeconds(40);
        final int count = 2;
        final RetryPolicy policy = new MockRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(invalidException, count);

        // Assert
        Assert.assertNull(actual);
    }

    /**
     * Verifies that we return {@link RetryOptions#maxDelay()} if the returned delay is larger than the maximum.
     */
    @Test
    public void returnsMaxDelayIfDelayLarger() {
        // Arrange
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration returnedDelay = maxDelay.plus(Duration.ofMillis(50));
        final int count = 2;
        final RetryPolicy policy = new MockRetryPolicy(options, returnedDelay);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assert.assertEquals(maxDelay, actual);
        Assert.assertEquals(maxRetries, policy.getMaxRetries());
    }

    private class MockRetryPolicy extends RetryPolicy {
        private final Duration expectedDuration;

        /**
         * Creates an instance with the given retry options.
         *
         * @param retryOptions The options to set on this retry policy.
         */
        MockRetryPolicy(RetryOptions retryOptions, Duration expectedDuration) {
            super(retryOptions);
            this.expectedDuration = expectedDuration;
        }

        @Override
        protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter, ThreadLocalRandom random) {
            return expectedDuration;
        }

        @Override
        public RetryPolicy clone() {
            return new MockRetryPolicy(getRetryOptions(), expectedDuration);
        }
    }
}
