// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    /**
     * Code sample for creating an asynchronous Service Bus sender.
     */
    public void createAsynchronousServiceBusSender() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildSenderClientBuilder()
            .entityName("<< QUEUE OR TOPIC NAME >>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildReceiverClientBuilder()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
    }
}
