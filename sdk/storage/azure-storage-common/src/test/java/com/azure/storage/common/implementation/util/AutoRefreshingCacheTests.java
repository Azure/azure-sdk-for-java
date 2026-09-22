// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic, network-free tests for {@link AutoRefreshingCache} time-based behavior.
 */
public class AutoRefreshingCacheTests {
    private static final String FIRST_VALUE = "first-value";
    private static final String SECOND_VALUE = "second-value";
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);

    @Test
    public void competingSyncAndAsyncCallersShareOneLoad() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch lookupsReady = new CountDownLatch(2);
        CountDownLatch releaseLookups = new CountDownLatch(1);
        CountDownLatch loaderEntered = new CountDownLatch(1);
        AtomicInteger clockReads = new AtomicInteger();
        Instant instant = Instant.parse("2026-06-19T00:00:00Z");
        Clock clock = new Clock() {
            @Override
            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return Clock.fixed(instant, zone);
            }

            @Override
            public Instant instant() {
                if (clockReads.incrementAndGet() <= 2) {
                    // Both lookups read the empty state before either can claim the load.
                    lookupsReady.countDown();
                    await(releaseLookups);
                }
                return instant;
            }
        };
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        Supplier<Mono<TestExpiringValue>> supplier = () -> {
            calls.incrementAndGet();
            loaderEntered.countDown();
            return pending.asMono();
        };
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(supplier,
            () -> supplier.get().block(Duration.ofSeconds(5)), TestExpiringValue::getExpiration, clock);
        TestExpiringValue created = value(FIRST_VALUE, instant.atOffset(ZoneOffset.UTC).plus(VALUE_LIFETIME));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<TestExpiringValue> sync = pool.submit(cache::getValidValueSync);
            Future<TestExpiringValue> async
                = pool.submit(() -> cache.getValidValueAsync().block(Duration.ofSeconds(5)));
            assertTrue(lookupsReady.await(5, TimeUnit.SECONDS));
            releaseLookups.countDown();
            assertTrue(loaderEntered.await(5, TimeUnit.SECONDS));
            pending.tryEmitValue(created);

            assertSame(created, sync.get(5, TimeUnit.SECONDS));
            assertSame(created, async.get(5, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
        } finally {
            releaseLookups.countDown();
            pending.tryEmitEmpty();
            pool.shutdownNow();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void refreshPublicationPreservesConcurrentInvalidationOrHint(boolean invalidate) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        CountDownLatch metadataEntered = new CountDownLatch(1);
        CountDownLatch releaseMetadata = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            calls.incrementAndGet();
            return pending.asMono();
        }, () -> first, value -> {
            if (value == second) {
                metadataEntered.countDown();
                await(releaseMetadata);
            }
            return value.getExpiration();
        }, clock);
        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> publication = pool.submit(() -> pending.tryEmitValue(second));
            assertTrue(metadataEntered.await(5, TimeUnit.SECONDS));
            if (invalidate) {
                assertTrue(cache.invalidateValue(first));
            } else {
                cache.forceRefreshValueInBackground();
            }
            CompletableFuture<TestExpiringValue> waiter = cache.getValidValueAsync().toFuture();
            if (invalidate) {
                assertFalse(waiter.isDone());
            } else {
                assertSame(first, waiter.get(5, TimeUnit.SECONDS));
            }

            releaseMetadata.countDown();
            publication.get(5, TimeUnit.SECONDS);
            if (invalidate) {
                assertSame(second, waiter.get(5, TimeUnit.SECONDS));
            }
            assertFalse(cache.invalidateValue(first));
            assertSame(second, cache.getValidValueSync());
            assertEquals(1, calls.get());
        } finally {
            releaseMetadata.countDown();
            pool.shutdownNow();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void usableValueIsReturnedWhileOneBackgroundRefreshIsPending(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onAsync(pending.asMono());
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueSync());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        for (int i = 0; i < 3; i++) {
            assertSame(first,
                async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
            cache.refreshValueInBackground();
            cache.forceRefreshValueInBackground();
        }
        assertEquals(1, provider.createSyncCount());
        assertEquals(1, provider.createAsyncCount());

        pending.tryEmitValue(second);
        clock.advance(Duration.ofSeconds(3));
        assertSame(second, cache.getValidValueSync());
        assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(1, provider.createAsyncCount());
        assertEquals(1, provider.createSyncCount());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void backgroundRefreshDoesNotLoadMissingOrExpiredValues(boolean force) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onSync(second);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);
        Runnable refresh = force ? cache::forceRefreshValueInBackground : cache::refreshValueInBackground;

        refresh.run();
        assertEquals(0, provider.createAsyncCount());
        assertEquals(0, provider.createSyncCount());
        assertSame(first, cache.getValidValueSync());

        clock.advance(VALUE_LIFETIME.plusSeconds(1));
        refresh.run();
        assertEquals(0, provider.createAsyncCount());
        assertEquals(1, provider.createSyncCount());
        assertSame(second, cache.getValidValueSync());
        assertEquals(2, provider.createSyncCount());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void cacheHitStartsRefreshOutsideTheCacheLock(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AtomicReference<AutoRefreshingCache<TestExpiringValue>> cacheReference = new AtomicReference<>();
        AtomicReference<Boolean> loaderHeldLock = new AtomicReference<>();
        AtomicReference<Boolean> subscriberHeldLock = new AtomicReference<>();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            AutoRefreshingCache<TestExpiringValue> currentCache = cacheReference.get();
            loaderHeldLock.set(Thread.holdsLock(currentCache));
            // Reject the old value so a reentrant subscriber joins this background load rather than using the cache.
            assertTrue(currentCache.invalidateValue(first));
            currentCache.getValidValueAsync()
                .doOnNext(ignored -> subscriberHeldLock.set(Thread.holdsLock(currentCache)))
                .subscribe();
            return Mono.just(second);
        }, () -> first, TestExpiringValue::getExpiration, clock);
        cacheReference.set(cache);
        assertSame(first, cache.getValidValueSync());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertSame(first, async ? cache.getValidValueAsync().block() : cache.getValidValueSync());
        assertEquals(Boolean.FALSE, loaderHeldLock.get());
        assertEquals(Boolean.FALSE, subscriberHeldLock.get());
        assertSame(second, cache.getValidValueSync());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void expiredValuesAreReplacedAfterInclusiveExpirationBoundary(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AtomicInteger calls = new AtomicInteger();
        Sinks.One<TestExpiringValue> refresh = Sinks.one();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(() -> calls.incrementAndGet() == 1 ? Mono.just(first) : refresh.asMono(),
                () -> refresh.asMono().block(), TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueAsync().block());
        clock.advance(VALUE_LIFETIME);
        assertSame(first, async ? cache.getValidValueAsync().block() : cache.getValidValueSync());
        assertEquals(2, calls.get());
        clock.advance(Duration.ofNanos(1));
        CompletableFuture<TestExpiringValue> waiting = cache.getValidValueAsync().toFuture();
        assertFalse(waiting.isDone());
        refresh.tryEmitValue(second);
        assertSame(second, waiting.join());
        assertSame(second, async ? cache.getValidValueAsync().block() : cache.getValidValueSync());
        assertEquals(2, calls.get());
    }

    @Test
    public void synchronousLoaderFailureReleasesAsyncJoiners() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        IllegalStateException failure = new IllegalStateException("sync load failed");
        TestExpiringValue recovered = value(SECOND_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> Mono.just(recovered), () -> {
            entered.countDown();
            await(release);
            throw failure;
        }, TestExpiringValue::getExpiration, Clock.systemUTC());
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> owner = pool.submit(cache::getValidValueSync);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
            assertFalse(joiner.isDone());
            release.countDown();
            assertSame(failure,
                assertThrows(java.util.concurrent.ExecutionException.class, () -> owner.get(5, TimeUnit.SECONDS))
                    .getCause());
            assertSame(failure,
                assertThrows(java.util.concurrent.ExecutionException.class, () -> joiner.get(5, TimeUnit.SECONDS))
                    .getCause());
            assertSame(recovered, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    public void suppliersKeepSyncAndAsyncLoadersIndependent() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AtomicInteger syncCalls = new AtomicInteger();
        AtomicInteger asyncCalls = new AtomicInteger();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return Mono.just(second);
        }, () -> {
            syncCalls.incrementAndGet();
            return first;
        }, TestExpiringValue::getExpiration, clock);

        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block());
        assertEquals(1, syncCalls.get());
        assertEquals(0, asyncCalls.get());
        cache.forceRefreshValueInBackground();
        assertSame(second, cache.getValidValueSync());
        assertEquals(1, asyncCalls.get());
        assertEquals(1, syncCalls.get());
    }

    @Test
    public void explicitSyncFallbackUsesAsyncSupplier() {
        TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
        Supplier<Mono<TestExpiringValue>> asyncSupplier = () -> Mono.just(created);
        AutoRefreshingCache<TestExpiringValue> supplied = new AutoRefreshingCache<>(asyncSupplier,
            () -> asyncSupplier.get().block(), TestExpiringValue::getExpiration, Clock.systemUTC());
        assertSame(created, supplied.getValidValueSync());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void invalidationDuringRefreshPreservesSingleAcquisition(boolean matching) throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            calls.incrementAndGet();
            return pending.asMono();
        }, () -> first, TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        assertEquals(matching, cache.invalidateValue(matching ? first : second));
        assertFalse(cache.invalidateValue(null));
        cache.forceRefreshValueInBackground();
        CompletableFuture<TestExpiringValue> caller = cache.getValidValueAsync().toFuture();
        assertEquals(1, calls.get());
        if (matching) {
            assertFalse(caller.isDone());
        } else {
            assertSame(first, caller.get(5, TimeUnit.SECONDS));
        }
        pending.tryEmitValue(second);
        assertSame(second, cache.getValidValueSync());
        if (matching) {
            assertSame(second, caller.get(5, TimeUnit.SECONDS));
        }
        assertEquals(1, calls.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void cancelledSubscribersDoNotCancelSharedAcquisition(boolean cancelAll) throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            calls.incrementAndGet();
            return pending.asMono().doOnCancel(cancellations::incrementAndGet);
        }, () -> pending.asMono().block(), TestExpiringValue::getExpiration, Clock.systemUTC());
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
        owner.cancel(true);
        if (cancelAll) {
            joiner.cancel(true);
        }
        CompletableFuture<TestExpiringValue> remaining = cache.getValidValueAsync().toFuture();
        assertEquals(1, calls.get());
        assertEquals(0, cancellations.get());
        TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
        pending.tryEmitValue(created);
        assertSame(created, remaining.get(5, TimeUnit.SECONDS));
        if (!cancelAll) {
            assertSame(created, joiner.get(5, TimeUnit.SECONDS));
        }
        assertSame(created, cache.getValidValueSync());
    }

    @Test
    public void asyncJoinerSharesSynchronousOwner() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger asyncCalls = new AtomicInteger();
        TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return Mono.just(created);
        }, () -> {
            entered.countDown();
            await(release);
            return created;
        }, TestExpiringValue::getExpiration, Clock.systemUTC());
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<TestExpiringValue> owner = pool.submit(cache::getValidValueSync);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
            assertFalse(joiner.isDone());
            release.countDown();
            assertSame(created, owner.get(5, TimeUnit.SECONDS));
            assertSame(created, joiner.get(5, TimeUnit.SECONDS));
            assertEquals(0, asyncCalls.get());
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void syncJoinerSharesAsyncOwnerEvenForExpiredResult(boolean expired) throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        TestExpiringValue created
            = value(FIRST_VALUE, expired ? OffsetDateTime.now().minusSeconds(1) : OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            calls.incrementAndGet();
            return pending.asMono();
        }, () -> {
            throw new IllegalStateException("The sync loader must not run.");
        }, TestExpiringValue::getExpiration, Clock.systemUTC());
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<TestExpiringValue> joiner = pool.submit(() -> {
                waitingThread.set(Thread.currentThread());
                return cache.getValidValueSync();
            });
            awaitWaiting(waitingThread);
            pending.tryEmitValue(created);
            assertSame(created, owner.get(5, TimeUnit.SECONDS));
            assertSame(created, joiner.get(5, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
        } finally {
            pending.tryEmitEmpty();
            pool.shutdownNow();
        }
    }

    @Test
    public void interruptingSyncWaiterDoesNotCancelAsyncAcquisition() throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(pending::asMono,
            () -> pending.asMono().block(), TestExpiringValue::getExpiration, Clock.systemUTC());
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> joiner = pool.submit(() -> {
                waitingThread.set(Thread.currentThread());
                IllegalStateException error = assertThrows(IllegalStateException.class, cache::getValidValueSync);
                assertTrue(error.getCause() instanceof InterruptedException);
                return Thread.currentThread().isInterrupted();
            });
            awaitWaiting(waitingThread);
            waitingThread.get().interrupt();
            assertTrue(joiner.get(5, TimeUnit.SECONDS));
            assertFalse(owner.isDone());
            TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
            pending.tryEmitValue(created);
            assertSame(created, owner.get(5, TimeUnit.SECONDS));
            assertSame(created, cache.getValidValueSync());
        } finally {
            pending.tryEmitEmpty();
            pool.shutdownNow();
        }
    }

    @Test
    public void invalidRefreshMetadataRetainsPublishedValueAndBacksOff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AtomicInteger calls = new AtomicInteger();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            return Mono.just(calls.incrementAndGet() == 1 ? value(SECOND_VALUE, null) : second);
        }, () -> first, TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        assertFalse(cache.invalidateValue(second));
        cache.forceRefreshValueInBackground();
        assertSame(first, cache.getValidValueAsync().block());
        assertEquals(1, calls.get());
        clock.advance(Duration.ofSeconds(30));
        assertSame(first, cache.getValidValueAsync().block());
        assertSame(second, cache.getValidValueAsync().block());
        assertEquals(2, calls.get());
    }

    @Test
    public void failedRefreshAfterInvalidationSettlesWaitersAndAllowsForegroundRetry() {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        TestExpiringValue first = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
        TestExpiringValue second = value(SECOND_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(() -> calls.incrementAndGet() == 1 ? pending.asMono() : Mono.just(second),
                () -> first, TestExpiringValue::getExpiration, Clock.systemUTC());
        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        assertTrue(cache.invalidateValue(first));
        CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
        assertFalse(joiner.isDone());
        pending.tryEmitError(new IllegalStateException("refresh failed"));
        assertTrue(joiner.isCompletedExceptionally());
        assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(2, calls.get());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4 })
    public void invalidAsyncResultsFailAllWaitersAndPermitRetry(int failure) throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        TestExpiringValue recovered = value(SECOND_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            if (calls.incrementAndGet() > 1) {
                return Mono.just(recovered);
            }
            if (failure == 0) {
                throw new IllegalStateException("supplier threw");
            }
            if (failure == 1) {
                return null;
            }
            return pending.asMono();
        }, () -> recovered, TestExpiringValue::getExpiration, Clock.systemUTC());
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        CompletableFuture<TestExpiringValue> joiner = null;
        if (failure >= 2) {
            joiner = cache.getValidValueAsync().toFuture();
            assertFalse(joiner.isDone());
            if (failure == 2) {
                pending.tryEmitEmpty();
            } else if (failure == 3) {
                pending.tryEmitError(new IllegalStateException("load failed"));
            } else {
                pending.tryEmitValue(value(FIRST_VALUE, null));
            }
        }
        assertTrue(owner.isCompletedExceptionally());
        if (joiner != null) {
            assertTrue(joiner.isCompletedExceptionally());
        }
        assertSame(recovered, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(2, calls.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void invalidSyncResultsPermitRetry(boolean nullValue) {
        AtomicInteger calls = new AtomicInteger();
        TestExpiringValue recovered = value(SECOND_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> Mono.just(recovered), () -> {
            if (calls.incrementAndGet() == 1) {
                return nullValue ? null : value(FIRST_VALUE, null);
            }
            return recovered;
        }, TestExpiringValue::getExpiration, Clock.systemUTC());
        assertThrows(NullPointerException.class, cache::getValidValueSync);
        assertSame(recovered, cache.getValidValueSync());
    }

    @Test
    public void asyncAcquisitionPreservesContextButBackgroundRefreshDoesNotInheritIt() {
        AtomicReference<String> context = new AtomicReference<>();
        TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> Mono.deferContextual(view -> {
            context.set(view.getOrDefault("request", "background"));
            return Mono.just(created);
        }), () -> created, TestExpiringValue::getExpiration, Clock.systemUTC());
        assertSame(created, cache.getValidValueAsync().contextWrite(view -> view.put("request", "foreground")).block());
        assertEquals("foreground", context.get());
        cache.getValidValueAsync()
            .doOnNext(ignored -> cache.forceRefreshValueInBackground())
            .contextWrite(view -> view.put("request", "another-request"))
            .block();
        assertEquals("background", context.get());
    }

    @Test
    public void extremeExpirationDoesNotOverflow() {
        OffsetDateTime now = OffsetDateTime.parse("2026-06-19T00:00:00Z");
        for (int i = 0; i < 100; i++) {
            OffsetDateTime refresh = AutoRefreshingCache.computeRefreshTime(now, OffsetDateTime.MAX);
            assertTrue(refresh.isAfter(now));
            assertTrue(refresh.isBefore(OffsetDateTime.MAX));
            assertEquals(now, AutoRefreshingCache.computeRefreshTime(now, OffsetDateTime.MIN));
            OffsetDateTime normal = AutoRefreshingCache.computeRefreshTime(now, now.plusMinutes(5));
            assertFalse(normal.isBefore(now.plusSeconds(236)));
            assertTrue(normal.isBefore(now.plusSeconds(295)));
        }
        TestExpiringValue created = value(FIRST_VALUE, OffsetDateTime.MAX);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> Mono.just(created),
            () -> created, TestExpiringValue::getExpiration, Clock.systemUTC());
        assertSame(created, cache.getValidValueAsync().block());
        assertSame(created, cache.getValidValueSync());
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error);
        }
    }

    private static void awaitWaiting(AtomicReference<Thread> thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Thread current = thread.get();
            if (current != null && current.getState() == Thread.State.WAITING) {
                return;
            }
            Thread.yield();
        }
        throw new AssertionError("The sync caller did not join the pending acquisition.");
    }

    @Test
    public void expiredByTimeOnSecondRequestCreatesNewValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        OffsetDateTime expiration = now(clock).plus(VALUE_LIFETIME);
        provider.onSync(value(FIRST_VALUE, expiration))
            .onSync(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createSyncCount());
        assertEquals(0, provider.createAsyncCount());

        clock.advance(VALUE_LIFETIME.plusSeconds(1));

        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        assertEquals(2, provider.createSyncCount());
        assertEquals(0, provider.createAsyncCount());
    }

    @Test
    public void automaticBackgroundRefreshFiresWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onAsync(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createSyncCount());
        assertEquals(0, provider.createAsyncCount());

        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createSyncCount());
        assertEquals(1, provider.createAsyncCount());
    }

    @Test
    public void failedBackgroundRefreshIsThrottled() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onAsync(Mono.error(new RuntimeException("boom")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        }

        assertEquals(1, provider.createSyncCount());
        assertEquals(1, provider.createAsyncCount());
    }

    @Test
    public void throttledRefreshRetriesAfterBackoffElapses() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onAsync(Mono.error(new RuntimeException("boom")))
            .onAsync(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        cache.forceRefreshValueInBackground();

        assertEquals(1, provider.createAsyncCount());

        // One second before the backoff elapses the retry must still be suppressed.
        clock.advance(Duration.ofSeconds(29));
        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());

        // Once it elapses the retry proceeds and the new value is adopted.
        clock.advance(Duration.ofSeconds(1));
        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(2, provider.createAsyncCount());
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
    }

    @Test
    public void forcedRefreshRespectsFailureBackoff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onAsync(Mono.error(new RuntimeException("boom")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());

        cache.forceRefreshValueInBackground();

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void explicitBackgroundRefreshRetriesAtBackoffBoundary(boolean force) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first)
            .onAsync(Mono.error(new IllegalStateException("refresh failed")))
            .onAsync(Mono.just(second));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);
        Runnable refresh = force ? cache::forceRefreshValueInBackground : cache::refreshValueInBackground;

        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        clock.advance(Duration.ofSeconds(29));
        refresh.run();
        assertEquals(1, provider.createAsyncCount());
        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block(Duration.ofSeconds(5)));

        // The original hint remains due even before the normal jitter window opens.
        clock.advance(Duration.ofSeconds(1));
        refresh.run();
        assertEquals(2, provider.createAsyncCount());
        assertSame(second, cache.getValidValueSync());
        assertEquals(1, provider.createSyncCount());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void invalidatedValueIsReacquiredDuringBackoff(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onAsync(Mono.error(new IllegalStateException("refresh failed")));
        if (async) {
            provider.onAsync(Mono.just(second));
        } else {
            provider.onSync(second);
        }
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        assertTrue(cache.invalidateValue(first));
        assertSame(second, async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
        assertEquals(async ? 2 : 1, provider.createAsyncCount());
        assertEquals(async ? 1 : 2, provider.createSyncCount());
    }

    @Test
    public void expiredValueIsStillCreatedDuringBackoff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onSync(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2))))
            .onAsync(Mono.error(new RuntimeException("boom")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());

        clock.advance(Duration.ofSeconds(3));

        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        assertEquals(2, provider.createSyncCount());
        assertEquals(1, provider.createAsyncCount());
    }

    @Test
    public void successfulCreationClearsFailureBackoff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        // The replacement is deliberately short-lived so its own refresh window opens while the failure
        // backoff armed by the first value would still have been active.
        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)))
            .onSync(value(SECOND_VALUE, now(clock).plus(Duration.ofSeconds(321))))
            .onAsync(Mono.error(new RuntimeException("boom")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        // T+298s: inside the first value's refresh window. The background refresh fails and arms the
        // backoff until T+328s.
        clock.advance(VALUE_LIFETIME.minusSeconds(2));
        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(1, provider.createAsyncCount());

        // T+301s: the first value has expired, so the unthrottled foreground path mints a replacement.
        clock.advance(Duration.ofSeconds(3));
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        assertEquals(2, provider.createSyncCount());

        // T+317s: inside the replacement's refresh window but still before T+328s, so this refresh can
        // only happen because the successful creation cleared the backoff.
        clock.advance(Duration.ofSeconds(16));
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        assertEquals(2, provider.createAsyncCount());
    }

    @Test
    public void forcedRefreshFromWithinOnNextStartsNewCreation() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onAsync(Mono.just(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME))))
            .onAsync(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        // Mirrors the production pipeline: a downstream subscriber inspects the response for the
        // "session expiring" hint and forces a refresh from inside onNext, which runs before the
        // creation Mono has reached its terminal signal.
        TestExpiringValue delivered
            = cache.getValidValueAsync().doOnNext(ignored -> cache.forceRefreshValueInBackground()).block();

        assertEquals(FIRST_VALUE, delivered.getValue());
        // The reentrant force must start a brand new creation rather than handing back the creation
        // that is still mid-delivery.
        assertEquals(2, provider.createAsyncCount());
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
    }

    @Test
    public void forcedRefreshFromWithinJoinerOnNextStartsNewCreation() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        // The first creation is delayed so a joiner can attach.
        provider
            .onAsync(
                Mono.just(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME))).delayElement(Duration.ofMillis(500)))
            .onAsync(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        Mono<TestExpiringValue> owner = cache.getValidValueAsync();
        Mono<TestExpiringValue> joiner
            = cache.getValidValueAsync().doOnNext(ignored -> cache.forceRefreshValueInBackground());

        // Run both owner and joiner pipelines. The joiner will be notified when the owner completes.
        reactor.core.publisher.Mono.when(owner, joiner).block();

        // The reentrant force from the joiner must start a brand new creation.
        assertEquals(2, provider.createAsyncCount());
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
    }

    @Test
    public void noRefreshBeforeJitterWindowWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        provider.onSync(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(Duration.ofSeconds(30));

        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        }

        assertEquals(1, provider.createSyncCount());
        assertEquals(0, provider.createAsyncCount());
    }

    @Test
    public void invalidateValueClearsMatchingValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue first = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onSync(second);

        assertSame(first, cache.getValidValueSync());

        assertTrue(cache.invalidateValue(first), "Invalidating the live value should report success.");

        // The rejected value was the live one, so the next call must mint a replacement.
        assertSame(second, cache.getValidValueSync());
        assertEquals(2, provider.createSyncCount());
    }

    @Test
    public void invalidateValueIgnoresStaleTarget() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue live = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        provider.onSync(live);

        assertSame(live, cache.getValidValueSync());

        // A late rejection naming a value that has already been replaced must not evict the live one.
        assertFalse(cache.invalidateValue(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME))),
            "Invalidating a value that is not the cached one should report failure.");

        assertSame(live, cache.getValidValueSync());
        assertEquals(1, provider.createSyncCount());
    }

    @Test
    public void concurrentSyncCallersShareASingleCreation() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue created = value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME));
        CountDownLatch creationEntered = new CountDownLatch(1);
        CountDownLatch releaseCreation = new CountDownLatch(1);
        provider.onSync(() -> {
            creationEntered.countDown();
            assertTrue(releaseCreation.await(10, TimeUnit.SECONDS));
            return created;
        });

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<TestExpiringValue> owner = pool.submit(cache::getValidValueSync);
            // Only start the second caller once the first genuinely owns the in-flight creation.
            assertTrue(creationEntered.await(10, TimeUnit.SECONDS));
            Future<TestExpiringValue> joiner = pool.submit(cache::getValidValueSync);

            releaseCreation.countDown();

            assertSame(created, owner.get(10, TimeUnit.SECONDS));
            assertSame(created, joiner.get(10, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        // The joiner must have reused the owner's creation rather than minting a duplicate.
        assertEquals(1, provider.createSyncCount());
    }

    @Test
    public void valueThatIsAlreadyExpiredOnArrivalIsReturnedNotRecreated() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        // Clock skew can make a freshly minted value look expired the moment it arrives. It must be
        // handed back once rather than sending the caller into another creation.
        TestExpiringValue stillborn = value(FIRST_VALUE, now(clock).minusSeconds(1));
        provider.onSync(stillborn);

        assertSame(stillborn, cache.getValidValueSync());
        assertEquals(1, provider.createSyncCount());
    }

    @Test
    public void syncJoinerReturnsAnAlreadyExpiredValueWithoutRecreating() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue stillborn = value(FIRST_VALUE, now(clock).minusSeconds(1));
        CountDownLatch creationEntered = new CountDownLatch(1);
        CountDownLatch releaseCreation = new CountDownLatch(1);
        provider.onSync(() -> {
            creationEntered.countDown();
            assertTrue(releaseCreation.await(10, TimeUnit.SECONDS));
            return stillborn;
        });

        AtomicReference<Thread> joinerThread = new AtomicReference<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<TestExpiringValue> owner = pool.submit(cache::getValidValueSync);
            assertTrue(creationEntered.await(10, TimeUnit.SECONDS));

            Future<TestExpiringValue> joiner = pool.submit(() -> {
                joinerThread.set(Thread.currentThread());
                return cache.getValidValueSync();
            });

            // Wait until the joiner has actually parked on the in-flight creation's latch.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            Thread running = joinerThread.get();
            while (System.nanoTime() < deadline
                && (running == null
                    || running.getState() == Thread.State.RUNNABLE
                    || running.getState() == Thread.State.NEW)) {
                Thread.yield();
                running = joinerThread.get();
            }

            releaseCreation.countDown();

            assertSame(stillborn, owner.get(10, TimeUnit.SECONDS));
            // The joiner must hand back what the owner published even though it is already expired,
            // rather than looping and minting a second value.
            assertSame(stillborn, joiner.get(10, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, provider.createSyncCount());
    }

    /**
     * Records how many times each creation path was entered and hands back queued results in order,
     * replaying the last configured result once the queue is drained.
     */
    private static final class RecordingSuppliers {
        private final Queue<Callable<TestExpiringValue>> syncResults = new ArrayDeque<>();
        private final Queue<Mono<TestExpiringValue>> asyncResults = new ArrayDeque<>();
        private final AtomicInteger syncCount = new AtomicInteger();
        private final AtomicInteger asyncCount = new AtomicInteger();
        private Callable<TestExpiringValue> lastSyncResult;
        private Mono<TestExpiringValue> lastAsyncResult;

        RecordingSuppliers onSync(TestExpiringValue result) {
            return onSync(() -> result);
        }

        synchronized RecordingSuppliers onSync(Callable<TestExpiringValue> result) {
            syncResults.add(result);
            return this;
        }

        synchronized RecordingSuppliers onAsync(Mono<TestExpiringValue> result) {
            asyncResults.add(result);
            return this;
        }

        int createSyncCount() {
            return syncCount.get();
        }

        int createAsyncCount() {
            return asyncCount.get();
        }

        public TestExpiringValue createSync() {
            syncCount.incrementAndGet();
            Callable<TestExpiringValue> result;
            synchronized (this) {
                Callable<TestExpiringValue> queued = syncResults.poll();
                if (queued != null) {
                    lastSyncResult = queued;
                }
                result = lastSyncResult;
            }

            if (result == null) {
                throw new AssertionError("createSync() was called but no result was configured.");
            }

            try {
                return result.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Mono<TestExpiringValue> createAsync() {
            asyncCount.incrementAndGet();
            Mono<TestExpiringValue> result;
            synchronized (this) {
                Mono<TestExpiringValue> queued = asyncResults.poll();
                if (queued != null) {
                    lastAsyncResult = queued;
                }
                result = lastAsyncResult;
            }

            if (result == null) {
                throw new AssertionError("createAsync() was called but no result was configured.");
            }

            return result;
        }
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static TestExpiringValue value(String value, OffsetDateTime expiration) {
        return new TestExpiringValue(value, expiration);
    }

    private static final class TestExpiringValue {
        private final String value;
        private final OffsetDateTime expiration;

        private TestExpiringValue(String value, OffsetDateTime expiration) {
            this.value = value;
            this.expiration = expiration;
        }

        public OffsetDateTime getExpiration() {
            return expiration;
        }

        private String getValue() {
            return value;
        }
    }

    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

        private MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new MutableClock(instant, newZone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
