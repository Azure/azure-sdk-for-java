// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A pipeline policy that selects between session token and bearer token authentication.
 * <p>
 * This policy occupies the authentication policy slot in the pipeline, wrapping the
 * {@link StorageBearerTokenChallengeAuthorizationPolicy}. For eligible blob GET requests,
 * the policy authenticates with a session token. For all other requests, it delegates to the
 * wrapped bearer token policy.
 * <p>
 * Request analysis is performed by {@link #analyzeRequest(HttpPipelineCallContext)} which returns
 * an {@link AuthStrategy} indicating the authentication approach to use.
 */
final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final String SESSION_SCHEME = "Session";
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_EXPIRED = "session_expired";
    private static final String SESSION_TOKEN_INVALID = "session_token_invalid";
    private static final String SESSION_OPS_UNAVAILABLE = "SessionOperationsTemporarilyUnavailable";

    private final StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private final StorageSessionCredentialCache sessionCredentialCache;
    private final SessionMode mode;
    private final AtomicBoolean autoActivated = new AtomicBoolean(false);

    /**
     * Authentication strategy determined by {@link #analyzeRequest(HttpPipelineCallContext)}.
     */
    enum AuthStrategy {
        /** Delegate to the wrapped bearer token policy. */
        USE_BEARER_TOKEN,
        /** Acquire a session token and sign the request. */
        USE_SESSION_TOKEN
    }

    SessionTokenCredentialPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        StorageSessionCredentialCache sessionCredentialCache, SessionMode mode) {
        this.bearerPolicy = Objects.requireNonNull(bearerPolicy, "'bearerPolicy' cannot be null.");
        this.sessionCredentialCache
            = Objects.requireNonNull(sessionCredentialCache, "'sessionCredentialCache' cannot be null.");
        this.mode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    /**
     * Returns the wrapped bearer token policy. Used when constructing per-container pipelines from a service
     * pipeline so that the bearer policy can be reused without scanning the pipeline.
     */
    StorageBearerTokenChallengeAuthorizationPolicy getBearerPolicy() {
        return bearerPolicy;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (analyzeRequest(context) == AuthStrategy.USE_BEARER_TOKEN) {
            return bearerPolicy.process(context, next);
        }

        HttpPipelineNextPolicy retryNext = next.clone();
        return getValidSessionAsync().flatMap(session -> {
            signRequest(context, session);
            return next.process().flatMap(response -> handleSessionResponse(context, response, session, retryNext));
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (analyzeRequest(context) == AuthStrategy.USE_BEARER_TOKEN) {
            return bearerPolicy.processSync(context, next);
        }

        HttpPipelineNextSyncPolicy retryNext = next.clone();
        StorageSessionCredential session = getValidSessionSync();
        signRequest(context, session);

        HttpResponse response = next.processSync();
        return handleSessionResponseSync(context, response, session, retryNext);
    }

    /**
     * Analyzes the request to determine whether a session token or bearer token should be used.
     * <p>
     * Session tokens are only used for blob GET download operations that satisfy:
     * <ul>
     *   <li>HTTP method is GET</li>
     *   <li>URL has both container name and blob name (not service or container-level)</li>
     *   <li>No {@code comp} or {@code restype} query parameters (those indicate sub-operations)</li>
     *   <li>Session mode permits it ({@link SessionMode#ALWAYS}, or {@link SessionMode#AUTO} after first request)</li>
     * </ul>
     * GET requests with {@code snapshot} or {@code versionid} parameters are still eligible.
     */
    AuthStrategy analyzeRequest(HttpPipelineCallContext context) {
        if (mode == SessionMode.NONE) {
            return AuthStrategy.USE_BEARER_TOKEN;
        }

        if (context.getHttpRequest().getHttpMethod() != HttpMethod.GET) {
            return AuthStrategy.USE_BEARER_TOKEN;
        }

        BlobUrlParts parts = BlobUrlParts.parse(context.getHttpRequest().getUrl());

        // Must target a specific blob (container + blob name present).
        if (CoreUtils.isNullOrEmpty(parts.getBlobContainerName()) || CoreUtils.isNullOrEmpty(parts.getBlobName())) {
            return AuthStrategy.USE_BEARER_TOKEN;
        }

        // comp= indicates sub-operations (metadata, tags, etc.) that should use bearer auth.
        Map<String, String[]> queryParams = parts.getUnparsedParameters();
        if (queryParams.containsKey("comp")) {
            return AuthStrategy.USE_BEARER_TOKEN;
        }

        // AUTO mode: first eligible GetBlob uses bearer, subsequent requests use session.
        if (mode == SessionMode.AUTO && !autoActivated.getAndSet(true)) {
            return AuthStrategy.USE_BEARER_TOKEN;
        }

        return AuthStrategy.USE_SESSION_TOKEN;
    }

    /**
     * Handles the response after a session-authenticated async request. Inspects for
     * session-expiring hints, retryable failures, and fallback conditions.
     */
    private Mono<HttpResponse> handleSessionResponse(HttpPipelineCallContext context, HttpResponse response,
        StorageSessionCredential session, HttpPipelineNextPolicy retryNext) {

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
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            return bearerPolicy.process(context, retryNext);
        }

        return Mono.just(response);
    }

    /**
     * Handles the response after a session-authenticated sync request. Inspects for
     * session-expiring hints, retryable failures, and fallback conditions.
     */
    private HttpResponse handleSessionResponseSync(HttpPipelineCallContext context, HttpResponse response,
        StorageSessionCredential session, HttpPipelineNextSyncPolicy retryNext) {

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
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            return bearerPolicy.processSync(context, retryNext);
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
     * The session infrastructure is temporarily down, so we strip session auth and
     * delegate to the wrapped bearer policy to handle the request with a bearer token.
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
