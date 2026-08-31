// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic, network-free tests for {@link AutoRefreshingCache} time-based behavior.
 */
public class AutoRefreshingCacheTests {
    private static final String FIRST_VALUE = "first-value";
    private static final String SECOND_VALUE = "second-value";
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);

    @Test
    public void expiredByTimeOnSecondRequestCreatesNewValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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

    @Test
    public void expiredValueIsStillCreatedDuringBackoff() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
        RecordingValueProvider provider = new RecordingValueProvider();
        AutoRefreshingCache<TestExpiringValue> cache
            = new AutoRefreshingCache<>(provider, TestExpiringValue::getExpiration, clock);

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
    private static final class RecordingValueProvider implements AutoRefreshingCache.ValueProvider<TestExpiringValue> {
        private final Queue<Callable<TestExpiringValue>> syncResults = new ArrayDeque<>();
        private final Queue<Mono<TestExpiringValue>> asyncResults = new ArrayDeque<>();
        private final AtomicInteger syncCount = new AtomicInteger();
        private final AtomicInteger asyncCount = new AtomicInteger();
        private Callable<TestExpiringValue> lastSyncResult;
        private Mono<TestExpiringValue> lastAsyncResult;

        RecordingValueProvider onSync(TestExpiringValue result) {
            return onSync(() -> result);
        }

        synchronized RecordingValueProvider onSync(Callable<TestExpiringValue> result) {
            syncResults.add(result);
            return this;
        }

        synchronized RecordingValueProvider onAsync(Mono<TestExpiringValue> result) {
            asyncResults.add(result);
            return this;
        }

        int createSyncCount() {
            return syncCount.get();
        }

        int createAsyncCount() {
            return asyncCount.get();
        }

        @Override
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

        @Override
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
