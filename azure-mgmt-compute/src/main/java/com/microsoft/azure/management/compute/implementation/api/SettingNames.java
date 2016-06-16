/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SettingNames.
 */
public enum SettingNames {
    /** Enum value AutoLogon. */
    AUTO_LOGON("AutoLogon"),

    /** Enum value FirstLogonCommands. */
    FIRST_LOGON_COMMANDS("FirstLogonCommands");

    /** The actual serialized value for a SettingNames instance. */
    private String value;

    SettingNames(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a SettingNames instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a SettingNames instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SettingNames object, or null if unable to parse.
     */
    @JsonCreator
    public static SettingNames fromValue(String value) {
        SettingNames[] items = SettingNames.values();
        for (SettingNames item : items) {
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
