// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cache for container-scoped storage session credentials.
 */
final class StorageSessionCredentialCache {
    private static final ClientLogger LOGGER = new ClientLogger(StorageSessionCredentialCache.class);
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;

    private final BlobSessionClient sessionClient;
    private final Object creationLock = new Object();
    private volatile StorageSessionCredential credential;
    private volatile OffsetDateTime nextRefreshTime;
    private volatile boolean refreshing;
    private volatile Mono<StorageSessionCredential> inflightCreation;

    StorageSessionCredentialCache(BlobSessionClient sessionClient) {
        this.sessionClient = Objects.requireNonNull(sessionClient, "'sessionClient' cannot be null.");
    }

    Mono<StorageSessionCredential> getValidSessionAsync() {
        OffsetDateTime now = OffsetDateTime.now();
        StorageSessionCredential current = credential;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshSessionInBackground();
            }
            return Mono.just(current);
        }

        return startSessionCreationAsync();
    }

    StorageSessionCredential getValidSessionSync() {
        OffsetDateTime now = OffsetDateTime.now();
        StorageSessionCredential current = credential;
        if (isUsable(current, now)) {
            if (isRefreshDue(now)) {
                refreshSessionInBackground();
            }
            return current;
        }

        // Join in-flight async creation outside the lock to avoid deadlock with doOnNext.
        Mono<StorageSessionCredential> inFlight = inflightCreation;
        if (inFlight != null) {
            StorageSessionCredential refreshed = inFlight.block();
            if (refreshed != null) {
                return refreshed;
            }
        }

        synchronized (creationLock) {
            current = credential;
            now = OffsetDateTime.now();
            if (isUsable(current, now)) {
                if (isRefreshDue(now)) {
                    refreshSessionInBackground();
                }
                return current;
            }

            StorageSessionCredential created = sessionClient.createSessionSync();
            setActiveCredential(created);
            return created;
        }
    }

    void invalidateSession(StorageSessionCredential target) {
        synchronized (creationLock) {
            if (credential == target) {
                credential = null;
                nextRefreshTime = null;
                refreshing = false;
            }
            inflightCreation = null;
        }
    }

    void refreshSessionInBackground() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now();
            if (!isUsable(credential, now) || !isRefreshDue(now) || refreshing) {
                return;
            }
            refreshing = true;
        }

        startSessionCreationAsync().subscribe(ignored -> {
        }, error -> LOGGER.warning("Background session refresh failed.", error));
    }

    private Mono<StorageSessionCredential> startSessionCreationAsync() {
        synchronized (creationLock) {
            OffsetDateTime now = OffsetDateTime.now();
            StorageSessionCredential current = credential;
            if (isUsable(current, now) && !isRefreshDue(now)) {
                return Mono.just(current);
            }

            if (inflightCreation != null) {
                return inflightCreation;
            }

            refreshing = true;

            inflightCreation = sessionClient.createSessionAsync().doOnNext(cred -> {
                synchronized (creationLock) {
                    setActiveCredential(cred);
                }
            }).doFinally(ignored -> {
                synchronized (creationLock) {
                    inflightCreation = null;
                    refreshing = false;
                }
            }).cache();

            return inflightCreation;
        }
    }

    private void setActiveCredential(StorageSessionCredential newCredential) {
        credential = newCredential;
        nextRefreshTime = computeRefreshTime(OffsetDateTime.now(), newCredential.getExpiration());
        refreshing = false;
    }

    private static boolean isUsable(StorageSessionCredential cred, OffsetDateTime now) {
        return cred != null && !now.isAfter(cred.getExpiration());
    }

    private boolean isRefreshDue(OffsetDateTime now) {
        OffsetDateTime refresh = nextRefreshTime;
        return refresh != null && !now.isBefore(refresh);
    }

    private static OffsetDateTime computeRefreshTime(OffsetDateTime now, OffsetDateTime expiration) {
        long availableMillis = Duration.between(now, expiration.minus(SAFETY_BUFFER)).toMillis();
        if (availableMillis <= 0) {
            return now;
        }

        double refreshPoint
            = JITTER_WINDOW_START_RATIO + (1.0 - JITTER_WINDOW_START_RATIO) * ThreadLocalRandom.current().nextDouble();
        return now.plus(Duration.ofMillis((long) (availableMillis * refreshPoint)));
    }
}
