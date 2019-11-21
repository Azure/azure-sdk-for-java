// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class AmqpRetryOptionsTest {
    /**
     * Test there are defaults set when creating RetryOptions
     */
    @Test
    public void constructor() {
        // Arrange
        final Duration defaultTimeout = Duration.ofMinutes(1);
        final int maxRetries = 3;

        // Act
        final AmqpRetryOptions options = new AmqpRetryOptions();

        // Assert
        Assertions.assertEquals(maxRetries, options.getMaxRetries());
        Assertions.assertEquals(AmqpRetryMode.EXPONENTIAL, options.getMode());
        Assertions.assertEquals(defaultTimeout, options.getMaxDelay());
        Assertions.assertEquals(defaultTimeout, options.getTryTimeout());
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
        final AmqpRetryMode retryMode = AmqpRetryMode.FIXED;
        final AmqpRetryOptions options = new AmqpRetryOptions();

        // Act
        final AmqpRetryOptions actual = options.setMode(retryMode)
            .setMaxDelay(maxDelay)
            .setDelay(delay)
            .setMaxRetries(retries)
            .setTryTimeout(tryTimeout);

        // Assert
        Assertions.assertEquals(delay, actual.getDelay());
        Assertions.assertEquals(maxDelay, actual.getMaxDelay());
        Assertions.assertEquals(tryTimeout, actual.getTryTimeout());
        Assertions.assertEquals(retries, actual.getMaxRetries());
        Assertions.assertEquals(retryMode, actual.getMode());
    }

    /**
     * Verifies that we can clone the RetryOptions object, and its fields change independent of the original.
     */
    @Test
    public void copyConstructor() {
        // Arrange
        final Duration delay = Duration.ofMillis(1000);
        final Duration maxDelay = Duration.ofMinutes(10);
        final Duration tryTimeout = Duration.ofMinutes(2);
        final int retries = 10;
        final AmqpRetryMode retryMode = AmqpRetryMode.FIXED;

        final Duration newDelay = Duration.ofMillis(700);
        final Duration newMaxDelay = Duration.ofSeconds(90);
        final Duration newTryTimeout = Duration.ofMinutes(2);
        final int newRetries = 5;
        final AmqpRetryMode newRetryMode = AmqpRetryMode.EXPONENTIAL;

        final AmqpRetryOptions original = new AmqpRetryOptions().setMode(retryMode)
            .setMaxDelay(maxDelay)
            .setDelay(delay)
            .setMaxRetries(retries)
            .setTryTimeout(tryTimeout);

        // Act
        final AmqpRetryOptions clone = new AmqpRetryOptions(original);
        Assertions.assertNotNull(clone);
        Assertions.assertEquals(original, clone);

        final AmqpRetryOptions actual = clone
            .setMode(newRetryMode)
            .setMaxDelay(newMaxDelay)
            .setDelay(newDelay)
            .setMaxRetries(newRetries)
            .setTryTimeout(newTryTimeout);

        // Assert
        Assertions.assertNotSame(original, actual);
        Assertions.assertEquals(delay, original.getDelay());
        Assertions.assertEquals(maxDelay, original.getMaxDelay());
        Assertions.assertEquals(tryTimeout, original.getTryTimeout());
        Assertions.assertEquals(retries, original.getMaxRetries());
        Assertions.assertEquals(retryMode, original.getMode());

        Assertions.assertEquals(newDelay, actual.getDelay());
        Assertions.assertEquals(newMaxDelay, actual.getMaxDelay());
        Assertions.assertEquals(newTryTimeout, actual.getTryTimeout());
        Assertions.assertEquals(newRetries, actual.getMaxRetries());
        Assertions.assertEquals(newRetryMode, actual.getMode());
    }

    @Test
    public void isEqual() {
        // Arrange
        final AmqpRetryOptions first = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setMaxDelay(Duration.ofMinutes(10))
            .setDelay(Duration.ofMillis(1000))
            .setMaxRetries(10)
            .setTryTimeout(Duration.ofMinutes(2));

        final AmqpRetryOptions second = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setMaxDelay(Duration.ofMinutes(10))
            .setDelay(Duration.ofMillis(1000))
            .setMaxRetries(10)
            .setTryTimeout(Duration.ofMinutes(2));

        Assertions.assertEquals(first, second);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
    }
}
