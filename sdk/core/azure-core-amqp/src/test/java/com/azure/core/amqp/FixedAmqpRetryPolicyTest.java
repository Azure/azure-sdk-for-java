// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class FixedAmqpRetryPolicyTest {
    private final AmqpErrorContext errorContext = new AmqpErrorContext("test-namespace");
    private final AmqpException exception = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "error message", errorContext);
    private final Duration minBackoff = Duration.ofSeconds(15);
    private final Duration maxBackoff = Duration.ofSeconds(60);
    private final Duration tolerance = Duration.ofSeconds(1);
    private final int retryAttempts = 5;
    private final AmqpRetryOptions options = new AmqpRetryOptions()
        .setDelay(minBackoff)
        .setMaxDelay(maxBackoff)
        .setMaxRetries(retryAttempts)
        .setMode(AmqpRetryMode.FIXED);

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void retryDurationIsTheSame() {
        // Arrange
        final FixedAmqpRetryPolicy retry = new FixedAmqpRetryPolicy(options);

        // Act
        final Duration firstRetryInterval = retry.calculateRetryDelay(exception, 1);
        final Duration secondRetryInterval = retry.calculateRetryDelay(exception, 2);

        // Assert
        Assertions.assertNotNull(firstRetryInterval);
        Assertions.assertNotNull(secondRetryInterval);

        // Assert that the second retry interval is within our jitter threshold.
        final Duration minValue = firstRetryInterval.minus(tolerance);
        final Duration maxValue = firstRetryInterval.plus(tolerance);
        Assertions.assertTrue(minValue.compareTo(secondRetryInterval) < 0
            && maxValue.compareTo(secondRetryInterval) > 0);
    }

    /**
     * Verify that two instances created with the same set of RetryOptions are equal.
     */
    @Test
    public void isEquals() {
        // Arrange
        final FixedAmqpRetryPolicy policy = new FixedAmqpRetryPolicy(options);

        final AmqpRetryOptions otherOptions = new AmqpRetryOptions()
            .setDelay(minBackoff)
            .setMaxDelay(maxBackoff)
            .setMaxRetries(retryAttempts)
            .setMode(AmqpRetryMode.FIXED);
        final FixedAmqpRetryPolicy otherPolicy = new FixedAmqpRetryPolicy(otherOptions);

        // Assert
        Assertions.assertEquals(policy, otherPolicy);
        Assertions.assertEquals(policy.hashCode(), otherPolicy.hashCode());
    }
}
