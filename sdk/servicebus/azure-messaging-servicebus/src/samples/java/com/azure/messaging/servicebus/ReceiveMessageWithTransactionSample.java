// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;

public class ReceiveMessageWithTransactionSample {
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
        connectionString = "Endpoint=sb://sbtrack2-hemanttest-prototype.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=7uJdC9utZi6pxJ2trk4MmiiEyuHltIz1Oyejp1jZRgM=";
        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        ServiceBusReceiverClient receiverClient = builder
            .receiver()
            .queueName("<<queue-name>>")
            .queueName("hemant-test1")
            .buildClient();

        // Transaction is actually started in ServiceBus until you perform first operation
        // (Example receiver.complete(message, transaction)) with it.
        // Create transaction
        ServiceBusTransactionContext transactionContext = receiverClient.createTransaction();

        final IterableStream<ServiceBusReceivedMessageContext> receivedMessages =
            receiverClient.receive(5);

        receivedMessages.stream().forEach(context -> {
            ServiceBusReceivedMessage message = context.getMessage();

            System.out.println("Received Message Id: " + message.getMessageId());
            System.out.println("Received Message: " + new String(message.getBody()));

            boolean messageProcessed = true;
            // Process the context and its message here.
            // Change the `messageProcessed` according to you business logic and if you are able to process the
            // message successfully.

            if (messageProcessed) {
                receiverClient.complete(message, transactionContext);
            } else {
                receiverClient.abandon(message, null, transactionContext);
            }
        });

        // Commit the transaction
        receiverClient.commitTransaction(transactionContext);
        System.out.println("Transaction is committed.");

        // Close the receiver.
        receiverClient.close();
    }
}
