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
        Assert.assertEquals(maxRetries, options.maxRetries());
        Assert.assertEquals(RetryMode.EXPONENTIAL, options.retryMode());
        Assert.assertEquals(defaultTimeout, options.maxDelay());
        Assert.assertEquals(defaultTimeout, options.tryTimeout());
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
        final RetryOptions actual = options.retryMode(retryMode)
            .maxDelay(maxDelay)
            .delay(delay)
            .maxRetries(retries)
            .tryTimeout(tryTimeout);

        // Assert
        Assert.assertEquals(delay, actual.delay());
        Assert.assertEquals(maxDelay, actual.maxDelay());
        Assert.assertEquals(tryTimeout, actual.tryTimeout());
        Assert.assertEquals(retries, actual.maxRetries());
        Assert.assertEquals(retryMode, actual.retryMode());
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

        final RetryOptions original = new RetryOptions().retryMode(retryMode)
            .maxDelay(maxDelay)
            .delay(delay)
            .maxRetries(retries)
            .tryTimeout(tryTimeout);

        // Act
        final Object clone = original.clone();
        Assert.assertTrue(clone instanceof RetryOptions);

        final RetryOptions actual = ((RetryOptions) clone)
            .retryMode(newRetryMode)
            .maxDelay(newMaxDelay)
            .delay(newDelay)
            .maxRetries(newRetries)
            .tryTimeout(newTryTimeout);

        // Assert
        Assert.assertNotSame(original, actual);
        Assert.assertEquals(delay, original.delay());
        Assert.assertEquals(maxDelay, original.maxDelay());
        Assert.assertEquals(tryTimeout, original.tryTimeout());
        Assert.assertEquals(retries, original.maxRetries());
        Assert.assertEquals(retryMode, original.retryMode());

        Assert.assertEquals(newDelay, actual.delay());
        Assert.assertEquals(newMaxDelay, actual.maxDelay());
        Assert.assertEquals(newTryTimeout, actual.tryTimeout());
        Assert.assertEquals(newRetries, actual.maxRetries());
        Assert.assertEquals(newRetryMode, actual.retryMode());
    }
}
