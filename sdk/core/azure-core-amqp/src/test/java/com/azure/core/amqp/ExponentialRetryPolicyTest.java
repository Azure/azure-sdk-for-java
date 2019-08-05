// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.exception.ErrorContext;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class ExponentialRetryPolicyTest {
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
        .retryMode(RetryMode.EXPONENTIAL);

    /**
     * Verifies that when the service is busy and we retry an exception multiple times, the retry duration gets longer.
     */
    @Test
    public void retryDurationIncreases() {
        // Arrange

        final ExponentialRetryPolicy retry = new ExponentialRetryPolicy(options);

        // Act
        final Duration firstRetryInterval = retry.calculateRetryDelay(exception, 1);
        final Duration secondRetryInterval = retry.calculateRetryDelay(exception, 2);

        // Assert
        Assert.assertNotNull(firstRetryInterval);
        Assert.assertNotNull(secondRetryInterval);
        Assert.assertTrue(secondRetryInterval.toNanos() > firstRetryInterval.toNanos());
    }

    /**
     * Verifies that we can clone the retry instance and it behaves the same as its original.
     */
    @Test
    public void retryCloneBehavesSame() {
        // Arrange
        final ExponentialRetryPolicy retry = new ExponentialRetryPolicy(options);
        final ExponentialRetryPolicy clone = (ExponentialRetryPolicy) retry.clone();

        final Duration retryInterval = retry.calculateRetryDelay(exception, 1);
        final Duration cloneRetryInterval = clone.calculateRetryDelay(exception, 4);

        // Assert
        Assert.assertNotNull(retryInterval);
        Assert.assertNotNull(cloneRetryInterval);

        // The retry interval for the clone will be larger because we've incremented the retry count, so it should
        // calculate a longer waiting period.
        Assert.assertTrue(cloneRetryInterval.compareTo(retryInterval) > 0);
    }

    /**
     * Verify that two instances created with the same set of RetryOptions are equal.
     */
    @Test
    public void isEquals() {
        // Arrange
        final ExponentialRetryPolicy policy = new ExponentialRetryPolicy(options);

        final RetryOptions otherOptions = new RetryOptions()
            .delay(minBackoff)
            .maxDelay(maxBackoff)
            .maxRetries(retryAttempts)
            .retryMode(RetryMode.EXPONENTIAL);
        final ExponentialRetryPolicy otherPolicy = new ExponentialRetryPolicy(otherOptions);

        // Assert
        Assert.assertEquals(policy, otherPolicy);
        Assert.assertEquals(policy.hashCode(), otherPolicy.hashCode());
    }

    @Test
    public void retryClone() {
        // Arrange
        final ExponentialRetryPolicy retry = new ExponentialRetryPolicy(options);
        final ExponentialRetryPolicy clone = (ExponentialRetryPolicy) retry.clone();
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
