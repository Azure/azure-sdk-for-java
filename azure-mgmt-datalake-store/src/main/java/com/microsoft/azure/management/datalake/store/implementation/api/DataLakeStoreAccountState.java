/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DataLakeStoreAccountState.
 */
public enum DataLakeStoreAccountState {
    /** Enum value active. */
    ACTIVE("active"),

    /** Enum value suspended. */
    SUSPENDED("suspended");

    /** The actual serialized value for a DataLakeStoreAccountState instance. */
    private String value;

    DataLakeStoreAccountState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a DataLakeStoreAccountState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a DataLakeStoreAccountState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DataLakeStoreAccountState object, or null if unable to parse.
     */
    @JsonCreator
    public static DataLakeStoreAccountState fromValue(String value) {
        DataLakeStoreAccountState[] items = DataLakeStoreAccountState.values();
        for (DataLakeStoreAccountState item : items) {
            if (item.toValue().equals(value)) {
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
