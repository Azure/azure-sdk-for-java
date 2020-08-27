// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Container to store items that are periodically cleaned.
 */
public class LockContainer<T> implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(LockContainer.class);
    private final ConcurrentHashMap<String, Instant> lockTokenExpirationMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, T> lockTokenItemMap = new ConcurrentHashMap<>();
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Disposable cleanupOperation;
    private final Consumer<T> onExpired;

    public LockContainer(Duration cleanupInterval) {
        this(cleanupInterval, t -> {
        });
    }

    public LockContainer(Duration cleanupInterval, Consumer<T> onExpired) {
        Objects.requireNonNull(cleanupInterval, "'cleanupInterval' cannot be null.");

        this.onExpired = Objects.requireNonNull(onExpired, "'onExpired' cannot be null.");
        this.cleanupOperation = Flux.interval(cleanupInterval).subscribe(e -> {
            if (lockTokenExpirationMap.isEmpty()) {
                return;
            }

            final Instant now = Instant.now();
            final List<String> expired = lockTokenExpirationMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().isBefore(now))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            expired.forEach(this::remove);
        });
    }

    /**
     * Adds or updates the expiration time on a lock token. If the expiration time in the container is larger than
     * {@code lockTokenExpiration}, then the current container value is used.
     *
     * @param lockToken Token to update associated lock expiration.
     * @param item Item to hold in the container.
     * @param lockTokenExpiration Time at which the lock token expires.
     *
     * @return The updated value in the container. If the expiration time in the container is larger than {@code
     *     lockTokenExpiration}, then the current container value is used.
     */
    public Instant addOrUpdate(String lockToken, Instant lockTokenExpiration, T item) {
        if (isDisposed.get()) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot perform operations on a disposed set."));
        }

        Objects.requireNonNull(lockToken, "'lockToken' cannot be null.");
        Objects.requireNonNull(item, "'item' cannot be null.");
        Objects.requireNonNull(lockTokenExpiration, "'lockTokenExpiration' cannot be null.");


        final Instant computed = lockTokenExpirationMap.compute(lockToken, (key, existing) -> {
            if (existing == null) {
                return lockTokenExpiration;
            } else {
                return existing.isBefore(lockTokenExpiration)
                    ? lockTokenExpiration
                    : existing;
            }
        });

        lockTokenItemMap.put(lockToken, item);

        return computed;
    }

    /**
     * Gets whether or not the lock token is held in the container and has not expired.
     *
     * @param lockToken Lock token to check.
     *
     * @return {@code true} if the lock token is in the container and has not expired; {@code false} otherwise.
     */
    public boolean containsUnexpired(String lockToken) {
        if (isDisposed.get()) {
            throw logger.logExceptionAsError(new IllegalStateException("Cannot perform operations on a disposed set."));
        }

        final Instant value = lockTokenExpirationMap.getOrDefault(lockToken, Instant.MIN);
        return value.isAfter(Instant.now());
    }

    /**
     * Removes the lock token from the map.
     *
     * @param lockToken Token to remove.
     */
    public void remove(String lockToken) {
        lockTokenExpirationMap.remove(lockToken);
        final T remove = lockTokenItemMap.remove(lockToken);

        if (remove != null) {
            onExpired.accept(remove);
        }
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        cleanupOperation.dispose();

        for (String key : lockTokenExpirationMap.keySet().toArray(new String[0])) {
            remove(key);
        }
    }
}
