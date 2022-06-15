// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class contains tests for {@link RetryUtil} using
 * {@link reactor.test.scheduler.VirtualTimeScheduler} hence needs
 * to run in isolated and sequential.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class RetryUtilIsolatedTest {
    /**
     * Tests a retry that times out on a Flux.
     */
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void withRetryFluxEmitsItemsLaterThanTimeout() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofSeconds(5);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final TestPublisher<AmqpTransportType> singleItem = TestPublisher.create();

        final Flux<AmqpTransportType> flux = singleItem.flux()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();
        try {
            // Act & Assert
            StepVerifier.withVirtualTime(() -> RetryUtil.withRetry(flux, options, timeoutMessage),
                    () -> virtualTimeScheduler, 1)
                .expectSubscription()
                .then(() -> singleItem.next(AmqpTransportType.AMQP_WEB_SOCKETS))
                .expectNext(AmqpTransportType.AMQP_WEB_SOCKETS)
                .expectNoEvent(totalWaitTime)
                .thenCancel()
                .verify();
        } finally {
            virtualTimeScheduler.dispose();
        }

        assertEquals(1, resubscribe.get());
    }

    static Stream<Throwable> withNonTransientError() {
        return Stream.of(
            new AmqpException(false, "Test-exception", new AmqpErrorContext("test-ns")),
            new IllegalStateException("Some illegal State")
        );
    }

    @ParameterizedTest
    @MethodSource
    @Execution(ExecutionMode.SAME_THREAD)
    void withNonTransientError(Throwable nonTransientError) {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofSeconds(30);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(1)
            .setTryTimeout(timeout);

        final Flux<Integer> stream = Flux.concat(
            Flux.defer(() -> Flux.just(0, 1, 2)),
            Flux.defer(() -> Flux.error(nonTransientError)),
            Flux.defer(() -> Flux.just(3, 4)));

        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create(true);

        // Act & Assert
        try {
            StepVerifier.withVirtualTime(() -> RetryUtil.withRetry(stream, options, timeoutMessage),
                    () -> virtualTimeScheduler, 4)
                .expectNext(0, 1, 2)
                .expectErrorMatches(error -> error.equals(nonTransientError))
                .verify();
        } finally {
            virtualTimeScheduler.dispose();
        }
    }
}
