/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

/**
 * Possible traffic manager profile statuses.
 */
public enum ProfileMonitorStatus {
    /**
     * Enum value Inactive.
     */
    INACTIVE("Inactive"),

    /**
     * Enum value Disabled.
     */
    DISABLED("Disabled"),

    /**
     * Enum value Online.
     */
    ONLINE("Online"),

    /**
     * Enum value Degraded.
     */
    DEGRADED("Degraded"),

    /**
     * Enum value CheckingEndpoint.
     */
    CHECKINGENDPOINT("CheckingEndpoint");

    private String value;

    ProfileMonitorStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a string value to a ProfileMonitorStatus instance.
     *
     * @param value the string value to parse.
     * @return the parsed ProfileMonitorStatus object, or null if unable to parse.
     */
    public static ProfileMonitorStatus fromValue(String value) {
        ProfileMonitorStatus[] items = ProfileMonitorStatus.values();
        for (ProfileMonitorStatus item : items) {
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
