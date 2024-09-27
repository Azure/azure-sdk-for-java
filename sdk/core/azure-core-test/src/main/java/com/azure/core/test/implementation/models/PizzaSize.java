// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.implementation.models;

/**
 * Pizza size.
 */
public enum PizzaSize {
    /**
     * Small pizza.
     */
    SMALL("small"),

    /**
     * Medium pizza.
     */
    MEDIUM("medium"),

    /**
     * Large pizza.
     */
    LARGE("large");

    private final String value;

    PizzaSize(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Parses a string value to a {@link PizzaSize}.
     *
     * @param value The string value to parse.
     * @return The parsed {@link PizzaSize} object, or null if the string value is invalid.
     */
    public static PizzaSize fromString(String value) {
        for (PizzaSize size : PizzaSize.values()) {
            if (size.value.equals(value)) {
                return size;
            }
        }
        return null;
    }
}
