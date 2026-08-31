// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.AuthenticationType;
import com.azure.storage.blob.implementation.models.CreateSessionConfiguration;
import com.azure.storage.blob.implementation.models.CreateSessionResponse;
import com.azure.storage.blob.implementation.models.SessionCredentials;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.common.implementation.util.AutoRefreshingCache;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in {@link SessionProvider} implementation that creates sessions via the CreateSession REST
 * API and manages their lifecycle: per-container caching, proactive background refresh, idle
 * eviction, and compare-and-invalidate race safety.
 *
 * <h2>Caching model</h2>
 * <p>
 * One {@link AutoRefreshingCache} of {@link SessionCredential} per container (keyed by a
 * lowercase-normalized name) is maintained, allowing a single {@link TokenCredentialSessionProvider} to serve
 * many containers without creating a new session for every request.  Entries are opportunistically
 * evicted once they have not been accessed for {@value #IDLE_EVICTION_THRESHOLD_MINUTES} minutes.
 *
 * <h2>Invalidation</h2>
 * <p>
 * {@link #invalidateSession} delegates to the cache's compare-and-swap: it succeeds only for the
 * caller presenting the credential that is still cached. A caller presenting a credential that a
 * concurrent refresh has already replaced gets {@code false} and the newer credential is left in
 * place, so a stale 401 cannot evict a healthy session.
 *
 * <h2>Background refresh</h2>
 * <p>
 * {@link #refreshSession} forces an immediate background refresh even when the client's own
 * jittered refresh timer has not yet elapsed, so the service's
 * {@code x-ms-auth-info: session_expiring} hint is acted on promptly. The one exception is that the
 * refresh is suppressed while the cache is backing off from a recent session creation failure, which
 * stops a failing service from being retried once per request.
 *
 * <p>
 * Follows the same constructor pattern as {@link com.azure.storage.blob.BlobContainerClient}:
 * takes an {@link HttpPipeline} (bearer-only, no session policy) and builds an
 * {@link AzureBlobStorageImpl} internally.
 */
final class TokenCredentialSessionProvider implements SessionProvider {

    static final int IDLE_EVICTION_THRESHOLD_MINUTES = 5;

    private static final ClientLogger LOGGER = new ClientLogger(TokenCredentialSessionProvider.class);
    private static final Duration IDLE_EVICTION_THRESHOLD = Duration.ofMinutes(IDLE_EVICTION_THRESHOLD_MINUTES);
    // Defensive fallback expiration for a malformed/absent service response.
    private static final Duration DEFAULT_EXPIRATION_OFFSET = Duration.ofMinutes(5L);

    private final AzureBlobStorageImpl azureBlobStorage;
    private final String accountName;
    private final Clock clock;
    private final ConcurrentHashMap<String, ContainerSessionCache> containerSessionCaches = new ConcurrentHashMap<>();

    TokenCredentialSessionProvider(HttpPipeline bearerPipeline, String url, BlobServiceVersion serviceVersion,
        String accountName) {
        this(bearerPipeline, url, serviceVersion, accountName, Clock.systemUTC());
    }

    /** Package-private constructor that accepts an injectable clock for deterministic testing. */
    TokenCredentialSessionProvider(HttpPipeline bearerPipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, Clock clock) {
        this.azureBlobStorage = new AzureBlobStorageImplBuilder().pipeline(bearerPipeline)
            .url(url)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.accountName = accountName;
        this.clock = Objects.requireNonNull(clock, "'clock' cannot be null.");
    }

    @Override
    public Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
        return Mono.defer(() -> {
            String container = requireContainerName(context);
            String resolvedAccount = resolveAccountName(context);
            return updateCache(container, resolvedAccount).cache.getValidValueAsync();
        });
    }

    @Override
    public SessionCredential getSession(SessionRequestContext context) {
        String container = requireContainerName(context);
        String resolvedAccount = resolveAccountName(context);
        return updateCache(container, resolvedAccount).cache.getValidValueSync();
    }

    @Override
    public boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential) {
        if (context == null) {
            return false;
        }
        ContainerSessionCache containerSessionCache = containerSessionCaches.get(normalize(context.getContainerName()));
        return containerSessionCache != null && containerSessionCache.cache.invalidateValue(rejectedCredential);
    }

    @Override
    public void refreshSession(SessionRequestContext context) {
        if (context == null) {
            return;
        }
        String key = normalize(context.getContainerName());
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

    private String resolveAccountName(SessionRequestContext context) {
        String contextAccountName = context == null ? null : context.getAccountName();
        String resolvedAccountName = CoreUtils.isNullOrEmpty(accountName) ? contextAccountName : accountName;
        if (CoreUtils.isNullOrEmpty(resolvedAccountName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The account name could not be resolved from the request URL."));
        }
        return resolvedAccountName;
    }

    private ContainerSessionCache updateCache(String containerName, String resolvedAccountName) {
        String key = normalize(containerName);
        OffsetDateTime now = OffsetDateTime.now(clock);
        ContainerSessionCache containerSessionCache = containerSessionCaches.compute(key, (k, existing) -> {
            if (existing == null) {
                return new ContainerSessionCache(this, clock, containerName, resolvedAccountName, now);
            }
            existing.lastAccess = now;
            return existing;
        });
        evictStaleCaches();
        return containerSessionCache;
    }

    private void evictStaleCaches() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        containerSessionCaches.forEach((key, cache) -> {
            if (Duration.between(cache.lastAccess, now).compareTo(IDLE_EVICTION_THRESHOLD) >= 0) {
                containerSessionCaches.remove(key, cache);
            }
        });
    }

    private Mono<SessionCredential> createSessionAsync(String container, String resolvedAccountName) {
        CreateSessionConfiguration config
            = new CreateSessionConfiguration().setAuthenticationType(AuthenticationType.HMAC);
        return azureBlobStorage.getContainers()
            .createSessionWithResponseAsync(container, config, null, null)
            .map(response -> toCredential(response, resolvedAccountName));
    }

    private SessionCredential createSessionSync(String container, String resolvedAccountName) {
        CreateSessionConfiguration config
            = new CreateSessionConfiguration().setAuthenticationType(AuthenticationType.HMAC);
        Response<CreateSessionResponse> response
            = azureBlobStorage.getContainers().createSessionWithResponse(container, config, null, null, Context.NONE);
        return toCredential(response, resolvedAccountName);
    }

    private SessionCredential toCredential(Response<CreateSessionResponse> response, String resolvedAccountName) {
        CreateSessionResponse session = response.getValue();
        if (session == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("CreateSession response did not contain a session payload."));
        }

        SessionCredentials creds = session.getCredentials();
        if (creds == null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("CreateSession response did not contain HMAC session credentials."));
        }

        OffsetDateTime expiration = session.getExpiration();
        if (expiration == null) {
            expiration = OffsetDateTime.now(clock).plus(DEFAULT_EXPIRATION_OFFSET);
        }
        return new SessionCredential(creds.getSessionToken(), creds.getSessionKey(), expiration, resolvedAccountName);
    }

    private static String normalize(String name) {
        return CoreUtils.isNullOrEmpty(name) ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static final class ContainerSessionCache {
        final AutoRefreshingCache<SessionCredential> cache;
        volatile OffsetDateTime lastAccess;

        private ContainerSessionCache(TokenCredentialSessionProvider provider, Clock clock, String containerName,
            String resolvedAccountName, OffsetDateTime lastAccess) {
            this.cache = createCache(provider, clock, containerName, resolvedAccountName);
            this.lastAccess = lastAccess;
        }

        private static AutoRefreshingCache<SessionCredential> createCache(TokenCredentialSessionProvider provider,
            Clock clock, String containerName, String resolvedAccountName) {
            AutoRefreshingCache.ValueProvider<SessionCredential> valueProvider
                = new AutoRefreshingCache.ValueProvider<SessionCredential>() {
                    @Override
                    public Mono<SessionCredential> createAsync() {
                        return provider.createSessionAsync(containerName, resolvedAccountName);
                    }

                    @Override
                    public SessionCredential createSync() {
                        return provider.createSessionSync(containerName, resolvedAccountName);
                    }
                };
            return new AutoRefreshingCache<>(valueProvider, SessionCredential::getExpiresAt, clock);
        }

    }
}
