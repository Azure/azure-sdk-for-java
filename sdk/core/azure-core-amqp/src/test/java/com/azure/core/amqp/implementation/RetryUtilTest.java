// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryUtilTest {
    @Test
    public void getCorrectModeFixed() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(FixedAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    public void getCorrectModeExponential() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.EXPONENTIAL);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(ExponentialAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    public void withRetryFlux() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());
        final Duration timeout = Duration.ofMillis(500);

        final AtomicInteger resubscribe = new AtomicInteger();
        final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(options);
        final Flux<AmqpTransportType> neverFlux = Flux.<AmqpTransportType>never()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, timeout, retryPolicy))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectError(TimeoutException.class)
            .verify();

        Assertions.assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    @Test
    public void withRetryMono() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());
        final Duration timeout = Duration.ofMillis(500);

        final AtomicInteger resubscribe = new AtomicInteger();
        final AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(options);
        final Mono<AmqpTransportType> neverFlux = Mono.<AmqpTransportType>never()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, timeout, retryPolicy))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectError(TimeoutException.class)
            .verify();

        Assertions.assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }
}
