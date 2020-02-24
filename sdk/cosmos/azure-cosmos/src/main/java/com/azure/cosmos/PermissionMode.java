// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Enumeration specifying applicability of permission in the Azure Cosmos DB database service.
 */
public enum PermissionMode {
    /**
     * Permission applicable for read operations only.
     */
    READ(0x1, "Read"),

    /**
     * Permission applicable for all operations.
     */
    ALL(0x2, "All");

    private int value;

    PermissionMode(int value, String overWireValue) {
        this.value = value;
        this.overWireValue = overWireValue;
    }

    /**
     * Gets the numerical value of the permission mode.
     *
     * @return the numerical value.
     */
    public int getValue() {
        return value;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}
