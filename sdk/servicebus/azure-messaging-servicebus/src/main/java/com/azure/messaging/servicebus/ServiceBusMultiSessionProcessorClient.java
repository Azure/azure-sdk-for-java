// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

public class ServiceBusMultiSessionProcessorClient implements AutoCloseable{
    public void start(){}
    public void stop(){}
    public  boolean isRunning() { return true;
    }

    /**
     * Disposes of the {@link ServiceBusMultiSessionProcessorClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {

    }
}

