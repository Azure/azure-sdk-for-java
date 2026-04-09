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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Policy that acquires container-scoped session credentials and signs requests using the Session auth scheme.
 */
final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionTokenCredentialPolicy.class);
    private static final Duration DEFAULT_REFRESH_OFFSET = Duration.ofMinutes(5);
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";

    private final BlobSessionClient sessionClient;
    private final Duration refreshOffset;
    private final AtomicReference<StorageSessionCredential> cached = new AtomicReference<>();
    private final Object creationLock = new Object();
    private volatile Mono<StorageSessionCredential> inflightCreation;

    SessionTokenCredentialPolicy(BlobSessionClient sessionClient) {
        this(sessionClient, DEFAULT_REFRESH_OFFSET);
    }

    SessionTokenCredentialPolicy(BlobSessionClient sessionClient, Duration refreshOffset) {
        this.sessionClient = Objects.requireNonNull(sessionClient, "'sessionClient' cannot be null.");
        this.refreshOffset = Objects.requireNonNull(refreshOffset, "'refreshOffset' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpPipelineNextPolicy retryNext = next.clone();
        return getValidSessionAsync().flatMap(session -> {
            signRequest(context, session);
            return next.process().flatMap(response -> {
                if (shouldRefreshSession(context, response)) {
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
        if (shouldRefreshSession(context, response)) {
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
        StorageSessionCredential current = cached.get();
        if (current != null && !current.isExpired()) {
            if (shouldRefresh(current)) {
                refreshSessionInBackground();
            }
            return Mono.just(current);
        }

        return startSessionCreationAsync();
    }

    StorageSessionCredential getValidSessionSync() {
        StorageSessionCredential current = cached.get();
        if (current != null && !current.isExpired()) {
            if (shouldRefresh(current)) {
                refreshSessionInBackground();
            }
            return current;
        }

        synchronized (creationLock) {
            current = cached.get();
            if (current != null && !current.isExpired()) {
                if (shouldRefresh(current)) {
                    refreshSessionInBackground();
                }
                return current;
            }

            Mono<StorageSessionCredential> inFlight = inflightCreation;
            if (inFlight != null) {
                StorageSessionCredential refreshed = inFlight.block();
                if (refreshed != null) {
                    return refreshed;
                }
            }

            StorageSessionCredential created = sessionClient.createSessionSync();
            cached.set(created);
            return created;
        }
    }

    void invalidateSession(StorageSessionCredential credential) {
        synchronized (creationLock) {
            cached.compareAndSet(credential, null);
            inflightCreation = null;
        }
    }

    private void signRequest(HttpPipelineCallContext context, StorageSessionCredential credential) {
        context.getHttpRequest()
            .setHeader(HttpHeaderName.AUTHORIZATION,
                credential.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
                    context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders()));
    }

    private boolean shouldRefresh(StorageSessionCredential credential) {
        return !refreshOffset.isNegative()
            && !refreshOffset.isZero()
            && !OffsetDateTime.now().plus(refreshOffset).isBefore(credential.getExpiration());
    }

    private void refreshSessionInBackground() {
        startSessionCreationAsync().subscribe(ignored -> {
        }, error -> LOGGER.warning("Background session refresh failed.", error));
    }

    private Mono<StorageSessionCredential> startSessionCreationAsync() {
        synchronized (creationLock) {
            StorageSessionCredential current = cached.get();
            if (current != null && !current.isExpired() && !shouldRefresh(current)) {
                return Mono.just(current);
            }

            if (inflightCreation != null) {
                return inflightCreation;
            }

            inflightCreation = sessionClient.createSessionAsync().doOnNext(cached::set).doFinally(ignored -> {
                synchronized (creationLock) {
                    inflightCreation = null;
                }
            }).cache();

            return inflightCreation;
        }
    }

    private boolean shouldRefreshSession(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return true;
        }

        int statusCode = response.getStatusCode();
        return statusCode != 401 && statusCode != 403;
    }
}
