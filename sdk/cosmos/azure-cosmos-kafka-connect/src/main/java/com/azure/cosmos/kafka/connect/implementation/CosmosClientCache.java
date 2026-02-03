// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * A thread-safe cache for CosmosDB client instances.
 */
public class CosmosClientCache implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosClientCache.class);
    private static final CosmosClientCache INSTANCE = new CosmosClientCache();
    private static final long UNUSED_CLIENT_TTL_IN_MS = 15 * 60 * 1000; // 15 minutes
    private static final long CLEANUP_INTERVAL_IN_SECONDS = 60; // 1 minute

    private final Map<CosmosClientCacheConfig, CosmosClientCacheMetadata> clientCache;
    private final Map<CosmosClientCacheConfig, CosmosClientCacheMetadata> toBeCleanedMap;
    private final ScheduledExecutorService cleanupExecutor;

    public CosmosClientCache() {
        this.clientCache = new ConcurrentHashMap<>();
        this.toBeCleanedMap = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CosmosClientCache-Cleanup");
            t.setDaemon(true);
            return t;
        });

        this.cleanupExecutor.scheduleWithFixedDelay(
            this::cleanup,
            CLEANUP_INTERVAL_IN_SECONDS,
            CLEANUP_INTERVAL_IN_SECONDS,
            TimeUnit.SECONDS);
    }

    public static CosmosClientCache getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a Cosmos client with the given configuration.
     */
    public static CosmosClientCacheItem getCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName) {
        checkNotNull(accountConfig, "Argument 'accountConfig' must not be null");

        return getCosmosClient(accountConfig, sourceName, null);
    }

    /**
     * Gets a Cosmos client with the given configuration and metadata snapshot.
     */
    public static CosmosClientCacheItem getCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName,
        CosmosClientMetadataCachesSnapshot snapshot) {
        checkNotNull(accountConfig, "Argument 'accountConfig' must not be null");

        return getInstance().getOrCreateClient(accountConfig, sourceName, snapshot);
    }

    /**
     * Gets or creates a cached client for the given configuration and metadata snapshot.
     */
    synchronized CosmosClientCacheItem getOrCreateClient(
        CosmosAccountConfig accountConfig,
        String context,
        CosmosClientMetadataCachesSnapshot snapshot) {

        checkNotNull(accountConfig, "Argument 'accountConfig' must not be null");

        CosmosClientCacheConfig cacheConfig = new CosmosClientCacheConfig(
            accountConfig.getEndpoint(),
            accountConfig.getCosmosAuthConfig(),
            accountConfig.getApplicationName(),
            accountConfig.isUseGatewayMode(),
            accountConfig.getPreferredRegionsList(),
            context);

        CosmosClientCacheMetadata metadata = clientCache.get(cacheConfig);

        if (metadata != null) {
            metadata.updateLastAccessed();
            metadata.incrementRefCount();

            return new CosmosClientCacheItem(cacheConfig, metadata);
        }

        CosmosAsyncClient newClient = createCosmosClient(accountConfig, context, snapshot);
        metadata = new CosmosClientCacheMetadata(newClient, Instant.now());
        clientCache.put(cacheConfig, metadata);

        return new CosmosClientCacheItem(cacheConfig, metadata);
    }

    /**
     * Creates a new Cosmos client with the given configuration.
     */
    private CosmosAsyncClient createCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName,
        CosmosClientMetadataCachesSnapshot snapshot) {

        checkNotNull(accountConfig, "Argument 'accountConfig' must not be null");

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(accountConfig.getEndpoint())
            .preferredRegions(accountConfig.getPreferredRegionsList())
            .throttlingRetryOptions(
                new ThrottlingRetryOptions()
                    .setMaxRetryAttemptsOnThrottledRequests(Integer.MAX_VALUE)
                    .setMaxRetryWaitTime(Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1)))
            .userAgentSuffix(getUserAgentSuffix(accountConfig, sourceName));

        if (accountConfig.isUseGatewayMode()) {
            cosmosClientBuilder.gatewayMode(new GatewayConnectionConfig().setMaxConnectionPoolSize(10000));
        }

        if (accountConfig.getCosmosAuthConfig() instanceof CosmosMasterKeyAuthConfig) {
            cosmosClientBuilder.key(((CosmosMasterKeyAuthConfig) accountConfig.getCosmosAuthConfig()).getMasterKey());
        } else if (accountConfig.getCosmosAuthConfig() instanceof CosmosAadAuthConfig) {
            CosmosAadAuthConfig aadAuthConfig = (CosmosAadAuthConfig) accountConfig.getCosmosAuthConfig();
            ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .authorityHost(aadAuthConfig.getAuthEndpoint())
                .tenantId(aadAuthConfig.getTenantId())
                .clientId(aadAuthConfig.getClientId())
                .clientSecret(aadAuthConfig.getClientSecret())
                .build();
            cosmosClientBuilder.credential(tokenCredential);
        } else {
            throw new IllegalArgumentException("Authorization type " + accountConfig.getCosmosAuthConfig().getClass() + " is not supported");
        }

        if (snapshot != null) {
            ImplementationBridgeHelpers.CosmosClientBuilderHelper
                .getCosmosClientBuilderAccessor()
                .setCosmosClientMetadataCachesSnapshot(cosmosClientBuilder, snapshot);
        }

        return cosmosClientBuilder.buildAsyncClient();
    }

    private String getUserAgentSuffix(CosmosAccountConfig accountConfig, String sourceName) {
        checkNotNull(accountConfig, "Argument 'accountConfig' must not be null");

        String userAgentSuffix = KafkaCosmosConstants.USER_AGENT_SUFFIX;
        if (StringUtils.isNotEmpty(sourceName)) {
            userAgentSuffix += "|" + sourceName;
        }

        if (StringUtils.isNotEmpty(accountConfig.getApplicationName())) {
            userAgentSuffix += "|" + accountConfig.getApplicationName();
        }

        return userAgentSuffix;
    }

    /**
     * Releases a client from use. When reference count reaches 0, the client becomes eligible for cleanup.
     */
    public synchronized void releaseClient(CosmosClientCacheConfig cacheConfig) {
        checkNotNull(cacheConfig, "Argument 'cacheConfig' must not be null");

        CosmosClientCacheMetadata metadata = clientCache.get(cacheConfig);

        if (metadata != null) {
            long currentRefCount = metadata.decrementRefCount();
            if (currentRefCount < 0) {
                LOGGER.warn("Released Cosmos client more than it referenced");
            }
        }
    }

    /**
     * Releases a client from the cache.
     */
    public static void releaseCosmosClient(CosmosClientCacheConfig cacheConfig) {
        checkNotNull(cacheConfig, "Argument 'cacheConfig' must not be null");

        getInstance().releaseClient(cacheConfig);
    }

    private void cleanup() {
        try {
            LOGGER.debug("Starting cleanup of unused clients");

            // First check clientCache for unused clients
            synchronized (this) {
                Set<CosmosClientCacheConfig> unusedConfigs = clientCache.entrySet().stream()
                    .filter(entry -> {
                        CosmosClientCacheMetadata metadata = entry.getValue();
                        return shouldPurgeClient(metadata);
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

                // Move unused clients to toBeCleanedMap
                for (CosmosClientCacheConfig config : unusedConfigs) {
                    purgeClient(config);
                }
            }

            // Clean up clients in toBeCleanedMap
            toBeCleanedMap.entrySet().removeIf(entry -> {
                CosmosClientCacheMetadata metadata = entry.getValue();
                LOGGER.info("Closing client from cleanup queue");
                // only close the client when the ref count is 0
                if (metadata.getRefCount() <= 0) {
                    metadata.close();
                    return true;
                }

                return false;
            });
        } catch (Exception e) {
            LOGGER.error("Error during client cache cleanup", e);
        }
    }

    private boolean shouldPurgeClient(CosmosClientCacheMetadata cacheClientMetadata) {
        return cacheClientMetadata.getRefCount() <= 0
                && (Instant.now().toEpochMilli() - cacheClientMetadata.getLastAccessed().toEpochMilli() >= UNUSED_CLIENT_TTL_IN_MS);
    }

    void purgeClient(CosmosClientCacheConfig cacheClientConfig) {
        CosmosClientCacheMetadata metadata = clientCache.remove(cacheClientConfig);
        if (metadata != null) {
            LOGGER.info("Moving unused client to cleanup queue after being idle for more than {} minutes",
                UNUSED_CLIENT_TTL_IN_MS / (60 * 1000));
            toBeCleanedMap.put(cacheClientConfig, metadata);
        }
    }

    @Override
    public synchronized void close() {
        if (this.cleanupExecutor != null) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Close all clients
        for (CosmosClientCacheMetadata metadata : clientCache.values()) {
            metadata.close();
        }
        for (CosmosClientCacheMetadata metadata : toBeCleanedMap.values()) {
            metadata.close();
        }
        clientCache.clear();
        toBeCleanedMap.clear();
    }
}
