// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesComputerCallOutputItemOutputType.
 */
public enum ResponsesComputerCallOutputItemOutputType {
    /**
     * Enum value computer_screenshot.
     */
    COMPUTER_SCREENSHOT("computer_screenshot");

    /**
     * The actual serialized value for a ResponsesComputerCallOutputItemOutputType instance.
     */
    private final String value;

    ResponsesComputerCallOutputItemOutputType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesComputerCallOutputItemOutputType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesComputerCallOutputItemOutputType object, or null if unable to parse.
     */
    public static ResponsesComputerCallOutputItemOutputType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesComputerCallOutputItemOutputType[] items = ResponsesComputerCallOutputItemOutputType.values();
        for (ResponsesComputerCallOutputItemOutputType item : items) {
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
