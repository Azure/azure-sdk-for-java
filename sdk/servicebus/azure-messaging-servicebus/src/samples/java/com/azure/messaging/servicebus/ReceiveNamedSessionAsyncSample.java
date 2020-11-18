// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * Demonstrates how to receive messages from a named session.
 */
public class ReceiveNamedSessionAsyncSample {
    /**
     * Main method to invoke this demo on how to receive messages from a session with id "greetings" in an Azure Service
     * Bus Queue.
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
        // "<<queue-name>>" will be the name of the Service Bus session-enabled queue instance you created inside the
        // Service Bus namespace.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptSession("greetings");
        Disposable subscription = receiverMono.flatMapMany(receiver -> receiver.receiveMessages()
            .flatMap(message -> {

                System.out.println("Processing message from session: " + message.getSessionId());

                // Process message then complete it.
                //return receiver.complete(context.getMessage());
                return Mono.empty();
            }))
            .subscribe(aVoid -> {
            }, error -> System.err.println("Error occurred: " + error));

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(10);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        sessionReceiver.close();
    }
}
