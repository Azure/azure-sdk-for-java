// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Cache for values that expire.
 * <p>
 * {@code T} is not required to implement a particular interface; the caller supplies a
 * {@link Function} that extracts the expiration time from a value.
 */
public final class AutoRefreshingCache<T> {
    public interface ValueProvider<T> {
        Mono<T> createAsync();

        T createSync();
    }

    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;

    private final ValueProvider<T> valueProvider;
    private final Function<T, OffsetDateTime> expirationExtractor;
    private final Clock clock;
    private final Object creationLock = new Object();
    private volatile T value;
    private volatile OffsetDateTime nextRefreshTime;
    private volatile boolean refreshing;
    private volatile Mono<T> inflightCreation;

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
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshValueInBackground();
            }
            return Mono.just(current);
        }

        return startValueCreationAsync();
    }

    public T getValidValueSync() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshValueInBackground();
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
                    refreshValueInBackground();
                }
                return current;
            }

            T created = valueProvider.createSync();
            setActiveValue(created);
            return created;
        }
    }

    public void invalidateValue(T target) {
        synchronized (creationLock) {
            if (value == target) {
                value = null;
                nextRefreshTime = null;
                refreshing = false;
            }
            inflightCreation = null;
        }
    }

    public void refreshValueInBackground() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (!isUsable(value, now) || !isRefreshDue(now) || refreshing) {
                return;
            }
            refreshing = true;
        }

        startValueCreationAsync().subscribe(ignored -> {
        }, error -> LOGGER.warning("Background value refresh failed.", error));
    }

    public void forceRefreshValueInBackground() {
        synchronized (creationLock) {
            if (isUsable(value, OffsetDateTime.now(clock))) {
                nextRefreshTime = OffsetDateTime.now(clock);
            }
        }

        refreshValueInBackground();
    }

    private Mono<T> startValueCreationAsync() {
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

            AtomicReference<Mono<T>> creationReference = new AtomicReference<>();
            Mono<T> creation = valueProvider.createAsync().doOnNext(newValue -> {
                synchronized (creationLock) {
                    setActiveValue(newValue);
                }
            }).doFinally(ignored -> {
                synchronized (creationLock) {
                    if (inflightCreation == creationReference.get()) {
                        inflightCreation = null;
                        refreshing = false;
                    }
                }
            }).cache();
            creationReference.set(creation);
            inflightCreation = creation;

            return inflightCreation;
        }
    }

    private void setActiveValue(T newValue) {
        value = newValue;
        nextRefreshTime = computeRefreshTime(OffsetDateTime.now(clock), expirationExtractor.apply(newValue));
        refreshing = false;
        inflightCreation = null;
    }

    private boolean isUsable(T value, OffsetDateTime now) {
        return value != null && !now.isAfter(expirationExtractor.apply(value));
    }

    private boolean isRefreshDue(OffsetDateTime now) {
        OffsetDateTime refresh = nextRefreshTime;
        return refresh != null && !now.isBefore(refresh);
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
