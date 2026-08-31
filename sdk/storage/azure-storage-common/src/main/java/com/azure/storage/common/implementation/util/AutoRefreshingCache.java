// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Cache for values that expire and must be refreshed transparently, such as container-scoped storage session
 * credentials and blob data locality layouts.
 * <p>
 * {@code T} is not required to implement any particular interface; the caller supplies a {@link Function} that
 * extracts the expiration instant from a value, decoupling this cache from any specific value shape.
 * <p>
 * Refresh is opportunistic rather than scheduled: a caller that observes a value past its jittered refresh point
 * receives the still-valid cached value immediately and triggers the refresh in the background. There is no timer
 * to cancel, so instances need no cleanup.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class AutoRefreshingCache<T> {
    /**
     * Supplies the values held by the cache.
     *
     * @param <T> The type of value produced.
     */
    @FunctionalInterface
    public interface ValueProvider<T> {
        /**
         * Creates the value asynchronously.
         *
         * @return A {@link Mono} that emits the created value.
         */
        Mono<T> createAsync();

        /**
         * Creates the value synchronously. Defaults to blocking on {@link #createAsync()}; override when a
         * non-blocking synchronous path is available.
         *
         * @return The created value.
         */
        default T createSync() {
            return createAsync().block();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final Duration REFRESH_RETRY_DELAY = Duration.ofSeconds(30);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;

    private final ValueProvider<T> valueProvider;
    private final Function<T, OffsetDateTime> expirationExtractor;
    private final Clock clock;
    // Doubles as the "a creation is in flight" flag and the latch that wakes callers waiting on that
    // creation. The thread that wins the compare-and-set owns the creation and must terminate the sink
    // and clear this reference. Clearing happens before the value is delivered downstream, so a caller
    // that reacts inside onNext sees no creation in flight and can start a fresh one.
    private final AtomicReference<Sinks.One<T>> wip = new AtomicReference<>();
    private final AtomicReference<T> value = new AtomicReference<>();
    private volatile OffsetDateTime nextRefreshTime;
    // Throttles background refresh retries after creation failures so a failing provider is not retried
    // once per caller request. Foreground creation remains intentionally unthrottled.
    private volatile OffsetDateTime retryNotBefore;

    public AutoRefreshingCache(ValueProvider<T> valueProvider, Function<T, OffsetDateTime> expirationExtractor) {
        this(valueProvider, expirationExtractor, Clock.systemUTC());
    }

    public AutoRefreshingCache(ValueProvider<T> valueProvider, Function<T, OffsetDateTime> expirationExtractor,
        Clock clock) {
        this.valueProvider = Objects.requireNonNull(valueProvider, "'valueProvider' cannot be null.");
        this.expirationExtractor = Objects.requireNonNull(expirationExtractor, "'expirationExtractor' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    public Mono<T> getValidValueAsync() {
        return Mono.defer(() -> {
            OffsetDateTime now = OffsetDateTime.now(clock);
            T current = value.get();
            if (isUsable(current, now)) {
                if (isRefreshDue(now)) {
                    refreshValueInBackground();
                }
                return Mono.just(current);
            }

            return createOrJoinAsync();
        });
    }

    public T getValidValueSync() {
        while (true) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            T current = value.get();
            if (isUsable(current, now)) {
                if (isRefreshDue(now)) {
                    refreshValueInBackground();
                }
                return current;
            }

            Sinks.One<T> latch = Sinks.one();
            if (wip.compareAndSet(null, latch)) {
                T created;
                try {
                    // Re-check under ownership: another caller may have published a value between the
                    // check at the top of this loop and the compare-and-set above.
                    created = value.get();
                    if (!isUsable(created, OffsetDateTime.now(clock))) {
                        created = valueProvider.createSync();
                        setActiveValue(created);
                    }
                } catch (RuntimeException e) {
                    armRetryBackoff();
                    wip.compareAndSet(latch, null);
                    latch.tryEmitError(e);
                    throw LOGGER.logExceptionAsError(e);
                }
                // Clear ownership before waking waiters and before returning, so a caller reacting to
                // this value sees no creation in flight.
                wip.compareAndSet(latch, null);
                latch.tryEmitValue(created);
                return created;
            }

            Sinks.One<T> inFlight = wip.get();
            if (inFlight != null) {
                // Join the in-flight creation rather than minting a duplicate. Blocking here is the
                // same exposure the previous implementation had. Return what the owner published
                // rather than re-testing it, so a value that is already expired on arrival is
                // surfaced once instead of sending this loop back for another attempt.
                T joined = inFlight.asMono().block();
                if (joined != null) {
                    return joined;
                }
            }
        }
    }

    /**
     * Clears the cached value, but only if it is still the value the caller is rejecting.
     *
     * @param target The value the caller believes is cached.
     * @return true if {@code target} was still the cached value and has been cleared; false if it had
     * already been replaced or removed, in which case the cache is left untouched.
     */
    public boolean invalidateValue(T target) {
        boolean invalidated = target != null && value.compareAndSet(target, null);
        if (invalidated) {
            nextRefreshTime = null;
        }
        wip.set(null);
        return invalidated;
    }

    public void refreshValueInBackground() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (!isUsable(value.get(), now) || !isRefreshDue(now) || wip.get() != null || isRetryBackoffActive(now)) {
            return;
        }

        createOrJoinAsync().subscribe(ignored -> {
        }, error -> LOGGER.warning("Background value refresh failed.", error));
    }

    public void forceRefreshValueInBackground() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (isUsable(value.get(), now)) {
            nextRefreshTime = now;
        }

        refreshValueInBackground();
    }

    private Mono<T> createOrJoinAsync() {
        return Mono.defer(() -> {
            OffsetDateTime now = OffsetDateTime.now(clock);
            T current = value.get();
            if (isUsable(current, now) && !isRefreshDue(now)) {
                return Mono.just(current);
            }

            Sinks.One<T> latch = Sinks.one();
            if (wip.compareAndSet(null, latch)) {
                return Mono.using(() -> latch, ignored -> valueProvider.createAsync().doOnNext(created -> {
                    setActiveValue(created);
                    // Clear ownership before waking waiters so a caller reacting to this value
                    // sees no creation in flight.
                    wip.compareAndSet(latch, null);
                    latch.tryEmitValue(created);
                }).doOnError(error -> {
                    armRetryBackoff();
                    wip.compareAndSet(latch, null);
                    latch.tryEmitError(error);
                }), owned -> {
                    wip.compareAndSet(owned, null);
                    // No-op when the creation already emitted. On cancellation it releases anyone
                    // waiting on the latch so they retry instead of hanging.
                    owned.tryEmitEmpty();
                }).cache();
            }

            Sinks.One<T> inFlight = wip.get();
            if (inFlight == null) {
                return createOrJoinAsync();
            }

            return inFlight.asMono().switchIfEmpty(Mono.defer(this::createOrJoinAsync));
        });
    }

    private void setActiveValue(T newValue) {
        value.set(newValue);
        nextRefreshTime = computeRefreshTime(OffsetDateTime.now(clock), expirationExtractor.apply(newValue));
        retryNotBefore = null;
    }

    private void armRetryBackoff() {
        retryNotBefore = OffsetDateTime.now(clock).plus(REFRESH_RETRY_DELAY);
    }

    private boolean isUsable(T value, OffsetDateTime now) {
        return value != null && !now.isAfter(expirationExtractor.apply(value));
    }

    private boolean isRefreshDue(OffsetDateTime now) {
        OffsetDateTime refresh = nextRefreshTime;
        return refresh != null && !now.isBefore(refresh);
    }

    private boolean isRetryBackoffActive(OffsetDateTime now) {
        OffsetDateTime notBefore = retryNotBefore;
        return notBefore != null && now.isBefore(notBefore);
    }

    private static OffsetDateTime computeRefreshTime(OffsetDateTime now, OffsetDateTime expiration) {
        long availableMillis = Duration.between(now, expiration.minus(SAFETY_BUFFER)).toMillis();
        if (availableMillis <= 0) {
            return now;
        }

        double refreshPoint
            = JITTER_WINDOW_START_RATIO + (1.0 - JITTER_WINDOW_START_RATIO) * ThreadLocalRandom.current().nextDouble();
        return now.plus(Duration.ofMillis((long) (availableMillis * refreshPoint)));
    }
}
