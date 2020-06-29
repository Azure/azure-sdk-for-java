// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to receive {@link ServiceBusReceivedMessage messages} from an Azure Service Bus Queue using
 * connection string.
 */
public class ReceiveMessageAsyncSample {
    /**
     * Main method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages} from an Azure
     * Service Bus Queue.
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

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
            .flatMap(context -> {
                ServiceBusReceivedMessage message = context.getMessage();

                // process message
                System.out.println("Received Message Id: " + message.getMessageId());
                System.out.println("Received Message: " + new String(message.getBody()));

                boolean isSuccessfullyProcessed = processMessage(message);

                // When we are finished processing the message, then complete or abandon it.
                if (isSuccessfullyProcessed) {
                    return receiver.complete(message).thenReturn("Completed: " + message.getMessageId());
                } else {
                    return receiver.abandon(message).thenReturn("Abandoned: " + message.getMessageId());
                }
            })
            .subscribe(message -> System.out.printf("Processed at %s. %s%n", Instant.now(), message),
                error -> System.err.println("Error occurred while receiving message: " + error),
                () -> System.out.println("Receiving complete."));

        // Receiving messages from the queue for a duration of 20 seconds.
        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(20);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        return true;
    }
}
