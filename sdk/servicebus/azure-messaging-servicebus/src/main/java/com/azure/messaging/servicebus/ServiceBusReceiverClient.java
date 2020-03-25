// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;

/**
 * A <b>synchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue or
 * topic on Azure Service Bus.
 *
 * @see ServiceBusClientBuilder
 * @see ServiceBusReceiverAsyncClient See ServiceBusReceiverAsyncClient to communicate with a Service Bus resource using
 *     an asynchronous client.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public class ServiceBusReceiverClient implements AutoCloseable {
    private final ServiceBusReceiverAsyncClient asyncClient;

    ServiceBusReceiverClient(ServiceBusReceiverAsyncClient asyncClient) {
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
