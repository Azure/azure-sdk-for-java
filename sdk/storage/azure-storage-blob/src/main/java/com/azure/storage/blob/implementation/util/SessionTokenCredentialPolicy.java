// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Policy that acquires container-scoped session credentials and signs requests using the Session auth scheme.
 */
final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionTokenCredentialPolicy.class);
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final Duration SAFETY_BUFFER = Duration.ofSeconds(5);
    private static final double JITTER_WINDOW_START_RATIO = 0.8d;
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final String SESSION_SCHEME = "Session";
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_EXPIRED = "session_expired";
    private static final String SESSION_TOKEN_INVALID = "session_token_invalid";

    private final BlobSessionClient sessionClient;
    private final Object creationLock = new Object();
    private volatile StorageSessionCredential credential;
    private volatile OffsetDateTime nextRefreshTime;
    private volatile boolean refreshing;
    private volatile Mono<StorageSessionCredential> inflightCreation;

    SessionTokenCredentialPolicy(BlobSessionClient sessionClient) {
        this.sessionClient = Objects.requireNonNull(sessionClient, "'sessionClient' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpPipelineNextPolicy retryNext = next.clone();
        return getValidSessionAsync().flatMap(session -> {
            signRequest(context, session);
            return next.process().flatMap(response -> {
                handleSessionExpiringHeader(response);

                if (shouldReturnResponse(context, response)) {
                    return Mono.just(response);
                }

                response.close();
                invalidateSession(session);
                context.setData(RETRY_CONTEXT_KEY, true);
                return getValidSessionAsync().flatMap(refreshed -> {
                    signRequest(context, refreshed);
                    return retryNext.process();
                });
            });
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpPipelineNextSyncPolicy retryNext = next.clone();
        StorageSessionCredential session = getValidSessionSync();
        signRequest(context, session);

        HttpResponse response = next.processSync();
        handleSessionExpiringHeader(response);
        if (shouldReturnResponse(context, response)) {
            return response;
        }

        response.close();
        invalidateSession(session);
        context.setData(RETRY_CONTEXT_KEY, true);

        StorageSessionCredential refreshed = getValidSessionSync();
        signRequest(context, refreshed);
        return retryNext.processSync();
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

    private void signRequest(HttpPipelineCallContext context, StorageSessionCredential cred) {
        context.getHttpRequest()
            .setHeader(HttpHeaderName.AUTHORIZATION, cred.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
                context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders()));
    }

    private void refreshSessionInBackground() {
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

    private void handleSessionExpiringHeader(HttpResponse response) {
        String authInfo = response.getHeaderValue(X_MS_AUTH_INFO);
        if (authInfo != null && authInfo.contains(SESSION_EXPIRING)) {
            refreshSessionInBackground();
        }
    }

    private static boolean isSessionAuthFailure(HttpResponse response) {
        if (response.getStatusCode() != 401) {
            return false;
        }
        String wwwAuth = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        return wwwAuth != null
            && wwwAuth.startsWith(SESSION_SCHEME)
            && (wwwAuth.contains(SESSION_EXPIRED) || wwwAuth.contains(SESSION_TOKEN_INVALID));
    }

    private boolean shouldReturnResponse(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return true;
        }

        return !isSessionAuthFailure(response);
    }
}
