// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.identity.ClientSecretCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Sample demonstrates how to send {@link ServiceBusMessageBatch} to an Azure Service Bus Topic with the synchronous
 * sender.
 */
public class SendMessageBatch {

    private static final String TAG = "ServiceBusSendMessageBatchOutput";

    public static void main(String queueName, ClientSecretCredential credential) {
        List<ServiceBusMessage> testMessages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromString("Green")),
            new ServiceBusMessage(BinaryData.fromString("Red")),
            new ServiceBusMessage(BinaryData.fromString("Blue")),
            new ServiceBusMessage(BinaryData.fromString("Orange")));

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(credential)
            .sender()
            .queueName(queueName)
            .buildClient();

        if (sender.getFullyQualifiedNamespace().isEmpty()) {
            throw new RuntimeException("Sample was not successful: fullyQualifiedNamespace is empty");
        }

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
                Log.e(TAG, String.format("Message is too large for an empty batch. Skipping. Max size: %s. Message: %s%n",
                    currentBatch.getMaxSizeInBytes(), message.getBody().toString()));
            }
        }

        sender.sendMessages(currentBatch);

        //close the client
        sender.close();
    }
}
