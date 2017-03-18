/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AppServiceOperatingSystem.
 */
public enum AppServiceOperatingSystem {
    /** Enum value Windows. */
    WINDOWS("windows"),

    /** Enum value Linux. */
    LINUX("linux");

    /** The actual serialized value for a AppServiceOperatingSystem instance. */
    private String value;

    AppServiceOperatingSystem(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a AppServiceOperatingSystem instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AppServiceOperatingSystem object, or null if unable to parse.
     */
    @JsonCreator
    public static AppServiceOperatingSystem fromString(String value) {
        AppServiceOperatingSystem[] items = AppServiceOperatingSystem.values();
        for (AppServiceOperatingSystem item : items) {
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
