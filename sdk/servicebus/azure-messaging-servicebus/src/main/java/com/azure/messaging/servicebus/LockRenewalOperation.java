// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Represents a renewal session or message lock renewal operation that.
 */
class LockRenewalOperation implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(LockRenewalOperation.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicReference<OffsetDateTime> lockedUntil = new AtomicReference<>();
    private final AtomicReference<Throwable> throwable = new AtomicReference<>();
    private final AtomicReference<LockRenewalStatus> status = new AtomicReference<>(LockRenewalStatus.RUNNING);
    private final MonoProcessor<Void> cancellationProcessor = MonoProcessor.create();
    private final Mono<Void> completionMono;

    private final String lockToken;
    private final boolean isSession;
    private final Function<String, Mono<OffsetDateTime>> renewalOperation;
    private final Disposable subscription;

    /**
     * Creates a new lock renewal operation. The lock is initially renewed.
     *
     * @param lockToken Message lock or session id to renew.
     * @param maxLockRenewalDuration The maximum duration this lock should be renewed.
     * @param isSession Whether the lock represents a session lock or message lock.
     * @param renewalOperation The renewal operation to call.
     */
    LockRenewalOperation(String lockToken, Duration maxLockRenewalDuration, boolean isSession,
        Function<String, Mono<OffsetDateTime>> renewalOperation) {
        this(lockToken, maxLockRenewalDuration, isSession, renewalOperation, OffsetDateTime.now());
    }

    /**
     * Creates a new lock renewal operation.
     *
     * @param lockToken Lock or session id to renew.
     * @param tokenLockedUntil The initial period the message or session is locked until.
     * @param maxLockRenewalDuration The maximum duration this lock should be renewed.
     * @param isSession Whether the lock represents a session lock or message lock.
     * @param renewalOperation The renewal operation to call.
     */
    LockRenewalOperation(String lockToken, Duration maxLockRenewalDuration, boolean isSession,
        Function<String, Mono<OffsetDateTime>> renewalOperation, OffsetDateTime tokenLockedUntil) {
        this.lockToken = Objects.requireNonNull(lockToken, "'lockToken' cannot be null.");
        this.renewalOperation = Objects.requireNonNull(renewalOperation, "'renewalOperation' cannot be null.");
        this.isSession = isSession;

        Objects.requireNonNull(tokenLockedUntil, "'lockedUntil cannot be null.'");
        Objects.requireNonNull(maxLockRenewalDuration, "'maxLockRenewalDuration' cannot be null.");

        if (maxLockRenewalDuration.isNegative()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxLockRenewalDuration' cannot be negative."));
        }

        this.lockedUntil.set(tokenLockedUntil);

        final Flux<OffsetDateTime> renewLockOperation = getRenewLockOperation(tokenLockedUntil,
            maxLockRenewalDuration)
            .takeUntilOther(cancellationProcessor)
            .cache(Duration.ofMinutes(2));

        this.completionMono = renewLockOperation.then();
        this.subscription = renewLockOperation.subscribe(until -> this.lockedUntil.set(until),
            error -> {
                logger.error("token[{}]. Error occurred while renewing lock token.", error);
                status.set(LockRenewalStatus.FAILED);
                throwable.set(error);
                cancellationProcessor.onComplete();
            }, () -> {
                if (status.compareAndSet(LockRenewalStatus.RUNNING, LockRenewalStatus.COMPLETE)) {
                    logger.verbose("token[{}]. Renewing session lock task completed.", lockToken);
                }

                cancellationProcessor.onComplete();
            });
    }

    /**
     * Gets a mono that completes when the operation does.
     *
     * @return A mono that completes when the renewal operation does.
     */
    Mono<Void> getCompletionOperation() {
        return completionMono;
    }

    /**
     * Gets the current datetime the message or session is locked until.
     *
     * @return the datetime the message or session is locked until.
     */
    OffsetDateTime getLockedUntil() {
        return lockedUntil.get();
    }

    /**
     * Gets the message lock token for the renewal operation.
     *
     * @return The message lock token or {@code null} if a session is being renewed instead.
     */
    String getLockToken() {
        return isSession ? null : lockToken;
    }

    /**
     * Gets the session id for this lock renewal operation.
     *
     * @return The session id or {@code null} if it is not a session renewal.
     */
    String getSessionId() {
        return isSession ? lockToken : null;
    }

    /**
     * Gets the current status of the renewal operation.
     *
     * @return The current status of the renewal operation.
     */
    LockRenewalStatus getStatus() {
        return status.get();
    }

    /**
     * Gets the exception if an error occurred whilst renewing the message or session lock.
     *
     * @return the exception if an error occurred whilst renewing the message or session lock, otherwise {@code null}.
     */
    Throwable getThrowable() {
        return throwable.get();
    }

    /**
     * Cancels the lock renewal operation.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (status.compareAndSet(LockRenewalStatus.RUNNING, LockRenewalStatus.CANCELLED)) {
            logger.verbose("token[{}] Cancelled operation.", lockToken);
        }

        cancellationProcessor.onComplete();
        subscription.dispose();
    }

    /**
     * Gets the lock renewal operation. if the {@code maxLockRenewalDuration} is {@link Duration#isZero()}, then the
     * lock is never renewed.
     *
     * @param initialLockedUntil When the initial call is locked until.
     * @param maxLockRenewalDuration Duration to renew lock for.
     * @return The subscription for the operation.
     */
    private Flux<OffsetDateTime> getRenewLockOperation(OffsetDateTime initialLockedUntil,
        Duration maxLockRenewalDuration) {
        if (maxLockRenewalDuration.isZero()) {
            status.set(LockRenewalStatus.COMPLETE);
            return Flux.empty();
        }

        final OffsetDateTime now = OffsetDateTime.now();
        Duration initialInterval = Duration.between(now, initialLockedUntil);

        if (initialInterval.isNegative()) {
            logger.info("Duration was negative. now[{}] lockedUntil[{}]", now, initialLockedUntil);
            initialInterval = Duration.ZERO;
        } else {
            // Adjust the interval, so we can buffer time for the time it'll take to refresh.
            final Duration adjusted = MessageUtils.adjustServerTimeout(initialInterval);
            if (adjusted.isNegative()) {
                logger.info("Adjusted duration is negative. Adjusted: {}ms", initialInterval.toMillis());
            } else {
                initialInterval = adjusted;
            }
        }

        final EmitterProcessor<Duration> emitterProcessor = EmitterProcessor.create();
        final FluxSink<Duration> sink = emitterProcessor.sink();

        sink.next(initialInterval);

        final Flux<Object> cancellationSignals = Flux.first(cancellationProcessor, Mono.delay(maxLockRenewalDuration));
        return Flux.switchOnNext(emitterProcessor.map(interval -> Mono.delay(interval)
            .thenReturn(Flux.create(s -> s.next(interval)))))
            .takeUntilOther(cancellationSignals)
            .flatMap(delay -> {
                logger.info("token[{}]. now[{}]. Starting lock renewal.", lockToken, OffsetDateTime.now());
                return renewalOperation.apply(lockToken);
            })
            .map(offsetDateTime -> {
                final Duration next = Duration.between(OffsetDateTime.now(), offsetDateTime);
                logger.info("token[{}]. nextExpiration[{}]. next: [{}]. isSession[{}]", lockToken, offsetDateTime, next,
                    isSession);

                sink.next(MessageUtils.adjustServerTimeout(next));
                return offsetDateTime;
            });
    }
}
