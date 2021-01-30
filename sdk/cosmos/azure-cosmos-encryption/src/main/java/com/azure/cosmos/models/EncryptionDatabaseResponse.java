package com.azure.cosmos.models;

import com.azure.cosmos.encryption.EncryptionAsyncCosmosClient;

public class EncryptionDatabaseResponse {

    private final CosmosDatabaseResponse databaseResponse;

    private final EncryptionAsyncCosmosClient encryptionCosmosClient;

    public EncryptionDatabaseResponse(
        CosmosDatabaseResponse databaseResponse,
        EncryptionAsyncCosmosClient encryptionCosmosClient) {
        this.databaseResponse = databaseResponse;
        this.encryptionCosmosClient = encryptionCosmosClient;
    }

    public CosmosDatabaseResponse getDatabaseResponse() {
        return databaseResponse;
    }

    public EncryptionAsyncCosmosClient getEncryptionCosmosClient() {
        return encryptionCosmosClient;
    }
}
