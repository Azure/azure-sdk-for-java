// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

public enum IdStrategyType {
    TEMPLATE_STRATEGY("TemplateStrategy"),
    FULL_KEY_STRATEGY("FullKeyStrategy"),
    KAFKA_METADATA_STRATEGY("KafkaMetadataStrategy"),
    PROVIDED_IN_KEY_STRATEGY("ProvidedInKeyStrategy"),
    PROVIDED_IN_VALUE_STRATEGY("ProvidedInValueStrategy");

    private final String name;

    IdStrategyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static IdStrategyType fromName(String name) {
        for (IdStrategyType idStrategyType : IdStrategyType.values()) {
            if (idStrategyType.getName().equalsIgnoreCase(name)) {
                return idStrategyType;
            }
        }
        return null;
    }
}
