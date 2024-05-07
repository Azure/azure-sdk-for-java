// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

public enum CosmosPriorityLevel {
    NONE("None"),
    LOW("Low"),
    HIGH("High");

    private final String name;
    CosmosPriorityLevel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static CosmosPriorityLevel fromName(String name) {
        for (CosmosPriorityLevel priorityLevel : CosmosPriorityLevel.values()) {
            if (priorityLevel.getName().equalsIgnoreCase(name)) {
                return priorityLevel;
            }
        }
        return null;
    }
}
