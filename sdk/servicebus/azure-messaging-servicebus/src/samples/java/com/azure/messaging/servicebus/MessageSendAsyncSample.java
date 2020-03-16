// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send an {@link ServiceBusMessage} to an Azure Service Bus queue or topic.
 */
public class MessageSendAsyncSample {
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     */
    @Test
    public void sendMessage() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Creating an Queue instance.
        // 3. Creating a "Shared access policy" for your Queue instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING")
            + ";EntityPath=hemant-test1";
        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildAsyncSenderClient();

        // Create a message to send.
        ServiceBusMessage message = new ServiceBusMessage("Hello world!".getBytes(UTF_8));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.

        Disposable disposable = senderAsyncClient.send(message).subscribe();
        try {
            Thread.sleep(5000);
        } catch (Exception ee) {

        }
        disposable.dispose();
    }


}
