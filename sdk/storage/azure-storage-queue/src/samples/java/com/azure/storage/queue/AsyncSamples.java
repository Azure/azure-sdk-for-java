// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

/*
 *  This example mimics some arbitrary number of clients continuously sending messages up to a queue in a parallel and
 *  a server dequeuing the messages and processing them.
 */
public class AsyncSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");
    private static final String QUEUE_NAME = generateRandomName("async-call", 16);

    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client.
     * @param args No args needed for main method.
     */
    public static void main(String[] args) {
        // Create an async queue client.
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, SAS_TOKEN);
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsyncClient();

        // Create a queue, enqueue two messages.
        queueAsyncClient.create()
            .doOnSuccess(response -> queueAsyncClient.sendMessage("This is message 1"))
            .then(queueAsyncClient.sendMessage("This is message 2"))
            .subscribe(
                response -> System.out.println(
                    "Message successfully equeueed by queueAsyncClient. Message id:" + response.getMessageId()),
                err -> System.out.println("Error thrown when enqueue the message. Error message: " + err.getMessage()),
                () -> System.out.println("The enqueue has been completed."));
    }
}
