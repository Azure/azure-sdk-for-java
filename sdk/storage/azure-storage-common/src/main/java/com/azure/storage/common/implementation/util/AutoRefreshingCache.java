// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache for expiring values, such as storage session credentials and blob layouts. Values need not implement an
 * interface; an expiration extractor supplies their immutable expiration metadata.
 * <p>
 * Refresh is opportunistic rather than scheduled. Access to a usable value starts an asynchronous refresh when due,
 * while returning the current value. Failed background refreshes are retried after thirty seconds; acquisition of a
 * missing or expired value is not delayed by that backoff. There is no periodic timer or executor owned by this cache.
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
 * @param <T> The cached value type.
 */
public final class AutoRefreshingCache<T> {
    private static final ClientLogger LOGGER = new ClientLogger(AutoRefreshingCache.class);
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final Duration REFRESH_RETRY_DELAY = Duration.ofSeconds(30);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;

    private final Supplier<Mono<T>> asyncSupplier;
    private final Supplier<T> syncSupplier;
    private final Function<T, OffsetDateTime> expirationExtractor;
    private final Clock clock;
    private final AtomicReference<CacheState<T>> state = new AtomicReference<>(new CacheState<>(null, null, null));

    /**
     * Creates a cache with independent async and sync loaders and an explicit clock.
     * Callers without a dedicated sync loader can supply {@code () -> asyncSupplier.get().block()} for sync access.
     *
     * @param asyncSupplier The nonblocking async loader, also used for background refresh.
     * @param syncSupplier The sync loader.
     * @param expirationExtractor Extracts each value's expiration.
     * @param clock The clock used for cache decisions.
     */
    public AutoRefreshingCache(Supplier<Mono<T>> asyncSupplier, Supplier<T> syncSupplier,
        Function<T, OffsetDateTime> expirationExtractor, Clock clock) {
        this.asyncSupplier = Objects.requireNonNull(asyncSupplier, "'asyncSupplier' cannot be null.");
        this.syncSupplier = Objects.requireNonNull(syncSupplier, "'syncSupplier' cannot be null.");
        this.expirationExtractor = Objects.requireNonNull(expirationExtractor, "'expirationExtractor' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    /**
     * Gets a cached value or shares an acquisition. A usable value can trigger background refresh.
     *
     * @return A publisher emitting the cached or acquired value.
     */
    public Mono<T> getValidValueAsync() {
        return Mono.defer(() -> {
            AcquisitionDecision<T> decision = evaluateState();
            if (decision.cachedValue != null) {
                if (decision.shouldAcquire) {
                    startBackgroundRefresh(decision.pendingAcquisition);
                }
                return Mono.just(decision.cachedValue);
            }
            if (decision.shouldAcquire) {
                return acquireValueAsync(decision.pendingAcquisition);
            }
            return Mono.fromFuture(decision.pendingAcquisition, true);
        });
    }

    /**
     * Gets a cached value, invokes the sync loader, or waits for an existing acquisition.
     *
     * @return The cached or acquired value.
     */
    public T getValidValueSync() {
        AcquisitionDecision<T> decision = evaluateState();
        if (decision.cachedValue != null) {
            if (decision.shouldAcquire) {
                startBackgroundRefresh(decision.pendingAcquisition);
            }
            return decision.cachedValue;
        }
        if (decision.shouldAcquire) {
            try {
                publishAcquiredValue(decision.pendingAcquisition, syncSupplier.get());
            } catch (RuntimeException | Error error) {
                recordAcquisitionFailure(decision.pendingAcquisition, error);
            }
        }
        return awaitAcquisition(decision.pendingAcquisition);
    }

    /**
     * Removes the value only if it is the exact instance supplied by the caller. A stale rejection is a no-op.
     * An independent replacement already being acquired remains in flight and can be joined by subsequent callers.
     *
     * @param target The rejected value.
     * @return Whether the matching cached value was removed.
     */
    public boolean invalidateValue(T target) {
        while (true) {
            CacheState<T> current = state.get();
            if (current.cachedValue == null || current.cachedValue.value != target) {
                return false;
            }
            if (state.compareAndSet(current,
                new CacheState<>(null, current.pendingAcquisition, current.retryNotBefore))) {
                return true;
            }
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
        while (true) {
            CacheState<T> current = state.get();
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (!current.hasUsableValue(now)) {
                return;
            }

            CachedValue<T> cachedValue = current.cachedValue;
            if (force) {
                // Retain the hint even if backoff or a pending acquisition prevents starting a refresh.
                cachedValue = new CachedValue<>(cachedValue.value, cachedValue.expiration, now);
            }

            boolean shouldAcquire = current.pendingAcquisition == null
                && (force || current.isRefreshDue(now))
                && current.isRetryAllowed(now);
            if (!force && !shouldAcquire) {
                return;
            }

            CompletableFuture<T> pendingAcquisition = current.pendingAcquisition;
            if (shouldAcquire) {
                pendingAcquisition = new CompletableFuture<>();
            }
            CacheState<T> updated = new CacheState<>(cachedValue, pendingAcquisition, current.retryNotBefore);
            if (state.compareAndSet(current, updated)) {
                if (shouldAcquire) {
                    startBackgroundRefresh(pendingAcquisition);
                }
                return;
            }
        }
    }

    private AcquisitionDecision<T> evaluateState() {
        while (true) {
            CacheState<T> current = state.get();
            OffsetDateTime now = OffsetDateTime.now(clock);
            T cachedValue = current.hasUsableValue(now) ? current.cachedValue.value : null;

            // A pending acquisition is shared, but callers with a usable value need not wait for it.
            if (current.pendingAcquisition != null) {
                return new AcquisitionDecision<>(cachedValue, current.pendingAcquisition, false);
            }
            if (cachedValue != null) {
                if (!current.isRefreshDue(now)) {
                    return new AcquisitionDecision<>(cachedValue, null, false);
                }
                if (!current.isRetryAllowed(now)) {
                    return new AcquisitionDecision<>(cachedValue, null, false);
                }
            }

            // Claim background refresh for a hit, or foreground acquisition for a miss (without backoff).
            CompletableFuture<T> pendingAcquisition = new CompletableFuture<>();
            CacheState<T> updated = new CacheState<>(current.cachedValue, pendingAcquisition, current.retryNotBefore);
            if (state.compareAndSet(current, updated)) {
                return new AcquisitionDecision<>(cachedValue, pendingAcquisition, true);
            }
        }
    }

    private void startBackgroundRefresh(CompletableFuture<T> pendingAcquisition) {
        acquireValueAsync(pendingAcquisition).subscribe(ignored -> {
        }, error -> LOGGER.warning("Background value refresh failed.", error));
    }

    private Mono<T> acquireValueAsync(CompletableFuture<T> pendingAcquisition) {
        return Mono.defer(asyncSupplier)
            .switchIfEmpty(Mono.error(new IllegalStateException("The value supplier completed without a value.")))
            .doOnNext(value -> publishAcquiredValue(pendingAcquisition, value))
            .doOnError(error -> recordAcquisitionFailure(pendingAcquisition, error))
            // Keep the shared acquisition running even if its initiating subscriber cancels.
            .cache();
    }

    private T awaitAcquisition(CompletableFuture<T> pendingAcquisition) {
        try {
            return pendingAcquisition.get();
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

    private void publishAcquiredValue(CompletableFuture<T> pendingAcquisition, T value) {
        Objects.requireNonNull(value, "The value supplier returned null.");
        OffsetDateTime expiration
            = Objects.requireNonNull(expirationExtractor.apply(value), "The value expiration cannot be null.");
        OffsetDateTime nextRefresh = computeRefreshTime(OffsetDateTime.now(clock), expiration);
        CacheState<T> completed = new CacheState<>(new CachedValue<>(value, expiration, nextRefresh), null, null);
        state.updateAndGet(current -> current.pendingAcquisition == pendingAcquisition ? completed : current);
        // Release ownership before invoking callbacks, which may invalidate or refresh the published value.
        pendingAcquisition.complete(value);
    }

    private void recordAcquisitionFailure(CompletableFuture<T> pendingAcquisition, Throwable error) {
        OffsetDateTime retryNotBefore = OffsetDateTime.now(clock).plus(REFRESH_RETRY_DELAY);
        state.updateAndGet(current -> current.pendingAcquisition == pendingAcquisition
            ? new CacheState<>(current.cachedValue, null, retryNotBefore)
            : current);
        pendingAcquisition.completeExceptionally(error);
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

    // A caller's decision, not shared state: only the caller that claims acquisition sets shouldAcquire.
    private static final class AcquisitionDecision<T> {
        private final T cachedValue;
        private final CompletableFuture<T> pendingAcquisition;
        private final boolean shouldAcquire;

        private AcquisitionDecision(T cachedValue, CompletableFuture<T> pendingAcquisition, boolean shouldAcquire) {
            this.cachedValue = cachedValue;
            this.pendingAcquisition = pendingAcquisition;
            this.shouldAcquire = shouldAcquire;
        }
    }

    private static final class CacheState<T> {
        private final CachedValue<T> cachedValue;
        private final CompletableFuture<T> pendingAcquisition;
        private final OffsetDateTime retryNotBefore;

        private CacheState(CachedValue<T> cachedValue, CompletableFuture<T> pendingAcquisition,
            OffsetDateTime retryNotBefore) {
            this.cachedValue = cachedValue;
            this.pendingAcquisition = pendingAcquisition;
            this.retryNotBefore = retryNotBefore;
        }

        private boolean hasUsableValue(OffsetDateTime now) {
            return cachedValue != null && !now.isAfter(cachedValue.expiration);
        }

        private boolean isRefreshDue(OffsetDateTime now) {
            return !now.isBefore(cachedValue.refreshAt);
        }

        private boolean isRetryAllowed(OffsetDateTime now) {
            return retryNotBefore == null || !now.isBefore(retryNotBefore);
        }
    }

    private static final class CachedValue<T> {
        private final T value;
        private final OffsetDateTime expiration;
        private final OffsetDateTime refreshAt;

        private CachedValue(T value, OffsetDateTime expiration, OffsetDateTime refreshAt) {
            this.value = value;
            this.expiration = expiration;
            this.refreshAt = refreshAt;
        }
    }
}
