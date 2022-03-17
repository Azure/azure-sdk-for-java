// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.util.Beta;

/**
 * The type Cosmos clientEncryptionKey. This contains methods to operate on a cosmos clientEncryptionKey
 * synchronously
 */
@Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getId() {
        return id;
    }

    /**
     * Reads a cosmos client encryption key
     *
     * @return the single resource response with the read client encryption key or an error.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosClientEncryptionKeyResponse read() {
        return this.database.blockClientEncryptionKeyResponse(this.cosmosAsyncClientEncryptionKey.read());
    }
}
