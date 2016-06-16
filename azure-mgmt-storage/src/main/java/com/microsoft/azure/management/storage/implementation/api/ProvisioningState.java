/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for ProvisioningState.
 */
public enum ProvisioningState {
    /** Enum value Creating. */
    CREATING("Creating"),

    /** Enum value ResolvingDNS. */
    RESOLVING_DNS("ResolvingDNS"),

    /** Enum value Succeeded. */
    SUCCEEDED("Succeeded");

    /** The actual serialized value for a ProvisioningState instance. */
    private String value;

    ProvisioningState(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a ProvisioningState instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a ProvisioningState instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed ProvisioningState object, or null if unable to parse.
     */
    @JsonCreator
    public static ProvisioningState fromValue(String value) {
        ProvisioningState[] items = ProvisioningState.values();
        for (ProvisioningState item : items) {
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
