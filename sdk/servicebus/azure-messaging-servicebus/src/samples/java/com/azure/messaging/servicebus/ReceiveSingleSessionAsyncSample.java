// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to receive from the first available session.
 */
public class ReceiveSingleSessionAsyncSample {
    /**
     * Main method to invoke this demo on how to receive messages from the first available session in a Service Bus
     * topic subscription.
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

        // Create a receiver.
        // "<<topic-name>>" will be the name of the Service Bus topic you created inside the Service Bus namespace.
        // "<<subscription-name>>" will be the name of the session-enabled subscription.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .topicName("<<topic-name>>")
            .subscriptionName("<<subscription-name>>")
            .buildAsyncClient();

        // Messages are continuously received until there are no more messages in the current session. Then, the Flux
        // completes.
        Disposable subscription = receiver.receive()
            .subscribe(context -> {
                if (context.hasError()) {
                    System.out.printf("An error occurred in session %s. Error: %s%n",
                        context.getSessionId(), context.getThrowable());
                    return;
                }

                System.out.println("Processing message from session: " + context.getSessionId());

                // Process message
                // The message is automatically completed if no exceptions are thrown while processing message.
            }, error -> {
                    System.err.println("Error occurred: " + error);
                });

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(60);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }
}
