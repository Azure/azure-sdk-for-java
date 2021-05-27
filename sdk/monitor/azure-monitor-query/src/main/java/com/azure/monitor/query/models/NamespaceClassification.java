// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Defines values for NamespaceClassification. */
public enum NamespaceClassification {
    /** Enum value Platform. */
    PLATFORM("Platform"),

    /** Enum value Custom. */
    CUSTOM("Custom"),

    /** Enum value Qos. */
    QOS("Qos");

    /** The actual serialized value for a NamespaceClassification instance. */
    private final String value;

    NamespaceClassification(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a NamespaceClassification instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed NamespaceClassification object, or null if unable to parse.
     */
    @JsonCreator
    public static NamespaceClassification fromString(String value) {
        NamespaceClassification[] items = NamespaceClassification.values();
        for (NamespaceClassification item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
