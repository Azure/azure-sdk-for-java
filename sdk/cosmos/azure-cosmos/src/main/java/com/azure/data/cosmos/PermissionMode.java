// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * Enumeration specifying applicability of permission in the Azure Cosmos DB database service.
 */
public enum PermissionMode {
    /**
     * Permission applicable for read operations only.
     */
    READ(0x1),

    /**
     * Permission applicable for all operations.
     */
    ALL(0x2);

    private int value;

    PermissionMode(int value) {
        this.value = value;
    }

    /**
     * Gets the numerical value of the permission mode.
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
