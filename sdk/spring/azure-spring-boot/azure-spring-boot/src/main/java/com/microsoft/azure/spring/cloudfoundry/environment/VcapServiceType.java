/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloudfoundry.environment;

public enum VcapServiceType {
    AZURE_REDISCACHE("azure-rediscache"), AZURE_DOCUMENTDB("azure-documentdb"), AZURE_STORAGE(
            "azure-storage"), AZURE_SQLDB("azure-sqldb"), AZURE_SERVICEBUS(
            "azure-servicebus");

    private String text;

    private VcapServiceType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
