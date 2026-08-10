// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic, network-free tests for {@link SessionAcquisitionCooldown}'s time-based, per-account
 * behavior.
 * <p>
 * These tests drive the cooldown with an injectable {@link Clock} so its five-minute suppression window
 * can be exercised without sleeping. This complements {@code BlobSessionClientCacheTest} (which covers
 * per-container session caching) and {@code SessionTokenCredentialPolicyTest} (which covers the pipeline
 * policy's use of both collaborators together).
 */
public class SessionAcquisitionCooldownTest {

    private static final String ACCOUNT_NAME = "myaccount";

    @Test
    public void accountCooldownExpiresAfterFiveMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        SessionAcquisitionCooldown cooldown = new SessionAcquisitionCooldown(clock);

        assertFalse(cooldown.isAccountInCooldown(ACCOUNT_NAME));
        assertTrue(cooldown.beginAccountCooldown(ACCOUNT_NAME));
        assertTrue(cooldown.isAccountInCooldown(ACCOUNT_NAME.toUpperCase(Locale.ROOT)));
        assertFalse(cooldown.beginAccountCooldown(ACCOUNT_NAME));

        clock.advance(Duration.ofMinutes(5));

        assertFalse(cooldown.isAccountInCooldown(ACCOUNT_NAME));
        assertTrue(cooldown.beginAccountCooldown(ACCOUNT_NAME));
    }

    /**
     * A {@link Clock} whose instant can be advanced, allowing deterministic control of the cooldown's
     * notion of "now" without sleeping.
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
