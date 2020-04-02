// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.CreateBatchOptions;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SendMessageBatchSyncSample {

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessageBatch} to an Azure Service Bus.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        List<ServiceBusMessage> testMessages = Arrays.asList(
            new ServiceBusMessage("Green".getBytes(UTF_8)),
            new ServiceBusMessage("Red".getBytes(UTF_8)),
            new ServiceBusMessage("Blue".getBytes(UTF_8)),
            new ServiceBusMessage("Orange".getBytes(UTF_8)));

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a Queue or Topic in that Service Bus namespace.
        String queueName = "queueName";

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        // Creates an ServiceBusMessageBatch where the ServiceBus.
        ServiceBusMessageBatch currentBatch = senderClient.createBatch(
            new CreateBatchOptions().setMaximumSizeInBytes(1024));

        // We try to add as many messages as a batch can fit based on the maximum size and send to Service Bus when
        // the batch can hold no more messages. Create a new batch for next set of messages and repeat until all
        // messages are sent.
        for (ServiceBusMessage message : testMessages) {
            if (currentBatch.tryAdd(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            senderClient.send(currentBatch);
            currentBatch = senderClient.createBatch();

            // Add that message that we couldn't before.
            if (!currentBatch.tryAdd(message)) {
                System.err.printf("Message is too large for an empty batch. Skipping. Max size: %s. Message: %s%n",
                    currentBatch.getMaxSizeInBytes(), new String(message.getBody(), UTF_8));
            }
        }

        senderClient.send(currentBatch);

        //close the client
        senderClient.close();
    }

}
