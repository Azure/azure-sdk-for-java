// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.util;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deterministic, network-free tests for {@link AutoRefreshingCache} time-based behavior.
 */
@SuppressWarnings("unchecked")
public class AutoRefreshingCacheTests {
    private static final String FIRST_VALUE = "first-value";
    private static final String SECOND_VALUE = "second-value";
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);
    private static final Duration MIN_REFRESH_BEFORE_EXPIRATION = Duration.ofSeconds(30);
    private static final Duration MAX_REFRESH_BEFORE_EXPIRATION = Duration.ofSeconds(90);
    private static final Duration REFRESH_FAILURE_BACKOFF = Duration.ofSeconds(30);

    @Test
    public void refreshPointIsJitteredBeforeExpiration() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        OffsetDateTime expiration = now(clock).plus(VALUE_LIFETIME);
        Set<OffsetDateTime> refreshTimes = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            AutoRefreshingCache.ValueProvider<TestExpiringValue> provider
                = mock(AutoRefreshingCache.ValueProvider.class);
            AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock);
            when(provider.createSync()).thenReturn(value(FIRST_VALUE, expiration));

            assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
            OffsetDateTime refreshTime = cache.getNextRefreshTime();

            assertNotNull(refreshTime);
            assertTrue(refreshTime.isBefore(expiration));
            assertFalse(refreshTime.isBefore(expiration.minus(MAX_REFRESH_BEFORE_EXPIRATION)));
            assertFalse(refreshTime.isAfter(expiration.minus(MIN_REFRESH_BEFORE_EXPIRATION)));
            refreshTimes.add(refreshTime);
            cache.close();
        }

        assertTrue(refreshTimes.size() > 1);
    }

    @Test
    public void scheduledRefreshFiresWithoutAccess() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock, scheduler);

        when(provider.createSync())
            .thenReturn(value(FIRST_VALUE, now(clock).plusSeconds(30), now(clock).plusSeconds(5)));
        when(provider.createAsync())
            .thenReturn(Mono.just(value(SECOND_VALUE, now(clock).plusSeconds(60), now(clock).plusSeconds(20))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, never()).createAsync();

        clock.advance(Duration.ofSeconds(5));
        scheduler.advanceTimeBy(Duration.ofSeconds(5));

        verify(provider, times(1)).createAsync();
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        cache.close();
    }

    @Test
    public void failedRefreshBacksOffBeforeRetrying() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock, scheduler);

        when(provider.createSync())
            .thenReturn(value(FIRST_VALUE, now(clock).plusMinutes(1), now(clock).plusSeconds(1)));
        when(provider.createAsync()).thenReturn(Mono.error(new IllegalStateException("refresh failed")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        clock.advance(Duration.ofSeconds(1));
        scheduler.advanceTimeBy(Duration.ofSeconds(1));

        verify(provider, times(1)).createAsync();
        assertEquals(now(clock).plus(REFRESH_FAILURE_BACKOFF), cache.getNextRefreshTime());

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(REFRESH_FAILURE_BACKOFF.minusSeconds(1));
        assertEquals(FIRST_VALUE, cache.getValidValueAsync().block().getValue());
        verify(provider, times(1)).createAsync();

        clock.advance(Duration.ofSeconds(1));
        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(2)).createAsync();
        cache.close();
    }

    @Test
    public void failedRefreshWhileCurrentValueIsValidServesCurrentValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock, scheduler);

        when(provider.createSync())
            .thenReturn(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME), now(clock).plusSeconds(1)));
        when(provider.createAsync()).thenReturn(Mono.error(new IllegalStateException("refresh failed")));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        clock.advance(Duration.ofSeconds(1));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        assertEquals(FIRST_VALUE, cache.getValidValueAsync().block().getValue());
        verify(provider, times(1)).createAsync();
        cache.close();
    }

    @Test
    public void closeCancelsPendingRefreshAndStopsScheduling() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock, scheduler);

        when(provider.createSync())
            .thenReturn(value(FIRST_VALUE, now(clock).plusSeconds(30), now(clock).plusSeconds(5)));
        when(provider.createAsync())
            .thenReturn(Mono.just(value(SECOND_VALUE, now(clock).plusSeconds(60), now(clock).plusSeconds(20))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        cache.close();
        cache.close();

        clock.advance(Duration.ofSeconds(5));
        scheduler.advanceTimeBy(Duration.ofSeconds(5));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, never()).createAsync();
    }

    @Test
    public void expiredValueServesCurrentValueAndRefreshesInBackground() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.create();
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock, scheduler);

        OffsetDateTime expiration = now(clock).plusSeconds(5);
        when(provider.createSync()).thenReturn(value(FIRST_VALUE, expiration, expiration.minusSeconds(1)));
        when(provider.createAsync())
            .thenReturn(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME), now(clock).plusMinutes(1))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        clock.advance(Duration.ofSeconds(6));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(1)).createAsync();
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        cache.close();
    }

    @Test
    public void singleFlightCreationAndSyncAsyncReadUsableValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock);

        when(provider.createAsync()).thenReturn(Mono.just(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME))));

        Mono<TestExpiringValue> first = cache.getValidValueAsync();
        Mono<TestExpiringValue> second = cache.getValidValueAsync();

        assertEquals(FIRST_VALUE, first.block().getValue());
        assertEquals(FIRST_VALUE, second.block().getValue());
        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());

        verify(provider, times(1)).createAsync();
        verify(provider, never()).createSync();
        cache.close();
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static TestExpiringValue value(String value, OffsetDateTime expiration) {
        return new TestExpiringValue(value, expiration, null);
    }

    private static TestExpiringValue value(String value, OffsetDateTime expiration, OffsetDateTime refreshOn) {
        return new TestExpiringValue(value, expiration, refreshOn);
    }

    private static final class TestExpiringValue implements AutoRefreshingCache.ExpiringValue {
        private final String value;
        private final OffsetDateTime expiration;
        private final OffsetDateTime refreshOn;

        private TestExpiringValue(String value, OffsetDateTime expiration, OffsetDateTime refreshOn) {
            this.value = value;
            this.expiration = expiration;
            this.refreshOn = refreshOn;
        }

        @Override
        public OffsetDateTime getExpiration() {
            return expiration;
        }

        @Override
        public OffsetDateTime getRefreshOn() {
            return refreshOn;
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
