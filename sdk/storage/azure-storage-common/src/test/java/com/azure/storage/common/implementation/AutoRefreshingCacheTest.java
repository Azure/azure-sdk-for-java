// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Deterministic, network-free tests for {@link AutoRefreshingCache} time-based behavior, using an injectable
 * {@link Clock} and mocked creator functions so expiry and proactive-refresh logic can be exercised without
 * sleeping or hitting a real service.
 */
public class AutoRefreshingCacheTest {

    private static final String FIRST_TOKEN = "first-token";
    private static final String SECOND_TOKEN = "second-token";
    private static final Duration VALUE_LIFETIME = Duration.ofMinutes(5);

    @Test
    public void expiredByTimeOnSecondRequestCreatesNewValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        @SuppressWarnings("unchecked")
        Supplier<TestValue> syncCreator = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Supplier<Mono<TestValue>> asyncCreator = mock(Supplier.class);
        AutoRefreshingCache<TestValue> cache
            = new AutoRefreshingCache<>(syncCreator, asyncCreator, TestValue::getExpiration, clock);

        OffsetDateTime expiration = now(clock).plus(VALUE_LIFETIME);
        when(syncCreator.get()).thenReturn(value(FIRST_TOKEN, expiration))
            .thenReturn(value(SECOND_TOKEN, now(clock).plus(VALUE_LIFETIME.multipliedBy(2))));

        TestValue firstRequest = cache.getValidSync();
        assertEquals(FIRST_TOKEN, firstRequest.getToken());
        verify(syncCreator, times(1)).get();
        verify(asyncCreator, never()).get();

        clock.advance(VALUE_LIFETIME.plusSeconds(1));

        TestValue secondRequest = cache.getValidSync();
        assertEquals(SECOND_TOKEN, secondRequest.getToken());
        verify(syncCreator, times(2)).get();
        verify(asyncCreator, never()).get();
    }

    @Test
    public void automaticBackgroundRefreshFiresWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        @SuppressWarnings("unchecked")
        Supplier<TestValue> syncCreator = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Supplier<Mono<TestValue>> asyncCreator = mock(Supplier.class);
        AutoRefreshingCache<TestValue> cache
            = new AutoRefreshingCache<>(syncCreator, asyncCreator, TestValue::getExpiration, clock);

        OffsetDateTime firstExpiration = now(clock).plus(VALUE_LIFETIME);
        when(syncCreator.get()).thenReturn(value(FIRST_TOKEN, firstExpiration));
        when(asyncCreator.get())
            .thenReturn(Mono.just(value(SECOND_TOKEN, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        assertEquals(FIRST_TOKEN, cache.getValidSync().getToken());
        verify(syncCreator, times(1)).get();
        verify(asyncCreator, never()).get();

        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertEquals(FIRST_TOKEN, cache.getValidSync().getToken());
        verify(asyncCreator, times(1)).get();

        assertEquals(SECOND_TOKEN, cache.getValidSync().getToken());
        verify(syncCreator, times(1)).get();
        verify(asyncCreator, times(1)).get();
    }

    @Test
    public void noRefreshBeforeJitterWindowWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        @SuppressWarnings("unchecked")
        Supplier<TestValue> syncCreator = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Supplier<Mono<TestValue>> asyncCreator = mock(Supplier.class);
        AutoRefreshingCache<TestValue> cache
            = new AutoRefreshingCache<>(syncCreator, asyncCreator, TestValue::getExpiration, clock);

        OffsetDateTime expiration = now(clock).plus(VALUE_LIFETIME);
        when(syncCreator.get()).thenReturn(value(FIRST_TOKEN, expiration));

        assertEquals(FIRST_TOKEN, cache.getValidSync().getToken());

        clock.advance(Duration.ofSeconds(30));

        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_TOKEN, cache.getValidSync().getToken());
        }

        verify(syncCreator, times(1)).get();
        verify(asyncCreator, never()).get();
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static TestValue value(String token, OffsetDateTime expiration) {
        return new TestValue(token, expiration);
    }

    private static final class TestValue {
        private final String token;
        private final OffsetDateTime expiration;

        TestValue(String token, OffsetDateTime expiration) {
            this.token = token;
            this.expiration = expiration;
        }

        String getToken() {
            return token;
        }

        OffsetDateTime getExpiration() {
            return expiration;
        }
    }

    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

        MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            this.instant = this.instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
