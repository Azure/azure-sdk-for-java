// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic, thread-safe cache for a single value that has an expiration time, supporting automatic
 * jittered proactive background refresh and single-flight (de-duplicated) creation.
 * <p>
 * This is used anywhere a client needs to cache a value obtained from a service call that is valid for a
 * limited time and expensive enough to refresh proactively rather than on every request — for example,
 * session credentials or layout/routing information for locality-aware downloads.
 *
 * @param <T> The type of value to cache.
 */
public final class AutoRefreshingCache<T> {
    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration DEFAULT_SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final double DEFAULT_JITTER_WINDOW_START_RATIO = 0.8d;

    private final Supplier<T> syncCreator;
    private final Supplier<Mono<T>> asyncCreator;
    private final Function<T, OffsetDateTime> expirationExtractor;
    private final Duration safetyBuffer;
    private final double jitterWindowStartRatio;
    private final Clock clock;
    private final Object creationLock = new Object();
    private volatile T value;
    private volatile OffsetDateTime nextRefreshTime;
    private volatile boolean refreshing;
    private volatile Mono<T> inflightCreation;

    /**
     * Creates a cache using the default safety buffer, jitter window, and system UTC clock.
     *
     * @param syncCreator Synchronous value creator.
     * @param asyncCreator Asynchronous value creator.
     * @param expirationExtractor Extracts the expiration time from a value.
     */
    public AutoRefreshingCache(Supplier<T> syncCreator, Supplier<Mono<T>> asyncCreator,
        Function<T, OffsetDateTime> expirationExtractor) {
        this(syncCreator, asyncCreator, expirationExtractor, DEFAULT_SAFETY_BUFFER, DEFAULT_JITTER_WINDOW_START_RATIO,
            Clock.systemUTC());
    }

    /**
     * Creates a cache using the default safety buffer and jitter window with a custom clock.
     *
     * @param syncCreator Synchronous value creator.
     * @param asyncCreator Asynchronous value creator.
     * @param expirationExtractor Extracts the expiration time from a value.
     * @param clock Clock used to evaluate expiration and refresh times.
     */
    public AutoRefreshingCache(Supplier<T> syncCreator, Supplier<Mono<T>> asyncCreator,
        Function<T, OffsetDateTime> expirationExtractor, Clock clock) {
        this(syncCreator, asyncCreator, expirationExtractor, DEFAULT_SAFETY_BUFFER, DEFAULT_JITTER_WINDOW_START_RATIO,
            clock);
    }

    /**
     * Creates a cache with custom refresh timing configuration.
     *
     * @param syncCreator Synchronous value creator.
     * @param asyncCreator Asynchronous value creator.
     * @param expirationExtractor Extracts the expiration time from a value.
     * @param safetyBuffer Duration before expiration that should not be used for jittered refresh scheduling.
     * @param jitterWindowStartRatio Ratio of usable lifetime at which the jitter window starts.
     * @param clock Clock used to evaluate expiration and refresh times.
     */
    public AutoRefreshingCache(Supplier<T> syncCreator, Supplier<Mono<T>> asyncCreator,
        Function<T, OffsetDateTime> expirationExtractor, Duration safetyBuffer, double jitterWindowStartRatio,
        Clock clock) {
        this.syncCreator = Objects.requireNonNull(syncCreator, "'syncCreator' cannot be null.");
        this.asyncCreator = Objects.requireNonNull(asyncCreator, "'asyncCreator' cannot be null.");
        this.expirationExtractor = Objects.requireNonNull(expirationExtractor, "'expirationExtractor' cannot be null.");
        this.safetyBuffer = Objects.requireNonNull(safetyBuffer, "'safetyBuffer' cannot be null.");
        this.jitterWindowStartRatio = jitterWindowStartRatio;
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    /**
     * Gets a valid cached value asynchronously, creating one if needed.
     *
     * @return A {@link Mono} containing a valid value.
     */
    public Mono<T> getValidAsync() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshInBackground();
            }
            return Mono.just(current);
        }

        return startCreationAsync();
    }

    /**
     * Gets a valid cached value synchronously, creating one if needed.
     *
     * @return A valid value.
     */
    public T getValidSync() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshInBackground();
            }
            return current;
        }

        // Join in-flight async creation outside the lock to avoid deadlock with doOnNext.
        Mono<T> inFlight = inflightCreation;
        if (inFlight != null) {
            T refreshed = inFlight.block();
            if (refreshed != null) {
                return refreshed;
            }
        }

        synchronized (creationLock) {
            current = value;
            now = OffsetDateTime.now(clock);
            if (isUsable(current, now)) {
                if (isRefreshDue(now)) {
                    refreshInBackground();
                }
                return current;
            }

            T created = syncCreator.get();
            setActiveValue(created);
            return created;
        }
    }

    /**
     * Invalidates the cached value if it matches the target instance.
     *
     * @param target Value to invalidate.
     */
    public void invalidate(T target) {
        synchronized (creationLock) {
            if (value == target) {
                value = null;
                nextRefreshTime = null;
                refreshing = false;
            }
            inflightCreation = null;
        }
    }

    /**
     * Starts a proactive background refresh if the cached value is due for refresh.
     */
    public void refreshInBackground() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (!isUsable(value, now) || !isRefreshDue(now) || refreshing) {
                return;
            }
            refreshing = true;
        }

        startCreationAsync().subscribe(ignored -> {
        }, error -> LOGGER.warning("Background refresh failed.", error));
    }

    /**
     * Marks the cached value for refresh and starts a background refresh if it is still usable.
     */
    public void forceRefreshInBackground() {
        synchronized (creationLock) {
            if (isUsable(value, OffsetDateTime.now(clock))) {
                nextRefreshTime = OffsetDateTime.now(clock);
            }
        }

        refreshInBackground();
    }

    private Mono<T> startCreationAsync() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            T current = value;
            if (isUsable(current, now) && !isRefreshDue(now)) {
                return Mono.just(current);
            }

            if (inflightCreation != null) {
                return inflightCreation;
            }

            refreshing = true;

            inflightCreation = asyncCreator.get().doOnNext(val -> {
                synchronized (creationLock) {
                    setActiveValue(val);
                }
            }).doFinally(ignored -> {
                synchronized (creationLock) {
                    inflightCreation = null;
                    refreshing = false;
                }
            }).cache();

            return inflightCreation;
        }
    }

    private void setActiveValue(T newValue) {
        value = newValue;
        nextRefreshTime = computeRefreshTime(OffsetDateTime.now(clock), expirationExtractor.apply(newValue));
        refreshing = false;
    }

    private boolean isUsable(T val, OffsetDateTime now) {
        return val != null && !now.isAfter(expirationExtractor.apply(val));
    }

    private boolean isRefreshDue(OffsetDateTime now) {
        OffsetDateTime refresh = nextRefreshTime;
        return refresh != null && !now.isBefore(refresh);
    }

    private OffsetDateTime computeRefreshTime(OffsetDateTime now, OffsetDateTime expiration) {
        long availableMillis = Duration.between(now, expiration.minus(safetyBuffer)).toMillis();
        if (availableMillis <= 0) {
            return now;
        }

        double refreshPoint
            = jitterWindowStartRatio + (1.0 - jitterWindowStartRatio) * ThreadLocalRandom.current().nextDouble();
        return now.plus(Duration.ofMillis((long) (availableMillis * refreshPoint)));
    }
}
