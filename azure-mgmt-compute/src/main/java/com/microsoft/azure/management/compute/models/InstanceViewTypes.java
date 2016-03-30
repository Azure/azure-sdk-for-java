/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for InstanceViewTypes.
 */
public enum InstanceViewTypes {
    /** Enum value instanceView. */
    INSTANCEVIEW("instanceView");

    /** The actual serialized value for a InstanceViewTypes instance. */
    private String value;

    InstanceViewTypes(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a InstanceViewTypes instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a InstanceViewTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed InstanceViewTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static InstanceViewTypes fromValue(String value) {
        InstanceViewTypes[] items = InstanceViewTypes.values();
        for (InstanceViewTypes item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
