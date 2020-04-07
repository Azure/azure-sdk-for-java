// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container of Message lock and related metadata related.
 */
public class MessageLockContainer {
    private final ConcurrentHashMap<String, Instant> lockTokenExpirationMap = new ConcurrentHashMap<>();

    /**
     * Adds or updates the expiration time on a lock token. If the expiration time in the container is larger than
     * {@code lockTokenExpiration}, then the current container value is used.
     *
     * @param lockToken Token to update associated lock expiration.
     * @param lockTokenExpiration Time at which the lock token expires.
     *
     * @return The updated value in the container. If the expiration time in the container is larger than
     *     {@code lockTokenExpiration}, then the current container value is used.
     */
    public Instant addOrUpdate(String lockToken, Instant lockTokenExpiration) {
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

    /**
     * Gets the lock expiration given a lock token.
     *
     * @param lockToken Token to get associated lock expiration for.
     *
     * @return An {@link Instant} for when the lock expires or {@code null} if the {@code lockToken} does not exist.
     */
    public Instant getLockTokenExpiration(String lockToken) {
        return lockTokenExpirationMap.get(lockToken);
    }

    /**
     * Removes the lock token from the map.
     *
     * @param lockToken Token to remove.
     */
    public void remove(String lockToken) {
        lockTokenExpirationMap.remove(lockToken);
    }
}
