// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

/*
 *  This example mimics some arbitrary number of clients continuously sending messages up to a queue in a parallel and
 *  a server dequeuing the messages and processing them.
 */
public class AsyncSamples1 {
    private static final String ACCOUNT_NAME = "shilistorage";
    private static final String SAS_TOKEN = "?sv=2021-06-08&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2022-07-18T17:31:21Z&st=2022-07-14T09:31:21Z&spr=https&sig=2h8Q%2Fj3aV6ICAESMY50ltof14O%2B%2BuVvC%2BQorPIjsog8%3D";
    private static final String QUEUE_NAME = generateRandomName("async-call", 16);

    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client.
     * @param args No args needed for main method.
     */
    public static void main(String[] args) {
        // Create an async queue client.
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, "aaaa", SAS_TOKEN);
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsyncClient();

        // Create a queue, enqueue two messages.
        queueAsyncClient.create()
//            .doOnSuccess(response -> System.out.println("aaaa"))
            .subscribe(
                response -> System.out.println(
                    "Message successfully equeueed by queueAsyncClient. Message id:" + response.toString()),
                err -> System.out.println("Error thrown when enqueue the message. Error message: " + err.getMessage()),
                () -> System.out.println("The enqueue has been completed."));
    }
}
