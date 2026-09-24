// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core lifecycle and shared-acquisition tests for {@link AutoRefreshingCache}.
 */
public class AutoRefreshingCacheTests {
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);

    private MutableClock clock;
    private AtomicInteger syncCalls;
    private AtomicInteger asyncCalls;
    private TestExpiringValue first;
    private TestExpiringValue second;

    @BeforeEach
    public void setup() {
        clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        syncCalls = new AtomicInteger();
        asyncCalls = new AtomicInteger();
        OffsetDateTime now = OffsetDateTime.now(clock);
        first = new TestExpiringValue(now.plus(VALUE_LIFETIME));
        second = new TestExpiringValue(now.plus(VALUE_LIFETIME.multipliedBy(2)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void usableValueIsReturnedWhileOneBackgroundRefreshIsPending(boolean async) {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return pending.asMono();
        }, () -> {
            syncCalls.incrementAndGet();
            return first;
        }, TestExpiringValue::getExpiration, clock);
        assertSame(first, cache.getValidValueSync());
        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        for (int i = 0; i < 3; i++) {
            assertSame(first,
                async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
            assertEquals(1, asyncCalls.get());
        }
        assertEquals(1, syncCalls.get());
        assertEquals(1, asyncCalls.get());

        pending.tryEmitValue(second);
        clock.advance(Duration.ofSeconds(3));
        assertSame(second, cache.getValidValueSync());
        assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(1, asyncCalls.get());
        assertEquals(1, syncCalls.get());
    }

    @Test
    public void synchronousLoaderFailureReleasesAsyncJoiners() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        IllegalStateException failure = new IllegalStateException("sync load failed");
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> Mono.just(second), () -> {
            entered.countDown();
            await(release);
            throw failure;
        }, TestExpiringValue::getExpiration, clock);
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
            assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    public void cancelledSubscriberDoesNotCancelSharedAcquisition() throws Exception {
        Sinks.One<TestExpiringValue> pending = Sinks.one();
        AtomicInteger cancellations = new AtomicInteger();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return pending.asMono().doOnCancel(cancellations::incrementAndGet);
        }, () -> pending.asMono().block(), TestExpiringValue::getExpiration, clock);
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
        owner.cancel(true);
        assertEquals(1, asyncCalls.get());
        assertEquals(0, cancellations.get());
        pending.tryEmitValue(first);
        assertSame(first, joiner.get(5, TimeUnit.SECONDS));
        assertSame(first, cache.getValidValueSync());
    }

    @Test
    public void syncAndAsyncCallersShareSynchronousAcquisition() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return Mono.just(first);
        }, () -> {
            syncCalls.incrementAndGet();
            entered.countDown();
            await(release);
            return first;
        }, TestExpiringValue::getExpiration, clock);
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
            assertSame(first, owner.get(5, TimeUnit.SECONDS));
            assertSame(first, joiner.get(5, TimeUnit.SECONDS));
            assertSame(first, syncJoiner.get(5, TimeUnit.SECONDS));
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
        AtomicReference<Thread> waitingThread = new AtomicReference<>();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            asyncCalls.incrementAndGet();
            return pending.asMono();
        }, () -> {
            throw new IllegalStateException("The sync loader must not run.");
        }, TestExpiringValue::getExpiration, clock);
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<TestExpiringValue> joiner = pool.submit(() -> {
                waitingThread.set(Thread.currentThread());
                return cache.getValidValueSync();
            });
            awaitWaiting(waitingThread);
            pending.tryEmitValue(first);
            assertSame(first, owner.get(5, TimeUnit.SECONDS));
            assertSame(first, joiner.get(5, TimeUnit.SECONDS));
            assertEquals(1, asyncCalls.get());
        } finally {
            pending.tryEmitEmpty();
            pool.shutdownNow();
        }
    }

    @Test
    public void backgroundTimeoutReleasesSyncAndAsyncWaitersAndAllowsRetry() {
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AtomicInteger cancellations = new AtomicInteger();
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> asyncCalls.incrementAndGet() == 1
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
            assertEquals(1, asyncCalls.get());

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
            assertEquals(2, asyncCalls.get());
        } finally {
            pool.shutdownNow();
            scheduler.dispose();
        }
    }

    @Test
    public void foregroundAcquisitionDoesNotUseBackgroundTimeout() throws Exception {
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        Sinks.One<TestExpiringValue> pending = Sinks.one();
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
        IllegalStateException failure = new IllegalStateException("load failed");
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(() -> asyncCalls.incrementAndGet() == 1 ? pending.asMono() : Mono.just(second),
                () -> second, TestExpiringValue::getExpiration, clock);
        CompletableFuture<TestExpiringValue> owner = cache.getValidValueAsync().toFuture();
        CompletableFuture<TestExpiringValue> joiner = cache.getValidValueAsync().toFuture();
        assertFalse(joiner.isDone());
        pending.tryEmitError(failure);
        assertSame(failure, assertThrows(ExecutionException.class, () -> owner.get(5, TimeUnit.SECONDS)).getCause());
        assertSame(failure, assertThrows(ExecutionException.class, () -> joiner.get(5, TimeUnit.SECONDS)).getCause());
        assertSame(second, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(2, asyncCalls.get());
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
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(() -> Mono.just(asyncCalls.incrementAndGet() == 1 ? first : second),
                () -> syncCalls.incrementAndGet() == 1 ? first : second, TestExpiringValue::getExpiration, clock);

        assertSame(first, async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
        clock.advance(Duration.ofSeconds(30));
        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block(Duration.ofSeconds(5)));
        assertEquals(async ? 1 : 0, asyncCalls.get());
        assertEquals(async ? 0 : 1, syncCalls.get());
        clock.advance(VALUE_LIFETIME.plusSeconds(1));

        assertSame(second, async ? cache.getValidValueAsync().block(Duration.ofSeconds(5)) : cache.getValidValueSync());
        assertEquals(async ? 2 : 0, asyncCalls.get());
        assertEquals(async ? 0 : 2, syncCalls.get());
    }

    @Test
    public void failedRefreshRetainsValueAndRetriesAfterBackoff() {
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> asyncCalls.incrementAndGet() == 1
            ? Mono.error(new IllegalStateException("refresh failed"))
            : Mono.just(second), () -> {
                syncCalls.incrementAndGet();
                return first;
            }, TestExpiringValue::getExpiration, clock);

        assertSame(first, cache.getValidValueSync());
        cache.forceRefreshValueInBackground();
        for (int i = 0; i < 3; i++) {
            assertSame(first, cache.getValidValueSync());
        }
        assertEquals(1, asyncCalls.get());
        assertSame(first, cache.getValidValueSync());
        assertSame(first, cache.getValidValueAsync().block(Duration.ofSeconds(5)));

        clock.advance(Duration.ofSeconds(31));
        assertSame(first, cache.getValidValueSync());
        assertEquals(2, asyncCalls.get());
        assertSame(second, cache.getValidValueSync());
        assertEquals(1, syncCalls.get());
    }

    @Test
    public void invalidateValueClearsOnlyMatchingValue() {
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(() -> {
            throw new AssertionError("The async loader must not run.");
        }, () -> syncCalls.incrementAndGet() == 1 ? first : second, TestExpiringValue::getExpiration, clock);

        assertSame(first, cache.getValidValueSync());
        assertTrue(cache.invalidateValue(first));
        assertSame(second, cache.getValidValueSync());
        assertFalse(cache.invalidateValue(first));
        assertSame(second, cache.getValidValueSync());
        assertEquals(2, syncCalls.get());
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
