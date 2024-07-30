// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

public enum CosmosChangeFeedMode {
    LATEST_VERSION("LatestVersion"),
    ALL_VERSION_AND_DELETES("AllVersionsAndDeletes");

    private final String name;
    CosmosChangeFeedMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosChangeFeedMode fromName(String name) {
        for (CosmosChangeFeedMode mode : CosmosChangeFeedMode.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
