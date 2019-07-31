// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.models.DequeuedMessage;

import java.time.Duration;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

public class MessageSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");

    /**
     * The main method illustrate the basic operations for enqueue and dequeue messages using sync client.
     * @param args No args needed for main method.
     * @throws InterruptedException If the Thread.sleep operation gets interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        // Build Queue Client using SAS Token
        String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();

        // Create a queue client
        Response<QueueClient> queueClientResponse = queueServiceClient.createQueue(generateRandomName("enqueue", 16));
        QueueClient queueClient = queueClientResponse.value();
        // Using queue client to enqueue several "Hello World" messages into queue.
        for (int i = 0; i < 3; i++) {
            queueClient.enqueueMessage("Hello World");
        }

        // Enqueue json file into message.
        // TODO

        // Get the total count of msg in the queue
        int count = queueClient.getProperties().value().approximateMessagesCount();

        // Peek all messages in queue. It is supposed to print "Hello World" 3 times.
        queueClient.peekMessages(count).forEach(
            peekedMessage -> {
                System.out.println("Here is the msg: " + peekedMessage.messageText());
            }
        );

        // Dequeue all messages in queue and update the message "Hello World" to Hello, world!"
        queueClient.dequeueMessages(count, Duration.ZERO).forEach(
            queueMessage -> {
                String msgToReplace = String.format("Hello, world!");
                queueClient.updateMessage(queueMessage.messageId(), msgToReplace, queueMessage.popReceipt(), Duration.ZERO);
            }
        );

        // Delete the first available msg.
        // Since there is no invisible time for above dequeue, the following if condition should be true.
        if (queueClient.dequeueMessages().iterator().hasNext()) {
            DequeuedMessage queueMessage = queueClient.dequeueMessages().iterator().next();
            queueClient.deleteMessage(queueMessage.messageId(), queueMessage.popReceipt());
        } else {
            System.out.println("OOps, the messages disappear!");
        }

        // Clear all messages in the queue
        // Sleep to guarantee we skip the default invisible time.
        Thread.sleep(500);
        queueClient.clearMessages();

        // Finally, we delete the queue.
        queueClient.delete();
    }
}
