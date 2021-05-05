// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * CosmosClient with encryption support.
 */
public class CosmosEncryptionClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(CosmosEncryptionAsyncClient.class);
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;
    private final CosmosAsyncClient cosmosAsyncClient;
    private final CosmosClient cosmosClient;

    CosmosEncryptionClient(CosmosClient cosmosClient, EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        this.encryptionKeyStoreProvider = encryptionKeyStoreProvider;
        this.cosmosClient = cosmosClient;
        this.cosmosAsyncClient = CosmosBridgeInternal.getCosmosAsyncClient(cosmosClient);
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
     * @param cosmosClient          Regular Cosmos Client.
     * @param encryptionKeyStoreProvider encryptionKeyStoreProvider, provider that allows interaction with the master
     *                                   keys.
     * @return encryptionCosmosClient to perform operations supporting client-side encryption / decryption.
     */
    public static CosmosEncryptionClient createCosmosEncryptionClient(CosmosClient cosmosClient,
                                                                                EncryptionKeyStoreProvider encryptionKeyStoreProvider) {
        return new CosmosEncryptionClient(cosmosClient, encryptionKeyStoreProvider);
    }

    CosmosEncryptionAsyncClient getCosmosEncryptionAsyncClient() {
        return cosmosEncryptionAsyncClient;
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
     * Block cosmos clientEncryptionPolicy response
     *
     * @param clientEncryptionPolicyMono the clientEncryptionPolicy mono.
     * @return the cosmos clientEncryptionPolicy response.
     */
     ClientEncryptionPolicy blockClientEncryptionPolicyResponse(Mono<ClientEncryptionPolicy> clientEncryptionPolicyMono) {
        try {
            return clientEncryptionPolicyMono.block();
        } catch (Exception ex) {
            final Throwable throwable = Exceptions.unwrap(ex);
            if (throwable instanceof CosmosException) {
                throw (CosmosException) throwable;
            } else {
                throw ex;
            }
        }
    }
}
