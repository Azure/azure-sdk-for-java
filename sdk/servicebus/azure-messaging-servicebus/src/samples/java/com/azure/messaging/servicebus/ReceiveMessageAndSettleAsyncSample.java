// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue and settle
 * it <b>manually</b>.
 *
 * Messages can be settled via:
 * <ul>
 * <li>{@link ServiceBusReceiverAsyncClient#complete(ServiceBusReceivedMessage) complete}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#defer(ServiceBusReceivedMessage) defer}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#abandon(ServiceBusReceivedMessage) abandon}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#deadLetter(ServiceBusReceivedMessage) dead-letter}</li>
 * </ul>
 *
 */
public class ReceiveMessageAndSettleAsyncSample {

    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus
     * Queue
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
        // 1. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 2. "<<queue-name>>" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.
        // 3. Messages are not automatically settled when `disableAutoComplete()` is toggled.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .disableAutoComplete()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
            .flatMap(message -> {
                boolean messageProcessed = processMessage(message);

                // Process the context and its message here.
                // Change the `messageProcessed` according to you business logic and if you are able to process the
                // message successfully.
                // Messages MUST be manually settled because automatic settlement was disabled when creating the
                // receiver.
                if (messageProcessed) {
                    return receiver.complete(message);
                } else {
                    return receiver.abandon(message);
                }
            }).subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(60);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
            message.getBody());

        return true;
    }
}
