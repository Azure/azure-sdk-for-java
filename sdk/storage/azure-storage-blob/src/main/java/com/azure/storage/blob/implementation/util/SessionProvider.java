// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages session lifecycle: create, cache, refresh, and concurrency control.
 * Holds an immutable {@link StorageSessionCredential} in an {@link AtomicReference}
 * for lock-free reads and atomic swaps on refresh.
 *
 * <p>Concurrency strategy:</p>
 * <ul>
 *   <li>Async: a single in-flight {@code Mono} is shared via {@code .cache()} so all
 *       concurrent subscribers piggyback on one CreateSession call.</li>
 *   <li>Sync: {@code synchronized} with double-check locking guards the creation path.</li>
 * </ul>
 */
final class SessionProvider {

    private final BlobSessionClient sessionClient;
    private final AtomicReference<StorageSessionCredential> cached = new AtomicReference<>();

    /**
     * Guards access to {@link #inflightCreation}. Only held briefly to read/write the
     * reference — never held while waiting for a network call.
     */
    private final Object creationLock = new Object();
    private volatile Mono<StorageSessionCredential> inflightCreation;

    SessionProvider(BlobSessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    /**
     * Returns the cached session credential, or creates a new session if none is cached
     * or the cached one is expired. Concurrent callers share a single in-flight Mono
     * so only one CreateSession call is made.
     */
    Mono<StorageSessionCredential> getOrCreateSessionAsync() {
        StorageSessionCredential current = getValidCachedSession();
        if (current != null) {
            return Mono.just(current);
        }

        synchronized (creationLock) {
            // Double-check after acquiring lock
            current = getValidCachedSession();
            if (current != null) {
                return Mono.just(current);
            }

            // Return existing in-flight Mono if another caller already started creation
            if (inflightCreation != null) {
                return inflightCreation;
            }

            // Create and cache a shared Mono — all concurrent subscribers get the same result
            inflightCreation = sessionClient.createSessionAsync()
                .doOnNext(cached::set)
                .doFinally(signal -> inflightCreation = null)
                .cache();

            return inflightCreation;
        }
    }

    /**
     * Sync equivalent of {@link #getOrCreateSessionAsync()}.
     * Uses double-check locking so only the first thread makes the network call.
     */
    StorageSessionCredential getOrCreateSessionSync() {
        StorageSessionCredential current = getValidCachedSession();
        if (current != null) {
            return current;
        }

        synchronized (this) {
            // Double-check after acquiring lock
            current = getValidCachedSession();
            if (current != null) {
                return current;
            }

            StorageSessionCredential newCred = sessionClient.createSessionSync();
            cached.set(newCred);
            return newCred;
        }
    }

    private StorageSessionCredential getValidCachedSession() {
        StorageSessionCredential current = cached.get();
        return current != null && !current.isExpired() ? current : null;
    }
}
