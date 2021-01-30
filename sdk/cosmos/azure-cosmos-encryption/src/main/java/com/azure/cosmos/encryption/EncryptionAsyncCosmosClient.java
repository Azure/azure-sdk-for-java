// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import reactor.core.publisher.Mono;

/**
 * CosmosClient with Encryption support.
 */
public class EncryptionAsyncCosmosClient {
    private final EncryptionCosmosAsyncContainer encryptionCosmosContainer;
    private final AsyncCache<String, ClientEncryptionPolicy> clientEncryptionPolicyCacheByContainerId;
    private final AsyncCache<String, CosmosClientEncryptionKeyProperties> clientEncryptionKeyPropertiesCacheByKeyId;
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;

    public EncryptionAsyncCosmosClient(EncryptionCosmosAsyncContainer encryptionCosmosContainer,
                                       EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        if (encryptionCosmosContainer == null) {
            throw new IllegalArgumentException("cosmosClient is null");
        }
        if (encryptionKeyStoreProvider == null) {
            throw new IllegalArgumentException("encryptionKeyStoreProvider is null");
        }

        this.encryptionCosmosContainer = encryptionCosmosContainer;
        this.encryptionKeyStoreProvider = encryptionKeyStoreProvider;
        this.clientEncryptionKeyPropertiesCacheByKeyId = new AsyncCache<>();
        this.clientEncryptionPolicyCacheByContainerId = new AsyncCache<>();
    }

    public EncryptionKeyStoreProvider getEncryptionKeyStoreProvider() {
        return encryptionKeyStoreProvider;
    }

    public Mono<ClientEncryptionPolicy> getClientEncryptionPolicyAsync(
        EncryptionCosmosAsyncContainer encryptionCosmosContainer,
        boolean shouldForceRefresh) {
        // container Id is unique within a Database.
        String cacheKey = encryptionCosmosContainer.getContainer().getDatabase().getId() + "/" + encryptionCosmosContainer.getContainer().getId();

        // cache it against Database and Container ID key.
        if (!shouldForceRefresh) {
            return this.clientEncryptionPolicyCacheByContainerId.getAsync(
                cacheKey,
                null,
                () -> encryptionCosmosContainer.getContainer().read().map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy()));
        } else {
            return this.clientEncryptionPolicyCacheByContainerId.getAsync(
                cacheKey,
                null,
                () -> encryptionCosmosContainer.getContainer().read().map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy()))
                .flatMap(clientEncryptionPolicy -> this.clientEncryptionPolicyCacheByContainerId.getAsync(
                    cacheKey,
                    clientEncryptionPolicy,
                    () -> encryptionCosmosContainer.getContainer().read().map(cosmosContainerResponse -> cosmosContainerResponse.getProperties().getClientEncryptionPolicy())));
        }
    }

    public Mono<CosmosClientEncryptionKeyProperties> getClientEncryptionPropertiesAsync(
        String clientEncryptionKeyId,
        EncryptionCosmosAsyncContainer encryptionCosmosContainer,
        boolean shouldForceRefresh) {
        /// Client Encryption key Id is unique within a Database.
        String cacheKey = encryptionCosmosContainer.getContainer().getDatabase().getId() + "/" + clientEncryptionKeyId;
        if (!shouldForceRefresh) {
            return this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, null, () ->
                this.fetchClientEncryptionKeyPropertiesAsync(encryptionCosmosContainer.getContainer(), clientEncryptionKeyId));
        } else {
            return this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, null, () ->
                this.fetchClientEncryptionKeyPropertiesAsync(encryptionCosmosContainer.getContainer(), clientEncryptionKeyId)
            ).flatMap(cachedClientEncryptionProperties -> this.clientEncryptionKeyPropertiesCacheByKeyId.getAsync(cacheKey, cachedClientEncryptionProperties, () ->
                this.fetchClientEncryptionKeyPropertiesAsync(encryptionCosmosContainer.getContainer(), clientEncryptionKeyId)));
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

    public EncryptionCosmosAsyncContainer getEncryptionAsyncCosmosClient() {
        return this.encryptionCosmosContainer;
    }
}
