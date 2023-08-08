// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;

/**
 * The type Cosmos clientEncryptionKey. This contains methods to operate on a cosmos clientEncryptionKey
 * synchronously
 */
public class CosmosClientEncryptionKey {
    private final CosmosDatabase database;
    private String id;
    private final CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey;

    CosmosClientEncryptionKey(String id,
                              CosmosDatabase database,
                              CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey) {
        this.database = database;
        this.id = id;
        this.cosmosAsyncClientEncryptionKey = cosmosAsyncClientEncryptionKey;
    }

    /**
     * Get the id of the {@link CosmosClientEncryptionKey}
     *
     * @return the id of the {@link CosmosClientEncryptionKey}
     */
    public String getId() {
        return id;
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @return the single resource response with the read client encryption key or an error.
     */
    public CosmosClientEncryptionKeyResponse read() {
        return this.database.blockClientEncryptionKeyResponse(this.cosmosAsyncClientEncryptionKey.read());
    }
}
