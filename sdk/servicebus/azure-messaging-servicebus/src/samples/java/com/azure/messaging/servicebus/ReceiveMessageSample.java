// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.time.Duration;

/**
 * Sample demonstrates how to receive a batch of {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue using
 * sync client.
 *
 * Messages <b>must</b> be manually settled.
 */
public class ReceiveMessageSample {
    /**
     * Main method to invoke this demo on how to receive a set of {@link ServiceBusMessage messages} from an Azure
     * Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

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
        // Each message's lock is renewed up to 1 minute.
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .maxAutoLockRenewDuration(Duration.ofMinutes(1))
            .queueName("<<queue-name>>")
            .buildClient();

        // Try to receive a set of messages from Service Bus 10 times. A batch of messages are returned when 5 messages
        // are received, or the operation timeout has elapsed, whichever occurs first.
        for (int i = 0; i < 10; i++) {

            receiver.receiveMessages(5).stream().forEach(message -> {
                // Process message. The message lock is renewed for up to 1 minute.
                System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody());

                // Messages from the sync receiver MUST be settled explicitly.
                receiver.complete(message);
            });
        }

        // Close the receiver.
        receiver.close();
    }
}
