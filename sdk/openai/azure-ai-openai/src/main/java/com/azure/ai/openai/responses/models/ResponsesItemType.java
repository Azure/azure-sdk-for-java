// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ResponsesItemType.
 */
public enum ResponsesItemType {
    /**
     * Enum value message.
     */
    MESSAGE("message"),

    /**
     * Enum value file_search_call.
     */
    FILE_SEARCH_CALL("file_search_call"),

    /**
     * Enum value function_call.
     */
    FUNCTION_CALL("function_call"),

    /**
     * Enum value function_call_output.
     */
    FUNCTION_CALL_OUTPUT("function_call_output"),

    /**
     * Enum value computer_call.
     */
    COMPUTER_CALL("computer_call"),

    /**
     * Enum value computer_call_output.
     */
    COMPUTER_CALL_OUTPUT("computer_call_output"),

    /**
     * Enum value web_search_call.
     */
    WEB_SEARCH_CALL("web_search_call"),

    /**
     * Enum value item_reference.
     */
    ITEM_REFERENCE("item_reference"),

    /**
     * Enum value reasoning.
     */
    REASONING("reasoning");

    /**
     * The actual serialized value for a ResponsesItemType instance.
     */
    private final String value;

    ResponsesItemType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ResponsesItemType instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ResponsesItemType object, or null if unable to parse.
     */
    public static ResponsesItemType fromString(String value) {
        if (value == null) {
            return null;
        }
        ResponsesItemType[] items = ResponsesItemType.values();
        for (ResponsesItemType item : items) {
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
