// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.policy;

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
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.implementation.accesshelpers.SessionProviderAccessHelper;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionOptions.SessionMode;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A pipeline policy that selects between session token and bearer token authentication.
 * <p>
 * This policy occupies the authentication policy slot in the pipeline, wrapping the
 * {@link StorageBearerTokenChallengeAuthorizationPolicy}. When sessions are enabled, for eligible blob GET requests,
 * the policy authenticates with a session token, refreshing {@code x-ms-date} before each signing.
 * For all other requests, it delegates to the wrapped bearer token policy.
 * <p>
 * Session-signed requests that receive HTTP 401 are retried once with bearer authentication, and the rejected
 * session credential is invalidated only if it is still current. Each transport retry may fall back independently.
 * Rejections do not start a cooldown;
 * subsequent eligible requests may acquire a new session. Other responses are returned to the caller unchanged.
 * <p>
 * If session acquisition fails with HTTP 403, 5xx, or HTTP 400 with the {@code FeatureNotEnabled} error code, the
 * container is placed in a five minute cooldown during which requests for that container go straight to bearer
 * authentication. Cooldown state is held by the provider and shared by all clients using that provider.
 * Acquisition failures that do not carry one of those status codes fall back to bearer for that request only
 * and do not start a cooldown.
 */
public final class SessionAuthenticationPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(SessionAuthenticationPolicy.class);
    private static final HttpHeaderName X_MS_AUTH_INFO = HttpHeaderName.fromString("x-ms-auth-info");
    private static final HttpHeaderName X_MS_DATE = HttpHeaderName.fromString("x-ms-date");
    private static final String SESSION_EXPIRING = "session_expiring";
    private static final String SESSION_PREFIX = "Session ";

    private final StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private final SessionProviderAccessHelper sessionProvider;
    private final SessionMode sessionMode;
    private final String accountName;
    private final Clock clock;

    /**
     * Creates a session authentication policy.
     *
     * @param bearerPolicy the bearer token policy used for non-session requests and fallback.
     * @param sessionProvider the provider used to acquire and manage session credentials.
     * @param sessionOptions the options that configure session authentication. Values are captured at construction.
     */
    public SessionAuthenticationPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        SessionProvider sessionProvider, SessionOptions sessionOptions) {
        this(bearerPolicy, sessionProvider, sessionOptions, Clock.systemUTC());
    }

    /**
     * Creates a session authentication policy with a clock.
     *
     * @param bearerPolicy the bearer token policy used for non-session requests and fallback.
     * @param sessionProvider the provider used to acquire and manage session credentials.
     * @param sessionOptions the options that configure session authentication. Values are captured at construction.
     * @param clock the clock used for signing.
     */
    SessionAuthenticationPolicy(StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy,
        SessionProvider sessionProvider, SessionOptions sessionOptions, Clock clock) {
        this.bearerPolicy = Objects.requireNonNull(bearerPolicy, "'bearerPolicy' cannot be null.");
        this.sessionProvider = SessionProviderAccessHelper
            .getInternal(Objects.requireNonNull(sessionProvider, "'sessionProvider' cannot be null."));
        Objects.requireNonNull(sessionOptions, "'sessionOptions' cannot be null.");
        this.sessionMode = ModelHelper.resolveSessionMode(sessionOptions.getSessionMode());
        this.accountName = sessionOptions.getAccountName();
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        SessionRequestContext requestContext = resolveSessionRequest(context);
        if (requestContext == null) {
            return bearerPolicy.process(context, next);
        }

        HttpPipelineNextPolicy retryNext = next.clone();
        Mono<SessionCredential> sessionMono;
        try {
            sessionMono = sessionProvider.getSessionAsync(requestContext);
        } catch (RuntimeException ex) {
            handleSessionAcquisitionFailure(ex);
            return bearerPolicy.process(context, next);
        }

        return sessionMono.onErrorResume(error -> {
            handleSessionAcquisitionFailure(error);
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

        HttpPipelineNextSyncPolicy retryNext = next.clone();
        SessionCredential session;
        try {
            session = sessionProvider.getSession(requestContext);
        } catch (RuntimeException ex) {
            handleSessionAcquisitionFailure(ex);
            return bearerPolicy.processSync(context, next);
        }
        if (session == null) {
            return bearerPolicy.processSync(context, next);
        }
        signRequest(context, session);

        HttpResponse response = next.processSync();
        return handleSessionResponseSync(context, response, session, requestContext, retryNext);
    }

    private SessionRequestContext resolveSessionRequest(HttpPipelineCallContext context) {
        if (sessionMode != SessionMode.ENABLED) {
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
        String accountName = getOverrideOrDefault(this.accountName, parts.getAccountName());

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
            response.close();
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            context.getHttpRequest().getHeaders().remove(X_MS_DATE);
            // retryNext starts after this policy, so the bearer response cannot trigger another session fallback.
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
            response.close();
            context.getHttpRequest().getHeaders().remove(HttpHeaderName.AUTHORIZATION);
            context.getHttpRequest().getHeaders().remove(X_MS_DATE);
            return bearerPolicy.processSync(context, retryNext);
        }

        return response;
    }

    private void signRequest(HttpPipelineCallContext context, SessionCredential credential) {
        context.getHttpRequest().setHeader(X_MS_DATE, DateTimeRfc1123.toRfc1123String(OffsetDateTime.now(clock)));

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
     * request can acquire a new session. A credential already replaced by a concurrent refresh is left unchanged.
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

    private void handleSessionAcquisitionFailure(Throwable error) {
        LOGGER.warning("Unable to obtain a session credential. Using bearer token.", error);
    }
}
