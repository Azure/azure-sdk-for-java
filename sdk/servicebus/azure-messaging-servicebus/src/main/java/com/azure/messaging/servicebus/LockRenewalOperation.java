// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.models.LockRenewalStatus;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Represents a renewal session or message lock renewal operation that.
 */
public class LockRenewalOperation implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(LockRenewalOperation.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicReference<Instant> lockedUntil = new AtomicReference<>();
    private final AtomicReference<Throwable> throwable = new AtomicReference<>();
    private final AtomicReference<LockRenewalStatus> status = new AtomicReference<>(LockRenewalStatus.RUNNING);
    private final MonoProcessor<Void> cancellationProcessor = MonoProcessor.create();

    private final String lockToken;
    private final boolean isSession;
    private final Function<String, Mono<Instant>> renewalOperation;
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
        Function<String, Mono<Instant>> renewalOperation) {
        this(lockToken, maxLockRenewalDuration, isSession, renewalOperation, Instant.now());
    }

    /**
     * Creates a new lock renewal operation.
     *
     * @param lockToken Lock or session id to renew.
     * @param lockedUntil The initial period the message or session is locked until.
     * @param maxLockRenewalDuration The maximum duration this lock should be renewed.
     * @param isSession Whether the lock represents a session lock or message lock.
     * @param renewalOperation The renewal operation to call.
     */
    LockRenewalOperation(String lockToken, Duration maxLockRenewalDuration, boolean isSession,
        Function<String, Mono<Instant>> renewalOperation, Instant lockedUntil) {
        this.lockToken = Objects.requireNonNull(lockToken, "'lockToken' cannot be null.");
        this.renewalOperation = Objects.requireNonNull(renewalOperation, "'renewalOperation' cannot be null.");
        this.isSession = isSession;

        Objects.requireNonNull(lockedUntil, "'lockedUntil cannot be null.'");
        Objects.requireNonNull(maxLockRenewalDuration, "'maxLockRenewalDuration' cannot be null.");

        if (maxLockRenewalDuration.isNegative()) {
            throw logger.logThrowableAsError(new IllegalArgumentException(
                "'maxLockRenewalDuration' cannot be negative."));
        }

        this.lockedUntil.set(lockedUntil);
        this.subscription = getRenewLockOperation(lockedUntil, maxLockRenewalDuration);
    }

    /**
     * Gets the current instant the message or session is locked until.
     *
     * @return the instant the message or session is locked until.
     */
    public Instant getLockedUntil() {
        return lockedUntil.get();
    }

    /**
     * Gets the message lock token for the renewal operation.
     *
     * @return The message lock token or {@code null} if a session is being renewed instead.
     */
    public String getLockToken() {
        return isSession ? null : lockToken;
    }

    /**
     * Gets the session id for this lock renewal operation.
     *
     * @return The session id or {@code null} if it is not a session renewal.
     */
    public String getSessionId() {
        return isSession ? lockToken : null;
    }

    /**
     * Gets the current status of the renewal operation.
     *
     * @return The current status of the renewal operation.
     */
    public LockRenewalStatus getStatus() {
        return status.get();
    }

    /**
     * Gets the exception if an error occurred whilst renewing the message or session lock.
     *
     * @return the exception if an error occurred whilst renewing the message or session lock, otherwise {@code null}.
     */
    public Throwable getThrowable() {
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
    private Disposable getRenewLockOperation(Instant initialLockedUntil, Duration maxLockRenewalDuration) {
        if (maxLockRenewalDuration.isZero()) {
            status.set(LockRenewalStatus.COMPLETE);
            return Disposables.single();
        }

        final Instant now = Instant.now();
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
                logger.info("token[{}]. now[{}]. Starting lock renewal.", lockToken, Instant.now());
                return renewalOperation.apply(lockToken);
            })
            .map(instant -> {
                final Duration next = Duration.between(Instant.now(), instant);
                logger.info("token[{}]. nextExpiration[{}]. next: [{}]. isSession[{}]", lockToken, instant, next,
                    isSession);

                sink.next(MessageUtils.adjustServerTimeout(next));
                return instant;
            })
            .subscribe(until -> lockedUntil.set(until),
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
}
