// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

public enum CosmosChangeFeedModes {
    LATEST_VERSION("LatestVersion"),
    ALL_VERSION_AND_DELETES("AllVersionsAndDeletes");

    private final String name;
    CosmosChangeFeedModes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosChangeFeedModes fromName(String name) {
        for (CosmosChangeFeedModes mode : CosmosChangeFeedModes.values()) {
            if (mode.getName().equals(name)) {
                return mode;
            }
        }
        return null;
    }
}
