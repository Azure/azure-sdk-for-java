/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for PoolState.
 */
public enum PoolState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value deleting. */
    DELETING("deleting"),

    /** Enum value upgrading. */
    UPGRADING("upgrading");

    /** The actual serialized value for a PoolState instance. */
    private String value;

    PoolState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a PoolState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a PoolState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed PoolState object, or null if unable to parse.
     */
    @JsonCreator
    public static PoolState fromValue(String value) {
        PoolState[] items = PoolState.values();
        for (PoolState item : items) {
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
