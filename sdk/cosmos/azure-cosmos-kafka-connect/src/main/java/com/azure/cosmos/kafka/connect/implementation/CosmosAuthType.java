// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public enum CosmosAuthType {
    MASTER_KEY("MasterKey"),
    SERVICE_PRINCIPAL("ServicePrincipal");

    private final String name;

    CosmosAuthType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosAuthType fromName(String name) {
        for (CosmosAuthType authTypes : CosmosAuthType.values()) {
            if (authTypes.getName().equalsIgnoreCase(name)) {
                return authTypes;
            }
        }

        return null;
    }
}
