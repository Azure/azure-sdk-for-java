// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public enum IdStrategy {
    TEMPLATE_STRATEGY("TemplateStrategy"),
    FULL_KEY_STRATEGY("FullKeyStrategy"),
    KAFKA_METADATA_STRATEGY("KafkaMetadataStrategy"),
    PROVIDED_IN_KEY_STRATEGY("ProvidedInKeyStrategy"),
    PROVIDED_IN_VALUE_STRATEGY("ProvidedInValueStrategy");

    private final String name;

    IdStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static IdStrategy fromName(String name) {
        for (IdStrategy mode : IdStrategy.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
