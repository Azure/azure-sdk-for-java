// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

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
 * Deterministic, network-free tests for {@link StorageSessionCredentialCache} time-based behavior.
 * <p>
 * These tests drive the cache with an injectable {@link Clock} and a mocked {@link BlobSessionClient} so the
 * expiry and proactive-refresh logic can be exercised without sleeping or hitting the service. The end-to-end
 * confidence that real rotation works on the wire is covered separately by the live
 * {@code ContainerApiTests.sessionTokenRotates} / {@code sessionTokenRotatesWithoutInvalidTokenGets} tests.
 */
public class StorageSessionCredentialCacheTest {

    private static final String FIRST_TOKEN = "first-session-token";
    private static final String SECOND_TOKEN = "second-session-token";

    // A session's usable lifetime in these tests (the service issues ~5 minute sessions).
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(5);

    /**
     * A request returns a good (valid) token. The clock then advances past the token's expiration. The next
     * request must detect that the cached token is expired purely due to the passage of time and request a
     * brand-new session rather than reuse or send the expired one.
     */
    @Test
    public void expiredByTimeOnSecondRequestCreatesNewSession() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        BlobSessionClient sessionClient = mock(BlobSessionClient.class);
        StorageSessionCredentialCache cache = new StorageSessionCredentialCache(sessionClient, clock);

        OffsetDateTime expiration = now(clock).plus(SESSION_LIFETIME);
        when(sessionClient.createSessionSync()).thenReturn(credential(FIRST_TOKEN, expiration))
            .thenReturn(credential(SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME.multipliedBy(2))));

        // First request: cold cache mints a good token and uses it.
        StorageSessionCredential firstRequest = cache.getValidSessionSync();
        assertEquals(FIRST_TOKEN, firstRequest.getSessionToken());
        verify(sessionClient, times(1)).createSessionSync();
        verify(sessionClient, never()).createSessionAsync();

        // Time advances past the first token's expiration with no traffic in between.
        clock.advance(SESSION_LIFETIME.plusSeconds(1));

        // Second request: the cached token is expired by time, so a new session is created instead of reused.
        StorageSessionCredential secondRequest = cache.getValidSessionSync();
        assertEquals(SECOND_TOKEN, secondRequest.getSessionToken());
        verify(sessionClient, times(2)).createSessionSync();
        // The expiry path mints inline; it must not have leaned on the background (async) refresh.
        verify(sessionClient, never()).createSessionAsync();
    }

    /**
     * When the service has NOT sent a {@code session_expiring} hint, the cache must still refresh
     * automatically once its own jittered timer elapses (while the current token is still usable), serving
     * the current token until the refreshed one is ready.
     */
    @Test
    public void automaticBackgroundRefreshFiresWithoutServiceHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        BlobSessionClient sessionClient = mock(BlobSessionClient.class);
        StorageSessionCredentialCache cache = new StorageSessionCredentialCache(sessionClient, clock);

        OffsetDateTime firstExpiration = now(clock).plus(SESSION_LIFETIME);
        when(sessionClient.createSessionSync()).thenReturn(credential(FIRST_TOKEN, firstExpiration));
        // Mono.just emits synchronously on subscribe, so the background swap completes inline for the test.
        when(sessionClient.createSessionAsync())
            .thenReturn(Mono.just(credential(SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME.multipliedBy(2)))));

        // First request: cold cache mints the initial token.
        assertEquals(FIRST_TOKEN, cache.getValidSessionSync().getSessionToken());
        verify(sessionClient, times(1)).createSessionSync();
        verify(sessionClient, never()).createSessionAsync();

        // Advance to a point guaranteed to be past the jittered refresh time (80-100% of lifetime minus the
        // 5s safety buffer => at most lifetime-5s) but still before hard expiry, so the token remains usable.
        clock.advance(SESSION_LIFETIME.minusSeconds(2));

        // Second request: token still usable, refresh timer elapsed, no service hint => automatic background
        // refresh. The current token is served while the refresh happens.
        assertEquals(FIRST_TOKEN, cache.getValidSessionSync().getSessionToken());
        verify(sessionClient, times(1)).createSessionAsync();

        // Third request: the background refresh has swapped in the new token, which is now served.
        assertEquals(SECOND_TOKEN, cache.getValidSessionSync().getSessionToken());
        // Still only one inline creation and one background refresh overall (no over-eager churn).
        verify(sessionClient, times(1)).createSessionSync();
        verify(sessionClient, times(1)).createSessionAsync();
    }

    /**
     * Guards against over-eager refreshing: while the token is comfortably before its jittered refresh point
     * and no service hint has arrived, repeated requests must reuse the same cached token and never trigger a
     * refresh.
     */
    @Test
    public void noRefreshBeforeJitterWindowWithoutServiceHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        BlobSessionClient sessionClient = mock(BlobSessionClient.class);
        StorageSessionCredentialCache cache = new StorageSessionCredentialCache(sessionClient, clock);

        OffsetDateTime expiration = now(clock).plus(SESSION_LIFETIME);
        when(sessionClient.createSessionSync()).thenReturn(credential(FIRST_TOKEN, expiration));

        // First request mints the token.
        assertEquals(FIRST_TOKEN, cache.getValidSessionSync().getSessionToken());

        // Advance only slightly — well before the earliest jittered refresh point (80% of lifetime).
        clock.advance(Duration.ofSeconds(30));

        // Several more requests reuse the same token; no refresh is triggered.
        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_TOKEN, cache.getValidSessionSync().getSessionToken());
        }

        verify(sessionClient, times(1)).createSessionSync();
        verify(sessionClient, never()).createSessionAsync();
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    private static StorageSessionCredential credential(String token, OffsetDateTime expiration) {
        return new StorageSessionCredential(token, SessionTestHelper.TEST_SESSION_KEY, expiration,
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }

    /**
     * A {@link Clock} whose instant can be advanced, allowing deterministic control of the cache's notion of
     * "now" without sleeping.
     */
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

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
