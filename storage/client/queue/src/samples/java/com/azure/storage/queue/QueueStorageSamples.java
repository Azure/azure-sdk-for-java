// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

public class QueueStorageSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");

    /**
     * The main method illustrated the basic operations of creating and deleting queues using queue service sync client.
     * @param args No args needed for main method.
     */
    public static void main(String[] args) {
        // Build Queue Service Client using SAS Token
        String queueStorageURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
        QueueStorageClient queueStorageClient = new QueueStorageClientBuilder().endpoint(queueStorageURL).buildClient();
        queueStorageClient.createQueue(generateRandomName("create-queue", 16));

        // Create another queue and list all queues, print the name and then delete the queue.
        queueStorageClient.createQueue(generateRandomName("create-extra", 16));
        queueStorageClient.listQueues().forEach(
            queueItem -> {
                System.out.println("The queue name is: " + queueItem.name());
                queueStorageClient.deleteQueue(queueItem.name());
            }
        );
    }
}
