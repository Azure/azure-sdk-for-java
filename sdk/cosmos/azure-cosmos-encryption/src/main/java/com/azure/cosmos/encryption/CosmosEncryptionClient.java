// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.annotation.ServiceClient;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.encryption.keyprovider.EncryptionKeyWrapProvider;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper.CosmosClientAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * CosmosClient with encryption support.
 * We have static method in this class which will takes two inputs
 * {@link CosmosClient} and {@link EncryptionKeyWrapProvider}  and creates cosmosEncryptionClient as shown below.
 * <pre>
 * {@code
 * CosmosEncryptionClient cosmosEncryptionClient =
 * CosmosEncryptionClient.createCosmosEncryptionClient(cosmosClient, encryptionKeyWrapProvider);
 * }
 * </pre>
 */
@ServiceClient(builder = CosmosEncryptionClientBuilder.class)
public final class CosmosEncryptionClient implements Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosEncryptionAsyncClient.class);
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private EncryptionKeyWrapProvider encryptionKeyWrapProvider;
    private final CosmosAsyncClient cosmosAsyncClient;
    private final CosmosClient cosmosClient;
    private final CosmosClientAccessor cosmosClientAccessor;

    CosmosEncryptionClient(CosmosClient cosmosClient, EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        this.cosmosClientAccessor = CosmosClientHelper.geCosmosClientAccessor();
        this.encryptionKeyWrapProvider = encryptionKeyWrapProvider;
        this.cosmosClient = cosmosClient;
        this.cosmosAsyncClient = this.cosmosClientAccessor.getCosmosAsyncClient(cosmosClient);
        this.cosmosEncryptionAsyncClient = new CosmosEncryptionAsyncClient(cosmosAsyncClient, encryptionKeyWrapProvider);
    }

    /**
     * @return the encryption key wrap provider
     */
    public EncryptionKeyWrapProvider getEncryptionKeyWrapProvider() {
        return encryptionKeyWrapProvider;
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
