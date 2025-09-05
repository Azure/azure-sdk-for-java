// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesComputerCallClickButtonType.
 */
public enum ResponsesComputerCallClickButtonType {
    /**
     * Enum value left.
     */
    LEFT("left"),

    /**
     * Enum value right.
     */
    RIGHT("right"),

    /**
     * Enum value wheel.
     */
    WHEEL("wheel"),

    /**
     * Enum value back.
     */
    BACK("back"),

    /**
     * Enum value forward,.
     */
    FORWARD("forward,");

    /**
     * The actual serialized value for a ResponsesComputerCallClickButtonType instance.
     */
    private final String value;

    ResponsesComputerCallClickButtonType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesComputerCallClickButtonType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesComputerCallClickButtonType object, or null if unable to parse.
     */
    public static ResponsesComputerCallClickButtonType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesComputerCallClickButtonType[] items = ResponsesComputerCallClickButtonType.values();
        for (ResponsesComputerCallClickButtonType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}
