// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.CoreUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks a per-account acquisition cooldown: after a CreateSession acquisition failure with HTTP
 * 400, 403, or 5xx, the affected account is placed in a five-minute cooldown during which all
 * further session acquisition attempts are suppressed and requests fall back to bearer authentication.
 * <p>
 * The cooldown is account-scoped (case-insensitive), not container-scoped, because the failure
 * indicates an account-level authorization or server issue rather than a container-specific one.
 * <p>
 * Thread-safe: all mutations are performed via compare-and-swap operations on a
 * {@link ConcurrentHashMap}.
 */
final class SessionAcquisitionCooldown {

    private static final Duration COOLDOWN_DURATION = Duration.ofMinutes(5);

    private final Clock clock;
    private final ConcurrentHashMap<String, OffsetDateTime> accountCooldowns = new ConcurrentHashMap<>();

    SessionAcquisitionCooldown() {
        this(Clock.systemUTC());
    }

    SessionAcquisitionCooldown(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    /**
     * Returns {@code true} if the given account is currently within its acquisition cooldown period.
     * The check is case-insensitive; an expired cooldown entry is removed opportunistically.
     *
     * @param accountName the storage account name to check.
     * @return {@code true} if the account is in cooldown; {@code false} otherwise.
     */
    boolean isAccountInCooldown(String accountName) {
        String key = normalize(accountName);
        OffsetDateTime cooldownUntil = accountCooldowns.get(key);
        if (cooldownUntil == null) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (now.isBefore(cooldownUntil)) {
            return true;
        }

        accountCooldowns.remove(key, cooldownUntil);
        return false;
    }

    /**
     * Attempts to start (or extend) the acquisition cooldown for the given account.
     * If the account is already in cooldown, this is a no-op and returns {@code false}.
     * The check and the write are performed atomically via compare-and-swap.
     *
     * @param accountName the storage account name to place in cooldown.
     * @return {@code true} if the cooldown was newly started (first caller wins);
     *         {@code false} if it was already active.
     */
    boolean beginAccountCooldown(String accountName) {
        String key = normalize(accountName);
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime cooldownUntil = now.plus(COOLDOWN_DURATION);

        while (true) {
            OffsetDateTime existing = accountCooldowns.get(key);
            if (existing != null && now.isBefore(existing)) {
                return false;
            }

            boolean updated = existing == null
                ? accountCooldowns.putIfAbsent(key, cooldownUntil) == null
                : accountCooldowns.replace(key, existing, cooldownUntil);
            if (updated) {
                return true;
            }
        }
    }

    private static String normalize(String name) {
        return CoreUtils.isNullOrEmpty(name) ? "" : name.trim().toLowerCase(Locale.ROOT);
    }
}
