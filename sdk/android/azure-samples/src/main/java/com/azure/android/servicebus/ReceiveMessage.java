// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.identity.ClientSecretCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

import android.util.Log;

import java.time.Duration;

/**
 * Sample demonstrates how to receive a batch of {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue using
 * sync client.
 *
 * Messages <b>must</b> be manually settled.
 */
public class ReceiveMessage {

    private static final String TAG = "ServiceBusReceiveMessageOutput";

    /**
     * Main method to invoke this demo on how to receive a set of {@link ServiceBusMessage messages} from an Azure
     * Service Bus Queue.
     *
     */
    public static void main(String queueName, ClientSecretCredential credential) {

        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(credential)
            .receiver()
            .maxAutoLockRenewDuration(Duration.ofMinutes(1))
            .queueName(queueName)
            .buildClient();

        // Try to receive a set of messages from Service Bus 10 times. A batch of messages are returned when 5 messages
        // are received, or the operation timeout has elapsed, whichever occurs first.
        for (int i = 0; i < 10; i++) {

            receiver.receiveMessages(5).stream().forEach(message -> {
                // Process message. The message lock is renewed for up to 1 minute.
                Log.i(TAG, String.format("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(), message.getBody()));

                // Messages from the sync receiver MUST be settled explicitly.
                receiver.complete(message);
            });
        }

        // Close the receiver.
        receiver.close();
    }
}
