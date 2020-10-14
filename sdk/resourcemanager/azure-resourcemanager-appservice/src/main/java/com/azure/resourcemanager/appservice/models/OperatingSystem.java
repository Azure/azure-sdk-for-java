// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Defines values for AppServiceOperatingSystem. */
public enum OperatingSystem {
    /** Enum value Windows. */
    WINDOWS("windows"),

    /** Enum value Linux. */
    LINUX("linux");

    /** The actual serialized value for a AppServiceOperatingSystem instance. */
    private String value;

    OperatingSystem(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a AppServiceOperatingSystem instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AppServiceOperatingSystem object, or null if unable to parse.
     */
    @JsonCreator
    public static OperatingSystem fromString(String value) {
        OperatingSystem[] items = OperatingSystem.values();
        for (OperatingSystem item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
