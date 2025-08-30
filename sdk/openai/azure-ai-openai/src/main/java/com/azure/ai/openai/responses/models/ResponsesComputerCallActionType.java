// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesComputerCallActionType.
 */
public enum ResponsesComputerCallActionType {
    /**
     * Enum value screenshot.
     */
    SCREENSHOT("screenshot"),

    /**
     * Enum value click.
     */
    CLICK("click"),

    /**
     * Enum value double_click.
     */
    DOUBLE_CLICK("double_click"),

    /**
     * Enum value scroll.
     */
    SCROLL("scroll"),

    /**
     * Enum value type.
     */
    TYPE("type"),

    /**
     * Enum value wait.
     */
    WAIT("wait"),

    /**
     * Enum value keypress.
     */
    KEYPRESS("keypress"),

    /**
     * Enum value drag.
     */
    DRAG("drag"),

    /**
     * Enum value move.
     */
    MOVE("move");

    /**
     * The actual serialized value for a ResponsesComputerCallActionType instance.
     */
    private final String value;

    ResponsesComputerCallActionType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesComputerCallActionType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesComputerCallActionType object, or null if unable to parse.
     */
    public static ResponsesComputerCallActionType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesComputerCallActionType[] items = ResponsesComputerCallActionType.values();
        for (ResponsesComputerCallActionType item : items) {
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
