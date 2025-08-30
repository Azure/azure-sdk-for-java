// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

/**
 * Defines values for ListInputItemsRequestOrder.
 */
public enum ListInputItemsRequestOrder {
    /**
     * Enum value asc.
     */
    ASC("asc"),

    /**
     * Enum value desc.
     */
    DESC("desc");

    /**
     * The actual serialized value for a ListInputItemsRequestOrder instance.
     */
    private final String value;

    ListInputItemsRequestOrder(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ListInputItemsRequestOrder instance.
     * 
     * @param value the serialized value to parse.
     * @return the parsed ListInputItemsRequestOrder object, or null if unable to parse.
     */
    public static ListInputItemsRequestOrder fromString(String value) {
        if (value == null) {
            return null;
        }
        ListInputItemsRequestOrder[] items = ListInputItemsRequestOrder.values();
        for (ListInputItemsRequestOrder item : items) {
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
