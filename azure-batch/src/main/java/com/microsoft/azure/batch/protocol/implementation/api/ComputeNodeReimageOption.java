/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ComputeNodeReimageOption.
 */
public enum ComputeNodeReimageOption {
    /** Enum value requeue. */
    REQUEUE("requeue"),

    /** Enum value terminate. */
    TERMINATE("terminate"),

    /** Enum value taskcompletion. */
    TASKCOMPLETION("taskcompletion"),

    /** Enum value retaineddata. */
    RETAINEDDATA("retaineddata");

    /** The actual serialized value for a ComputeNodeReimageOption instance. */
    private String value;

    ComputeNodeReimageOption(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a ComputeNodeReimageOption instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a ComputeNodeReimageOption instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ComputeNodeReimageOption object, or null if unable to parse.
     */
    @JsonCreator
    public static ComputeNodeReimageOption fromValue(String value) {
        ComputeNodeReimageOption[] items = ComputeNodeReimageOption.values();
        for (ComputeNodeReimageOption item : items) {
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
