// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public enum CosmosAzureEnvironment {
    AZURE("azure"),
    AZURE_CHINA("AzureChina"),
    AZURE_US_GOVERNMENT("AzureUsGovernment"),
    AZURE_GERMANY("AzureGermany");

    private final String name;

    CosmosAzureEnvironment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosAzureEnvironment fromName(String name) {
        for (CosmosAzureEnvironment azureEnvironment : CosmosAzureEnvironment.values()) {
            if (azureEnvironment.getName().equalsIgnoreCase(name)) {
                return azureEnvironment;
            }
        }
        return null;
    }
}
