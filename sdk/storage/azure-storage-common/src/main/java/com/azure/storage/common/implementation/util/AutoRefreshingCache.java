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
    private Entry<T> entry;
    private CompletableFuture<T> inFlight;
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
            T current = getUsableValue();
            if (current != null) {
                refreshValueInBackground();
                return Mono.just(current);
            }
            return loadAsync(acquireForegroundLoad());
        });
    }

    /**
     * Gets a cached value, invokes the sync loader, or waits for an existing acquisition.
     *
     * @return The cached or acquired value.
     */
    public T getValidValueSync() {
        T current = getUsableValue();
        if (current != null) {
            refreshValueInBackground();
            return current;
        }
        Load<T> load = acquireForegroundLoad();
        if (load.owner) {
            try {
                complete(load, syncSupplier.get());
            } catch (RuntimeException | Error error) {
                fail(load, error);
            }
        }
        try {
            return load.result.get();
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
    public boolean invalidateValue(T target) {
        synchronized (this) {
            if (entry == null || target == null || entry.value != target) {
                return false;
            }
            entry = null;
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
        Load<T> load;
        synchronized (this) {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (!isUsable(now)) {
                return;
            }
            if (force) {
                entry = new Entry<>(entry.value, entry.expiration, now);
            }
            if (now.isBefore(entry.refreshAt) || isRetryBackoffActive(now)) {
                return;
            }
            load = acquireLoad();
        }
        if (load.owner) {
            loadAsync(load).subscribe(ignored -> {
            }, error -> LOGGER.warning("Background value refresh failed.", error));
        }
    }

    private synchronized T getUsableValue() {
        return isUsable(OffsetDateTime.now(clock)) ? entry.value : null;
    }

    private synchronized Load<T> acquireForegroundLoad() {
        // Another caller may have published a usable value since the initial lookup.
        if (isUsable(OffsetDateTime.now(clock))) {
            return new Load<>(CompletableFuture.completedFuture(entry.value), false);
        }
        return acquireLoad();
    }

    private Load<T> acquireLoad() {
        if (inFlight != null) {
            return new Load<>(inFlight, false);
        }
        inFlight = new CompletableFuture<>();
        return new Load<>(inFlight, true);
    }

    private Mono<T> loadAsync(Load<T> load) {
        if (!load.owner) {
            return Mono.fromFuture(load.result, true);
        }
        return Mono.defer(asyncSupplier)
            .switchIfEmpty(Mono.error(new IllegalStateException("The value supplier completed without a value.")))
            .doOnNext(value -> complete(load, value))
            .doOnError(error -> fail(load, error))
            .cache();
    }

    private void complete(Load<T> load, T value) {
        Objects.requireNonNull(value, "The value supplier returned null.");
        OffsetDateTime expiration
            = Objects.requireNonNull(expirationExtractor.apply(value), "The value expiration cannot be null.");
        OffsetDateTime refreshAt = computeRefreshTime(OffsetDateTime.now(clock), expiration);
        synchronized (this) {
            if (inFlight == load.result) {
                entry = new Entry<>(value, expiration, refreshAt);
                retryNotBefore = null;
                inFlight = null;
            }
        }
        // Release ownership before invoking callbacks, which may invalidate or refresh the published value.
        load.result.complete(value);
    }

    private void fail(Load<T> load, Throwable error) {
        synchronized (this) {
            if (inFlight == load.result) {
                retryNotBefore = OffsetDateTime.now(clock).plus(REFRESH_RETRY_DELAY);
                inFlight = null;
            }
        }
        load.result.completeExceptionally(error);
    }

    private boolean isUsable(OffsetDateTime now) {
        return entry != null && !now.isAfter(entry.expiration);
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

    private static final class Load<T> {
        private final CompletableFuture<T> result;
        private final boolean owner;

        private Load(CompletableFuture<T> result, boolean owner) {
            this.result = result;
            this.owner = owner;
        }
    }

    private static final class Entry<T> {
        private final T value;
        private final OffsetDateTime expiration;
        private final OffsetDateTime refreshAt;

        private Entry(T value, OffsetDateTime expiration, OffsetDateTime refreshAt) {
            this.value = value;
            this.expiration = expiration;
            this.refreshAt = refreshAt;
        }
    }
}
