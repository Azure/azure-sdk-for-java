// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to send an {@link ServiceBusMessage} to an Azure Service Bus queue.
 */
public class SendMessageAsyncSample {
    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessageBatch} to an Azure Service Bus.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a Queue in that Service Bus namespace.
        String queueName = "queueName";

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // Create a message to send.
        ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString("Hello world!"));

        // Send that message. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.sendMessage(message).subscribe(
            unused -> System.out.println("Sent."),
            error -> System.err.println("Error occurred while publishing message: " + error),
            () -> System.out.println("Send complete."));

        // subscribe() is not a blocking call. We sleep here so the program does not end before the send is complete.
        TimeUnit.SECONDS.sleep(5);

        // Close the sender.
        sender.close();
    }
}
