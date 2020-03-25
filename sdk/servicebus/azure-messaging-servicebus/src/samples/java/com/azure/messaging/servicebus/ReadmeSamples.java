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
     * Code sample for creating a synchronous Service Bus sender.
     */
    public void createAsynchronousServiceBusSender() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS QUEUE or TOPIC >>";
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();
    }

    /**
     * Code sample for creating a synchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS QUEUE or TOPIC >>";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncReceiverClient();
    }
}
