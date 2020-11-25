// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send a {@link ServiceBusMessage} to a session-enabled Azure Service Bus queue.
 * Samples for receiving are {@link ReceiveNamedSessionAsyncSample} or {@link ReceiveNamedSessionSample}.
 */
public class SendSessionMessageAsyncSample {
    /**
     * Main method to invoke this demo on how to send and receive a {@link ServiceBusMessage} to and from a
     * session-enabled Azure Service Bus queue.
     *
     * @param args Unused arguments to the program.
     *
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

        // We want all our greetings in the same session to be processed.
        String sessionId = "greetings-id";

        // Any clients built from the same ServiceBusClientBuilder share the same connection.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        // Instantiate a client that will be used to send messages.
        ServiceBusSenderAsyncClient sender = builder
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // Setting the sessionId parameter ensures all messages end up in the same session and are received in order.
        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromBytes("Hello".getBytes(UTF_8))).setSessionId(sessionId),
            new ServiceBusMessage(BinaryData.fromBytes("Bonjour".getBytes(UTF_8))).setSessionId(sessionId),
            new ServiceBusMessage(BinaryData.fromBytes("Guten tag".getBytes(UTF_8))).setSessionId(sessionId)
        );

        // This sends all the messages in a single message batch.
        // This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.sendMessages(messages).subscribe(unused -> System.out.println("Batch sent."),
            error -> System.err.println("Error occurred while publishing message batch: " + error),
            () -> System.out.println("Batch send complete."));

        // subscribe() is not a blocking call. We sleep here so the program does not end before the send is complete.
        TimeUnit.SECONDS.sleep(10);

        // Close the sender.
        sender.close();
    }
}
