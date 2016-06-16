/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DataLakeAnalyticsAccountState.
 */
public enum DataLakeAnalyticsAccountState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value suspended. */
    SUSPENDED("suspended");

    /** The actual serialized value for a DataLakeAnalyticsAccountState instance. */
    private String value;

    DataLakeAnalyticsAccountState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a DataLakeAnalyticsAccountState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a DataLakeAnalyticsAccountState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DataLakeAnalyticsAccountState object, or null if unable to parse.
     */
    @JsonCreator
    public static DataLakeAnalyticsAccountState fromValue(String value) {
        DataLakeAnalyticsAccountState[] items = DataLakeAnalyticsAccountState.values();
        for (DataLakeAnalyticsAccountState item : items) {
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
