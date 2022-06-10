// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry.environment;

/**
 * enum VcapServiceType
 */
enum VcapServiceType {
    /**
     * Azure Redis cache
     */
    AZURE_REDISCACHE("azure-rediscache"),

    /**
     * Azure Cosmos DB
     */
    AZURE_COSMOSDB("azure-documentdb"),

    /**
     * Azure Storage
     */
    AZURE_STORAGE("azure-storage"),

    /**
     * Azure SQL Db
     */
    AZURE_SQLDB("azure-sqldb"),

    /**
     * Azure ServiceBus
     */
    AZURE_SERVICEBUS("azure-servicebus");

    private final String text;

    VcapServiceType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
