/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DisableComputeNodeSchedulingOption.
 */
public enum DisableComputeNodeSchedulingOption {
    /** Enum value requeue. */
    REQUEUE("requeue"),

    /** Enum value terminate. */
    TERMINATE("terminate"),

    /** Enum value taskcompletion. */
    TASKCOMPLETION("taskcompletion");

    /** The actual serialized value for a DisableComputeNodeSchedulingOption instance. */
    private String value;

    DisableComputeNodeSchedulingOption(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a DisableComputeNodeSchedulingOption instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a DisableComputeNodeSchedulingOption instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DisableComputeNodeSchedulingOption object, or null if unable to parse.
     */
    @JsonCreator
    public static DisableComputeNodeSchedulingOption fromValue(String value) {
        DisableComputeNodeSchedulingOption[] items = DisableComputeNodeSchedulingOption.values();
        for (DisableComputeNodeSchedulingOption item : items) {
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
