// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueAsyncClient}.
 */

public class QueueAsyncJavaDocCodeSamples {

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
     * Generates code sample for creating a {@link QueueAsyncClient} with {@link SASTokenCredential}
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
     * Generates code sample for creating a {@link QueueAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
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
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create
        queueAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create()}
     */
    public void createQueueAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create#map
        queueAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#createWithResponse(Map)}
     */
    public void createWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
        queueAsyncClient.createWithResponse(Collections.singletonMap("queue", "metadataMap")).subscribe(
            response -> System.out.println("Complete creating the queue with status code:" + response.statusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String)}
     */
    public void enqueueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
        queueAsyncClient.enqueueMessage("Hello, Azure").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessageWithResponse(String, Duration, Duration)}
     */
    public void enqueueMessageAsyncWithTimeoutOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessageWithResponse#string-duration-duration
        queueAsyncClient.enqueueMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().messageId(),
                    response.value().expirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessageWithResponse#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessageWithResponse(String, Duration, Duration)}
     */
    public void enqueueMessageAsyncWithLiveTimeOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration
        queueAsyncClient.enqueueMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5)).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().messageId(),
                    response.value().expirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages()}
     */
    public void dequeueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                    + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageAsyncWithOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
        queueAsyncClient.dequeueMessages(5).subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
        queueAsyncClient.dequeueMessages(5, Duration.ofSeconds(60)).subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
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
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages(Integer)}
     */
    public void peekMessageAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
        queueAsyncClient.peekMessages(5).subscribe(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.messageId(), peekMessage.dequeueCount()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.updateMessage("newText", dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt(), null).subscribe(
                        response -> { },
                        updateError -> System.err.print(updateError.toString()),
                        () -> System.out.println("Complete updating the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.updateMessage#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessageWithResponse(String, String, String, Duration)}
     */
    public void updateMessageWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.updateMessageWithResponse("newText", dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt(), null).subscribe(
                        response -> System.out.println("Complete updating the message with status code:"
                            + response.statusCode()),
                        updateError -> System.err.print(updateError.toString()),
                        () -> System.out.println("Complete updating the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessage(String, String)}
     */
    public void deleteMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()).subscribe(
                    response -> { },
                    deleteError -> System.err.print(deleteError.toString()),
                    () -> System.out.println("Complete deleting the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessageWithResponse(String, String)}
     */
    public void deleteMessageWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.deleteMessageWithResponse(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()).subscribe(
                    response -> System.out.println("Complete deleting the message with status code: " + response.statusCode()),
                    deleteError -> System.err.print(deleteError.toString()),
                    () -> System.out.println("Complete deleting the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#delete()}
     */
    public void deleteQueueAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.delete
        queueAsyncClient.delete().doOnSuccess(
            response -> System.out.println("Deleting the queue completed.")
        );
        // END: com.azure.storage.queue.queueAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteWithResponse()}
     */
    public void deleteWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.deleteWithResponse
        queueAsyncClient.deleteWithResponse().subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.deleteWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getProperties
        queueAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
                    properties.approximateMessagesCount());
            });
        // END: com.azure.storage.queue.queueAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getProperties()}
     */
    public void getPropertiesWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse
        queueAsyncClient.getPropertiesWithResponse()
            .subscribe(response -> {
                QueueProperties properties = response.value();
                System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
                    properties.approximateMessagesCount());
            });
        // END: com.azure.storage.queue.queueAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadataAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadata#map
        queueAsyncClient.setMetadata(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.printf("Setting metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadataWithResponse(Map)} to set metadata.
     */
    public void setMetadataWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
        queueAsyncClient.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.printf("Setting metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
        queueAsyncClient.setMetadata(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
        queueAsyncClient.setMetadataWithResponse(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
        queueAsyncClient.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s",
                result.id(), result.accessPolicy().permission()));
        // END: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicyWithResponse(List)}
     */
    public void setAccessPolicyWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#List
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        queueAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#List
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicy(List)}
     */
    public void setAccessPolicyAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#List
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        queueAsyncClient.setAccessPolicy(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed."));
        // END: com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessagesWithResponse()}
     */
    public void clearMessagesWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse
        queueAsyncClient.clearMessagesWithResponse().doOnSuccess(
            response -> System.out.println("Clearing messages completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessages()}
     */
    public void clearMessagesAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessages
        queueAsyncClient.clearMessages().subscribe(
            response -> System.out.println("Clearing messages completed."));
        // END: com.azure.storage.queue.queueAsyncClient.clearMessages
    }
}
