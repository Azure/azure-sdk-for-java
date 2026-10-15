// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.models.CreateSessionResponse;
import com.azure.storage.blob.implementation.models.SessionCredentials;
import com.azure.storage.common.implementation.util.AutoRefreshingCache;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.storage.common.implementation.Constants.HeaderConstants.ERROR_CODE_HEADER_NAME;

/**
 * An SDK-managed, thread-safe session provider that caches credentials per container.
 * <p>
 * The provider owns only the shared session cache. It never owns a pipeline: CreateSession requests are sent by the
 * {@link SessionAcquirer} carried in each {@link SessionRequestContext}, which is bound to the pipeline of the client
 * that made the request. Background and forced refreshes use the acquirer of the most recent caller for that
 * container.
 */
final class ContainerSessionProvider extends SessionProvider {

    static final int IDLE_EVICTION_THRESHOLD_MINUTES = 1;

    private static final ClientLogger LOGGER = new ClientLogger(ContainerSessionProvider.class);
    private static final Duration IDLE_EVICTION_THRESHOLD = Duration.ofMinutes(IDLE_EVICTION_THRESHOLD_MINUTES);
    private static final Duration DEFAULT_EXPIRATION_OFFSET = Duration.ofMinutes(5L);
    private static final String FEATURE_NOT_ENABLED = "FeatureNotEnabled";

    private final Clock clock;
    private final ConcurrentHashMap<String, ContainerSessionCache> containerSessionCaches = new ConcurrentHashMap<>();

    ContainerSessionProvider(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    @Override
    boolean isRequestEligible(HttpRequest request) {
        if (request == null || request.getHttpMethod() != HttpMethod.GET) {
            return false;
        }

        BlobUrlParts parts;
        try {
            parts = BlobUrlParts.parse(request.getUrl());
        } catch (RuntimeException ex) {
            return false;
        }

        if (CoreUtils.isNullOrEmpty(parts.getBlobContainerName())
            || CoreUtils.isNullOrEmpty(parts.getBlobName())
            || parts.getUnparsedParameters().containsKey("comp")
            || parts.getUnparsedParameters().containsKey("restype")) {
            return false;
        }

        return request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-structured-body")) == null;
    }

    @Override
    Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
        return Mono.defer(() -> {
            String container = requireContainerName(context);
            SessionAcquirer acquirer = requireAcquirer(context);
            String resolvedAccount = resolveAccountName(context);
            return updateCache(container, resolvedAccount, acquirer).getSessionAsync();
        });
    }

    @Override
    boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential) {
        if (context == null) {
            return false;
        }
        ContainerSessionCache containerSessionCache = containerSessionCaches.get(cacheKey(context));
        return containerSessionCache != null && containerSessionCache.cache.invalidateValue(rejectedCredential);
    }

    @Override
    void refreshSession(SessionRequestContext context) {
        if (context == null) {
            return;
        }
        String key = cacheKey(context);
        ContainerSessionCache containerSessionCache = containerSessionCaches.get(key);
        if (containerSessionCache != null) {
            containerSessionCache.cache.forceRefreshValueInBackground();
        }
    }

    private String requireContainerName(SessionRequestContext context) {
        String containerName = context == null ? null : context.getContainerName();
        if (CoreUtils.isNullOrEmpty(containerName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'context.getContainerName()' cannot be null or empty."));
        }
        return containerName;
    }

    private SessionAcquirer requireAcquirer(SessionRequestContext context) {
        SessionAcquirer acquirer = context == null ? null : context.getAcquirer();
        if (acquirer == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'context.getAcquirer()' cannot be null."));
        }
        return acquirer;
    }

    private String resolveAccountName(SessionRequestContext context) {
        String resolvedAccountName = context == null ? null : context.getAccountName();
        if (CoreUtils.isNullOrEmpty(resolvedAccountName) && context != null && context.getAcquirer() != null) {
            resolvedAccountName = context.getAcquirer().getAccountName();
        }
        if (CoreUtils.isNullOrEmpty(resolvedAccountName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The account name could not be resolved from the request URL."));
        }
        return resolvedAccountName;
    }

    private ContainerSessionCache updateCache(String containerName, String resolvedAccountName,
        SessionAcquirer acquirer) {
        String key = normalize(resolvedAccountName) + "/" + normalize(containerName);
        ContainerSessionCache containerSessionCache = containerSessionCaches.compute(key, (k, existing) -> {
            OffsetDateTime now = OffsetDateTime.now(clock);
            if (existing == null) {
                return new ContainerSessionCache(this, clock, containerName, resolvedAccountName, acquirer, now);
            }
            existing.lastAccess = now;
            existing.latestAcquirer = acquirer;
            return existing;
        });
        evictStaleCaches();
        return containerSessionCache;
    }

    private void evictStaleCaches() {
        containerSessionCaches.keySet().forEach(this::evictStaleCache);
    }

    private void evictStaleCache(String key) {
        containerSessionCaches.computeIfPresent(key, this::retainIfActive);
    }

    private ContainerSessionCache retainIfActive(String ignored, ContainerSessionCache cache) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Duration idleTime = Duration.between(cache.lastAccess, now);
        if (idleTime.compareTo(IDLE_EVICTION_THRESHOLD) >= 0) {
            return null;
        }
        return cache;
    }

    private Mono<SessionCredential> createSessionAsync(SessionAcquirer acquirer, String container,
        String resolvedAccountName) {
        return acquirer.createSessionAsync(container).map(response -> toCredential(response, resolvedAccountName));
    }

    private static boolean shouldFallback(Throwable error) {
        Throwable current = error;
        while (current != null && !(current instanceof HttpResponseException)) {
            current = current.getCause();
        }
        HttpResponse response = current == null ? null : ((HttpResponseException) current).getResponse();
        if (response == null) {
            return false;
        }
        int status = response.getStatusCode();
        return status == 403
            || (status >= 500 && status <= 599)
            || (status == 400 && FEATURE_NOT_ENABLED.equalsIgnoreCase(response.getHeaderValue(ERROR_CODE_HEADER_NAME)));
    }

    private SessionCredential toCredential(Response<CreateSessionResponse> response, String resolvedAccountName) {
        CreateSessionResponse session = response.getValue();
        if (session == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("CreateSession response did not contain a session payload."));
        }

        SessionCredentials sessionCredentials = session.getCredentials();
        if (sessionCredentials == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("CreateSession response did not contain HMAC session credentials."));
        }

        OffsetDateTime expiration = session.getExpiration();
        if (expiration == null) {
            expiration = OffsetDateTime.now(clock).plus(DEFAULT_EXPIRATION_OFFSET);
        }
        return new SessionCredential(sessionCredentials.getSessionToken(), sessionCredentials.getSessionKey(),
            expiration, resolvedAccountName);
    }

    private static String normalize(String name) {
        return CoreUtils.isNullOrEmpty(name) ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private String cacheKey(SessionRequestContext context) {
        return normalize(resolveAccountName(context)) + "/" + normalize(context.getContainerName());
    }

    private static final class ContainerSessionCache {
        final AutoRefreshingCache<SessionCredential> cache;
        volatile OffsetDateTime lastAccess;
        // The acquirer of the most recent caller. Refreshes go through that client's pipeline. An idle entry keeps
        // this pipeline reachable until the entry is evicted.
        volatile SessionAcquirer latestAcquirer;
        private final Clock clock;
        private volatile SessionCredential fallback;

        private ContainerSessionCache(ContainerSessionProvider provider, Clock clock, String containerName,
            String resolvedAccountName, SessionAcquirer acquirer, OffsetDateTime lastAccess) {
            this.clock = clock;
            this.latestAcquirer = acquirer;
            this.cache = new AutoRefreshingCache<>(() -> {
                SessionCredential currentFallback = fallback;
                if (currentFallback != null && OffsetDateTime.now(clock).isBefore(currentFallback.getExpiresAt())) {
                    // Proactive refresh and service hints must not shorten a cached fallback's cooldown.
                    return Mono.just(currentFallback);
                }
                return provider.createSessionAsync(latestAcquirer, containerName, resolvedAccountName)
                    .doOnNext(ignored -> fallback = null)
                    .onErrorResume(error -> {
                        if (!shouldFallback(error)) {
                            return Mono.error(error);
                        }
                        SessionCredential newFallback
                            = SessionCredential.fallback(OffsetDateTime.now(clock).plus(DEFAULT_EXPIRATION_OFFSET));
                        fallback = newFallback;
                        return Mono.just(newFallback);
                    });
            }, SessionCredential::getExpiresAt, clock);
            this.lastAccess = lastAccess;
        }

        private Mono<SessionCredential> getSessionAsync() {
            SessionCredential currentFallback = fallback;
            if (currentFallback != null && !OffsetDateTime.now(clock).isBefore(currentFallback.getExpiresAt())) {
                cache.invalidateValue(currentFallback);
            }
            return cache.getValidValueAsync().filter(value -> !value.isFallback());
        }
    }
}
