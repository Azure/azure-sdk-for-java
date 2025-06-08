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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private final Queue<ClientCleanupItem> toBeCleanedQueue;
    private final ScheduledExecutorService cleanupExecutor;

    private CosmosClientCache() {
        this.clientCache = new ConcurrentHashMap<>();
        this.toBeCleanedQueue = new ConcurrentLinkedQueue<>();
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
    public static CosmosAsyncClient getCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName) {
        return getCosmosClient(accountConfig, sourceName, null);
    }

    /**
     * Gets a Cosmos client with the given configuration and metadata snapshot.
     */
    public static CosmosAsyncClient getCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName,
        CosmosClientMetadataCachesSnapshot snapshot) {
        return getInstance().getOrCreateClient(accountConfig, sourceName, snapshot);
    }

    /**
     * Gets or creates a cached client for the given configuration.
     */
    public synchronized CosmosAsyncClient getOrCreateClient(
        CosmosAccountConfig accountConfig, 
        String context) {
        return getOrCreateClient(accountConfig, context, null);
    }

    /**
     * Gets or creates a cached client for the given configuration and metadata snapshot.
     */
    private synchronized CosmosAsyncClient getOrCreateClient(
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
            return metadata.getClient();
        }

        CosmosAsyncClient newClient = createCosmosClient(accountConfig, context, snapshot);
        metadata = new CosmosClientCacheMetadata(newClient, Instant.now());
        clientCache.put(cacheConfig, metadata);
        return newClient;
    }

    /**
     * Creates a new Cosmos client with the given configuration.
     */
    private CosmosAsyncClient createCosmosClient(
        CosmosAccountConfig accountConfig,
        String sourceName,
        CosmosClientMetadataCachesSnapshot snapshot) {

        if (accountConfig == null) {
            return null;
        }

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
    public synchronized void releaseClient(CosmosAccountConfig accountConfig, String context) {
        if (accountConfig == null) {
            return;
        }

        CosmosClientCacheConfig cacheConfig = new CosmosClientCacheConfig(
            accountConfig.getEndpoint(),
            accountConfig.getCosmosAuthConfig(),
            accountConfig.getApplicationName(),
            accountConfig.isUseGatewayMode(),
            accountConfig.getPreferredRegionsList(),
            context);

        CosmosClientCacheMetadata metadata = clientCache.get(cacheConfig);

        if (metadata != null) {
            metadata.decrementRefCount();
            checkForCleanup(cacheConfig, metadata);
        }
    }

    /**
     * Check if a client should be added to cleanup queue.
     */
    private synchronized void checkForCleanup(CosmosClientCacheConfig config, CosmosClientCacheMetadata metadata) {
        if (metadata.getRefCount() == 0) {
            long now = Instant.now().toEpochMilli();
            if (now - metadata.getLastAccessed().toEpochMilli() > UNUSED_CLIENT_TTL_IN_MS) {
                toBeCleanedQueue.offer(new ClientCleanupItem(config, metadata));
            }
        }
    }

    /**
     * Releases a client from the cache.
     */
    public static void releaseCosmosClient(CosmosAccountConfig accountConfig, String sourceName) {
        getInstance().releaseClient(accountConfig, sourceName);
    }

    private void cleanup() {
        try {
            LOGGER.debug("Starting cleanup of unused clients");
            
            while (!toBeCleanedQueue.isEmpty()) {
                ClientCleanupItem item = toBeCleanedQueue.poll();
                if (item != null) {
                    synchronized (this) {
                        // Double-check that the client is still unused and eligible for cleanup
                        CosmosClientCacheMetadata currentMetadata = clientCache.get(item.config);
                        if (currentMetadata == item.metadata && 
                            currentMetadata.getRefCount() == 0 &&
                            (Instant.now().toEpochMilli() - currentMetadata.getLastAccessed().toEpochMilli() > UNUSED_CLIENT_TTL_IN_MS)) {
                            
                            LOGGER.info("Closing unused client that has been idle for more than {} minutes",
                                UNUSED_CLIENT_TTL_IN_MS / (60 * 1000));
                            currentMetadata.close();
                            clientCache.remove(item.config);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error during client cache cleanup", e);
        }
    }

    @Override
    public synchronized void close() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close all clients
        for (CosmosClientCacheMetadata metadata : clientCache.values()) {
            metadata.close();
        }
        clientCache.clear();
        toBeCleanedQueue.clear();
    }

    /**
     * Represents a client that is queued for cleanup.
     */
    private static class ClientCleanupItem {
        private final CosmosClientCacheConfig config;
        private final CosmosClientCacheMetadata metadata;

        ClientCleanupItem(CosmosClientCacheConfig config, CosmosClientCacheMetadata metadata) {
            this.config = config;
            this.metadata = metadata;
        }
    }
}
