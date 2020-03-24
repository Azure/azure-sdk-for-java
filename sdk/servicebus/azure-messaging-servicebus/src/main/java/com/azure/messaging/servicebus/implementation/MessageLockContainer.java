// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container of Message lock and related metadata related.
 */
public class MessageLockContainer {
    private final ConcurrentHashMap<UUID, Instant> lockTokenExpirationMap;

    public MessageLockContainer() {
        lockTokenExpirationMap = new ConcurrentHashMap<>();
    }

    public Instant getLockTokenExpiration(UUID lockToken) {
        return lockTokenExpirationMap.get(lockToken);
    }

    public void remove(UUID lockToken) {
        lockTokenExpirationMap.remove(lockToken);
    }

    public Instant addOrUpdate(UUID lockToken, Instant lockTokenExpiration) {
        return lockTokenExpirationMap.compute(lockToken, (key, existing) -> {
            if (existing == null) {
                return lockTokenExpiration;
            } else {
                return existing.isBefore(lockTokenExpiration)
                    ? lockTokenExpiration
                    : existing;
            }
        });
    }
}
