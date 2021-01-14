// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to receive {@link ServiceBusReceivedMessage messages} from an Azure Service Bus Queue using
 * connection string.
 *
 * By default, messages are <b>settled automatically</b> via {@link ServiceBusReceiverAsyncClient#complete(ServiceBusReceivedMessage)
 * complete} or {@link ServiceBusReceiverAsyncClient#abandon(ServiceBusReceivedMessage) abandon}.
 *
 * A message is abandoned if an exception occurs downstream while the message is processed.
 */
public class ReceiveMessageAsyncSample {
    /**
     * Main method to invoke this demo on how to receive {@link ServiceBusReceivedMessage messages} from an Azure
     * Service Bus Queue.
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
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages().subscribe(message -> {
            // Process message. If an exception is thrown from this consumer, the message is abandoned.
            // Otherwise, it is completed.
            // Automatic message settlement can be disabled via disableAutoComplete() when creating the receiver
            // client. Consequently, messages have to be manually settled.
            System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody());
        },
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
}
