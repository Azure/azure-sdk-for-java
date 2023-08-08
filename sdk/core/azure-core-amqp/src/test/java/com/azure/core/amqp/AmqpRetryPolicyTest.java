// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

public class AmqpRetryPolicyTest {
    private final AmqpErrorContext errorContext = new AmqpErrorContext("test-namespace");
    private final int maxRetries = 10;
    private final Duration maxDelay = Duration.ofSeconds(120);
    private final Duration delay = Duration.ofSeconds(20);
    private final AmqpRetryOptions options = new AmqpRetryOptions()
        .setMaxRetries(maxRetries)
        .setMaxDelay(maxDelay)
        .setDelay(delay);

    /**
     * Verifies we retry on a retriable AmqpException.
     */
    @Test
    public void isRetriableException() {
        // Arrange
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration expected = Duration.ofSeconds(40);
        final int count = 2;
        final AmqpRetryPolicy policy = new MockAmqpRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assertions.assertEquals(expected, actual);
        Assertions.assertEquals(maxRetries, policy.getMaxRetries());
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
        final AmqpRetryPolicy policy = new MockAmqpRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assertions.assertEquals(expected, actual);
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
        final AmqpRetryPolicy policy = new MockAmqpRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(invalidException, count);

        // Assert
        Assertions.assertNull(actual);
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
        final AmqpRetryPolicy policy = new MockAmqpRetryPolicy(options, expected);

        // Act
        final Duration actual = policy.calculateRetryDelay(invalidException, count);

        // Assert
        Assertions.assertNull(actual);
    }

    /**
     * Verifies that we return {@link AmqpRetryOptions#getMaxDelay()} if the returned delay is larger than the maximum.
     */
    @Test
    public void returnsMaxDelayIfDelayLarger() {
        // Arrange
        final Exception exception = new AmqpException(true, "error message", errorContext);
        final Duration returnedDelay = maxDelay.plus(Duration.ofMillis(50));
        final int count = 2;
        final AmqpRetryPolicy policy = new MockAmqpRetryPolicy(options, returnedDelay);

        // Act
        final Duration actual = policy.calculateRetryDelay(exception, count);

        // Assert
        Assertions.assertEquals(maxDelay, actual);
        Assertions.assertEquals(maxRetries, policy.getMaxRetries());
    }

    private class MockAmqpRetryPolicy extends AmqpRetryPolicy {
        private final Duration expectedDuration;

        /**
         * Creates an instance with the given retry options.
         *
         * @param retryOptions The options to set on this retry policy.
         */
        MockAmqpRetryPolicy(AmqpRetryOptions retryOptions, Duration expectedDuration) {
            super(retryOptions);
            this.expectedDuration = expectedDuration;
        }

        @Override
        protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter, ThreadLocalRandom random) {
            return expectedDuration;
        }

        @Override
        public AmqpRetryPolicy clone() {
            return new MockAmqpRetryPolicy(getRetryOptions(), expectedDuration);
        }
    }
}
