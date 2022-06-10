// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper.CosmosClientAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * Provides a client-side logical representation of the Azure Cosmos DB service.
 * Calls to CosmosClient API's are blocked for completion.
 */
@ServiceClient(builder = CosmosEncryptionClientBuilder.class)
public final class CosmosEncryptionClient implements Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosEncryptionClient.class);
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private final KeyEncryptionKeyResolver keyEncryptionKeyResolver;
    private final CosmosAsyncClient cosmosAsyncClient;
    private final CosmosClient cosmosClient;
    private final CosmosClientAccessor cosmosClientAccessor;
    private final String keyEncryptionKeyResolverName;

    CosmosEncryptionClient(CosmosClient cosmosClient, KeyEncryptionKeyResolver keyEncryptionKeyResolver,
                           String keyEncryptionKeyResolverName) {
        this.cosmosClientAccessor = CosmosClientHelper.getCosmosClientAccessor();
        this.keyEncryptionKeyResolver = keyEncryptionKeyResolver;
        this.cosmosClient = cosmosClient;
        this.cosmosAsyncClient = this.cosmosClientAccessor.getCosmosAsyncClient(cosmosClient);
        this.cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(cosmosAsyncClient, keyEncryptionKeyResolver,
            keyEncryptionKeyResolverName);
        this.keyEncryptionKeyResolverName = keyEncryptionKeyResolverName;
    }

    /**
     * @return the key encryption key resolver
     */
    public KeyEncryptionKeyResolver getEncryptionKeyWrapProvider() {
        return this.keyEncryptionKeyResolver;
    }

    /**
     * @return the key encryption key resolver name
     */
    public String getKeyEncryptionKeyResolverName() {
        return keyEncryptionKeyResolverName;
    }

    /**
     * @return the Cosmos client
     */
    public CosmosClient getCosmosClient() {
        return cosmosClient;
    }

    /**
     * Gets a database with Encryption capabilities
     *
     * @param databaseId original database id
     * @return database with encryption capabilities
     */
    public CosmosEncryptionDatabase getCosmosEncryptionDatabase(String databaseId) {
        CosmosDatabase syncDatabase = this.cosmosClient.getDatabase(databaseId);
        CosmosAsyncDatabase asyncDatabase = this.cosmosAsyncClient.getDatabase(databaseId);
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = new CosmosEncryptionAsyncDatabase(asyncDatabase
            , this.getCosmosEncryptionAsyncClient());
        return new CosmosEncryptionDatabase(syncDatabase, cosmosEncryptionAsyncDatabase);
    }

    /**
     * Gets a database with Encryption capabilities
     *
     * @param cosmosDatabase original database
     * @return database with encryption capabilities
     */
    public CosmosEncryptionDatabase getCosmosEncryptionDatabase(CosmosDatabase cosmosDatabase) {
        CosmosAsyncDatabase asyncDatabase = this.cosmosAsyncClient.getDatabase(cosmosDatabase.getId());
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = new CosmosEncryptionAsyncDatabase(asyncDatabase
            , this.getCosmosEncryptionAsyncClient());
        return new CosmosEncryptionDatabase(cosmosDatabase, cosmosEncryptionAsyncDatabase);
    }

    CosmosEncryptionAsyncClient getCosmosEncryptionAsyncClient() {
        return cosmosEncryptionAsyncClient;
    }

    /**
     * Close this {@link CosmosClient} instance and cleans up the resources.
     */
    @Override
    public void close() {
        cosmosClient.close();
    }
}
