// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * A <b>synchronous</b> client to send messages to a Service Bus resource.
 */
public class ServiceBusSenderClient implements AutoCloseable {
    private final ServiceBusSenderAsyncClient asyncClient;

    ServiceBusSenderClient(ServiceBusSenderAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
