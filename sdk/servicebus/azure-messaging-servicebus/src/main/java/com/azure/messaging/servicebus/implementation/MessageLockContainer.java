// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Container for message locks that are cleaned up periodically.
 */
public class MessageLockContainer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(MessageLockContainer.class);
    private final ConcurrentHashMap<String, Instant> lockTokens = new ConcurrentHashMap<>();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Disposable cleanupOperation;

    public MessageLockContainer(Duration cleanupInterval) {
        cleanupOperation = Flux.interval(cleanupInterval).subscribe(e -> {
            if (lockTokens.isEmpty()) {
                return;
            }

            final Instant now = Instant.now();
            lockTokens.entrySet().removeIf(entry -> {
                final Instant expiration = entry.getValue();
                final boolean isExpired = expiration != null && expiration.isBefore(now);
                if (isExpired) {
                    logger.info("lockToken[{}]. expiration[{}]. Removing expired entry. ",
                        entry.getKey(), expiration, e);
                }
                return isExpired;
            });
        });
    }

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
        if (isDisposed.get()) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot perform operations on a disposed set."));
        }

        return lockTokens.compute(lockToken, (key, existing) -> {
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
     * Gets whether or not the lock token is held in the container and has not expired.
     *
     * @param lockToken Lock token to check.
     *
     * @return {@code true} if the lock token is in the container and has not expired; {@code false} otherwise.
     */
    public boolean contains(String lockToken) {
        if (isDisposed.get()) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot perform operations on a disposed set."));
        }

        final Instant value = lockTokens.getOrDefault(lockToken, Instant.MIN);
        return value.isAfter(Instant.now());
    }

    /**
     * Removes the lock token from the map.
     *
     * @param lockToken Token to remove.
     */
    public void remove(String lockToken) {
        lockTokens.remove(lockToken);
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        lockTokens.clear();
        cleanupOperation.dispose();
    }
}
