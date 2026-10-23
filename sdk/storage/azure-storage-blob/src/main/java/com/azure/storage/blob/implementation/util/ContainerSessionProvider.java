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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private static final ClientLogger LOGGER = new ClientLogger(ContainerSessionProvider.class);
    private static final Duration EVICTION_SWEEP_INTERVAL = Duration.ofMinutes(1);
    private static final Duration DEFAULT_EXPIRATION_OFFSET = Duration.ofMinutes(5L);
    private static final String FEATURE_NOT_ENABLED = "FeatureNotEnabled";

    private final Clock clock;
    private final ConcurrentHashMap<String, ContainerSessionCache> containerSessionCaches = new ConcurrentHashMap<>();
    private final Lock evictionSweepLock = new ReentrantLock();
    private volatile OffsetDateTime nextEvictionSweep;

    ContainerSessionProvider(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    @Override
    BlobUrlParts getEligibleRequestParts(HttpRequest request) {
        if (request == null || request.getHttpMethod() != HttpMethod.GET) {
            return null;
        }

        BlobUrlParts parts;
        try {
            parts = BlobUrlParts.parse(request.getUrl());
        } catch (RuntimeException ex) {
            return null;
        }

        if (CoreUtils.isNullOrEmpty(parts.getBlobContainerName())
            || CoreUtils.isNullOrEmpty(parts.getBlobName())
            || parts.getUnparsedParameters().containsKey("comp")
            || parts.getUnparsedParameters().containsKey("restype")) {
            return null;
        }

        if (request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-structured-body")) == null) {
            return parts;
        }
        return null;
    }

    @Override
    Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
        return Mono.defer(() -> {
            String container = requireContainerName(context);
            SessionAcquirer acquirer = requireAcquirer(context);
            // Reserve under the map's per-key lock, before subscribing outside it. Shield the reservation as well
            // as the shared acquisition from cancellation, including cancellation before the supplier is subscribed.
            return Mono
                .using(() -> reserveCache(container, acquirer.getAccountName(), context),
                    ContainerSessionCache::getSessionAsync, ContainerSessionCache::release, false)
                .cache();
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
        ContainerSessionCache containerSessionCache
            = containerSessionCaches.computeIfPresent(key, (ignored, cache) -> cache.retain());
        if (containerSessionCache != null) {
            try {
                containerSessionCache.cache.forceRefreshValueInBackground();
            } finally {
                containerSessionCache.release();
            }
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

    private ContainerSessionCache reserveCache(String containerName, String resolvedAccountName,
        SessionRequestContext requestContext) {
        String key = normalize(resolvedAccountName) + "/" + normalize(containerName);
        OffsetDateTime now = OffsetDateTime.now(clock);
        evictExpiredCaches(now);
        return containerSessionCaches.compute(key, (k, existing) -> {
            if (existing == null) {
                existing = new ContainerSessionCache(this, clock, containerName, resolvedAccountName, requestContext);
            }
            existing.latestRequestContext = requestContext;
            return existing.retain();
        });
    }

    private void evictExpiredCaches(OffsetDateTime now) {
        OffsetDateTime nextSweep = nextEvictionSweep;
        if (nextSweep != null && now.isBefore(nextSweep)) {
            return;
        }

        //using tryLock to avoid blocking the caller thread if another thread is already performing eviction sweep
        if (!evictionSweepLock.tryLock()) {
            return;
        }
        try {
            nextSweep = nextEvictionSweep;
            if (nextSweep != null && now.isBefore(nextSweep)) {
                return;
            }
            nextEvictionSweep = now.plus(EVICTION_SWEEP_INTERVAL);
            containerSessionCaches.keySet()
                .forEach(key -> containerSessionCaches.computeIfPresent(key,
                    (ignored, cache) -> cache.isExpired(now) ? null : cache));
        } finally {
            evictionSweepLock.unlock();
        }
    }

    private Mono<SessionCredential> createSessionAsync(SessionRequestContext requestContext, String container,
        String resolvedAccountName) {
        return requestContext.getAcquirer()
            .createSessionAsync(container, requestContext.getContext())
            .map(response -> toCredential(response, resolvedAccountName));
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
        return normalize(context.getAcquirer().getAccountName()) + "/" + normalize(context.getContainerName());
    }

    private static final class ContainerSessionCache {
        final AutoRefreshingCache<SessionCredential> cache;
        // The most recent caller's acquirer and Context are retained together so refreshes use one client's pipeline
        // and request data. The sweep removes entries with no active references and no unexpired session/cooldown.
        volatile SessionRequestContext latestRequestContext;
        private final Clock clock;
        private final AtomicInteger references = new AtomicInteger();
        private volatile SessionCredential fallback;
        private volatile OffsetDateTime expiration;

        private ContainerSessionCache(ContainerSessionProvider provider, Clock clock, String containerName,
            String resolvedAccountName, SessionRequestContext requestContext) {
            this.clock = clock;
            this.latestRequestContext = requestContext;
            this.cache = new AutoRefreshingCache<>(() -> Mono.using(this::retain, ignored -> {
                SessionCredential currentFallback = fallback;
                Mono<SessionCredential> acquisition;
                if (currentFallback != null && OffsetDateTime.now(clock).isBefore(currentFallback.getExpiresAt())) {
                    // Proactive refresh and service hints must not shorten a cached fallback's cooldown.
                    acquisition = Mono.just(currentFallback);
                } else {
                    SessionRequestContext latestContext = latestRequestContext;
                    acquisition = provider.createSessionAsync(latestContext, containerName, resolvedAccountName)
                        .doOnNext(credential -> fallback = null)
                        .onErrorResume(error -> {
                            if (!shouldFallback(error)) {
                                return Mono.error(error);
                            }
                            SessionCredential newFallback
                                = SessionCredential.fallback(OffsetDateTime.now(clock).plus(DEFAULT_EXPIRATION_OFFSET));
                            fallback = newFallback;
                            return Mono.just(newFallback);
                        });
                }
                return acquisition.doOnNext(value -> expiration = value.getExpiresAt());
            }, ContainerSessionCache::release, false), SessionCredential::getExpiresAt, clock);
        }

        private ContainerSessionCache retain() {
            references.incrementAndGet();
            return this;
        }

        private void release() {
            references.decrementAndGet();
        }

        private boolean isExpired(OffsetDateTime now) {
            if (references.get() != 0) {
                return false;
            }
            OffsetDateTime currentExpiration = expiration;
            return currentExpiration == null || !now.isBefore(currentExpiration);
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
