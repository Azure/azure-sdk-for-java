// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
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
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create
        client.create().subscribe(
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
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create#map
        client.create().subscribe(
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
        // BEGIN: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
        client.createWithResponse(Collections.singletonMap("queue", "metadataMap")).subscribe(
            response -> System.out.println("Complete creating the queue with status code:" + response.statusCode()),
            error -> System.err.print(error.toString())
        );
        // END: com.azure.storage.queue.queueAsyncClient.createWithResponse#map
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String)}
     */
    public void enqueueMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
        client.enqueueMessage("Hello, Azure").subscribe(
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
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessageWithResponse#string-duration-duration
        client.enqueueMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().getMessageId(),
                    response.value().getExpirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessageWithResponse#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessageWithResponse(String, Duration, Duration)}
     */
    public void enqueueMessageAsyncWithLiveTimeOverload() {
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration
        client.enqueueMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5)).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().getMessageId(),
                    response.value().getExpirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.QueueAsyncClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages()}
     */
    public void dequeueMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages
        client.dequeueMessages().subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                    + dequeuedMessage.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageAsyncWithOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
        client.dequeueMessages(5).subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                + dequeuedMessage.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
        client.dequeueMessages(5, Duration.ofSeconds(60))
            .subscribe(
                dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                    + dequeuedMessage.getMessageText()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete dequeuing the message!")
            );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages()}
     */
    public void peekMessageAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages
        client.peekMessages().subscribe(
            peekMessages -> System.out.println("The message got from peek operation: " + peekMessages.getMessageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages(Integer)}
     */
    public void peekMessageAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
        client.peekMessages(5).subscribe(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
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
        client.dequeueMessages().subscribe(
            dequeuedMessage -> {
                client.updateMessage("newText", dequeuedMessage.getMessageId(),
                    dequeuedMessage.getPopReceipt(), null).subscribe(
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
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.updateMessageWithResponse#String-String-String-Duration
        client.dequeueMessages().subscribe(
            dequeuedMessage -> {
                client.updateMessageWithResponse("newText", dequeuedMessage.getMessageId(),
                    dequeuedMessage.getPopReceipt(), null).subscribe(
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
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessage#String-String
        client.dequeueMessages().subscribe(
            dequeuedMessage -> {
                client.deleteMessage(dequeuedMessage.getMessageId(), dequeuedMessage.getPopReceipt()).subscribe(
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
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.deleteMessageWithResponse#String-String
        client.dequeueMessages().subscribe(
            dequeuedMessage -> {
                client.deleteMessageWithResponse(dequeuedMessage.getMessageId(), dequeuedMessage.getPopReceipt()).subscribe(
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
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
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
                QueueProperties properties = response.value();
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
            .subscribe(response -> System.out.printf("Setting metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadataWithResponse(Map)} to set metadata.
     */
    public void setMetadataWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
        client.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.printf("Setting metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadataWithResponse#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
        client.setMetadata(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed."));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
        client.setMetadataWithResponse(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadataWithResponse#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {

        // BEGIN: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
        client.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s",
                result.getId(), result.getAccessPolicy().getPermission()));
        // END: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicyWithResponse(List)}
     */
    public void setAccessPolicyWithResponse() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.QueueAsyncClient.setAccessPolicyWithResponse#List
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicyWithResponse(Collections.singletonList(permission))
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
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicy(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed."));
        // END: com.azure.storage.queue.QueueAsyncClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessagesWithResponse()}
     */
    public void clearMessagesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessagesWithResponse
        client.clearMessagesWithResponse().doOnSuccess(
            response -> System.out.println("Clearing messages completed with status code: " + response.statusCode())
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
     * Code snippet for {@link QueueAsyncClient#generateSAS(String, QueueSASPermission, OffsetDateTime, OffsetDateTime,
     * String, SASProtocol, IPRange)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.generateSAS#String-QueueSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange
        QueueSASPermission permissions = new QueueSASPermission()
            .setRead(true)
            .setAdd(true)
            .setUpdate(true)
            .setProcess(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange);
        // END: com.azure.storage.queue.queueAsyncClient.generateSAS#String-QueueSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange
    }
}
