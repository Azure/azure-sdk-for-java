// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper.CosmosClientAccessor;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CosmosClient with encryption support.
 */
public class CosmosEncryptionClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosEncryptionAsyncClient.class);
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;
    private final CosmosAsyncClient cosmosAsyncClient;
    private final CosmosClient cosmosClient;
    private final CosmosClientAccessor cosmosClientAccessor;

    CosmosEncryptionClient(CosmosClient cosmosClient, EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        this.cosmosClientAccessor = CosmosClientHelper.geCosmosClientAccessor();
        this.encryptionKeyStoreProvider = encryptionKeyStoreProvider;
        this.cosmosClient = cosmosClient;
        this.cosmosAsyncClient = this.cosmosClientAccessor.getCosmosAsyncClient(cosmosClient);
        this.cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(cosmosAsyncClient, encryptionKeyStoreProvider);
    }

    public EncryptionKeyStoreProvider getEncryptionKeyStoreProvider() {
        return encryptionKeyStoreProvider;
    }

    public CosmosClient getCosmosClient() {
        return cosmosClient;
    }

    /**
     * Create Cosmos Client with Encryption support for performing operations using client-side encryption.
     *
     * @param cosmosClient               Regular Cosmos Client.
     * @param encryptionKeyStoreProvider encryptionKeyStoreProvider, provider that allows interaction with the master
     *                                   keys.
     * @return encryptionCosmosClient to perform operations supporting client-side encryption / decryption.
     */
    public static CosmosEncryptionClient createCosmosEncryptionClient(CosmosClient cosmosClient,
                                                                      EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        return new CosmosEncryptionClient(cosmosClient, encryptionKeyStoreProvider);
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
}
