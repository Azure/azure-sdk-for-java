// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public enum CosmosAzureEnvironments {
    AZURE("azure"),
    AZURE_CHINA("AzureChina"),
    AZURE_US_GOVERNMENT("AzureUsGovernment"),
    AZURE_GERMANY("AzureGermany");

    private final String name;

    CosmosAzureEnvironments(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosAzureEnvironments fromName(String name) {
        for (CosmosAzureEnvironments azureEnvironment : CosmosAzureEnvironments.values()) {
            if (azureEnvironment.getName().equalsIgnoreCase(name)) {
                return azureEnvironment;
            }
        }
        return null;
    }
}
