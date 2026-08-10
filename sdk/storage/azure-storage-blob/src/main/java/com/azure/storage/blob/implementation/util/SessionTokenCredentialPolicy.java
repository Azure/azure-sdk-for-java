// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
public final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionTokenCredentialPolicy.class);
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final String SESSION_EXPIRING = "session_expiring";

    private final StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private final SessionProvider sessionProvider;
    private final SessionAcquisitionCooldown cooldown;
    private final SessionOptions sessionOptions;

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
        SessionProvider sessionProvider, SessionAcquisitionCooldown cooldown, SessionOptions sessionOptions) {
        this.bearerPolicy = Objects.requireNonNull(bearerPolicy, "'bearerPolicy' cannot be null.");
        this.sessionProvider = Objects.requireNonNull(sessionProvider, "'sessionProvider' cannot be null.");
        this.cooldown = Objects.requireNonNull(cooldown, "'cooldown' cannot be null.");
        this.sessionOptions = Objects.requireNonNull(sessionOptions, "'sessionOptions' cannot be null.");
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
        SessionRequestContext requestContext = resolveSessionRequest(context);
        if (requestContext == null) {
            return bearerPolicy.process(context, next);
        }
        if (cooldown.isAccountInCooldown(requestContext.getAccountName())) {
            return bearerPolicy.process(context, next);
        }

        HttpPipelineNextPolicy retryNext = next.clone();
        Mono<SessionCredential> sessionMono;
        try {
            sessionMono = sessionProvider.getSessionAsync(requestContext);
        } catch (RuntimeException ex) {
            handleSessionAcquisitionFailure(requestContext, ex);
            return bearerPolicy.process(context, next);
        }

        return sessionMono.map(Optional::of).onErrorResume(error -> {
            handleSessionAcquisitionFailure(requestContext, error);
            return Mono.just(Optional.empty());
        }).defaultIfEmpty(Optional.empty()).flatMap(sessionResult -> {
            if (!sessionResult.isPresent()) {
                context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
                return bearerPolicy.process(context, next);
            }
            SessionCredential session = sessionResult.get();
            signRequest(context, session);
            return next.process()
                .flatMap(response -> handleSessionResponse(context, response, session, requestContext, retryNext));
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        SessionRequestContext requestContext = resolveSessionRequest(context);
        if (requestContext == null) {
            return bearerPolicy.processSync(context, next);
        }
        if (cooldown.isAccountInCooldown(requestContext.getAccountName())) {
            return bearerPolicy.processSync(context, next);
        }

        HttpPipelineNextSyncPolicy retryNext = next.clone();
        SessionCredential session;
        try {
            session = sessionProvider.getSession(requestContext);
        } catch (RuntimeException ex) {
            handleSessionAcquisitionFailure(requestContext, ex);
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            return bearerPolicy.processSync(context, next);
        }
        signRequest(context, session);

        HttpResponse response = next.processSync();
        return handleSessionResponseSync(context, response, session, requestContext, retryNext);
    }

    /**
     * Analyzes the request to determine whether a session token or bearer token should be used.
     * Session tokens are only used for blob GET operations in
     * {@link SessionMode#ENABLED} mode targeting the configured container.
     *
     * @param context the pipeline call context for the request being analyzed.
     * @return {@link AuthStrategy#USE_SESSION_TOKEN} if the request is eligible for session-token
     * authentication (a GET against a blob in the configured container, with no {@code comp} query
     * parameter, while in {@link SessionMode#ENABLED} mode);
     * {@link AuthStrategy#USE_BEARER_TOKEN} otherwise.
     */
    AuthStrategy analyzeRequest(HttpPipelineCallContext context) {
        return resolveSessionRequest(context) == null ? AuthStrategy.USE_BEARER_TOKEN : AuthStrategy.USE_SESSION_TOKEN;
    }

    private SessionRequestContext resolveSessionRequest(HttpPipelineCallContext context) {
        if (sessionOptions.getSessionMode() == SessionMode.DISABLED) {
            return null;
        }

        if (context.getHttpRequest().getHttpMethod() != HttpMethod.GET) {
            return null;
        }

        BlobUrlParts parts;
        try {
            parts = BlobUrlParts.parse(context.getHttpRequest().getUrl());
        } catch (RuntimeException ex) {
            LOGGER.warning("Unable to resolve session authentication context from request URL. Using bearer token.",
                ex);
            return null;
        }

        String containerName = CoreUtils.isNullOrEmpty(sessionOptions.getContainerName())
            ? parts.getBlobContainerName()
            : sessionOptions.getContainerName();
        String accountName = CoreUtils.isNullOrEmpty(sessionOptions.getAccountName())
            ? parts.getAccountName()
            : sessionOptions.getAccountName();

        if (CoreUtils.isNullOrEmpty(containerName) || CoreUtils.isNullOrEmpty(parts.getBlobName())) {
            return null;
        }

        // comp indicates sub-operations (metadata, tags, etc.) that should use bearer auth.
        Map<String, String[]> queryParams = parts.getUnparsedParameters();
        if (queryParams.containsKey("comp")) {
            return null;
        }

        return new SessionRequestContext().setContainerName(containerName).setAccountName(accountName);
    }

    /**
     * Handles the response after a session-authenticated async request. Inspects for
     * session-expiring hints, retryable failures, and fallback conditions.
     */
    private Mono<HttpResponse> handleSessionResponse(HttpPipelineCallContext context, HttpResponse response,
        SessionCredential session, SessionRequestContext requestContext, HttpPipelineNextPolicy retryNext) {

        handleSessionExpiringHeader(response, requestContext);

        if (isUnauthorizedResponse(response)) {
            logSessionInvalidation(requestContext, sessionProvider.invalidateSession(requestContext, session));
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
        SessionCredential session, SessionRequestContext requestContext, HttpPipelineNextSyncPolicy retryNext) {

        handleSessionExpiringHeader(response, requestContext);

        if (isUnauthorizedResponse(response)) {
            logSessionInvalidation(requestContext, sessionProvider.invalidateSession(requestContext, session));
        }

        if (shouldFallBackToBearer(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            return bearerPolicy.processSync(context, retryNext);
        }

        return response;
    }

    private void signRequest(HttpPipelineCallContext context, SessionCredential credential) {
        SessionRequestSigner.signRequest(context.getHttpRequest(), credential);
    }

    private void handleSessionExpiringHeader(HttpResponse response, SessionRequestContext requestContext) {
        String authInfo = response.getHeaderValue(X_MS_AUTH_INFO);
        if (authInfo != null && authInfo.contains(SESSION_EXPIRING)) {
            sessionProvider.refreshSession(requestContext);
        }
    }

    private static void logSessionInvalidation(SessionRequestContext requestContext, boolean invalidated) {
        if (invalidated) {
            LOGGER.warning(
                "Session authentication was rejected with HTTP 401 for container '{}'. "
                    + "The cached session was invalidated and the request will proceed using bearer token.",
                requestContext.getContainerName());
        } else {
            LOGGER.verbose(
                "Session authentication was rejected with HTTP 401 for container '{}', but the cached "
                    + "session was already invalidated. The request will proceed using bearer token.",
                requestContext.getContainerName());
        }
    }

    /**
     * Returns true when the session-authenticated request was rejected as unauthorized.
     * Used to decide whether to invalidate the cached session.
     */
    private static boolean isUnauthorizedResponse(HttpResponse response) {
        return response.getStatusCode() == 401;
    }

    /**
     * Returns true for responses where retrying with bearer authentication can preserve
     * request compatibility when session authentication is unavailable or rejected.
     */
    private static boolean shouldFallBackToBearer(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return false;
        }

        return isUnauthorizedResponse(response) || isBadRequest(response);
    }

    private static boolean isBadRequest(HttpResponse response) {
        return response.getStatusCode() == 400;
    }

    private void handleSessionAcquisitionFailure(SessionRequestContext requestContext, Throwable error) {
        Throwable current = error;
        while (current != null && !(current instanceof HttpResponseException)) {
            current = current.getCause();
        }

        if (current != null && ((HttpResponseException) current).getResponse() != null) {
            int statusCode = ((HttpResponseException) current).getResponse().getStatusCode();
            if (statusCode == 400 || statusCode == 403 || (statusCode >= 500 && statusCode <= 599)) {
                if (cooldown.beginAccountCooldown(requestContext.getAccountName())) {
                    LOGGER.warning(
                        "Session acquisition failed with HTTP {}. Suppressing session acquisition for this account "
                            + "for five minutes and using bearer token.",
                        statusCode);
                }
                return;
            }
        }

        LOGGER.warning("Unable to obtain a session credential. Using bearer token.", error);
    }
}
