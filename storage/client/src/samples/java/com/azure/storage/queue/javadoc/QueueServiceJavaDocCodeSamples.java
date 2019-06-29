// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link QueueServiceAsyncClient}.
 */
public class QueueServiceJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@link QueueServiceClient}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.sastoken
        QueueServiceClient queueServiceClient = QueueServiceClient.builder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .build();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.sastoken
        return queueServiceClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken
        QueueServiceAsyncClient queueServiceAsyncClient = QueueServiceAsyncClient.builder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsync();
        // END: com.azure.storage.file.queueServiceAsyncClient.instantiation.sastoken
        return queueServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceClient queueServiceClient = QueueServiceClient.builder()
            .connectionString(connectionString)
            .build();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        return queueServiceClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceAsyncClient queueServiceAsyncClient = QueueServiceAsyncClient.builder()
            .connectionString(connectionString)
            .buildAsync();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        return queueServiceAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#createQueue(String)}
     */
    public void createQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueue#string
        Response<QueueClient> response = queueServiceClient.createQueue("myqueue");
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueue(String)}
     */
    public void createQueueAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
        queueServiceAsyncClient.createQueue("myqueue").subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
    }


    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues()}
     */
    public void listQueues() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues
        queueServiceClient.listQueues().forEach(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#listQueues()}
     */
    public void listQueuesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.listQueues
        queueServiceAsyncClient.listQueues().subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#deleteQueue(String)}
     */
    public void deleteQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueue#string
        VoidResponse response = queueServiceClient.deleteQueue("myqueue");
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.deleteQueue#string
    }


    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueue(String)}
     */
    public void deleteQueueAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
        queueServiceAsyncClient.deleteQueue("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
    }
}
