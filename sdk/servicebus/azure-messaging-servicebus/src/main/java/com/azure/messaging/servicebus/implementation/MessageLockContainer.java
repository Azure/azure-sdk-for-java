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

    public Instant getExpiryTime(UUID lockToken) {
           return null;
    }
}
