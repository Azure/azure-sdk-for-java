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
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A pipeline policy that selects between session token and bearer token authentication.
 * <p>
 * This policy occupies the authentication policy slot in the pipeline, wrapping the
 * {@link StorageBearerTokenChallengeAuthorizationPolicy}. For eligible blob GET requests,
 * the policy authenticates with a session token. For all other requests, it delegates to the
 * wrapped bearer token policy.
 */
public final class SessionTokenCredentialPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionTokenCredentialPolicy.class);
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_PREFIX = "Session ";
    private static final Duration SESSION_ACQUISITION_COOLDOWN = Duration.ofMinutes(5);

    private final StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private final SessionProvider sessionProvider;
    private final SessionOptions sessionOptions;
    private final Clock clock;
    private final ConcurrentHashMap<String, OffsetDateTime> accountCooldowns = new ConcurrentHashMap<>();

    SessionTokenCredentialPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        SessionProvider sessionProvider, SessionOptions sessionOptions) {
        this(bearerPolicy, sessionProvider, sessionOptions, Clock.systemUTC());
    }

    SessionTokenCredentialPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        SessionProvider sessionProvider, SessionOptions sessionOptions, Clock clock) {
        this.bearerPolicy = Objects.requireNonNull(bearerPolicy, "'bearerPolicy' cannot be null.");
        this.sessionProvider = Objects.requireNonNull(sessionProvider, "'sessionProvider' cannot be null.");
        this.sessionOptions = Objects.requireNonNull(sessionOptions, "'sessionOptions' cannot be null.");
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        SessionRequestContext requestContext = resolveSessionRequest(context);
        if (requestContext == null) {
            return bearerPolicy.process(context, next);
        }
        if (isAccountInCooldown(requestContext.getAccountName())) {
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
        if (isAccountInCooldown(requestContext.getAccountName())) {
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

    private SessionRequestContext resolveSessionRequest(HttpPipelineCallContext context) {
        if (sessionOptions.getSessionMode() == SessionMode.DISABLED
            || context.getHttpRequest().getHttpMethod() != HttpMethod.GET) {
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

        String containerName = getOverrideOrDefault(sessionOptions.getContainerName(), parts.getBlobContainerName());
        String accountName = getOverrideOrDefault(sessionOptions.getAccountName(), parts.getAccountName());

        // comp indicates sub-operations (metadata, tags, etc.) that should use bearer auth.
        if (CoreUtils.isNullOrEmpty(containerName)
            || CoreUtils.isNullOrEmpty(parts.getBlobName())
            || parts.getUnparsedParameters().containsKey("comp")) {
            return null;
        }

        return new SessionRequestContext().setContainerName(containerName).setAccountName(accountName);
    }

    private static String getOverrideOrDefault(String override, String defaultValue) {
        return CoreUtils.isNullOrEmpty(override) ? defaultValue : override;
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
        if (context.getHttpRequest().getHeaders().getValue(X_MS_DATE) == null) {
            context.getHttpRequest().setHeader(X_MS_DATE, DateTimeRfc1123.toRfc1123String(OffsetDateTime.now()));
        }

        StorageSharedKeyCredential sharedKey
            = new StorageSharedKeyCredential(credential.getAccountName(), credential.getSessionKey());
        boolean contentLengthMissing
            = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH) == null;
        if (contentLengthMissing) {
            context.getHttpRequest().setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
        }

        String sharedKeyAuthorization;
        try {
            sharedKeyAuthorization = sharedKey.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
                context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders(), false);
        } finally {
            if (contentLengthMissing) {
                context.getHttpRequest().getHeaders().remove(HttpHeaderName.CONTENT_LENGTH);
            }
        }
        String signature = sharedKeyAuthorization.substring(sharedKeyAuthorization.indexOf(':') + 1);
        context.getHttpRequest()
            .setHeader(HttpHeaderName.AUTHORIZATION, SESSION_PREFIX + credential.getSessionToken() + ":" + signature);
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
                if (beginAccountCooldown(requestContext.getAccountName())) {
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

    private boolean isAccountInCooldown(String accountName) {
        String key = normalize(accountName);
        OffsetDateTime cooldownUntil = accountCooldowns.get(key);
        if (cooldownUntil == null) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (now.isBefore(cooldownUntil)) {
            return true;
        }

        accountCooldowns.remove(key, cooldownUntil);
        return false;
    }

    private boolean beginAccountCooldown(String accountName) {
        String key = normalize(accountName);
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime cooldownUntil = now.plus(SESSION_ACQUISITION_COOLDOWN);

        while (true) {
            OffsetDateTime existing = accountCooldowns.get(key);
            if (existing != null && now.isBefore(existing)) {
                return false;
            }

            boolean updated = existing == null
                ? accountCooldowns.putIfAbsent(key, cooldownUntil) == null
                : accountCooldowns.replace(key, existing, cooldownUntil);
            if (updated) {
                return true;
            }
        }
    }

    private static String normalize(String accountName) {
        return CoreUtils.isNullOrEmpty(accountName) ? "" : accountName.trim().toLowerCase(Locale.ROOT);
    }
}
