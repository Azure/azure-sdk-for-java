// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.function.Supplier;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class SessionIdleTimerIsolatedTest {
    private static final ClientLogger LOGGER = new ClientLogger(SessionIdleTimerIsolatedTest.class);

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldCompleteTimer() {
        final Duration idleTimeout = Duration.ofMillis(5000);
        final SessionIdleTimer timer = new SessionIdleTimer(LOGGER, idleTimeout);
        final Duration totalAwait = idleTimeout.plusMillis(2000);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> timer.timeout())
                .thenAwait(totalAwait)
                .verifyComplete();
        }
        // Any call to reset post the completion have no effect, e.g, should not throw.
        timer.reset();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldResetDelaysTimerCompletion() {
        final Duration idleTimeout = Duration.ofMinutes(5);
        final SessionIdleTimer timer = new SessionIdleTimer(LOGGER, idleTimeout);
        final int valuesCount = 100;
        final int valuesCountHalf = valuesCount / 2;
        final Duration belowIdleTimeout = idleTimeout.minusMinutes(2);
        final Duration exceedsIdleTimeout = idleTimeout.plusMinutes(2);
        final Flux<Integer> values = Flux.range(0, valuesCount)
            .takeUntilOther(timer.timeout())
            .flatMap(v -> {
                final Mono<Integer> value = Mono.just(v);
                if (v < valuesCountHalf) {
                    return Mono.delay(belowIdleTimeout).then(value);
                } else {
                    return Mono.delay(exceedsIdleTimeout).then(value);
                }
            }, 1, 1)
            .doOnNext(e -> {
                timer.reset();
            });
        final Duration totalAwait = idleTimeout.multipliedBy(valuesCount);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> values)
                .thenRequest(valuesCount)
                .thenAwait(totalAwait)
                .thenConsumeWhile(v -> {
                    Assertions.assertTrue(v < valuesCountHalf + 10, () -> {
                        return String.format("Idle timeout should have triggered before the emission of the value %s.", v);
                    });
                    return true;
                })
                .verifyComplete();
        }
        // Any call to reset post the completion have no effect, e.g, should not throw.
        timer.reset();
    }

    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    public void shouldReplayTimerCompletionForSecondSubscription() {
        final Duration idleTimeout = Duration.ofMillis(5000);
        final SessionIdleTimer timer = new SessionIdleTimer(LOGGER, idleTimeout);
        final Duration totalAwait = idleTimeout.plusMillis(2000);

        try (VirtualTimeStepVerifier verifier = new VirtualTimeStepVerifier()) {
            verifier.create(() -> timer.timeout())
                .thenAwait(totalAwait)
                .verifyComplete();
        }
        StepVerifier.create(timer.timeout())
            .verifyComplete();
        // Any call to reset post the completion have no effect, e.g, should not throw.
        timer.reset();
    }


    private static final class VirtualTimeStepVerifier implements AutoCloseable {
        private final VirtualTimeScheduler scheduler;

        VirtualTimeStepVerifier() {
            scheduler = VirtualTimeScheduler.create();
        }

        <T> StepVerifier.Step<T> create(Supplier<Publisher<T>> scenarioSupplier) {
            return StepVerifier.withVirtualTime(scenarioSupplier, () -> scheduler, 0);
        }

        @Override
        public void close() {
            scheduler.dispose();
        }
    }
}
