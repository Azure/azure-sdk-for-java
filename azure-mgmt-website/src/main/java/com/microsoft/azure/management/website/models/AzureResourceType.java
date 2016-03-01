/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AzureResourceType.
 */
public enum AzureResourceType {
    /** Enum value Website. */
    WEBSITE("Website"),

    /** Enum value TrafficManager. */
    TRAFFICMANAGER("TrafficManager");

    /** The actual serialized value for a AzureResourceType instance. */
    private String value;

    AzureResourceType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a AzureResourceType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a AzureResourceType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AzureResourceType object, or null if unable to parse.
     */
    @JsonCreator
    public static AzureResourceType fromValue(String value) {
        AzureResourceType[] items = AzureResourceType.values();
        for (AzureResourceType item : items) {
            if (item.toValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
