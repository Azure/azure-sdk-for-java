// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.queue.QueueServiceClient;

import java.util.UUID;

public class QueueServiceSample {
    private static final String accountName = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String sasToken = System.getenv("PRIMARY_SAS_TOKEN");

    public static void main(String[] args) {
        // Build Queue Service Client using SAS Token
        String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", accountName, sasToken);
        QueueServiceClient queueServiceClient = QueueServiceClient.builder().endpoint(queueServiceURL).build();
        queueServiceClient.createQueue(generateRandomName("create-queue", 16));

        // Create another queue and list all queues, print the name and then delete the queue.
        queueServiceClient.createQueue(generateRandomName("create-extra" , 16));
        queueServiceClient.listQueues().forEach(
            queueItem -> {
                System.out.println("The queue name is: " + queueItem.name());
                queueServiceClient.deleteQueue(queueItem.name());
            }
        );
    }

    private static String generateRandomName(String prefix, int length) {
        int len = length > prefix.length() ? length - prefix.length() : 0;
        return prefix + UUID.randomUUID().toString().substring(0, len);
    }

}
