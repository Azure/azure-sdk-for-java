// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.models.SessionMode;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final String SESSION_OPS_UNAVAILABLE = "SessionOperationsTemporarilyUnavailable";

    private final StorageSessionCredentialCache sessionCredentialCache;
    private final SessionMode mode;
    private final AtomicBoolean autoActivated = new AtomicBoolean(false);

    SessionTokenCredentialPolicy(BlobSessionClient sessionClient) {
        this(
            new StorageSessionCredentialCache(Objects.requireNonNull(sessionClient, "'sessionClient' cannot be null.")),
            SessionMode.AUTO);
    }

    SessionTokenCredentialPolicy(StorageSessionCredentialCache sessionCredentialCache, SessionMode mode) {
        this.sessionCredentialCache
            = Objects.requireNonNull(sessionCredentialCache, "'sessionCredentialCache' cannot be null.");
        this.mode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!shouldUseSession()) {
            return next.process();
        }

        HttpPipelineNextPolicy retryNext = next.clone();
        // Save the bearer token set by the upstream BearerTokenPolicy so we can restore it on fallback.
        String bearerAuth = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        return getValidSessionAsync().flatMap(session -> {
            signRequest(context, session);
            return next.process().flatMap(response -> {
                handleSessionExpiringHeader(response);

                if (isSessionCredentialRejected(response)) {
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

                if (shouldFallBackToBearer(context, response)) {
                    response.close();
                    context.setData(RETRY_CONTEXT_KEY, true);
                    restoreBearerAuth(context, bearerAuth);
                    return retryNext.process();
                }

                return Mono.just(response);
            });
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!shouldUseSession()) {
            return next.processSync();
        }

        HttpPipelineNextSyncPolicy retryNext = next.clone();
        String bearerAuth = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        StorageSessionCredential session = getValidSessionSync();
        signRequest(context, session);

        HttpResponse response = next.processSync();
        handleSessionExpiringHeader(response);

        if (isSessionCredentialRejected(response)) {
            invalidateSession(session);
        }

        if (shouldRetryRequest(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);

            StorageSessionCredential refreshed = getValidSessionSync();
            signRequest(context, refreshed);
            return retryNext.processSync();
        }

        if (shouldFallBackToBearer(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);
            restoreBearerAuth(context, bearerAuth);
            return retryNext.processSync();
        }

        return response;
    }

    Mono<StorageSessionCredential> getValidSessionAsync() {
        return sessionCredentialCache.getValidSessionAsync();
    }

    /**
     * Determines whether this request should use session auth based on the configured mode.
     * <ul>
     *   <li>{@link SessionMode#NONE}: always returns false — passthrough to bearer.</li>
     *   <li>{@link SessionMode#ALWAYS}: always returns true — session from first request.</li>
     *   <li>{@link SessionMode#AUTO}: returns false for the first request (bearer), true thereafter.</li>
     * </ul>
     */
    private boolean shouldUseSession() {
        switch (mode) {
            case NONE:
                return false;

            case ALWAYS:
                return true;

            case AUTO:
                return autoActivated.getAndSet(true);

            default:
                return true;
        }
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

    private static void restoreBearerAuth(HttpPipelineCallContext context, String bearerAuth) {
        if (bearerAuth != null) {
            context.getHttpRequest().setHeader(HttpHeaderName.AUTHORIZATION, bearerAuth);
        } else {
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
        }
    }

    private void handleSessionExpiringHeader(HttpResponse response) {
        String authInfo = response.getHeaderValue(X_MS_AUTH_INFO);
        if (authInfo != null && authInfo.contains(SESSION_EXPIRING)) {
            sessionCredentialCache.refreshSessionInBackground();
        }
    }

    /**
     * Returns true for any 401 where the session service rejected the credential (expired or invalid token).
     * Used to decide whether to invalidate the cached session.
     */
    private static boolean isSessionCredentialRejected(HttpResponse response) {
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

    /**
     * Returns true for 503 with SessionOperationsTemporarilyUnavailable error code.
     * The session infrastructure is temporarily down, so we strip session auth and let
     * the downstream BearerTokenPolicy handle the request with a bearer token.
     */
    private static boolean shouldFallBackToBearer(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return false;
        }

        return isSessionUnavailable(response);
    }

    private static boolean isSessionUnavailable(HttpResponse response) {
        if (response.getStatusCode() != 503) {
            return false;
        }
        String errorCode = response.getHeaderValue(HttpHeaderName.fromString("x-ms-error-code"));
        return SESSION_OPS_UNAVAILABLE.equals(errorCode);
    }
}
