// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

public class ServiceBusMultiSessionProcessorClient implements AutoCloseable{
    private final ReceiveMode receiveMode;
    private boolean isRunning;
    ServiceBusMultiSessionProcessorClient(ReceiveMode receiveMode) {
        this.receiveMode = receiveMode;
    }
    public void start(){
        isRunning = true;
    }
    public void stop(){
        isRunning = false;
    }
    public ReceiveMode getReceiveMode(){
        return receiveMode;
    }
    public  boolean isRunning() { return isRunning;
    }

    /**
     * Disposes of the {@link ServiceBusMultiSessionProcessorClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {

    }
}

