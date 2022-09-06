// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Class containing code snippets that set advanced configurations.
 */
public class AdvancedConfigurationSample {

    /**
     * Create an asynchronous receiver that prefetch 100 messages.
     */
    public void createAsynchronousServiceBusReceiverWithPrefetch() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .prefetchCount(100)
            .buildAsyncClient();
    }

    /**
     * Creates a session-enabled receiver that prefetch 100 messages.
     */
    public void createSessionReceiverWithPrefetch() {
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .prefetchCount(100)
            .buildAsyncClient();
    }
}
