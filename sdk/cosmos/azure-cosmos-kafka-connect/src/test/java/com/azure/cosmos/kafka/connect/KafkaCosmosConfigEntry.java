// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

public class KafkaCosmosConfigEntry<T> {
    private final String name;
    private final T defaultValue;
    private final boolean isOptional;

    public KafkaCosmosConfigEntry(String name, T defaultValue, boolean isOptional) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.isOptional = isOptional;
    }

    public String getName() {
        return name;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isOptional() {
        return isOptional;
    }

}
