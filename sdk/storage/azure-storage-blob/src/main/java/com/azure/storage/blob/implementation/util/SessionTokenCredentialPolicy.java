// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Policy that acquires container-scoped session credentials and signs requests using the Session auth scheme.
 */
final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final String SESSION_SCHEME = "Session";
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_EXPIRED = "session_expired";
    private static final String SESSION_TOKEN_INVALID = "session_token_invalid";

    private final StorageSessionCredentialCache sessionCredentialCache;

    SessionTokenCredentialPolicy(BlobSessionClient sessionClient) {
        this(new StorageSessionCredentialCache(
            Objects.requireNonNull(sessionClient, "'sessionClient' cannot be null.")));
    }

    SessionTokenCredentialPolicy(StorageSessionCredentialCache sessionCredentialCache) {
        this.sessionCredentialCache
            = Objects.requireNonNull(sessionCredentialCache, "'sessionCredentialCache' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpPipelineNextPolicy retryNext = next.clone();
        return getValidSessionAsync().flatMap(session -> {
            signRequest(context, session);
            return next.process().flatMap(response -> {
                handleSessionExpiringHeader(response);

                if (isSessionAuthResponse(response)) {
                    invalidateSession(session);
                }

                if (shouldRetryRequest(context, response)) {
                    response.close();
                    context.setData(RETRY_CONTEXT_KEY, true);
                    return getValidSessionAsync().flatMap(refreshed -> {
                        signRequest(context, refreshed);
                        return retryNext.process();
                    });
                }

                return Mono.just(response);
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

        if (isSessionAuthResponse(response)) {
            invalidateSession(session);
        }

        if (shouldRetryRequest(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);

            StorageSessionCredential refreshed = getValidSessionSync();
            signRequest(context, refreshed);
            return retryNext.processSync();
        }

        return response;
    }

    Mono<StorageSessionCredential> getValidSessionAsync() {
        return sessionCredentialCache.getValidSessionAsync();
    }

    StorageSessionCredential getValidSessionSync() {
        return sessionCredentialCache.getValidSessionSync();
    }

    void invalidateSession(StorageSessionCredential target) {
        sessionCredentialCache.invalidateSession(target);
    }

    private void signRequest(HttpPipelineCallContext context, StorageSessionCredential cred) {
        context.getHttpRequest()
            .setHeader(HttpHeaderName.AUTHORIZATION, cred.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
                context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders()));
    }

    private void handleSessionExpiringHeader(HttpResponse response) {
        String authInfo = response.getHeaderValue(X_MS_AUTH_INFO);
        if (authInfo != null && authInfo.contains(SESSION_EXPIRING)) {
            sessionCredentialCache.refreshSessionInBackground();
        }
    }

    /**
     * Returns true for any 401 that the session service issued (expired or invalid token).
     * Used to decide whether to invalidate the cached session.
     */
    private static boolean isSessionAuthResponse(HttpResponse response) {
        if (response.getStatusCode() != 401) {
            return false;
        }
        String wwwAuth = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        return wwwAuth != null
            && wwwAuth.startsWith(SESSION_SCHEME)
            && (wwwAuth.contains(SESSION_EXPIRED) || wwwAuth.contains(SESSION_TOKEN_INVALID));
    }

    /**
     * Returns true only for 401 session_expired — the only error that warrants an automatic retry
     * with a refreshed session. session_token_invalid is not retryable because the token itself is
     * bad (not just expired), so a new session is needed but the current request should fail.
     */
    private static boolean isRetryableSessionFailure(HttpResponse response) {
        if (response.getStatusCode() != 401) {
            return false;
        }
        String wwwAuth = response.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE);
        return wwwAuth != null && wwwAuth.startsWith(SESSION_SCHEME) && wwwAuth.contains(SESSION_EXPIRED);
    }

    private static boolean shouldRetryRequest(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return false;
        }

        return isRetryableSessionFailure(response);
    }
}
