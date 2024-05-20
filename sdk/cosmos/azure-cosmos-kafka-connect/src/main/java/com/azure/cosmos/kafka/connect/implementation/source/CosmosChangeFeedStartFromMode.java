// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

public enum CosmosChangeFeedStartFromMode {
    BEGINNING("Beginning"),
    NOW("Now"),
    POINT_IN_TIME("PointInTime");

    private final String name;
    CosmosChangeFeedStartFromMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosChangeFeedStartFromMode fromName(String name) {
        for (CosmosChangeFeedStartFromMode startFromModes : CosmosChangeFeedStartFromMode.values()) {
            if (startFromModes.getName().equalsIgnoreCase(name)) {
                return startFromModes;
            }
        }
        return null;
    }
}
