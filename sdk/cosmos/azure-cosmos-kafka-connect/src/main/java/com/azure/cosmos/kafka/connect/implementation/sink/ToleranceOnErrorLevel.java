// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public enum ToleranceOnErrorLevel {
    NONE("None"),
    ALL("All");

    private final String name;

    ToleranceOnErrorLevel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ToleranceOnErrorLevel fromName(String name) {
        for (ToleranceOnErrorLevel mode : ToleranceOnErrorLevel.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
