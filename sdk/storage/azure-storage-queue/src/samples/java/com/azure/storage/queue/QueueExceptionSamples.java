// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueStorageException;

import java.time.Duration;

import static com.azure.storage.queue.SampleHelper.generateRandomName;

public class QueueExceptionSamples {

    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");

    /**
     * The main method shows how to handle the storage exception.
     *
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
            queueClientResponse = queueServiceClient.createQueueWithResponse(generateRandomName("delete-not-exist",
                16), null, Duration.ofSeconds(1), new Context("key1", "value1"));
            System.out.println("Successfully create the queue! Status code: " + queueClientResponse.getStatusCode());
        } catch (QueueStorageException e) {
            System.out.println(String.format("Error creating a queue. Error message: %s", e.getServiceMessage()));
            throw new RuntimeException(e);
        }
        QueueClient queueClient = queueClientResponse.getValue();
        queueClient.sendMessage("Hello, message 1!");
        queueClient.sendMessage("Hello, message 2!");

        // Delete message with wrong message id.
        try {
            QueueMessageItem queueMessageItem = queueClientResponse.getValue().receiveMessage();
            queueClient.deleteMessage("wrong id", queueMessageItem.getPopReceipt());
        } catch (QueueStorageException e) {
            if (QueueErrorCode.MESSAGE_NOT_FOUND.equals(e.getErrorCode())) {
                System.out.println("This is the error expected to throw");
            } else {
                System.out.println("This is not the error we expect!");
            }
        }

        // Delete message with wrong pop receipt.
        try {
            QueueMessageItem queueMessageItem = queueClientResponse.getValue().receiveMessage();
            queueClient.deleteMessage(queueMessageItem.getMessageId(), "Wrong Pop Receipt");
        } catch (QueueStorageException e) {
            if (QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE.equals(e.getErrorCode())) {
                System.out.println("This is the error expected to throw");
            } else {
                System.out.println("This is not the error we expect!");
            }
        }
    }
}
