// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * The trigger type in the Azure Cosmos DB database service.
 */
public enum TriggerType {
    /**
     * Trigger should be executed before the associated operation(s).
     */
    PRE(0x0),

    /**
     * Trigger should be executed after the associated operation(s).
     */
    POST(0x1);

    private int value;

    TriggerType(int value) {
        this.value = value;
    }

    /**
     * Gets the numerical value of the trigger type.
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
