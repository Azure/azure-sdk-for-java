// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueueSenderAsyncClientTest {

    private final String baseConnectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");
    @Captor
    ArgumentCaptor<com.azure.core.amqp.EventData> singleMessageCaptor;

    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void testPublishSingleMessage() throws Exception {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.

        String connectionString = baseConnectionString + ";EntityPath=queue-test1";

        // Instantiate a client that will be used to call the service.
        QueueSenderAsyncClient asyncSender = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        Message message = new Message("Hello world!".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service Bus. It completes with an error if an exception
        // occurred while sending the event.
        StepVerifier.create(asyncSender.send(message))
            .verifyComplete();
        Thread.sleep(1000 * 5);
    }

    /**
     * Main method to invoke this demo on how to send a message to an Azure Service Bus.
     */
    @Test
    public void testPublishMultipleMessage() throws Exception {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Creating an Service Bus instance.
        // 3. Creating a "Shared access policy" for your Service Bus instance.
        // 4. Copying the connection string from the policy's properties.

        String connectionString = baseConnectionString + ";EntityPath=queue-test1";
        // Instantiate a client that will be used to call the service.
        QueueSenderAsyncClient asyncSender = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create an event to send.
        Message message1 = new Message("Hello world - Multiple -part 1!".getBytes(UTF_8));
        Message message2 = new Message("Hello world - Multiple -part 2!".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        List<Message> list = new ArrayList<Message>();

        list.add(message1);
        list.add(message1);
        StepVerifier.create(asyncSender.send(list))
            .verifyComplete();
        Thread.sleep(5000);
    }
}
