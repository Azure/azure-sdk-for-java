// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Defines values for FormReadingOrder.
 */
public enum FormReadingOrder {

    /**
     * Enum value basic.
     * Set it to basic for the lines to be sorted top to bottom, left to right, although in certain cases
     * proximity is treated with higher priority.
     */
    BASIC("basic"),

    /**
     * Enum value natural.
     * Set it to "natural" value for the algorithm to use positional information to keep nearby lines together.
     */
    NATURAL("natural");

    /**
     * The actual serialized value for a FormReadingOrder instance.
     */
    private final String value;

    FormReadingOrder(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a FormReadingOrder instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed FormReadingOrder object, or null if unable to parse.
     */
    public static FormReadingOrder fromString(String value) {
        FormReadingOrder[] items = FormReadingOrder.values();
        for (FormReadingOrder item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
