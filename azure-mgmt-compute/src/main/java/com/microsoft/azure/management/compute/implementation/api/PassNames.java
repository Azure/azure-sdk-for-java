/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for PassNames.
 */
public enum PassNames {
    /** Enum value oobeSystem. */
    OOBE_SYSTEM("oobeSystem");

    /** The actual serialized value for a PassNames instance. */
    private String value;

    PassNames(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a PassNames instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a PassNames instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed PassNames object, or null if unable to parse.
     */
    @JsonCreator
    public static PassNames fromValue(String value) {
        PassNames[] items = PassNames.values();
        for (PassNames item : items) {
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
