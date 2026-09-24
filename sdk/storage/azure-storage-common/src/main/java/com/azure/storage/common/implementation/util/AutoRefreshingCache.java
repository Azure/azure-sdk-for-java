// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache for expiring values with shared single-flight acquisition.
 * <p>
 * Refresh is opportunistic rather than scheduled. A usable value can trigger a background refresh while remaining
 * available to callers. Background acquisitions time out after thirty seconds and cancel their subscriptions; failed or
 * timed-out refreshes retain the current value and retry after a backoff. There is no periodic timer or executor owned
 * by this cache.
 * <p>
 * The supplier must be nonblocking and emit exactly one non-null value. Null expiration metadata and empty async
 * results are acquisition errors. A newly acquired value that is already expired is returned once to its acquisition's
 * callers, but is not reused on subsequent calls.
 * <p>
 * RESERVED FOR INTERNAL USE.
 *
 * @param <CachedValue> The cached value type.
 */
public final class AutoRefreshingCache<CachedValue> {
    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final Duration REFRESH_RETRY_DELAY = Duration.ofSeconds(30);
    private static final Duration BACKGROUND_ACQUIRE_TIMEOUT = Duration.ofSeconds(30);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;

    private final Supplier<Mono<CachedValue>> asyncSupplier;
    private final Function<CachedValue, OffsetDateTime> expirationExtractor;
    private final Clock clock;
    private final Scheduler timeoutScheduler;
    private final Lock lock = new ReentrantLock();
    private CachedValueEntry<CachedValue> cachedValueEntry;
    private CompletableFuture<CachedValue> pendingAcquisition;
    private OffsetDateTime retryNotBefore;

    /**
     * Creates a cache with a nonblocking async loader and an explicit clock.
     *
     * @param asyncSupplier The nonblocking loader used for foreground and background acquisition.
     * @param expirationExtractor Extracts each value's expiration.
     * @param clock The clock used for cache decisions.
     */
    public AutoRefreshingCache(Supplier<Mono<CachedValue>> asyncSupplier,
        Function<CachedValue, OffsetDateTime> expirationExtractor, Clock clock) {
        this(asyncSupplier, expirationExtractor, clock, Schedulers.parallel());
    }

    AutoRefreshingCache(Supplier<Mono<CachedValue>> asyncSupplier,
        Function<CachedValue, OffsetDateTime> expirationExtractor, Clock clock, Scheduler timeoutScheduler) {
        this.asyncSupplier = Objects.requireNonNull(asyncSupplier, "'asyncSupplier' cannot be null.");
        this.expirationExtractor = Objects.requireNonNull(expirationExtractor, "'expirationExtractor' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
        this.timeoutScheduler = Objects.requireNonNull(timeoutScheduler, "'timeoutScheduler' cannot be null.");
    }

    /**
     * Gets a cached value or shares an acquisition.
     *
     * @return A publisher emitting the cached or acquired value.
     */
    public Mono<CachedValue> getValidValueAsync() {
        return Mono.defer(() -> {
            CachedValue cachedValue = getUsableCachedValue(OffsetDateTime.now(clock));
            if (cachedValue != null) {
                refreshValueInBackground(false);

                return Mono.just(cachedValue);
            }
            return acquireValueAsync(acquireForegroundValue(), false);
        });
    }

    /**
     * Gets a cached value, blocking on the shared async acquisition when needed.
     *
     * @return The cached or acquired value.
     */
    public CachedValue getValidValueSync() {
        return getValidValueAsync().block();
    }

    /**
     * Removes the value only if it is the exact instance supplied by the caller. A stale rejection is a no-op.
     * An independent replacement already being acquired remains in flight and can be joined by subsequent callers.
     *
     * @param target The rejected value.
     * @return Whether the matching cached value was removed.
     */
    public boolean invalidateValue(CachedValue target) {
        lock.lock();
        try {
            if (cachedValueEntry == null || target == null || cachedValueEntry.value != target) {
                return false;
            }
            cachedValueEntry = null;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Marks a usable value due for refresh, respecting failure backoff and sharing any existing acquisition.
     */
    public void forceRefreshValueInBackground() {
        refreshValueInBackground(true);
    }

    private void refreshValueInBackground(boolean force) {
        ValueAcquisition<CachedValue> acquisition;
        lock.lock();
        try {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (isValueMissingOrExpired(now)) {
                return;
            }
            if (force) {
                cachedValueEntry = new CachedValueEntry<>(cachedValueEntry.value, cachedValueEntry.expiration, now);
            }
            if (now.isBefore(cachedValueEntry.refreshAt) || isRetryBackoffActive(now)) {
                return;
            }
            acquisition = acquireValue();
        } finally {
            lock.unlock();
        }
        if (acquisition.isOwner) {
            acquireValueAsync(acquisition, true).subscribe(ignored -> {
            }, error -> LOGGER.warning("Background value refresh failed.", error));
        }
    }

    private ValueAcquisition<CachedValue> acquireForegroundValue() {
        lock.lock();
        try {
            // Another caller may have published a usable value since the initial lookup.
            if (isValueMissingOrExpired(OffsetDateTime.now(clock))) {
                return acquireValue();
            }
            return new ValueAcquisition<>(CompletableFuture.completedFuture(cachedValueEntry.value), false);
        } finally {
            lock.unlock();
        }
    }

    private ValueAcquisition<CachedValue> acquireValue() {
        if (pendingAcquisition != null) {
            return new ValueAcquisition<>(pendingAcquisition, false);
        }
        pendingAcquisition = new CompletableFuture<>();
        return new ValueAcquisition<>(pendingAcquisition, true);
    }

    private Mono<CachedValue> acquireValueAsync(ValueAcquisition<CachedValue> acquisition, boolean background) {
        return acquisition.isOwner ? startAcquisition(acquisition, background) : joinAcquisition(acquisition);
    }

    private Mono<CachedValue> startAcquisition(ValueAcquisition<CachedValue> acquisition, boolean background) {
        Mono<CachedValue> source = Mono.defer(asyncSupplier)
            .switchIfEmpty(Mono.error(new IllegalStateException("The value supplier completed without a value.")));
        if (background) {
            source = source.timeout(BACKGROUND_ACQUIRE_TIMEOUT, timeoutScheduler);
        }
        return source.doOnNext(value -> complete(acquisition, value))
            .doOnError(error -> fail(acquisition, error))
            .cache();
    }

    private Mono<CachedValue> joinAcquisition(ValueAcquisition<CachedValue> acquisition) {
        return Mono.fromFuture(acquisition.result, true);
    }

    private void complete(ValueAcquisition<CachedValue> acquisition, CachedValue value) {
        Objects.requireNonNull(value, "The value supplier returned null.");
        OffsetDateTime expiration
            = Objects.requireNonNull(expirationExtractor.apply(value), "The value expiration cannot be null.");
        OffsetDateTime refreshAt = computeRefreshTime(OffsetDateTime.now(clock), expiration);
        lock.lock();
        try {
            if (pendingAcquisition == acquisition.result) {
                cachedValueEntry = new CachedValueEntry<>(value, expiration, refreshAt);
                retryNotBefore = null;
                pendingAcquisition = null;
            }
        } finally {
            lock.unlock();
        }
        // Release ownership before invoking callbacks, which may invalidate or refresh the published value.
        acquisition.result.complete(value);
    }

    private void fail(ValueAcquisition<CachedValue> acquisition, Throwable error) {
        lock.lock();
        try {
            if (pendingAcquisition == acquisition.result) {
                retryNotBefore = OffsetDateTime.now(clock).plus(REFRESH_RETRY_DELAY);
                pendingAcquisition = null;
            }
        } finally {
            lock.unlock();
        }
        acquisition.result.completeExceptionally(error);
    }

    private CachedValue getUsableCachedValue(OffsetDateTime now) {
        lock.lock();
        try {
            if (isValueMissingOrExpired(now)) {
                return null;
            }
            return cachedValueEntry.value;
        } finally {
            lock.unlock();
        }
    }

    private boolean isValueMissingOrExpired(OffsetDateTime now) {
        return cachedValueEntry == null || now.isAfter(cachedValueEntry.expiration);
    }

    private boolean isRetryBackoffActive(OffsetDateTime now) {
        return retryNotBefore != null && now.isBefore(retryNotBefore);
    }

    static OffsetDateTime computeRefreshTime(OffsetDateTime now, OffsetDateTime expiration) {
        // Duration arithmetic avoids subtracting from MIN or overflowing milliseconds for MAX expiration.
        Duration available = Duration.between(now.toInstant(), expiration.toInstant()).minus(SAFETY_BUFFER);
        if (available.isNegative() || available.isZero()) {
            return now;
        }
        double ratio
            = JITTER_WINDOW_START_RATIO + (1.0 - JITTER_WINDOW_START_RATIO) * ThreadLocalRandom.current().nextDouble();
        double scaledSeconds = available.getSeconds() * ratio;
        long seconds = (long) scaledSeconds;
        long nanos = (long) ((scaledSeconds - seconds) * 1_000_000_000 + available.getNano() * ratio);
        return now.plusSeconds(seconds).plusNanos(nanos);
    }

    private static final class ValueAcquisition<CachedValue> {
        private final CompletableFuture<CachedValue> result;
        private final boolean isOwner;

        private ValueAcquisition(CompletableFuture<CachedValue> result, boolean isOwner) {
            this.result = result;
            this.isOwner = isOwner;
        }
    }

    private static final class CachedValueEntry<CachedValue> {
        private final CachedValue value;
        private final OffsetDateTime expiration;
        private final OffsetDateTime refreshAt;

        private CachedValueEntry(CachedValue value, OffsetDateTime expiration, OffsetDateTime refreshAt) {
            this.value = value;
            this.expiration = expiration;
            this.refreshAt = refreshAt;
        }
    }
}
