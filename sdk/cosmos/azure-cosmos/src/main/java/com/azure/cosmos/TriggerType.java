// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.apache.commons.text.WordUtils;

/**
 * The trigger type in the Azure Cosmos DB database service.
 */
public enum TriggerType {
    /**
     * Trigger should be executed before the associated operation(s).
     */
    PRE(0x0, "Pre"),

    /**
     * Trigger should be executed after the associated operation(s).
     */
    POST(0x1, "Post");

    private int value;

    TriggerType(int value, String overWireValue) {
        this.value = value;
        this.overWireValue = overWireValue;
    }

    /**
     * Gets the numerical value of the trigger type.
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
