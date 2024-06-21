// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>
 * The Simple Token Cache offers a basic in-memory token caching mechanism. It is designed to help improve
 * performance and reduce the number of token requests made to Azure services during application runtime.
 * </p>
 *
 * <p>
 * When using Azure services that require authentication, such as Azure Storage or Azure Key Vault, the library
 * handles the acquisition and management of access tokens. By default, each request made to an Azure service triggers
 * a token request, which involves authentication and token retrieval from the authentication provider
 * (e.g., Azure Active Directory).
 * </p>
 *
 * <p>
 * The Simple Token Cache feature caches the access tokens retrieved from the authentication provider in memory
 * for a certain period. This caching mechanism helps reduce the overhead of repeated token requests, especially when
 * multiple requests are made within a short time frame.
 * </p>
 *
 * <p>
 * The Simple Token Cache is designed for simplicity and ease of use. It automatically handles token expiration
 * and refreshing. When a cached token is about to expire, the SDK automatically attempts to refresh it by requesting
 * a new token from the authentication provider. The cached tokens are associated with a specific Azure resource or
 * scope and are used for subsequent requests to that resource.
 * </p>
 *
 * <p>
 * <strong>Sample: Azure SAS Authentication</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link com.azure.core.credential.SimpleTokenCache}.
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.simpleTokenCache -->
 * <pre>
 * SimpleTokenCache simpleTokenCache =
 *     new SimpleTokenCache&#40;&#40;&#41; -&gt; &#123;
 *         &#47;&#47; Your logic to retrieve access token goes here.
 *         return Mono.just&#40;new AccessToken&#40;&quot;dummy-token&quot;, OffsetDateTime.now&#40;&#41;.plusHours&#40;2&#41;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.credential.simpleTokenCache -->
 *
 * @see com.azure.core.credential
 * @see com.azure.core.credential.TokenCredential
 */
public class SimpleTokenCache {
    // The delay after a refresh to attempt another token refresh
    private static final Duration REFRESH_DELAY = Duration.ofSeconds(30);
    private static final String REFRESH_DELAY_STRING = String.valueOf(REFRESH_DELAY.getSeconds());
    // the offset before token expiry to attempt proactive token refresh
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    // SimpleTokenCache is commonly used, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(SimpleTokenCache.class);

    private final AtomicReference<Sinks.One<AccessToken>> wip;
    private volatile AccessToken cache;
    private volatile OffsetDateTime nextTokenRefresh = OffsetDateTime.now();
    private final Supplier<Mono<AccessToken>> tokenSupplier;
    private final Predicate<AccessToken> shouldRefresh;

    /**
     * Creates an instance of RefreshableTokenCredential with default scheme "Bearer".
     *
     * @param tokenSupplier a method to get a new token
     */
    public SimpleTokenCache(Supplier<Mono<AccessToken>> tokenSupplier) {
        this.wip = new AtomicReference<>();
        this.tokenSupplier = tokenSupplier;
        this.shouldRefresh
            = accessToken -> OffsetDateTime.now().isAfter(accessToken.getExpiresAt().minus(REFRESH_OFFSET));
    }

    /**
     * Asynchronously get a token from either the cache or replenish the cache with a new token.
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> getToken() {
        return Mono.defer(() -> {
            try {
                if (wip.compareAndSet(null, Sinks.one())) {
                    final Sinks.One<AccessToken> sinksOne = wip.get();
                    OffsetDateTime now = OffsetDateTime.now();
                    Mono<AccessToken> tokenRefresh;
                    Mono<AccessToken> fallback;
                    if (cache != null && !shouldRefresh.test(cache)) {
                        // fresh cache & no need to refresh
                        tokenRefresh = Mono.empty();
                        fallback = Mono.just(cache);
                    } else if (cache == null || cache.isExpired()) {
                        // no token to use
                        if (now.isAfter(nextTokenRefresh)) {
                            // refresh immediately
                            tokenRefresh = Mono.defer(tokenSupplier);
                        } else {
                            // wait for timeout, then refresh
                            tokenRefresh
                                = Mono.defer(tokenSupplier).delaySubscription(Duration.between(now, nextTokenRefresh));
                        }
                        // cache doesn't exist or expired, no fallback
                        fallback = Mono.empty();
                    } else {
                        // token available, but close to expiry
                        if (now.isAfter(nextTokenRefresh)) {
                            // refresh immediately
                            tokenRefresh = Mono.defer(tokenSupplier);
                        } else {
                            // still in timeout, do not refresh
                            tokenRefresh = Mono.empty();
                        }
                        // cache hasn't expired, ignore refresh error this time
                        fallback = Mono.just(cache);
                    }

                    return Mono.using(() -> wip, ignored -> tokenRefresh.materialize().flatMap(signal -> {
                        AccessToken accessToken = signal.get();
                        Throwable error = signal.getThrowable();
                        if (signal.isOnNext() && accessToken != null) { // SUCCESS
                            buildTokenRefreshLog(LogLevel.INFORMATIONAL, cache, now).log("Acquired a new access token");
                            cache = accessToken;
                            sinksOne.tryEmitValue(accessToken);
                            nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                            return Mono.just(accessToken);
                        } else if (signal.isOnError() && error != null) { // ERROR
                            buildTokenRefreshLog(LogLevel.ERROR, cache, now)
                                .log("Failed to acquire a new access token");
                            nextTokenRefresh = OffsetDateTime.now().plus(REFRESH_DELAY);
                            return fallback.switchIfEmpty(Mono.error(() -> error));
                        } else { // NO REFRESH
                            sinksOne.tryEmitEmpty();
                            return fallback;
                        }
                    }).doOnError(sinksOne::tryEmitError), w -> w.set(null));
                } else if (cache != null && !cache.isExpired()) {
                    // another thread might be refreshing the token proactively, but the current token is still valid
                    return Mono.just(cache);
                } else {
                    // another thread is definitely refreshing the expired token
                    Sinks.One<AccessToken> sinksOne = wip.get();
                    if (sinksOne == null) {
                        // the refreshing thread has finished
                        return Mono.just(cache);
                    } else {
                        // wait for refreshing thread to finish but defer to updated cache in case just missed onNext()
                        return sinksOne.asMono().switchIfEmpty(Mono.fromSupplier(() -> cache));
                    }
                }
            } catch (Exception t) {
                return Mono.error(t);
            }
        });
    }

    Sinks.One<AccessToken> getWipValue() {
        return wip.get();
    }

    private static LoggingEventBuilder buildTokenRefreshLog(LogLevel level, AccessToken cache, OffsetDateTime now) {
        LoggingEventBuilder logBuilder = LOGGER.atLevel(level);
        if (cache == null || !LOGGER.canLogAtLevel(level)) {
            return logBuilder;
        }

        Duration tte = Duration.between(now, cache.getExpiresAt());
        return logBuilder.addKeyValue("expiresAt", cache.getExpiresAt())
            .addKeyValue("tteSeconds", String.valueOf(tte.abs().getSeconds()))
            .addKeyValue("retryAfterSeconds", REFRESH_DELAY_STRING)
            .addKeyValue("expired", tte.isNegative());
    }
}
