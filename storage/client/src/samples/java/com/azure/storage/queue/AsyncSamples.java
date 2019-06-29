// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import java.util.UUID;

/*
 *  This example mimics some arbitrary number of clients continuously sending messages up to a queue in a parallel and
 *  a server dequeuing the messages and processing them.
 */
public class AsyncSamples {
    private static final String accountName = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String sasToken = System.getenv("PRIMARY_SAS_TOKEN");
    private static final String queueName = generateRandomName("async-call", 16);

    public static void main(String[] args) {
        // Create an async queue client.
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", accountName, queueName, sasToken);
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsync();

        // Create a queue, enqueue two messages.
        queueAsyncClient.create()
            .flatMap(response -> queueAsyncClient.enqueueMessage("This is message 1"))
            .flatMap(response -> queueAsyncClient.enqueueMessage("This is message 2"))
            .subscribe(
                response -> {
                    System.out.println("Message successfully equeueed by queueAsyncClient. Message id:" + response.value().messageId());
                },
                err -> {
                    System.out.println("Error thrown when enqueue the message. Error message: " + err.getMessage());
                },
                () -> {
                    System.out.println("The enqueue has been completed.");
                }
            );
    }

    private static String generateRandomName(String prefix, int length) {
        int len = length > prefix.length() ? length - prefix.length() : 0;
        return prefix + UUID.randomUUID().toString().substring(0, len);
    }
}
