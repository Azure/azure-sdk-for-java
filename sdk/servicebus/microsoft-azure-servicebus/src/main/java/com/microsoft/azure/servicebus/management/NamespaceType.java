// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

/**
 * Specifies the type of entities the namespace can contain.
 */
public enum NamespaceType {
    /**
     * Namespace contains service bus entities (queues / topics)
     */
    ServiceBus(0),

    /**
     * Supported only for backword compatibility.
     * Namespace can contain mixture of service bus entities and notification hubs.
     */
    Mixed(2),

    /**
     * Unknown entities.
     */
    Unknown(100);

    private int numVal;

    NamespaceType(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
