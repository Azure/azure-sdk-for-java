/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SkuTier.
 */
public enum SkuTier {
    /** Enum value Standard. */
    STANDARD("Standard"),

    /** Enum value Premium. */
    PREMIUM("Premium");

    /** The actual serialized value for a SkuTier instance. */
    private String value;

    SkuTier(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a SkuTier instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a SkuTier instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SkuTier object, or null if unable to parse.
     */
    @JsonCreator
    public static SkuTier fromValue(String value) {
        SkuTier[] items = SkuTier.values();
        for (SkuTier item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
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
