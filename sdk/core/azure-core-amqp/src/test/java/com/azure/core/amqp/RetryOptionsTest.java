// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class RetryOptionsTest {
    /**
     * Test there are defaults set when creating RetryOptions
     */
    @Test
    public void constructor() {
        // Arrange
        final Duration defaultTimeout = Duration.ofMinutes(1);
        final int maxRetries = 3;

        // Act
        final RetryOptions options = new RetryOptions();

        // Assert
        Assert.assertEquals(maxRetries, options.getMaxRetries());
        Assert.assertEquals(RetryMode.EXPONENTIAL, options.getRetryMode());
        Assert.assertEquals(defaultTimeout, options.getMaxDelay());
        Assert.assertEquals(defaultTimeout, options.getTryTimeout());
    }

    /**
     * Verifies we can set new properties.
     */
    @Test
    public void canSetProperties() {
        // Arrange
        final Duration delay = Duration.ofMillis(1000);
        final Duration maxDelay = Duration.ofMinutes(10);
        final Duration tryTimeout = Duration.ofMinutes(2);
        final int retries = 10;
        final RetryMode retryMode = RetryMode.FIXED;
        final RetryOptions options = new RetryOptions();

        // Act
        final RetryOptions actual = options.setRetryMode(retryMode)
            .setMaxDelay(maxDelay)
            .setDelay(delay)
            .setMaxRetries(retries)
            .setTryTimeout(tryTimeout);

        // Assert
        Assert.assertEquals(delay, actual.getDelay());
        Assert.assertEquals(maxDelay, actual.getMaxDelay());
        Assert.assertEquals(tryTimeout, actual.getTryTimeout());
        Assert.assertEquals(retries, actual.getMaxRetries());
        Assert.assertEquals(retryMode, actual.getRetryMode());
    }

    /**
     * Verifies that we can clone the RetryOptions object, and its fields change independent of the original.
     */
    @Test
    public void canClone() {
        // Arrange
        final Duration delay = Duration.ofMillis(1000);
        final Duration maxDelay = Duration.ofMinutes(10);
        final Duration tryTimeout = Duration.ofMinutes(2);
        final int retries = 10;
        final RetryMode retryMode = RetryMode.FIXED;

        final Duration newDelay = Duration.ofMillis(700);
        final Duration newMaxDelay = Duration.ofSeconds(90);
        final Duration newTryTimeout = Duration.ofMinutes(2);
        final int newRetries = 5;
        final RetryMode newRetryMode = RetryMode.EXPONENTIAL;

        final RetryOptions original = new RetryOptions().setRetryMode(retryMode)
            .setMaxDelay(maxDelay)
            .setDelay(delay)
            .setMaxRetries(retries)
            .setTryTimeout(tryTimeout);

        // Act
        final RetryOptions clone = original.clone();
        Assert.assertNotNull(clone);
        Assert.assertEquals(original, clone);

        final RetryOptions actual = clone
            .setRetryMode(newRetryMode)
            .setMaxDelay(newMaxDelay)
            .setDelay(newDelay)
            .setMaxRetries(newRetries)
            .setTryTimeout(newTryTimeout);

        // Assert
        Assert.assertNotSame(original, actual);
        Assert.assertEquals(delay, original.getDelay());
        Assert.assertEquals(maxDelay, original.getMaxDelay());
        Assert.assertEquals(tryTimeout, original.getTryTimeout());
        Assert.assertEquals(retries, original.getMaxRetries());
        Assert.assertEquals(retryMode, original.getRetryMode());

        Assert.assertEquals(newDelay, actual.getDelay());
        Assert.assertEquals(newMaxDelay, actual.getMaxDelay());
        Assert.assertEquals(newTryTimeout, actual.getTryTimeout());
        Assert.assertEquals(newRetries, actual.getMaxRetries());
        Assert.assertEquals(newRetryMode, actual.getRetryMode());
    }

    @Test
    public void isEqual() {
        // Arrange
        final RetryOptions first = new RetryOptions()
            .setRetryMode(RetryMode.FIXED)
            .setMaxDelay(Duration.ofMinutes(10))
            .setDelay(Duration.ofMillis(1000))
            .setMaxRetries(10)
            .setTryTimeout(Duration.ofMinutes(2));

        final RetryOptions second = new RetryOptions()
            .setRetryMode(RetryMode.FIXED)
            .setMaxDelay(Duration.ofMinutes(10))
            .setDelay(Duration.ofMillis(1000))
            .setMaxRetries(10)
            .setTryTimeout(Duration.ofMinutes(2));

        Assert.assertEquals(first, second);
        Assert.assertEquals(first.hashCode(), second.hashCode());
    }
}
