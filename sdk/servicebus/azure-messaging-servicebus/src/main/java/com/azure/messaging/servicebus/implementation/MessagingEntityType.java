// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

/**
 * Messaging types supported by Service Bus.
 */
public enum MessagingEntityType {
    QUEUE(0), TOPIC(1), SUBSCRIPTION(2), FILTER(3), UNKNOWN(-1);

    private int enumValue;

    MessagingEntityType(int enumValue) {
        this.enumValue = enumValue;
    }

    public int getValue() {
        return this.enumValue;
    }
}
