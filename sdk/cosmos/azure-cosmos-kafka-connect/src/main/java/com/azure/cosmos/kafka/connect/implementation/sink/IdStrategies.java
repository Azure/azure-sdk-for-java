// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public enum IdStrategies {
    TEMPLATE_STRATEGY("TemplateStrategy"),
    FULL_KEY_STRATEGY("FullKeyStrategy"),
    KAFKA_METADATA_STRATEGY("KafkaMetadataStrategy"),
    PROVIDED_IN_KEY_STRATEGY("ProvidedInKeyStrategy"),
    PROVIDED_IN_VALUE_STRATEGY("ProvidedInValueStrategy");

    private final String name;

    IdStrategies(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static IdStrategies fromName(String name) {
        for (IdStrategies mode : IdStrategies.values()) {
            if (mode.getName().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
