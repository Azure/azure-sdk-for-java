/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SkuName.
 */
public enum SkuName {
    /** Enum value Standard_LRS. */
    STANDARD_LRS("Standard_LRS"),

    /** Enum value Standard_GRS. */
    STANDARD_GRS("Standard_GRS"),

    /** Enum value Standard_RAGRS. */
    STANDARD_RAGRS("Standard_RAGRS"),

    /** Enum value Standard_ZRS. */
    STANDARD_ZRS("Standard_ZRS"),

    /** Enum value Premium_LRS. */
    PREMIUM_LRS("Premium_LRS");

    /** The actual serialized value for a SkuName instance. */
    private String value;

    SkuName(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a SkuName instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a SkuName instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SkuName object, or null if unable to parse.
     */
    @JsonCreator
    public static SkuName fromValue(String value) {
        SkuName[] items = SkuName.values();
        for (SkuName item : items) {
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
