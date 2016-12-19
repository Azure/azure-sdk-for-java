/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Possible routing methods supported by Traffic manager profile.
 */
@LangDefinition
public enum TrafficRoutingMethod {
    /**
     * Enum value Performance.
     */
    PERFORMANCE("Performance"),

    /**
     * Enum value Weighted.
     */
    WEIGHTED("Weighted"),

    /**
     * Enum value Priority.
     */
    PRIORITY("Priority");

    private String value;

    TrafficRoutingMethod(String value) {
        this.value = value;
    }

    /**
     * Parses a string value to a TrafficRoutingMethod instance.
     *
     * @param value the string value to parse.
     * @return the parsed TrafficRoutingMethod object, or null if unable to parse.
     */
    public static TrafficRoutingMethod fromValue(String value) {
        TrafficRoutingMethod[] items = TrafficRoutingMethod.values();
        for (TrafficRoutingMethod item : items) {
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
