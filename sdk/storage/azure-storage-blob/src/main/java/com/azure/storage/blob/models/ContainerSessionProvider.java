// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.implementation.AzureBlobStorageImplBuilder;
import com.azure.storage.blob.implementation.models.AuthenticationType;
import com.azure.storage.blob.implementation.models.CreateSessionConfiguration;
import com.azure.storage.blob.implementation.models.CreateSessionResponse;
import com.azure.storage.blob.implementation.models.SessionCredentials;
import com.azure.storage.blob.implementation.accesshelpers.SessionProviderAccessHelper;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
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
 * An SDK-managed, thread-safe session provider for one storage account's Blob endpoint.
 * <p>
 * Share this instance through {@link SessionOptions#setSessionProvider(SessionProvider)} across independently built
 * Blob or Data Lake clients to retain per-container sessions, refresh state, and acquisition cooldowns when replacing
 * clients. All clients must target the same account endpoint and use the same network context as this provider
 * (including transport, proxy, and network-affecting policies).
 * <p>
 * Sessions are acquired on demand, cached per container, and refreshed in the background before expiration.
 * Inactive containers are evicted after five minutes. Acquisition failures eligible for bearer fallback are cached
 * for five minutes and shared by all clients using this provider. Rejected sessions are invalidated only if current.
 * <p>
 * Sessions remain disabled unless {@link SessionOptions.SessionMode#ENABLED} is selected on each client.
 * For custom domains, set {@link SessionOptions#setAccountName(String)} on each client.
 *
 * <p><strong>Share a provider across independent clients</strong></p>
 * <!-- src_embed com.azure.storage.blob.models.ContainerSessionProvider.share -->
 * <pre>
 * String endpoint = &quot;https:&#47;&#47;account.blob.core.windows.net&quot;;
 * HttpClient transport = HttpClient.createDefault&#40;&#41;;
 * BlobServiceClient acquisitionClient = new BlobServiceClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;.credential&#40;credential&#41;.httpClient&#40;transport&#41;.buildClient&#40;&#41;;
 * ContainerSessionProvider provider = new ContainerSessionProvider&#40;acquisitionClient&#41;;
 * SessionOptions sessions = new SessionOptions&#40;&#41;.setSessionMode&#40;SessionOptions.SessionMode.ENABLED&#41;
 *     .setSessionProvider&#40;provider&#41;;
 * BlobServiceClient first = new BlobServiceClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;.credential&#40;credential&#41;.httpClient&#40;transport&#41;.sessionOptions&#40;sessions&#41;.buildClient&#40;&#41;;
 * BlobServiceAsyncClient second = new BlobServiceClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;.credential&#40;credential&#41;.httpClient&#40;transport&#41;.sessionOptions&#40;sessions&#41;.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.models.ContainerSessionProvider.share -->
 */
public final class ContainerSessionProvider extends SessionProvider {

    static final int IDLE_EVICTION_THRESHOLD_MINUTES = 5;

    private static final ClientLogger LOGGER = new ClientLogger(ContainerSessionProvider.class);
    private static final Duration IDLE_EVICTION_THRESHOLD = Duration.ofMinutes(IDLE_EVICTION_THRESHOLD_MINUTES);
    // Defensive fallback expiration when the service response omits the expiration field.
    private static final Duration DEFAULT_EXPIRATION_OFFSET = Duration.ofMinutes(5L);
    private static final String FEATURE_NOT_ENABLED = "FeatureNotEnabled";

    private final AzureBlobStorageImpl azureBlobStorage;
    private final String accountName;
    private final String endpoint;
    private final Clock clock;
    private final ConcurrentHashMap<String, ContainerSessionCache> containerSessionCaches = new ConcurrentHashMap<>();

    static {
        SessionProviderAccessHelper.setFactory(ContainerSessionProvider::new);
    }

    /**
     * Creates a provider using the default HTTP transport and client configuration.
     * Container, blob, and query components are removed; supported account-path endpoints retain their account path.
     *
     * @param endpoint The storage account's HTTPS Blob endpoint.
     * @param credential The token credential used to acquire sessions.
     * @throws NullPointerException if endpoint or credential is null.
     * @throws IllegalArgumentException if the endpoint is invalid or does not use HTTPS.
     */
    public ContainerSessionProvider(String endpoint, TokenCredential credential) {
        this(new BlobServiceClientBuilder()
            .endpoint(BuilderHelper.getSessionEndpoint(Objects.requireNonNull(endpoint, "'endpoint' cannot be null.")))
            .credential(Objects.requireNonNull(credential, "'credential' cannot be null."))
            .buildClient());
    }

    /**
     * Creates a provider reusing a configured client's transport, policies, retry configuration, audience, and service
     * version. The client must use the SDK's token-credential authentication and have sessions disabled; anonymous,
     * shared-key, SAS, and session-authenticated pipelines are rejected.
     * <p>
     * To protect session secrets, HTTP logging policies (including custom HTTP loggers) are removed from the acquisition
     * pipeline. Logging on the supplied client is unchanged. Other custom policies are retained and must not log
     * session response bodies. The same provider serves both synchronous and asynchronous clients.
     *
     * @param client The configured, session-disabled Blob service client.
     * @throws NullPointerException if client is null.
     * @throws IllegalArgumentException if the client does not use a supported HTTPS OAuth pipeline.
     */
    public ContainerSessionProvider(BlobServiceClient client) {
        this(BuilderHelper.createSessionPipeline(Objects.requireNonNull(client, "'client' cannot be null.")),
            client.getAccountUrl(), client.getServiceVersion(), null, Clock.systemUTC());
    }

    ContainerSessionProvider(HttpPipeline bearerPipeline, String url, BlobServiceVersion serviceVersion,
        String accountName, Clock clock) {
        this.endpoint = BuilderHelper.getSessionEndpoint(url);
        this.azureBlobStorage = new AzureBlobStorageImplBuilder().pipeline(bearerPipeline)
            .url(endpoint)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.accountName = accountName;
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
            if (!endpoint.equalsIgnoreCase(BuilderHelper.getSessionEndpoint(request.getUrl().toString()))) {
                return false;
            }
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
            String resolvedAccount = resolveAccountName(context);
            return updateCache(container, resolvedAccount).getSessionAsync();
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

    private String resolveAccountName(SessionRequestContext context) {
        String contextAccountName = context == null ? null : context.getAccountName();
        String resolvedAccountName = CoreUtils.isNullOrEmpty(contextAccountName) ? accountName : contextAccountName;
        if (CoreUtils.isNullOrEmpty(resolvedAccountName)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The account name could not be resolved from the request URL."));
        }
        return resolvedAccountName;
    }

    private ContainerSessionCache updateCache(String containerName, String resolvedAccountName) {
        String key = normalize(resolvedAccountName) + "/" + normalize(containerName);
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

    private String cacheKey(SessionRequestContext context) {
        return normalize(resolveAccountName(context)) + "/" + normalize(context.getContainerName());
    }

    private static final class ContainerSessionCache {
        final AutoRefreshingCache<SessionCredential> cache;
        volatile OffsetDateTime lastAccess;
        private final Clock clock;
        private volatile SessionCredential fallback;

        private ContainerSessionCache(ContainerSessionProvider provider, Clock clock, String containerName,
            String resolvedAccountName, OffsetDateTime lastAccess) {
            this.clock = clock;
            this.cache = new AutoRefreshingCache<>(() -> {
                SessionCredential currentFallback = fallback;
                if (currentFallback != null && OffsetDateTime.now(clock).isBefore(currentFallback.getExpiresAt())) {
                    // Proactive refresh and service hints must not shorten a cached fallback's cooldown.
                    return Mono.just(currentFallback);
                }
                return provider.createSessionAsync(containerName, resolvedAccountName)
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
