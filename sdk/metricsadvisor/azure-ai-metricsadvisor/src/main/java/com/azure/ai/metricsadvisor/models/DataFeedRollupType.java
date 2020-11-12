// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Defines values for LeaseStateType.
 */
public enum DataFeedRollupType {

    /**
     * Enum value NoRollup.
     */
    NO_ROLLUP("NoRollup"),

    /**
     * Enum value NeedRollup.
     */
    AUTO_ROLLUP("NeedRollup"),

    /**
     * Enum value AlreadyRollup.
     */
    ALREADY_ROLLUP("AlreadyRollup");

    /**
    /**
     * The actual serialized value for a DataFeedRollupType instance.
     */
    private final String value;

    DataFeedRollupType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DataFeedRollupType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DataFeedRollupType object, or null if unable to parse.
     */
    public static DataFeedRollupType fromString(String value) {
        DataFeedRollupType[] items = DataFeedRollupType.values();
        for (DataFeedRollupType item : items) {
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
