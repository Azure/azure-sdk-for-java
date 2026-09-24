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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache for expiring values
 * <p>
 * Refresh is opportunistic rather than scheduled. Access to a usable value starts an asynchronous refresh when due,
 * while returning the current value. Background acquisitions time out after thirty seconds and cancel their subscription.
 * Failed or timed-out background refreshes retain the current value and are retried after thirty seconds; acquisition of a
 * missing or expired value is not delayed by that backoff. There is no periodic timer or executor owned by this cache.
 * Foreground acquisitions retain the supplier's timeout behavior. Callers joining a background acquisition share its timeout.
 * <p>
 * Async suppliers must be nonblocking and emit exactly one non-null value. Sync suppliers must return a non-null value.
 * Null expiration metadata and empty async results are acquisition errors. A newly acquired value that is already
 * expired is returned once to its acquisition's callers, but is not reused on subsequent calls.
 * <p>
 * Cancellation of a subscriber does not cancel a shared acquisition. Subscribing does not itself move work to a
 * different thread; async suppliers are responsible for using an appropriate scheduler when required.
 * <p>
 * Foreground async loading uses the initiating subscriber's Reactor context. Detached background refresh starts with
 * an empty subscriber context; capture any stable, cache-scoped request context in the supplier.
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
    private final Supplier<CachedValue> syncSupplier;
    private final Function<CachedValue, OffsetDateTime> expirationExtractor;
    private final Clock clock;
    private final Scheduler timeoutScheduler;
    private CachedValueEntry<CachedValue> cachedValueEntry;
    private CompletableFuture<CachedValue> inFlightAcquisition;
    private OffsetDateTime retryNotBefore;

    /**
     * Creates a cache with independent async and sync loaders and an explicit clock.
     * Callers without a dedicated sync loader can supply {@code () -> asyncSupplier.get().block()} for sync access.
     *
     * @param asyncSupplier The nonblocking async loader, also used for background refresh.
     * @param syncSupplier The sync loader.
     * @param expirationExtractor Extracts each value's expiration.
     * @param clock The clock used for cache decisions.
     */
    public AutoRefreshingCache(Supplier<Mono<CachedValue>> asyncSupplier, Supplier<CachedValue> syncSupplier,
        Function<CachedValue, OffsetDateTime> expirationExtractor, Clock clock) {
        this(asyncSupplier, syncSupplier, expirationExtractor, clock, Schedulers.parallel());
    }

    AutoRefreshingCache(Supplier<Mono<CachedValue>> asyncSupplier, Supplier<CachedValue> syncSupplier,
        Function<CachedValue, OffsetDateTime> expirationExtractor, Clock clock, Scheduler timeoutScheduler) {
        this.asyncSupplier = Objects.requireNonNull(asyncSupplier, "'asyncSupplier' cannot be null.");
        this.syncSupplier = Objects.requireNonNull(syncSupplier, "'syncSupplier' cannot be null.");
        this.expirationExtractor = Objects.requireNonNull(expirationExtractor, "'expirationExtractor' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
        this.timeoutScheduler = Objects.requireNonNull(timeoutScheduler, "'timeoutScheduler' cannot be null.");
    }

    /**
     * Gets a cached value or shares an acquisition. A usable value can trigger background refresh.
     *
     * @return A publisher emitting the cached or acquired value.
     */
    public Mono<CachedValue> getValidValueAsync() {
        return Mono.defer(() -> {
            CachedValue current = getUsableValue();
            if (current != null) {
                refreshValueInBackground();
                return Mono.just(current);
            }
            return acquireValueAsync(acquireForegroundValue(), false);
        });
    }

    /**
     * Gets a cached value, invokes the sync loader, or waits for an existing acquisition.
     *
     * @return The cached or acquired value.
     */
    public CachedValue getValidValueSync() {
        CachedValue current = getUsableValue();
        if (current != null) {
            refreshValueInBackground();
            return current;
        }
        ValueAcquisition<CachedValue> acquisition = acquireForegroundValue();
        if (acquisition.isOwner) {
            try {
                complete(acquisition, syncSupplier.get());
            } catch (RuntimeException | Error error) {
                fail(acquisition, error);
            }
        }
        try {
            return acquisition.result.get();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Interrupted while waiting for a cached value.", error));
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) cause);
            }
            if (cause instanceof Error) {
                throw LOGGER.logThrowableAsError((Error) cause);
            }
            throw LOGGER.logExceptionAsError(new IllegalStateException("Failed to acquire a cached value.", cause));
        }
    }

    /**
     * Removes the value only if it is the exact instance supplied by the caller. A stale rejection is a no-op.
     * An independent replacement already being acquired remains in flight and can be joined by subsequent callers.
     *
     * @param target The rejected value.
     * @return Whether the matching cached value was removed.
     */
    public boolean invalidateValue(CachedValue target) {
        synchronized (this) {
            if (cachedValueEntry == null || target == null || cachedValueEntry.value != target) {
                return false;
            }
            cachedValueEntry = null;
            return true;
        }
    }

    /**
     * Starts refresh if a usable value is due for refresh and failure backoff has elapsed.
     */
    public void refreshValueInBackground() {
        refreshValueInBackground(false);
    }

    /**
     * Marks a usable value due for refresh, respecting failure backoff and sharing any existing acquisition.
     */
    public void forceRefreshValueInBackground() {
        refreshValueInBackground(true);
    }

    private void refreshValueInBackground(boolean force) {
        ValueAcquisition<CachedValue> acquisition;

        synchronized (this) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (!isUsable(now)) {
                return;
            }
            if (force) {
                cachedValueEntry = new CachedValueEntry<>(cachedValueEntry.value, cachedValueEntry.expiration, now);
            }
            if (now.isBefore(cachedValueEntry.refreshAt) || isRetryBackoffActive(now)) {
                return;
            }
            acquisition = acquireValue();
        }
        if (acquisition.isOwner) {
            acquireValueAsync(acquisition, true).subscribe(ignored -> {
            }, error -> LOGGER.warning("Background value refresh failed.", error));
        }
    }

    private synchronized CachedValue getUsableValue() {
        return isUsable(OffsetDateTime.now(clock)) ? cachedValueEntry.value : null;
    }

    private synchronized ValueAcquisition<CachedValue> acquireForegroundValue() {
        // Another caller may have published a usable value since the initial lookup.
        if (isUsable(OffsetDateTime.now(clock))) {
            return new ValueAcquisition<>(CompletableFuture.completedFuture(cachedValueEntry.value), false);
        }
        return acquireValue();
    }

    private ValueAcquisition<CachedValue> acquireValue() {
        if (inFlightAcquisition != null) {
            return new ValueAcquisition<>(inFlightAcquisition, false);
        }
        inFlightAcquisition = new CompletableFuture<>();
        return new ValueAcquisition<>(inFlightAcquisition, true);
    }

    private Mono<CachedValue> acquireValueAsync(ValueAcquisition<CachedValue> acquisition, boolean background) {
        if (!acquisition.isOwner) {
            return Mono.fromFuture(acquisition.result, true);
        }
        Mono<CachedValue> source = Mono.defer(asyncSupplier)
            .switchIfEmpty(Mono.error(new IllegalStateException("The value supplier completed without a value.")));
        if (background) {
            source = source.timeout(BACKGROUND_ACQUIRE_TIMEOUT, timeoutScheduler);
        }
        return source.doOnNext(value -> complete(acquisition, value))
            .doOnError(error -> fail(acquisition, error))
            .cache();
    }

    private void complete(ValueAcquisition<CachedValue> acquisition, CachedValue value) {
        Objects.requireNonNull(value, "The value supplier returned null.");
        OffsetDateTime expiration
            = Objects.requireNonNull(expirationExtractor.apply(value), "The value expiration cannot be null.");
        OffsetDateTime refreshAt = computeRefreshTime(OffsetDateTime.now(clock), expiration);
        synchronized (this) {
            if (inFlightAcquisition == acquisition.result) {
                cachedValueEntry = new CachedValueEntry<>(value, expiration, refreshAt);
                retryNotBefore = null;
                inFlightAcquisition = null;
            }
        }
        // Release ownership before invoking callbacks, which may invalidate or refresh the published value.
        acquisition.result.complete(value);
    }

    private void fail(ValueAcquisition<CachedValue> acquisition, Throwable error) {
        synchronized (this) {
            if (inFlightAcquisition == acquisition.result) {
                retryNotBefore = OffsetDateTime.now(clock).plus(REFRESH_RETRY_DELAY);
                inFlightAcquisition = null;
            }
        }
        acquisition.result.completeExceptionally(error);
    }

    private boolean isUsable(OffsetDateTime now) {
        return cachedValueEntry != null && !now.isAfter(cachedValueEntry.expiration);
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
