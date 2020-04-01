// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to schedule {@link ServiceBusMessage} to an Azure Service Bus queue or topic.
 */
public class MessageScheduleAsyncSample {

    /**
     * Main method to invoke this demo on how to schedule a message to an Azure Service Bus queue or topic.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // Create an message.
        ServiceBusMessage message = new ServiceBusMessage("Hello World!!".getBytes(UTF_8));

        // Schedule the message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when
        // the message has been delivered to the Service Bus. It completes with an error if an exception occurred
        // while scheduling the message.

        senderAsyncClient.scheduleMessage(message, Instant.now().plusSeconds(60))
            .subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end while finishing
        // the operation.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }
    }
}
