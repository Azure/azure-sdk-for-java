// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class FixedRetryPolicyTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");
    private final AmqpException exception = new AmqpException(true, ErrorCondition.SERVER_BUSY_ERROR, "error message", errorContext);
    private final Duration minBackoff = Duration.ofSeconds(15);
    private final Duration maxBackoff = Duration.ofSeconds(60);
    private final Duration tolerance = Duration.ofSeconds(1);
    private final int retryAttempts = 5;
    private final RetryOptions options = new RetryOptions()
        .delay(minBackoff)
        .maxDelay(maxBackoff)
        .maxRetries(retryAttempts)
        .retryMode(RetryMode.FIXED);

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void retryDurationIsTheSame() {
        // Arrange
        final FixedRetryPolicy retry = new FixedRetryPolicy(options);

        // Act
        final Duration firstRetryInterval = retry.calculateRetryDelay(exception, 1);
        final Duration secondRetryInterval = retry.calculateRetryDelay(exception, 2);

        // Assert
        Assert.assertNotNull(firstRetryInterval);
        Assert.assertNotNull(secondRetryInterval);

        // Assert that the second retry interval is within our jitter threshold.
        final Duration minValue = firstRetryInterval.minus(tolerance);
        final Duration maxValue = firstRetryInterval.plus(tolerance);
        Assert.assertTrue(minValue.compareTo(secondRetryInterval) < 0
            && maxValue.compareTo(secondRetryInterval) > 0);
    }

    /**
     * Verifies that we can clone the retry instance and it behaves the same as its original.
     */
    @Test
    public void retryCloneBehavesSame() {
        // Arrange
        final FixedRetryPolicy retry = new FixedRetryPolicy(options);
        final FixedRetryPolicy clone = (FixedRetryPolicy) retry.clone();

        final Duration retryInterval = retry.calculateRetryDelay(exception, 1);
        final Duration cloneRetryInterval = clone.calculateRetryDelay(exception, 4);

        // Assert
        Assert.assertNotNull(retryInterval);
        Assert.assertNotNull(cloneRetryInterval);

        // Assert that the cloned retry interval is within our jitter threshold.
        final Duration minValue = retryInterval.minus(tolerance);
        final Duration maxValue = retryInterval.plus(tolerance);
        Assert.assertTrue(minValue.compareTo(cloneRetryInterval) < 0
            && maxValue.compareTo(cloneRetryInterval) > 0);
    }

    /**
     * Verify that two instances created with the same set of RetryOptions are equal.
     */
    @Test
    public void isEquals() {
        // Arrange
        final FixedRetryPolicy policy = new FixedRetryPolicy(options);

        final RetryOptions otherOptions = new RetryOptions()
            .delay(minBackoff)
            .maxDelay(maxBackoff)
            .maxRetries(retryAttempts)
            .retryMode(RetryMode.FIXED);
        final FixedRetryPolicy otherPolicy = new FixedRetryPolicy(otherOptions);

        // Assert
        Assert.assertEquals(policy, otherPolicy);
        Assert.assertEquals(policy.hashCode(), otherPolicy.hashCode());
    }

    @Test
    public void retryClone() {
        // Arrange
        final FixedRetryPolicy retry = new FixedRetryPolicy(options);
        final FixedRetryPolicy clone = (FixedRetryPolicy) retry.clone();
        final int retryCount = 1;

        // Act
        final Duration retryInterval = retry.calculateRetryDelay(exception, retryCount);
        final Duration cloneRetryInterval = clone.calculateRetryDelay(exception, retryCount);

        // Assert
        Assert.assertNotSame(retry, clone);
        Assert.assertEquals(retry, clone);
        Assert.assertEquals(retry.hashCode(), clone.hashCode());

        Assert.assertNotNull(retryInterval);
        Assert.assertNotNull(cloneRetryInterval);

        // Assert that the cloned interval is within our jitter threshold.
        final Duration minValue = retryInterval.minus(tolerance);
        final Duration maxValue = retryInterval.plus(tolerance);
        Assert.assertTrue(minValue.compareTo(cloneRetryInterval) < 0
            && maxValue.compareTo(cloneRetryInterval) > 0);
    }
}
