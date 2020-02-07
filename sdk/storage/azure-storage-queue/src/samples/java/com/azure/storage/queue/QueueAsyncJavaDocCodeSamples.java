// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueAsyncClient}.
 */

public class QueueAsyncJavaDocCodeSamples {

    private QueueAsyncClient client = createAsyncClientWithSASToken();

    /**
     * Generates code sample for creating a {@link QueueAsyncClient}.
     */
    public void buildQueueAsyncClient() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation
        QueueAsyncClient client = new QueueClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with SAS token.
     *
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.sastoken
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.sastoken
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with SAS token.
     *
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .sasToken("{SASTokenQueryParams}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@code connectionString} which turns into
     * {@link StorageSharedKeyCredential}
     *
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        return queueAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create()}
     */
    public void createQueueAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create
        client.create().subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create()}
     */
    public void createQueueAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create#map
        client.create().subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#createWithResponse(Map)}
     */
    public void createWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
        client.createWithResponse(Collections.singletonMap("queue", "metadataMap")).subscribe(
            response -> System.out.println("Complete creating the queue with status code:" + response.getStatusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#sendMessage(String)}
     */
    public void enqueueMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.sendMessage#string
        client.sendMessage("Hello, Azure").subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.sendMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#sendMessageWithResponse(String, Duration,
     * Duration)}
     */
    public void enqueueMessageAsyncWithTimeoutOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#string-duration-duration
        client.sendMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.getValue().getMessageId(),
                    response.getValue().getExpirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.sendMessageWithResponse#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#sendMessageWithResponse(String, Duration,
     * Duration)}
     */
    public void enqueueMessageAsyncWithLiveTimeOverload() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#String-Duration-Duration
        client.sendMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5)).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.getValue().getMessageId(),
                    response.getValue().getExpirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.sendMessageWithResponse-liveTime#String-Duration-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#receiveMessage()}
     */
    public void getMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.receiveMessage
        client.receiveMessage().subscribe(
            message -> System.out.println("The message got from getMessages operation: "
                + message.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.receiveMessage
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#receiveMessages(Integer)}
     */
    public void getMessageAsyncWithOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.receiveMessages#integer
        client.receiveMessages(5).subscribe(
            message -> System.out.println("The message got from getMessages operation: "
                + message.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.receiveMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#receiveMessages(Integer, Duration)}
     */
    public void getMessageAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.receiveMessages#integer-duration
        client.receiveMessages(5, Duration.ofSeconds(60))
            .subscribe(
                message -> System.out.println("The message got from getMessages operation: "
                    + message.getMessageText()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete receiving the message!")
            );
        // END: com.azure.storage.queue.queueAsyncClient.receiveMessages#integer-duration
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessage()}
     */
    public void peekMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessage
        client.peekMessage().subscribe(
            peekMessages -> System.out.println("The message got from peek operation: " + peekMessages.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessage
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages(Integer)}
     */
    public void peekMessageAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
        client.peekMessages(5).subscribe(
            peekMessage -> System.out.printf("Peeked message %s has been received %d times",
                peekMessage.getMessageId(), peekMessage.getDequeueCount()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessageAsync() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration
        client.receiveMessage().subscribe(
            message -> {
                client.updateMessage("newText", message.getMessageId(),
                    message.getPopReceipt(), null).subscribe(
                        response -> {
                        },
                        updateError -> System.err.print(updateError.toString()),
                        () -> System.out.println("Complete updating the message!")
                );
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessageWithResponse(String, String, String,
     * Duration)}
     */
    public void updateMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration

        client.receiveMessage().subscribe(
            message -> {
                client.updateMessageWithResponse(message.getMessageId(), message.getPopReceipt(), "newText",
                    null).subscribe(
                        response -> System.out.println("Complete updating the message with status code:"
                            + response.getStatusCode()),
                        updateError -> System.err.print(updateError.toString()),
                        () -> System.out.println("Complete updating the message!")
                );
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessage(String, String)}
     */
    public void deleteMessageAsync() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String
        client.receiveMessage().subscribe(
            message -> {
                client.deleteMessage(message.getMessageId(), message.getPopReceipt()).subscribe(
                    response -> {
                    },
                    deleteError -> System.err.print(deleteError.toString()),
                    () -> System.out.println("Complete deleting the message!")
                );
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessageWithResponse(String, String)}
     */
    public void deleteMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String
        client.receiveMessage().subscribe(
            message -> {
                client.deleteMessageWithResponse(message.getMessageId(), message.getPopReceipt())
                    .subscribe(
                        response -> System.out.println("Complete deleting the message with status code: "
                            + response.getStatusCode()),
                        deleteError -> System.err.print(deleteError.toString()),
                        () -> System.out.println("Complete deleting the message!")
                    );
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete receiving the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#delete()}
     */
    public void deleteQueueAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.delete
        client.delete().doOnSuccess(
            response -> System.out.println("Deleting the queue completed.")
        );
        // END: com.azure.storage.queue.queueAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.deleteWithResponse
        client.deleteWithResponse().subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.getStatusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getProperties
        client.getProperties()
            .subscribe(properties -> {
                System.out.printf("Metadata: %s, Approximate message count: %d", properties.getMetadata(),
                    properties.getApproximateMessagesCount());
            });
        // END: com.azure.storage.queue.queueAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getProperties()}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse
        client.getPropertiesWithResponse()
            .subscribe(response -> {
                QueueProperties properties = response.getValue();
                System.out.printf("Metadata: %s, Approximate message count: %d", properties.getMetadata(),
                    properties.getApproximateMessagesCount());
            });
        // END: com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadataAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadata#map
        client.setMetadata(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.println("Setting metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadataWithResponse(Map)} to set metadata.
     */
    public void setMetadataWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
        client.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.printf("Setting metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
        client.setMetadata(null)
            .subscribe(response -> System.out.println("Clearing metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
        client.setMetadataWithResponse(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {

        // BEGIN: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
        client.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s",
                result.getId(), result.getAccessPolicy().getPermissions()));
        // END: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicyWithResponse(Iterable)}
     */
    public void setAccessPolicyWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#Iterable
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        QueueSignedIdentifier permission = new QueueSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicyWithResponse(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#Iterable
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicy(Iterable)}
     */
    public void setAccessPolicyAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#Iterable
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        QueueSignedIdentifier permission = new QueueSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicy(Collections.singletonList(permission))
            .subscribe(response -> System.out.println("Setting access policies completed."));
        // END: com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#Iterable
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessagesWithResponse()}
     */
    public void clearMessagesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse
        client.clearMessagesWithResponse().doOnSuccess(
            response -> System.out.println("Clearing messages completed with status code: " + response.getStatusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessages()}
     */
    public void clearMessagesAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessages
        client.clearMessages().subscribe(
            response -> System.out.println("Clearing messages completed."));
        // END: com.azure.storage.queue.queueAsyncClient.clearMessages
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getQueueName()}
     */
    public void getNameAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getQueueName
        String queueName = client.getQueueName();
        System.out.println("The name of the queue is " + queueName);
        // END: com.azure.storage.queue.queueAsyncClient.getQueueName
    }

    /**
     * Code snippet for {@link QueueAsyncClient#generateSas(QueueServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        QueueSasPermission permission = new QueueSasPermission().setReadPermission(true);

        QueueServiceSasSignatureValues values = new QueueServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.queue.QueueAsyncClient.generateSas#QueueServiceSasSignatureValues
    }
}
