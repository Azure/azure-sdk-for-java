// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.UpdatedMessage;
import java.time.Duration;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueClient} and {@link QueueAsyncClient}.
 */

public class QueueJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link QueueClient} with {@link QueueClient}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.sastoken
        QueueClient queueClient = QueueClient.builder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .build();
        // END: com.azure.storage.file.queueClient.instantiation.sastoken
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.sastoken
        QueueAsyncClient queueAsyncClient = QueueAsyncClient.builder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsync();
        // END: com.azure.storage.file.queueAsyncClient.instantiation.sastoken
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueClient queueClient = QueueClient.builder()
            .connectionString(connectionString)
            .build();
        // END: com.azure.storage.queue.queueClient.instantiation.connectionstring
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueAsyncClient queueAsyncClient = QueueAsyncClient.builder()
            .connectionString(connectionString)
            .buildAsync();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        return queueAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueClient#create()}
     */
    public void createQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.create
        VoidResponse response = queueClient.create();
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.create
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create()}
     */
    public void createQueueAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create
        queueAsyncClient.create().subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create
    }


    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String)}
     */
    public void enqueueMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessage#string
        Response<EnqueuedMessage> response = queueClient.enqueueMessage("hello msg");
        System.out.println("Complete enqueuing the message with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String)}
     */
    public void enqueueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
        queueAsyncClient.enqueueMessage("hello msg").subscribe(
            response -> {},
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages()}
     */
    public void dequeueMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                System.out.println("Complete dequeuing the message: " + dequeuedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.queueClient.dequeueMessages
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages()}
     */
    public void dequeueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: " + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages()}
     */
    public void peekMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.peekMessages
        queueClient.peekMessages().forEach(
            peekedMessage -> {
                System.out.println("Complete peeking the message: " + peekedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.queueClient.peekMessages
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages()}
     */
    public void peekMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages
        queueAsyncClient.peekMessages().subscribe(
            peekMessages -> System.out.println("The message got from peek operation: " + peekMessages.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.updateMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<UpdatedMessage> response = queueClient.updateMessage("newText", dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), null);
                System.out.println("Complete updating the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.updateMessage
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.updateMessage
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.updateMessage("newText", dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), null).subscribe(
                    response -> {},
                    updateError -> System.err.print(updateError.toString()),
                    () -> System.out.println("Complete updating the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.updateMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.deleteMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                VoidResponse response = queueClient.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt());
                System.out.println("Complete deleting the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.deleteMessage
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessage(String, String)}
     */
    public void deleteMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.deleteMessage
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()).subscribe(
                    response -> {},
                    deleteError -> System.err.print(deleteError.toString()),
                    () -> System.out.println("Complete deleting the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.deleteMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#delete()}
     */
    public void deleteQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.delete
        VoidResponse response = queueClient.delete();
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.delete
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#delete()}
     */
    public void deleteQueueAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.delete
        queueAsyncClient.delete().subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.delete
    }
}
