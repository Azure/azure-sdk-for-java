// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ExponentialRetryPolicy;
import com.azure.core.amqp.FixedRetryPolicy;
import com.azure.core.amqp.RetryMode;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.RetryPolicy;
import com.azure.core.amqp.TransportType;
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
        final RetryOptions retryOptions = new RetryOptions()
            .setRetryMode(RetryMode.FIXED);
        final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(FixedRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    public void getCorrectModeExponential() {
        // Act
        final RetryOptions retryOptions = new RetryOptions()
            .setRetryMode(RetryMode.EXPONENTIAL);
        final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        Assertions.assertEquals(ExponentialRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    public void withRetryFlux() {
        // Arrange
        final RetryOptions options = new RetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());
        final Duration timeout = Duration.ofMillis(500);

        final AtomicInteger resubscribe = new AtomicInteger();
        final RetryPolicy retryPolicy = new FixedRetryPolicy(options);
        final Flux<TransportType> neverFlux = Flux.<TransportType>never()
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
        final RetryOptions options = new RetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());
        final Duration timeout = Duration.ofMillis(500);

        final AtomicInteger resubscribe = new AtomicInteger();
        final RetryPolicy retryPolicy = new FixedRetryPolicy(options);
        final Mono<TransportType> neverFlux = Mono.<TransportType>never()
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
