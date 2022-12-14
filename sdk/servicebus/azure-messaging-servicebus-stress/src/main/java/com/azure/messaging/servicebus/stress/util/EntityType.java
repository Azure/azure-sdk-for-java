// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;


/**
 * Messaging entity types supported by Service Bus.
 */
public enum EntityType {
    QUEUE(0),
    TOPIC(1);

    private int enumValue;
    EntityType(int enumValue) {
        this.enumValue = enumValue;
    }

    public int getValue() {
        return this.enumValue;
    }
}
