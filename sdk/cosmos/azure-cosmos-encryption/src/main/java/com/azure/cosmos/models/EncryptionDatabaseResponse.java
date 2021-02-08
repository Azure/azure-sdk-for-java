// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.encryption.EncryptionCosmosAsyncClient;

public class EncryptionDatabaseResponse {

    private final CosmosDatabaseResponse databaseResponse;

    private final EncryptionCosmosAsyncClient encryptionCosmosClient;

    public EncryptionDatabaseResponse(
        CosmosDatabaseResponse databaseResponse,
        EncryptionCosmosAsyncClient encryptionCosmosClient) {
        this.databaseResponse = databaseResponse;
        this.encryptionCosmosClient = encryptionCosmosClient;
    }

    public CosmosDatabaseResponse getDatabaseResponse() {
        return databaseResponse;
    }

    public EncryptionCosmosAsyncClient getEncryptionCosmosClient() {
        return encryptionCosmosClient;
    }
}
