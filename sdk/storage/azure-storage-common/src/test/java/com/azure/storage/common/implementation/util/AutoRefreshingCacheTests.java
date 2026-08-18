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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void expiredByTimeOnSecondRequestCreatesNewValue() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock);

        OffsetDateTime expiration = now(clock).plus(VALUE_LIFETIME);
        when(provider.createSync()).thenReturn(value(FIRST_VALUE, expiration))
            .thenReturn(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(1)).createSync();
        verify(provider, never()).createAsync();

        clock.advance(VALUE_LIFETIME.plusSeconds(1));

        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(2)).createSync();
        verify(provider, never()).createAsync();
    }

    @Test
    public void automaticBackgroundRefreshFiresWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock);

        when(provider.createSync()).thenReturn(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)));
        when(provider.createAsync())
            .thenReturn(Mono.just(value(SECOND_VALUE, now(clock).plus(VALUE_LIFETIME.multipliedBy(2)))));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(1)).createSync();
        verify(provider, never()).createAsync();

        clock.advance(VALUE_LIFETIME.minusSeconds(2));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(1)).createAsync();
        assertEquals(SECOND_VALUE, cache.getValidValueSync().getValue());
        verify(provider, times(1)).createSync();
        verify(provider, times(1)).createAsync();
    }

    @Test
    public void noRefreshBeforeJitterWindowWithoutHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        AutoRefreshingCache.ValueProvider<TestExpiringValue> provider = mock(AutoRefreshingCache.ValueProvider.class);
        AutoRefreshingCache<TestExpiringValue> cache = new AutoRefreshingCache<>(provider, clock);

        when(provider.createSync()).thenReturn(value(FIRST_VALUE, now(clock).plus(VALUE_LIFETIME)));

        assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        clock.advance(Duration.ofSeconds(30));

        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_VALUE, cache.getValidValueSync().getValue());
        }

        verify(provider, times(1)).createSync();
        verify(provider, never()).createAsync();
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static TestExpiringValue value(String value, OffsetDateTime expiration) {
        return new TestExpiringValue(value, expiration);
    }

    private static final class TestExpiringValue implements AutoRefreshingCache.ExpiringValue {
        private final String value;
        private final OffsetDateTime expiration;

        private TestExpiringValue(String value, OffsetDateTime expiration) {
            this.value = value;
            this.expiration = expiration;
        }

        @Override
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
