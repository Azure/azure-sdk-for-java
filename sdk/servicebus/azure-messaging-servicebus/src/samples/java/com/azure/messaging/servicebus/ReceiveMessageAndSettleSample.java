// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue and settle
 * it. Settling of message include accept, defer and abandon the message as needed.
 */
public class ReceiveMessageAndSettleSample {
    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusMessage} from an Azure Service Bus
     * Queue
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING");

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.

        ServiceBusReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildReceiverClientBuilder()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .isLockAutoRenewed(true)
            .queueName("<<queue-name>>")
            .maxAutoLockRenewalDuration(Duration.ofSeconds(2))
            .buildAsyncClient();

        Disposable subscription = receiverAsyncClient.receive()
            .doOnNext(received -> {
                final Instant initial = received.getLockedUntil();
                Instant latest = Instant.MIN;

                // Simulate some sort of long processing. Sleep should not be used in production system.
                for (int i = 0; i < 3; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException error) {
                        System.out.println("Error occurred while sleeping: " + error);
                    }
                    latest = received.getLockedUntil();
                    System.out.println("Message Locked Until " + latest);
                }

                System.out.println("Completing message.");
                receiverAsyncClient.complete(received).block(Duration.ofSeconds(15));
            })
            .subscribe();

        // Receiving messages from the queue for a duration of 20 seconds.
        // Subscribe is not a blocking call so we sleep here so the program does not end.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiverAsyncClient.close();
    }
}
