/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AllocationState.
 */
public enum AllocationState {
    /** Enum value steady. */
    STEADY("steady"),

    /** Enum value resizing. */
    RESIZING("resizing"),

    /** Enum value stopping. */
    STOPPING("stopping");

    /** The actual serialized value for a AllocationState instance. */
    private String value;

    AllocationState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a AllocationState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a AllocationState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AllocationState object, or null if unable to parse.
     */
    @JsonCreator
    public static AllocationState fromValue(String value) {
        AllocationState[] items = AllocationState.values();
        for (AllocationState item : items) {
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
