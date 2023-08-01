// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

/**
 * Message settlement status.
 */
public enum DispositionStatus {
    COMPLETED("completed"),
    DEFERRED("defered"),
    SUSPENDED("suspended"),
    ABANDONED("abandoned"),
    RELEASED("released");

    private final String value;

    DispositionStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the disposition status.
     *
     * @return The string value of the disposition status.
     */
    public String getValue() {
        return value;
    }
}
