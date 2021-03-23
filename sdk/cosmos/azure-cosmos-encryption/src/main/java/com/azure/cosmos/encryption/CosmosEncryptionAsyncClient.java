// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import reactor.core.publisher.Mono;

/**
 * CosmosClient with Encryption support.
 */
public class CosmosEncryptionAsyncClient {
    private final CosmosAsyncClient cosmosAsyncClient;
    private final AsyncCache<String, ClientEncryptionPolicy> clientEncryptionPolicyCacheByContainerId;
    private final AsyncCache<String, CosmosClientEncryptionKeyProperties> clientEncryptionKeyPropertiesCacheByKeyId;
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;

    CosmosEncryptionAsyncClient(CosmosAsyncClient cosmosAsyncClient,
                                EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        if (cosmosAsyncClient == null) {
            throw new IllegalArgumentException("cosmosClient is null");
        }
        if (encryptionKeyStoreProvider == null) {
            throw new IllegalArgumentException("encryptionKeyStoreProvider is null");
        }
        this.cosmosAsyncClient = cosmosAsyncClient;
        this.encryptionKeyStoreProvider = encryptionKeyStoreProvider;
        this.clientEncryptionKeyPropertiesCacheByKeyId = new AsyncCache<>();
        this.clientEncryptionPolicyCacheByContainerId = new AsyncCache<>();
    }

    public EncryptionKeyStoreProvider getEncryptionKeyStoreProvider() {
        return encryptionKeyStoreProvider;
    }

    Mono<ClientEncryptionPolicy> getClientEncryptionPolicyAsync(
        CosmosAsyncContainer container,
        boolean shouldForceRefresh) {
        // container Id is unique within a Database.
        String cacheKey =
            container.getDatabase().getId() + "/" + container.getId();

        // cache it against Database and Container ID key.
        if (!shouldForceRefresh) {
            return this.clientEncryptionPolicyCacheByContainerId.getAsync(
                cacheKey,
                null,
                () -> container.read().
                    map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy()));
        } else {
            return this.clientEncryptionPolicyCacheByContainerId.getAsync(
                cacheKey,
                null,
                () -> container.read().map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy()))
                .flatMap(clientEncryptionPolicy -> this.clientEncryptionPolicyCacheByContainerId.getAsync(
                    cacheKey,
                    clientEncryptionPolicy,
                    () -> container.read().map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy())));
        }
    }

    Mono<CosmosClientEncryptionKeyProperties> getClientEncryptionPropertiesAsync(
        String clientEncryptionKeyId,
        CosmosAsyncContainer cosmosAsyncContainer,
        boolean shouldForceRefresh) {
        /// Client Encryption key Id is unique within a Database.
        String cacheKey = cosmosAsyncContainer.getDatabase().getId() + "/" + clientEncryptionKeyId;
        if (!shouldForceRefresh) {
            return this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, null, () -> {
                return this.fetchClientEncryptionKeyPropertiesAsync(cosmosAsyncContainer,
                    clientEncryptionKeyId);
            });
        } else {
            return this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, null, () ->
                this.fetchClientEncryptionKeyPropertiesAsync(cosmosAsyncContainer,
                    clientEncryptionKeyId)
            ).flatMap(cachedClientEncryptionProperties -> this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, cachedClientEncryptionProperties, () ->
                this.fetchClientEncryptionKeyPropertiesAsync(cosmosAsyncContainer,
                    clientEncryptionKeyId)));
        }
    }

    Mono<CosmosClientEncryptionKeyProperties> fetchClientEncryptionKeyPropertiesAsync(
        CosmosAsyncContainer container,
        String clientEncryptionKeyId) {
        CosmosAsyncClientEncryptionKey clientEncryptionKey =
            container.getDatabase().getClientEncryptionKey(clientEncryptionKeyId);

        return clientEncryptionKey.read().map(cosmosClientEncryptionKeyResponse ->
            cosmosClientEncryptionKeyResponse.getProperties()
        ).onErrorResume(throwable -> Mono.error(new IllegalStateException("Encryption Based Container without Data " +
            "Encryption Keys. " +
            "Please make sure you have created the Client Encryption Keys", throwable)));
    }

    /**
     * Get the regular CosmosAsyncClient back.
     *
     * @return cosmosAsyncClient
     */
    public CosmosAsyncClient getCosmosAsyncClient() {
        return cosmosAsyncClient;
    }

    /**
     * Create Cosmos Client with Encryption support for performing operations using client-side encryption.
     *
     * @param cosmosAsyncClient          Regular Cosmos Client.
     * @param encryptionKeyStoreProvider encryptionKeyStoreProvider, provider that allows interaction with the master
     *                                   keys.
     * @return encryptionAsyncCosmosClient to perform operations supporting client-side encryption / decryption.
     */
    public static CosmosEncryptionAsyncClient createCosmosEncryptionAsyncClient(CosmosAsyncClient cosmosAsyncClient,
                                                                                EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        return new CosmosEncryptionAsyncClient(cosmosAsyncClient, encryptionKeyStoreProvider);
    }

    /**
     * Gets a database with Encryption capabilities
     *
     * @param cosmosAsyncDatabase original database
     * @return database with encryption capabilities
     */
    public CosmosEncryptionAsyncDatabase getCosmosEncryptionAsyncDatabase(CosmosAsyncDatabase cosmosAsyncDatabase) {
        return new CosmosEncryptionAsyncDatabase(cosmosAsyncDatabase, this);
    }

    /**
     * Gets a database with Encryption capabilities
     *
     * @param databaseId original database id
     * @return database with encryption capabilities
     */
    public CosmosEncryptionAsyncDatabase getCosmosEncryptionAsyncDatabase(String databaseId) {
        CosmosAsyncDatabase database = this.cosmosAsyncClient.getDatabase(databaseId);
        return new CosmosEncryptionAsyncDatabase(database, this);
    }

    /**
     * Close this {@link CosmosAsyncClient} instance and cleans up the resources.
     */
    public void close() {
        cosmosAsyncClient.close();
    }
}
