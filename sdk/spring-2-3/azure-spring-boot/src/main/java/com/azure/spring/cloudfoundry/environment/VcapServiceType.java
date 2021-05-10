// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

/**
 * enum VcapServiceType
 */
public enum VcapServiceType {
    AZURE_REDISCACHE("azure-rediscache"),
    AZURE_COSMOSDB("azure-documentdb"),
    AZURE_STORAGE("azure-storage"),
    AZURE_SQLDB("azure-sqldb"),
    AZURE_SERVICEBUS("azure-servicebus");

    private String text;

    VcapServiceType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
