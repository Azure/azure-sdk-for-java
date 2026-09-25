// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE_HEADER_NAME;

/**
 * A pipeline policy that selects between session token and bearer token authentication.
 * <p>
 * This policy occupies the authentication policy slot in the pipeline, wrapping the
 * {@link StorageBearerTokenChallengeAuthorizationPolicy}. For eligible blob GET requests,
 * the policy authenticates with a session token. For all other requests, it delegates to the
 * wrapped bearer token policy.
 * <p>
 * Session-signed requests that receive HTTP 401 are retried once with bearer authentication, and the rejected
 * session credential is invalidated. Other responses are returned to the caller unchanged.
 * <p>
 * If session acquisition fails with HTTP 403, 5xx, or HTTP 400 with the {@code FeatureNotEnabled} error code, the
 * container is placed in a five minute cooldown during which requests for that container go straight to bearer
 * authentication. Cooldown state is held by this policy instance, so it is scoped to a single client pipeline.
 * Acquisition failures that do not carry one of those status codes fall back to bearer for that request only
 * and do not start a cooldown.
 */
public final class SessionAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionAuthenticationPolicy.class);
    private static final String RETRY_CONTEXT_KEY = "azure-storage-blob-session-auth-retried";
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_PREFIX = "Session ";
    private static final Duration SESSION_COOLDOWN = Duration.ofMinutes(5);

    private final StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private final SessionProvider sessionProvider;
    private final SessionOptions sessionOptions;
    private final Clock clock;
    private final ConcurrentHashMap<String, OffsetDateTime> containerCooldowns = new ConcurrentHashMap<>();

    SessionAuthenticationPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        SessionProvider sessionProvider, SessionOptions sessionOptions) {
        this(bearerPolicy, sessionProvider, sessionOptions, Clock.systemUTC());
    }

    SessionAuthenticationPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
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
        if (isContainerInCooldown(requestContext.getContainerName())) {
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

        return sessionMono.onErrorResume(error -> {
            handleSessionAcquisitionFailure(requestContext, error);
            return Mono.empty();
        }).flatMap(session -> {
            signRequest(context, session);
            return next.process()
                .flatMap(response -> handleSessionResponse(context, response, session, requestContext, retryNext));
        }).switchIfEmpty(Mono.defer(() -> bearerPolicy.process(context, next)));
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        SessionRequestContext requestContext = resolveSessionRequest(context);
        if (requestContext == null) {
            return bearerPolicy.processSync(context, next);
        }
        if (isContainerInCooldown(requestContext.getContainerName())) {
            return bearerPolicy.processSync(context, next);
        }

        HttpPipelineNextSyncPolicy retryNext = next.clone();
        SessionCredential session;
        try {
            session = sessionProvider.getSession(requestContext);
        } catch (RuntimeException ex) {
            handleSessionAcquisitionFailure(requestContext, ex);
            return bearerPolicy.processSync(context, next);
        }
        signRequest(context, session);

        HttpResponse response = next.processSync();
        return handleSessionResponseSync(context, response, session, requestContext, retryNext);
    }

    private SessionRequestContext resolveSessionRequest(HttpPipelineCallContext context) {
        if (sessionOptions.getSessionMode() == SessionMode.DISABLED) {
            return null;
        }

        if (!sessionProvider.isRequestEligible(context.getHttpRequest())) {
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

        String containerName = parts.getBlobContainerName();
        String accountName = getOverrideOrDefault(sessionOptions.getAccountName(), parts.getAccountName());

        if (CoreUtils.isNullOrEmpty(containerName) || CoreUtils.isNullOrEmpty(parts.getBlobName())) {
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

        if (response.getStatusCode() == 401) {
            handleSessionRejection(requestContext, session);
        }

        if (shouldFallBackToBearer(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            context.getHttpRequest().getHeaders().remove(X_MS_DATE);
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

        if (response.getStatusCode() == 401) {
            handleSessionRejection(requestContext, session);
        }

        if (shouldFallBackToBearer(context, response)) {
            response.close();
            context.setData(RETRY_CONTEXT_KEY, true);
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            context.getHttpRequest().getHeaders().remove(X_MS_DATE);
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

    /**
     * Handles a session credential being rejected by the service. The rejected credential is invalidated so the next
     * request attempts to create a new session.
     */
    private void handleSessionRejection(SessionRequestContext requestContext, SessionCredential session) {
        logSessionInvalidation(requestContext, sessionProvider.invalidateSession(requestContext, session));
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
     * Returns true for responses where retrying with bearer authentication can preserve
     * request compatibility when session authentication is unavailable or rejected.
     */
    private static boolean shouldFallBackToBearer(HttpPipelineCallContext context, HttpResponse response) {
        if (Boolean.TRUE.equals(context.getData(RETRY_CONTEXT_KEY).orElse(false))) {
            return false;
        }

        return response.getStatusCode() == 401;
    }

    /**
     * Handles a failure to obtain a session credential. When the failure carries an HTTP 403, 5xx, or HTTP 400
     * FeatureNotEnabled response, the container is placed in cooldown so following requests skip session
     * acquisition entirely. Any other failure is logged and falls back to bearer for the current request only.
     */
    private void handleSessionAcquisitionFailure(SessionRequestContext requestContext, Throwable error) {
        Throwable current = error;
        while (current != null && !(current instanceof HttpResponseException)) {
            current = current.getCause();
        }

        if (current != null && ((HttpResponseException) current).getResponse() != null) {
            HttpResponse response = ((HttpResponseException) current).getResponse();
            int statusCode = response.getStatusCode();
            if (shouldStartAcquisitionCooldown(response)) {
                if (beginContainerCooldown(requestContext.getContainerName())) {
                    LOGGER.warning(
                        "Session acquisition failed with HTTP {}. Suppressing session authentication for container '{}' "
                            + "for five minutes and using bearer token.",
                        statusCode, requestContext.getContainerName());
                }
                return;
            }
        }

        LOGGER.warning("Unable to obtain a session credential. Using bearer token.", error);
    }

    private static boolean shouldStartAcquisitionCooldown(HttpResponse response) {
        int statusCode = response.getStatusCode();
        if (statusCode == 403 || (statusCode >= 500 && statusCode <= 599)) {
            return true;
        }

        return statusCode == 400
            && "FeatureNotEnabled".equals(response.getHeaderValue(ERROR_CODE_HEADER_NAME));
    }

    private boolean isContainerInCooldown(String containerName) {
        String key = normalize(containerName);
        OffsetDateTime cooldownUntil = containerCooldowns.get(key);
        if (cooldownUntil == null) {
            return false;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        if (now.isBefore(cooldownUntil)) {
            return true;
        }

        containerCooldowns.remove(key, cooldownUntil);
        return false;
    }

    private boolean beginContainerCooldown(String containerName) {
        String key = normalize(containerName);
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime cooldownUntil = now.plus(SESSION_COOLDOWN);
        AtomicBoolean cooldownStarted = new AtomicBoolean();
        containerCooldowns.compute(key, (ignored, currentExpirationTime) -> {
            if (currentExpirationTime != null && now.isBefore(currentExpirationTime)) {
                return currentExpirationTime;
            }

            cooldownStarted.set(true);
            return cooldownUntil;
        });
        return cooldownStarted.get();
    }

    private static String normalize(String containerName) {
        return CoreUtils.isNullOrEmpty(containerName) ? "" : containerName.trim().toLowerCase(Locale.ROOT);
    }
}
