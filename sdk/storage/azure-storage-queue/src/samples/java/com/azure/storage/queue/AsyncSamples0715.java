// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

/*
 *  This example mimics some arbitrary number of clients continuously sending messages up to a queue in a parallel and
 *  a server dequeuing the messages and processing them.
 */
public class AsyncSamples0715 {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSamples.class);
    private static final String ACCOUNT_NAME = "shilistorage";
    private static final String SAS_TOKEN = "?sv=2021-06-08&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2022-07-18T17:31:21Z&st=2022-07-14T09:31:21Z&spr=https&sig=2h8Q%2Fj3aV6ICAESMY50ltof14O%2B%2BuVvC%2BQorPIjsog8%3D";
    private static final String QUEUE_NAME = generateRandomName("async-call", 16);
    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client.
     * @param args No args needed for main method.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create an async queue client.
//        final String CONNECTION_STRING = "BlobEndpoint=https://shilistorage.blob.core.windows.net/;QueueEndpoint=https://shilistorage.queue.core.windows.net/;FileEndpoint=https://shilistorage.file.core.windows.net/;TableEndpoint=https://shilistorage.table.core.windows.net/;SharedAccessSignature=sv=2021-06-08&ss=bfqt&srt=sco&sp=rwdlacupiytfx&se=2022-07-20T12:29:51Z&st=2022-07-15T04:29:51Z&spr=https&sig=Ah7nBNP7cI2ZhGxKhsxeXXlRn3nPkMzzAocnDbOCHEA%3D";
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, SAS_TOKEN);
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
                .endpoint(queueURL)
//                .connectionString(CONNECTION_STRING).queueName("NEW-" + QUEUE_NAME)
                .buildAsyncClient();
        // Create a queue, enqueue a message.
        System.out.println("Create queue name: " + QUEUE_NAME);
        queueAsyncClient.create()
                .subscribe(
                        response -> {},
                        error -> System.err.print(error.toString()),
                        () -> System.out.println("Finished queue creating.")
                );
        System.out.println("Waiting queue creation...");
        TimeUnit.SECONDS.sleep(10);
        queueAsyncClient.sendMessage("This message is the first from AsyncSamples.")
                .subscribe(
                        response -> {
                        },
                        error -> System.err.print(error.toString()),
                        () -> System.out.println("The enqueue has been completed.")
                );
//        queueAsyncClient.clearMessages()
//                .subscribe(
//                        response -> {},
//                        error -> System.err.print(error.toString()),
//                        () -> System.out.println("The enqueue has been completed.")
//                );
    }
}
