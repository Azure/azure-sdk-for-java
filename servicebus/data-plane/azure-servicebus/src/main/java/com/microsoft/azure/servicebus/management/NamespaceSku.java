// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.management;

/**
 * Specifies the SKU/tier of the service bus namespace.
 */
public enum NamespaceSku {
    /**
     * Basic namespace. Shared Resource. Only queues are available.
     */
    Basic(1),

    /**
     * Standard namespace. Shared Resource. Queues and topics.
     */
    Standard(2),

    /**
     * Premium namespace. Dedicated Resource. Queues and topics.
     */
    Premium(3),

    /**
     * Unknown SKU.
     */
    Unknown(100);

    private int numVal;

    NamespaceSku(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }
}
