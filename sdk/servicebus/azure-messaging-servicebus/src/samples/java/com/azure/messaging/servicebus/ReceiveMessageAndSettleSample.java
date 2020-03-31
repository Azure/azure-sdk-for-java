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
    private static final Duration TIME_OUT = Duration.ofSeconds(15);

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
            .isAutoComplete(false)
            .maxAutoLockRenewalDuration(Duration.ofSeconds(2))
            .buildAsyncClient();

        Disposable subscription = receiverAsyncClient.receive()
            .subscribe(received -> {
                Instant latest = Instant.MIN;

                // Simulate some sort of long processing. Sleep should not be used in production system.
                for (int i = 0; i < 2; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException error) {
                        System.out.println("Error occurred while sleeping: " + error);
                    }
                    latest = received.getLockedUntil();
                    System.out.println("Message Locked Until " + latest);
                }

                // This is application business logic to take action based on some application logic.
                // For demo purpose we are using a property for application logic.
                String actionToTake = null;

                String payload = new String(received.getBody());

                if (payload.contains("complete")) {
                    actionToTake = "COMPLETE";
                } else if (payload.contains("abandon")) {
                    actionToTake = "ABANDON";
                } else if (payload.contains("defer")) {
                    actionToTake = "DEFER";
                }

                switch (actionToTake) {
                    case "COMPLETE":
                        System.out.println("Completing message.");
                        receiverAsyncClient.complete(received).block(TIME_OUT);
                        break;
                    case "ABANDON":
                        System.out.println("Abandon message.");
                        receiverAsyncClient.abandon(received).block(TIME_OUT);
                        break;
                    case "DEFER":
                        System.out.println("Defer message.");
                        receiverAsyncClient.defer(received).block(TIME_OUT);
                        break;
                    default:
                        System.out.println("Deadletter message.");
                        receiverAsyncClient.deadLetter(received).block(TIME_OUT);
                }
            });

        // Receiving messages from the queue for a duration of 20 seconds.
        // Subscribe is not a blocking call so we sleep here so the program does not end.
        try {
            Thread.sleep(Duration.ofSeconds(60).toMillis());
        } catch (InterruptedException ignored) {
        }
        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiverAsyncClient.close();
    }
}
