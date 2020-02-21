// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueueSenderAsyncClientTest {

    private final String baseConnectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void testPublishSingleMessage() throws Exception {
        String connectionString = baseConnectionString + ";EntityPath=queue-test1";

        // Instantiate a client that will be used to call the service.
        QueueSenderAsyncClient asyncSender = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        Message message = new Message("Hello world!".getBytes(UTF_8));

        StepVerifier.create(asyncSender.send(message))
            .verifyComplete();
    }

    /**
     * Main method to invoke this demo on how to send a message to an Azure Service Bus.
     */
    @Test
    public void testPublishMultipleMessage() throws Exception {

        String connectionString = baseConnectionString + ";EntityPath=queue-test1";
        // Instantiate a client that will be used to call the service.
        QueueSenderAsyncClient asyncSender = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        Message message1 = new Message("Hello world - Multiple -part 1!".getBytes(UTF_8));
        Message message2 = new Message("Hello world - Multiple -part 2!".getBytes(UTF_8));

        // Create list of messages
        List<Message> list = new ArrayList<Message>();

        list.add(message1);
        list.add(message2);
        StepVerifier.create(asyncSender.send(list))
            .verifyComplete();

    }
}
