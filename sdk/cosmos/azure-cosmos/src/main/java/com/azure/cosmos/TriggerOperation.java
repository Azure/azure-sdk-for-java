// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Specifies the operations on which a trigger should be executed in the Azure Cosmos DB database service.
 */
public enum TriggerOperation {
    /**
     * ALL operations.
     */
    ALL(0x0, "All"),

    /**
     * CREATE operations only.
     */
    CREATE(0x1, "Create"),

    /**
     * UPDATE operations only.
     */
    UPDATE(0x2, "Update"),

    /**
     * DELETE operations only.
     */
    DELETE(0x3, "Delete"),

    /**
     * REPLACE operations only.
     */
    REPLACE(0x4, "Replace");

    private int value;

    TriggerOperation(int value, String overWireValue) {
        this.value = value;
        this.overWireValue = overWireValue;
    }

    /**
     * Gets the numerical value of the trigger operation.
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
