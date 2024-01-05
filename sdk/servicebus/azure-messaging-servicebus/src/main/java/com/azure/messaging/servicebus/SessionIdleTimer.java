// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A timer to support session idle timeout feature.
 */
final class SessionIdleTimer {
    private final Sinks.Many<Boolean> resetTimeoutSink = Sinks.many().unicast().onBackpressureBuffer();
    private final ClientLogger logger;
    private final Duration idleTimeout;
    private final Sinks.Empty<Void> timeoutCached = Sinks.empty();
    private final AtomicBoolean subscribedOnce = new AtomicBoolean(false);

    SessionIdleTimer(ClientLogger logger, Duration idleTimeout) {
        this.logger = Objects.requireNonNull(logger);
        this.idleTimeout = Objects.requireNonNull(idleTimeout);
    }

    /**
     * Gets the mono that terminates with completion signal once the timeout timer expires.
     * <p>
     * The timer start for the first time when the mono is subscribed. Each call to {@link SessionIdleTimer#reset()}
     * will postpone the timer expiration by restarting the timer. If the timer is not reset before it time out,
     * then the mono will be terminated.
     * </p>
     *
     * @return the timeout mono.
     */
    Mono<Void> timeout() {
        return Mono.defer(() -> {
            if (subscribedOnce.getAndSet(true)) {
                // the timeout publisher in session idle use case will be subscribed only once (but keeping the same
                // behavior as older implementation, along with logging).
                logger.atError().log("Unexpected multi-subscription to session timeout mono.");
                return timeoutCached.asMono();
            }
            final Flux<Mono<Long>> resetTimer = resetTimeoutSink.asFlux().map(__ -> Mono.delay(idleTimeout, Schedulers.parallel()));
            final Mono<Void> timeoutMono = Flux.switchOnNext(resetTimer)
                // If the upstream emits a value (means the timer expired), 'take' that value to cancel upstream and
                // complete the downstream chain to terminate the timeout mono.
                .take(1)
                .<Void>then(Mono.fromRunnable(() -> {
                    logger.atInfo().log("Did not a receive message from the session within timeout {}.", idleTimeout);
                }))
                .doOnSubscribe(__ -> {
                    // start the timer for the first time.
                    reset();
                });

            return timeoutMono
                .doOnEach(signal -> {
                    assert !signal.isOnNext();
                    if (signal.isOnError()) {
                        timeoutCached.emitError(signal.getThrowable(), Sinks.EmitFailureHandler.FAIL_FAST);
                    } else if (signal.isOnComplete()) {
                        timeoutCached.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                    }
                });
        });
    }

    /**
     * Resets the timeout timer.
     * <p>
     * The mono from {@link SessionIdleTimer#timeout()} will be terminated if the timer is not reset before it time out.
     * </p>
     * <p>
     * The caller should ensure that two reset calls never overlaps, i.e, calls must happens serially to adhere to
     * reactive serialized access rule (spec rule 1.3).
     * </p>
     */
    void reset() {
        resetTimeoutSink.emitNext(true, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
