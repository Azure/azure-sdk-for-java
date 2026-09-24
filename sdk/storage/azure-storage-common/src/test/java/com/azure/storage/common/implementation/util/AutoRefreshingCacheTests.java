// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core lifecycle and shared-acquisition tests for {@link AutoRefreshingCache}.
 */
public class AutoRefreshingCacheTests {
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void usableValueIsReturnedWhileOneBackgroundRefreshIsPending(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        TestExpiringValue first = value(now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onAsync(pending.asMono());
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueSync());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        for (int i = 0; i < 3; i++) {
            assertSame(first,
                async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
            assertEquals(1, provider.createAsyncCount());
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

    @Test
    public void synchronousLoaderFailureReleasesAsyncJoiners() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        IllegalStateException failure = new IllegalStateException("sync load failed");
        TestExpiringValue recovered = value(OffsetDateTime.now().plusHours(1));
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
    public void cancelledSubscriberDoesNotCancelSharedAcquisition() throws Exception {
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
        assertEquals(1, calls.get());
        assertEquals(0, cancellations.get());
        TestExpiringValue created = value(OffsetDateTime.now().plusHours(1));
        pending.tryEmitValue(created);
        assertSame(created, joiner.get(5, TimeUnit.SECONDS));
        assertSame(created, cache.getValidValueSync());
    }

    @Test
    public void syncAndAsyncCallersShareSynchronousAcquisition() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger syncCalls = new AtomicInteger();
        AtomicInteger asyncCalls = new AtomicInteger();
        TestExpiringValue created = value(OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return Mono.just(created);
        }, () -> {
            syncCalls.incrementAndGet();
            entered.countDown();
            await(release);
            return created;
        }, TestExpiringValue::getExpiration, Clock.systemUTC());
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<TestExpiringValue> owner = pool.submit(cache::getValidValueSync);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
            assertFalse(joiner.isDone());
            Future<TestExpiringValue> syncJoiner = pool.submit(() -> {
                waitingThread.set(Thread.currentThread());
                return cache.getValidValueSync();
            });
            awaitWaiting(waitingThread);
            release.countDown();
            assertSame(created, owner.get(5, TimeUnit.SECONDS));
            assertSame(created, joiner.get(5, TimeUnit.SECONDS));
            assertSame(created, syncJoiner.get(5, TimeUnit.SECONDS));
            assertEquals(1, syncCalls.get());
            assertEquals(0, asyncCalls.get());
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    public void syncJoinerSharesAsyncOwner() throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        TestExpiringValue created = value(OffsetDateTime.now().plusHours(1));
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
    public void backgroundTimeoutReleasesSyncAndAsyncWaitersAndAllowsRetry() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        TestExpiringValue first = value(now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(now(clock).plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> calls.incrementAndGet() == 1
            ? Mono.<TestExpiringValue>never().doOnCancel(cancellations::incrementAndGet)
            : Mono.just(second), () -> first, TestExpiringValue::getExpiration, clock, scheduler);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            assertSame(first, cache.getValidValueSync());
            clock.advance(VALUE_LIFETIME.minusSeconds(2));
            assertSame(first, cache.getValidValueSync());
            clock.advance(Duration.ofSeconds(3));
            CompletableFuture<TestExpiringValue> asyncWaiter = cache.getValidValueAsync().toFuture();
            AtomicReference<Thread> syncThread = new AtomicReference<>();
            Future<TestExpiringValue> syncWaiter = pool.submit(() -> {
                syncThread.set(Thread.currentThread());
                return cache.getValidValueSync();
            });
            awaitWaiting(syncThread);
            assertFalse(asyncWaiter.isDone());
            assertEquals(1, calls.get());

            scheduler.advanceTimeBy(Duration.ofSeconds(30));
            ExecutionException asyncError
                = assertThrows(ExecutionException.class, () -> asyncWaiter.get(5, TimeUnit.SECONDS));
            assertTrue(asyncError.getCause() instanceof TimeoutException);
            ExecutionException syncError
                = assertThrows(ExecutionException.class, () -> syncWaiter.get(5, TimeUnit.SECONDS));
            assertTrue(syncError.getCause() instanceof IllegalStateException);
            assertSame(asyncError.getCause(), syncError.getCause().getCause());
            assertEquals(1, cancellations.get());

            assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
            assertSame(second, cache.getValidValueSync());
            assertEquals(2, calls.get());
        } finally {
            pool.shutdownNow();
            scheduler.dispose();
        }
    }

    @Test
    public void foregroundAcquisitionDoesNotUseBackgroundTimeout() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        TestExpiringValue first = value(now(clock).plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(pending::asMono, () -> first,
            TestExpiringValue::getExpiration, clock, scheduler);
        try {
            CompletableFuture<TestExpiringValue> acquisition = cache.getValidValueAsync().toFuture();
            scheduler.advanceTimeBy(Duration.ofMinutes(1));
            assertFalse(acquisition.isDone());
            pending.tryEmitValue(first);
            assertSame(first, acquisition.get(5, TimeUnit.SECONDS));
        } finally {
            scheduler.dispose();
        }
    }

    @Test
    public void asyncLoaderFailureReleasesWaitersAndAllowsRetry() {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger calls = new AtomicInteger();
        IllegalStateException failure = new IllegalStateException("load failed");
        TestExpiringValue recovered = value(OffsetDateTime.now().plusHours(1));
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(() -> calls.incrementAndGet() == 1 ? pending.asMono() : Mono.just(recovered),
                () -> recovered, TestExpiringValue::getExpiration, Clock.systemUTC());
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
        assertFalse(joiner.isDone());
        pending.tryEmitError(failure);
        assertSame(failure, assertThrows(ExecutionException.class, () -> owner.get(5, TimeUnit.SECONDS)).getCause());
        assertSame(failure, assertThrows(ExecutionException.class, () -> joiner.get(5, TimeUnit.SECONDS)).getCause());
        assertSame(recovered, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(2, calls.get());
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

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void cachedValueIsReusedUntilExpiration(boolean async) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue first = value(now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        if (async) {
            provider.onAsync(Mono.just(first)).onAsync(Mono.just(second));
        } else {
            provider.onSync(first).onSync(second);
        }

        assertSame(first, async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
        clock.advance(Duration.ofSeconds(30));
        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(async ? 1 : 0, provider.createAsyncCount());
        assertEquals(async ? 0 : 1, provider.createSyncCount());
        clock.advance(VALUE_LIFETIME.plusSeconds(1));

        assertSame(second, async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
        assertEquals(async ? 2 : 0, provider.createAsyncCount());
        assertEquals(async ? 0 : 2, provider.createSyncCount());
    }

    @Test
    public void failedRefreshRetainsValueAndRetriesAfterBackoff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        TestExpiringValue first = value(now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first)
            .onAsync(Mono.error(new IllegalStateException("refresh failed")))
            .onAsync(Mono.just(second));
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        for (int i = 0; i < 3; i++) {
            assertSame(first, cache.getValidValueSync());
        }
        assertEquals(1, provider.createAsyncCount());
        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block(Duration.ofSeconds(5)));

        clock.advance(Duration.ofSeconds(31));
        assertSame(first, cache.getValidValueSync());
        assertEquals(2, provider.createAsyncCount());
        assertSame(second, cache.getValidValueSync());
        assertEquals(1, provider.createSyncCount());
    }

    @Test
    public void invalidateValueClearsOnlyMatchingValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingSuppliers provider = new RecordingSuppliers();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider::createAsync,
            provider::createSync, TestExpiringValue::getExpiration, clock);

        TestExpiringValue first = value(now(clock).plus(VALUE_LIFETIME));
        TestExpiringValue second = value(now(clock).plus(VALUE_LIFETIME.multipliedBy(2)));
        provider.onSync(first).onSync(second);

        assertSame(first, cache.getValidValueSync());
        assertTrue(cache.invalidateValue(first));
        assertSame(second, cache.getValidValueSync());
        assertFalse(cache.invalidateValue(first));
        assertSame(second, cache.getValidValueSync());
        assertEquals(2, provider.createSyncCount());
    }

    private static final class RecordingSuppliers {
        private final Queue<TestExpiringValue> syncResults = new ArrayDeque<>();
        private final Queue<Mono<TestExpiringValue>> asyncResults = new ArrayDeque<>();
        private final AtomicInteger syncCount = new AtomicInteger();
        private final AtomicInteger asyncCount = new AtomicInteger();

        RecordingSuppliers onSync(TestExpiringValue result) {
            syncResults.add(result);
            return this;
        }

        RecordingSuppliers onAsync(Mono<TestExpiringValue> result) {
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
            TestExpiringValue result = syncResults.poll();
            assertNotNull(result, "createSync() was called but no result was configured.");
            return result;
        }

        public Mono<TestExpiringValue> createAsync() {
            asyncCount.incrementAndGet();
            Mono<TestExpiringValue> result = asyncResults.poll();
            assertNotNull(result, "createAsync() was called but no result was configured.");
            return result;
        }
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static TestExpiringValue value(OffsetDateTime expiration) {
        return new TestExpiringValue(expiration);
    }

    private static final class TestExpiringValue {
        private final OffsetDateTime expiration;

        private TestExpiringValue(OffsetDateTime expiration) {
            this.expiration = expiration;
        }

        public OffsetDateTime getExpiration() {
            return expiration;
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
