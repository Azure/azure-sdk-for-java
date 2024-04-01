// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public enum CosmosAuthTypes {
    MASTER_KEY("MasterKey"),
    SERVICE_PRINCIPAL("ServicePrincipal");

    private final String name;

    CosmosAuthTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosAuthTypes fromName(String name) {
        for (CosmosAuthTypes authTypes : CosmosAuthTypes.values()) {
            if (authTypes.getName().equalsIgnoreCase(name)) {
                return authTypes;
            }
        }
        return null;
    }
}
