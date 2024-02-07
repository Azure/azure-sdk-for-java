// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.source.configs;

public enum CosmosChangeFeedStartFromModes {
    BEGINNING("Beginning"),
    NOW("Now"),
    POINT_IN_TIME("PointInTime");

    private final String name;
    CosmosChangeFeedStartFromModes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
