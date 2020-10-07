// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;

/**
 * Sample demonstrates how to receive a batch of {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue using
 * sync client.
 */
public class ReceiveMessageSyncSample {
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
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName("<<queue-name>>")
            .buildClient();

        // Try to receive a set of messages from Service Bus 10 times. A batch of messages are returned when 5 messages
        // are received, or the operation timeout has elapsed, whichever occurs first.
        for (int i = 0; i < 10; i++) {
            final IterableStream<ServiceBusReceivedMessageContext> receivedMessages =
                receiver.receiveMessages(5);

            receivedMessages.stream().forEach(context -> {
                ServiceBusReceivedMessage message = context.getMessage();

                System.out.println("Received Message Id: " + message.getMessageId());
                System.out.println("Received Message: " + new String(message.getBody()));

                receiver.complete(message);
            });
        }

        // Close the receiver.
        receiver.close();
    }
}
