package com.microsoft.azure.management.compute;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ComputeRoles.
 */
public enum ComputeRoles {
    /** Enum value PaaS. */
    PAAS("PaaS"),

    /** Enum value IaaS. */
    IAAS("IaaS");

    /** The actual serialized value for a ComputeRoles instance. */
    private String value;

    ComputeRoles(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a ComputeRoles instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ComputeRoles object, or null if unable to parse.
     */
    @JsonCreator
    public static ComputeRoles fromString(String value) {
        ComputeRoles[] items = ComputeRoles.values();
        for (ComputeRoles item : items) {
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