// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.models.StorageErrorCode;
import com.azure.storage.queue.models.StorageErrorException;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

public class QueueExceptionSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");

    /**
     * The main method shows how to handle the storage exception.
     * @param args No args needed for the main method.
     * @throws RuntimeException If queueServiceClient failed to create a queue.
     */
    public static void main(String[] args) {
        // Create a queue service client.
        String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();

        // Create queue client.
        Response<QueueClient> queueClientResponse;
        try {
            queueClientResponse = queueServiceClient.createQueue(generateRandomName("delete-not-exist", 16));
            System.out.println("Successfully create the queue! Status code: " + String.valueOf(queueClientResponse.statusCode()));
        } catch (StorageErrorException e) {
            System.out.println(String.format("Error creating a queue. Error message: %s", e.value().message()));
            throw new RuntimeException(e);
        }
        QueueClient queueClient = queueClientResponse.value();
        queueClient.enqueueMessage("Hello, message 1!");
        queueClient.enqueueMessage("Hello, message 2!");

        // Delete message with wrong message id.
        try {
            queueClientResponse.value().dequeueMessages().forEach(
                msg -> {
                    queueClient.deleteMessage("wrong id", msg.popReceipt());
                }
            );
        } catch (StorageErrorException e) {
            if (e.getMessage().contains(StorageErrorCode.MESSAGE_NOT_FOUND.toString())) {
                System.out.println("This is the error expected to throw");
            } else {
                System.out.println("This is not the error we expect!");
            }
        }

        // Delete message with wrong pop receipt.
        try {
            queueClient.dequeueMessages().forEach(
                msg -> {
                    queueClient.deleteMessage(msg.messageId(), "Wrong Pop Receipt");
                }
            );
        } catch (StorageErrorException e) {
            if (e.getMessage().contains(StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE.toString())) {
                System.out.println("This is the error expected to throw");
            } else {
                System.out.println("This is not the error we expect!");
            }
        }
    }
}
