// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.patch;

public enum KafkaCosmosPatchOperationType {
    NONE("None"),
    ADD("Add"),
    SET("Set"),
    REPLACE("Replace"),
    REMOVE("Remove"),
    INCREMENT("Increment");

    private final String name;
    KafkaCosmosPatchOperationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static KafkaCosmosPatchOperationType fromName(String name) {
        for (KafkaCosmosPatchOperationType patchOperationType : KafkaCosmosPatchOperationType.values()) {
            if (patchOperationType.getName().equalsIgnoreCase(name)) {
                return patchOperationType;
            }
        }
        return null;
    }
}
