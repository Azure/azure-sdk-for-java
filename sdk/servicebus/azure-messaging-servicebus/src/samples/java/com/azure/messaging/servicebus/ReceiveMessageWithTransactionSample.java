package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;

import java.util.concurrent.atomic.AtomicBoolean;

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

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        ServiceBusReceiverClient receiverClient = builder
            .receiver()
            .queueName("<<queue-name>>")
            .buildClient();

        TransactionManager syncTransactionManager = builder.buildTransactionManager();

        Transaction transaction = syncTransactionManager.beginTransaction();

        final IterableStream<ServiceBusReceivedMessageContext> receivedMessages =
            receiverClient.receive(5);

        AtomicBoolean processed = new AtomicBoolean(false);
        receivedMessages.stream().forEach(context -> {
            ServiceBusReceivedMessage message = context.getMessage();

            System.out.println("Received Message Id: " + message.getMessageId());
            System.out.println("Received Message: " + new String(message.getBody()));

            receiverClient.complete(message, transaction);
            // set flag appropriately based on message processing result
            processed.set(true);
        });

        syncTransactionManager.endTransaction(transaction, processed.get());

        // Close the receiver.
        receiverClient.close();
    }
}
