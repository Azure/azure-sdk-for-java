// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Specifies the operations on which a trigger should be executed in the Azure Cosmos DB database service.
 */
public enum TriggerOperation {
    /**
     * ALL operations.
     */
    ALL(0x0),

    /**
     * CREATE operations only.
     */
    CREATE(0x1),

    /**
     * UPDATE operations only.
     */
    UPDATE(0x2),

    /**
     * DELETE operations only.
     */
    DELETE(0x3),

    /**
     * REPLACE operations only.
     */
    REPLACE(0x4);

    private int value;

    TriggerOperation(int value) {
        this.value = value;
    }

    /**
     * Gets the numerical value of the trigger operation.
     *
     * @return the numerical value.
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());        
    }
}
