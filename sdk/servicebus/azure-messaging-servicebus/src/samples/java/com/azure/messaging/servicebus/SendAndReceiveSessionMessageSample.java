// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send and receive a {@link ServiceBusMessage} to and from a session-enabled Azure Service
 * Bus queue.
 */
public class SendAndReceiveSessionMessageSample {
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

        // Instantiate a client that will be used to receive messages from the session.
        ServiceBusReceiverAsyncClient receiver = builder.sessionReceiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName(queueName)
            .sessionId(sessionId)
            .buildAsyncClient();

        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage("Hello".getBytes(UTF_8)).setSessionId(sessionId),
            new ServiceBusMessage("Bonjour".getBytes(UTF_8)).setSessionId(sessionId),
            new ServiceBusMessage("Guten tag".getBytes(UTF_8)).setSessionId(sessionId)
        );

        // Create a message batch and send all messages.
        // This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.createBatch().flatMap(batch -> {
            for (ServiceBusMessage message : messages) {
                // There are only 3 messages, they should all fit in a batch. In a production case, we would send the
                // full batch, and then create another one to add this message into.
                if (!batch.tryAdd(message)) {
                    return Mono.error(new IllegalStateException("Should have been able to add message to batch."));
                }
            }

            // Publish the batch since we are done.
            return sender.sendMessages(batch);
        }).subscribe(unused -> System.out.println("Batch sent."),
            error -> System.err.println("Error occurred while publishing message batch: " + error),
            () -> System.out.println("Batch send complete."));

        // After sending that message, we receive the messages for that sessionId.
        receiver.receiveMessages().flatMap(context -> {
            ServiceBusReceivedMessage message = context.getMessage();

            System.out.println("Received Message Id: " + message.getMessageId());
            System.out.println("Received Message Session Id: " + message.getSessionId());
            System.out.println("Received Message: " + new String(message.getBody()));

            return receiver.complete(message);
        }).subscribe();

        // subscribe() is not a blocking call. We sleep here so the program does not end before the send is complete.
        TimeUnit.SECONDS.sleep(10);

        // Close the sender and receiver.
        sender.close();
        receiver.close();
    }
}
