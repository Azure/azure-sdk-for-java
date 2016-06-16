/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for CompileMode.
 */
public enum CompileMode {
    /** Enum value Semantic. */
    SEMANTIC("Semantic"),

    /** Enum value Full. */
    FULL("Full"),

    /** Enum value SingleBox. */
    SINGLE_BOX("SingleBox");

    /** The actual serialized value for a CompileMode instance. */
    private String value;

    CompileMode(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a CompileMode instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a CompileMode instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed CompileMode object, or null if unable to parse.
     */
    @JsonCreator
    public static CompileMode fromValue(String value) {
        CompileMode[] items = CompileMode.values();
        for (CompileMode item : items) {
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
