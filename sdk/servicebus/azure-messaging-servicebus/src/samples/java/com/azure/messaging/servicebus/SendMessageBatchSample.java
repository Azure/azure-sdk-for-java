// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to send {@link ServiceBusMessageBatch} to an Azure Service Bus Topic with the synchronous
 * sender.
 */
public class SendMessageBatchSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");

    /**
     * Main method to invoke this demo on how to send a {@link ServiceBusMessageBatch} to an Azure Service Bus Topic.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        SendMessageBatchSample sample = new SendMessageBatchSample();
        sample.run();
    }

    /**
     * This method to invoke this demo on how to send a {@link ServiceBusMessageBatch} to an Azure Service Bus Topic.
     */
    @Test
    public void run() {
        List<ServiceBusMessage> testMessages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromString("Green")),
            new ServiceBusMessage(BinaryData.fromString("Red")),
            new ServiceBusMessage(BinaryData.fromString("Blue")),
            new ServiceBusMessage(BinaryData.fromString("Orange")));

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "topicName" will be the name of the Service Bus topic instance you created in the Service Bus namespace.

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .topicName(topicName)
            .buildClient();

        // Creates an ServiceBusMessageBatch where the ServiceBus.
        // If no maximumSizeInBatch is set, the maximum message size is used.
        ServiceBusMessageBatch currentBatch = sender.createMessageBatch(
            new CreateMessageBatchOptions().setMaximumSizeInBytes(1024));

        // We try to add as many messages as a batch can fit based on the maximum size and send to Service Bus when
        // the batch can hold no more messages. Create a new batch for next set of messages and repeat until all
        // messages are sent.
        for (ServiceBusMessage message : testMessages) {
            if (currentBatch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            sender.sendMessages(currentBatch);
            currentBatch = sender.createMessageBatch();

            // Add that message that we couldn't before.
            if (!currentBatch.tryAddMessage(message)) {
                System.err.printf("Message is too large for an empty batch. Skipping. Max size: %s. Message: %s%n",
                    currentBatch.getMaxSizeInBytes(), message.getBody().toString());
            }
        }

        sender.sendMessages(currentBatch);

        //close the client
        sender.close();
    }
}
