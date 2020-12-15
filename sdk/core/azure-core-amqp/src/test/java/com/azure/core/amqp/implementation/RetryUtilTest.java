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
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryUtilTest {
    @Test
    void getCorrectModeFixed() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(FixedAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    void getCorrectModeExponential() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.EXPONENTIAL);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(ExponentialAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    void withRetryFlux() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(500);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final Flux<AmqpTransportType> neverFlux = TestPublisher.<AmqpTransportType>create().flux()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        Assertions.assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    @Test
    void withRetryMono() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(500);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final Mono<AmqpTransportType> neverFlux = TestPublisher.<AmqpTransportType>create().mono()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        Assertions.assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }
}
