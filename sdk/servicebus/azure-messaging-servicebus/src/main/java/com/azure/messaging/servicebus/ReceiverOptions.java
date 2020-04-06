// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

/**
 * Options set when creating a service bus receiver.
 */
class ReceiverOptions {
    private final ReceiveMode receiveMode;
    private final int prefetchCount;

    ReceiverOptions(ReceiveMode receiveMode, int prefetchCount) {
        this.receiveMode = receiveMode;
        this.prefetchCount = prefetchCount;
    }

    /**
     * Gets the receive mode for the message.
     *
     * @return the receive mode for the message.
     */
    ReceiveMode getReceiveMode() {
        return receiveMode;
    }

    /**
     * Gets the prefetch count of the receiver.
     *
     * @return The prefetch count of the receiver.
     */
    int getPrefetchCount() {
        return prefetchCount;
    }
}
