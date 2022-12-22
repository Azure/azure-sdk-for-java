// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public final class CosmosDiagnosticsContext {
    private final String accountName;
    private final String databaseName;
    private final String collectionName;
    private final String resourceType;
    private final String operationType;

    CosmosDiagnosticsContext(
        String accountName,
        String databaseName,
        String collectionName,
        String resourceType,
        String operationType) {

        // TODO null checks

        this.accountName = accountName;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.resourceType = resourceType;
        this.operationType = operationType;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getCollectionName() {
        return this.collectionName;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public String getOperationType() {
        return this.operationType;
    }
}
