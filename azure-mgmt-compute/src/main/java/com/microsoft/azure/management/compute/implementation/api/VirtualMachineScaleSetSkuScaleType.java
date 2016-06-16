/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VirtualMachineScaleSetSkuScaleType.
 */
public enum VirtualMachineScaleSetSkuScaleType {
    /** Enum value Automatic. */
    AUTOMATIC("Automatic"),

    /** Enum value None. */
    NONE("None");

    /** The actual serialized value for a VirtualMachineScaleSetSkuScaleType instance. */
    private String value;

    VirtualMachineScaleSetSkuScaleType(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a VirtualMachineScaleSetSkuScaleType instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a VirtualMachineScaleSetSkuScaleType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VirtualMachineScaleSetSkuScaleType object, or null if unable to parse.
     */
    @JsonCreator
    public static VirtualMachineScaleSetSkuScaleType fromValue(String value) {
        VirtualMachineScaleSetSkuScaleType[] items = VirtualMachineScaleSetSkuScaleType.values();
        for (VirtualMachineScaleSetSkuScaleType item : items) {
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
