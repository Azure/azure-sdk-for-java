/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

/**
 * Possible traffic manager profile endpoint monitor statuses.
 */
public enum EndpointMonitorStatus {
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
    CHECKINGENDPOINT("CheckingEndpoint"),

    /**
     * Enum value Stopped.
     */
    STOPPED("Stopped");

    private String value;

    EndpointMonitorStatus(String value) {
        this.value = value;
    }

    /**
     * Parses a string value to a EndpointMonitorStatus instance.
     *
     * @param value the string value to parse.
     * @return the parsed EndpointMonitorStatus object, or null if unable to parse.
     */
    public static EndpointMonitorStatus fromValue(String value) {
        EndpointMonitorStatus[] items = EndpointMonitorStatus.values();
        for (EndpointMonitorStatus item : items) {
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
