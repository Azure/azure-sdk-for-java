// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Demonstrates how to receive messages from a named session using {@link ServiceBusReceiverClient}.
 * The sample below runs for 2 minutes. In those two minutes, it will poll Service Bus for batches of messages.
 */
public class ReceiveNamedSessionSample {

    /**
     * Main method to invoke this demo on how to receive messages from a session with id "greetings" in an Azure Service
     * Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        final AtomicBoolean isRunning = new AtomicBoolean(true);

        Mono.delay(Duration.ofMinutes(2)).subscribe(index -> {
            System.out.println("2 minutes has elapsed, stopping receive loop.");
            isRunning.set(false);
        });

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a receiver.
        // "<<queue-name>>" will be the name of the Service Bus session-enabled queue instance you created inside the
        // Service Bus namespace.
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .sessionId("greetings")
            .queueName("<<queue-name>>")
            .buildClient();

        while (isRunning.get()) {
            IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receive(10, Duration.ofSeconds(30));

            for (ServiceBusReceivedMessageContext context : messages) {
                System.out.println("Processing message from session: " + context.getSessionId());

                ServiceBusReceivedMessage message = context.getMessage();
                boolean isSuccessfullyProcessed = processMessage(message);

                if (isSuccessfullyProcessed) {
                    receiver.complete(message);
                } else {
                    receiver.abandon(message);
                }
            }
        }

        // Close the receiver.
        receiver.close();
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.println("Processing message: " + message.getMessageId());
        return true;
    }
}
