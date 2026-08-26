// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cache for expiring storage values.
 */
public final class AutoRefreshingCache<T extends AutoRefreshingCache.ExpiringValue> implements Closeable {
    public interface ValueProvider<T extends ExpiringValue> {
        Mono<T> createAsync();

        T createSync();
    }

    public interface ExpiringValue {
        OffsetDateTime getExpiration();

        /**
         * Gets the time at which the value should be refreshed.
         *
         * @return The refresh time, or {@code null} to use the cache's default jittered refresh time.
         */
        default OffsetDateTime getRefreshOn() {
            return null;
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration MIN_REFRESH_BEFORE_EXPIRATION = Duration.ofSeconds(30);
    private static final Duration MAX_REFRESH_BEFORE_EXPIRATION = Duration.ofSeconds(90);
    private static final Duration REFRESH_FAILURE_BACKOFF = Duration.ofSeconds(30);

    private final ValueProvider<T> valueProvider;
    private final Clock clock;
    private final Scheduler scheduler;
    private final Object creationLock = new Object();
    private volatile T value;
    private volatile OffsetDateTime nextRefreshTime;
    private volatile boolean refreshing;
    private volatile Mono<T> inflightCreation;
    private volatile Disposable scheduledRefresh;
    private volatile boolean closed;
    private boolean valueReadSinceLastRefresh;

    public AutoRefreshingCache(ValueProvider<T> valueProvider) {
        this(valueProvider, Clock.systemUTC());
    }

    public AutoRefreshingCache(ValueProvider<T> valueProvider, Clock clock) {
        this(valueProvider, clock, Schedulers.parallel());
    }

    AutoRefreshingCache(ValueProvider<T> valueProvider, Clock clock, Scheduler scheduler) {
        this.valueProvider = Objects.requireNonNull(valueProvider, "'valueProvider' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' cannot be null.");
    }

    public Mono<T> getValidValueAsync() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (current != null) {
            markValueRead(current);
            if (isRefreshDue(now)) {
                refreshValueInBackground();
            }
            return Mono.just(current);
        }

        return startValueCreationAsync(true);
    }

    public T getValidValueSync() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        T current = value;
        if (current != null) {
            markValueRead(current);
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
            if (current != null) {
                markValueRead(current);
                if (isRefreshDue(now)) {
                    refreshValueInBackground();
                }
                return current;
            }

            T created = valueProvider.createSync();
            setActiveValue(created, true);
            return created;
        }
    }

    public void invalidateValue(T target) {
        synchronized (creationLock) {
            if (value == target) {
                value = null;
                nextRefreshTime = null;
                refreshing = false;
                valueReadSinceLastRefresh = false;
                cancelScheduledRefresh();
            }
            inflightCreation = null;
        }
    }

    public void refreshValueInBackground() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (closed || value == null || !isRefreshDue(now) || refreshing) {
                return;
            }
            refreshing = true;
        }

        startValueCreationAsync(false).subscribe(ignored -> {
        }, error -> LOGGER.warning("Background value refresh failed.", error));
    }

    public void forceRefreshValueInBackground() {
        synchronized (creationLock) {
            if (!closed && value != null) {
                nextRefreshTime = OffsetDateTime.now(clock);
                valueReadSinceLastRefresh = true;
            }
        }

        refreshValueInBackground();
    }

    /**
     * Closes the cache, cancelling any pending refresh and preventing additional background scheduling.
     */
    @Override
    public void close() {
        synchronized (creationLock) {
            if (closed) {
                return;
            }

            closed = true;
            refreshing = false;
            cancelScheduledRefresh();
        }
    }

    OffsetDateTime getNextRefreshTime() {
        return nextRefreshTime;
    }

    private Mono<T> startValueCreationAsync(boolean recordRead) {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            T current = value;
            if (current != null && !isRefreshDue(now)) {
                if (recordRead) {
                    markValueRead(current);
                }
                return Mono.just(current);
            }

            if (inflightCreation != null) {
                return inflightCreation;
            }

            refreshing = true;

            inflightCreation = valueProvider.createAsync().doOnNext(cred -> {
                synchronized (creationLock) {
                    setActiveValue(cred, recordRead);
                }
            }).doOnError(error -> {
                synchronized (creationLock) {
                    if (value != null) {
                        nextRefreshTime = OffsetDateTime.now(clock).plus(REFRESH_FAILURE_BACKOFF);
                    }
                }
            }).doFinally(ignored -> {
                synchronized (creationLock) {
                    inflightCreation = null;
                    refreshing = false;
                    scheduleNextRefresh();
                }
            }).cache();

            return inflightCreation;
        }
    }

    private void setActiveValue(T newValue, boolean recordRead) {
        value = newValue;
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime refreshOn = newValue.getRefreshOn();
        nextRefreshTime = refreshOn == null ? computeRefreshTime(now, newValue.getExpiration()) : refreshOn;
        refreshing = false;
        valueReadSinceLastRefresh = recordRead;
        scheduleNextRefresh();
    }

    private void markValueRead(T current) {
        synchronized (creationLock) {
            if (value == current && !closed) {
                valueReadSinceLastRefresh = true;
                if (scheduledRefresh == null && !isRefreshDue(OffsetDateTime.now(clock))) {
                    scheduleNextRefresh();
                }
            }
        }
    }

    private boolean isRefreshDue(OffsetDateTime now) {
        OffsetDateTime refresh = nextRefreshTime;
        return refresh != null && !now.isBefore(refresh);
    }

    private void scheduleNextRefresh() {
        if (closed || value == null || nextRefreshTime == null) {
            return;
        }

        cancelScheduledRefresh();

        Duration delay = Duration.between(OffsetDateTime.now(clock), nextRefreshTime);
        if (delay.isNegative()) {
            delay = Duration.ZERO;
        }

        scheduledRefresh = Mono.delay(delay, scheduler).subscribe(ignored -> onScheduledRefresh(), error -> {
            LOGGER.warning("Scheduled value refresh failed.", error);
        });
    }

    private void cancelScheduledRefresh() {
        Disposable refresh = scheduledRefresh;
        if (refresh != null) {
            refresh.dispose();
            scheduledRefresh = null;
        }
    }

    private void onScheduledRefresh() {
        synchronized (creationLock) {
            scheduledRefresh = null;
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (closed || value == null || refreshing) {
                return;
            }

            if (!isRefreshDue(now)) {
                scheduleNextRefresh();
                return;
            }

            if (!valueReadSinceLastRefresh) {
                return;
            }

            valueReadSinceLastRefresh = false;
            refreshing = true;
        }

        startValueCreationAsync(false).subscribe(ignored -> {
        }, error -> LOGGER.warning("Scheduled value refresh failed.", error));
    }

    private static OffsetDateTime computeRefreshTime(OffsetDateTime now, OffsetDateTime expiration) {
        /*
         * Refresh in a uniformly distributed window between 90 and 30 seconds before expiration. For the storage
         * layout cache's five-minute service TTL this refreshes around the 3.5-4.5 minute mark, avoiding synchronized
         * process-wide getLayout stampedes while still leaving at least 30 seconds before service-side expiry.
         */
        OffsetDateTime latestRefresh = expiration.minus(MIN_REFRESH_BEFORE_EXPIRATION);
        if (!now.isBefore(latestRefresh)) {
            return now;
        }

        OffsetDateTime earliestRefresh = expiration.minus(MAX_REFRESH_BEFORE_EXPIRATION);
        if (earliestRefresh.isBefore(now)) {
            earliestRefresh = now;
        }

        long refreshWindowMillis = Duration.between(earliestRefresh, latestRefresh).toMillis();
        if (refreshWindowMillis <= 0) {
            return earliestRefresh;
        }

        return earliestRefresh.plus(Duration.ofMillis(ThreadLocalRandom.current().nextLong(refreshWindowMillis + 1)));
    }
}
