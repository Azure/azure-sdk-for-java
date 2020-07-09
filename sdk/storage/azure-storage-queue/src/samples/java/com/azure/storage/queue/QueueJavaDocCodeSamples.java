// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.SendMessageResult;
import com.azure.storage.queue.models.UpdateMessageResult;
import com.azure.storage.queue.sas.QueueSasPermission;
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueClient}.
 */

public class QueueJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";
    private QueueClient client = createClientWithSASToken();

    /**
     * Generates code sample for creating a {@link QueueClient}.
     */
    public void buildQueueClient() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation
        QueueClient client = new QueueClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link QueueClient}
     *
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.sastoken
        QueueClient client = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.sastoken
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with SAS token.
     *
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.credential
        QueueClient client = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .sasToken("{SASTokenQueryParams}")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link
     * StorageSharedKeyCredential}
     *
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueClient client = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.connectionstring
        return client;
    }

    /**
     * Generates a code sample for using {@link QueueClient#create()}
     */
    public void createQueue() {
        // BEGIN: com.azure.storage.queue.queueClient.create
        client.create();
        System.out.println("Complete creating queue.");
        // END: com.azure.storage.queue.queueClient.create
    }

    /**
     * Generates a code sample for using {@link QueueClient#createWithResponse(Map, Duration, Context)}
     */
    public void createQueueMaxOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.createWithResponse#map-duration-context
        Response<Void> response = client.createWithResponse(Collections.singletonMap("queue", "metadataMap"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating queue with status code: " + response.getStatusCode());
        // END: com.azure.storage.queue.queueClient.createWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#sendMessage(String)}
     */
    public void sendMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.sendMessage#string
        SendMessageResult response = client.sendMessage("hello msg");
        System.out.println("Complete enqueuing the message with message Id" + response.getMessageId());
        // END: com.azure.storage.queue.queueClient.sendMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#sendMessageWithResponse(String, Duration, Duration,
     * Duration, Context)}
     */
    public void enqueueMessageWithTimeoutOverload() {

        // BEGIN: com.azure.storage.queue.QueueClient.sendMessageWithResponse#String-Duration-Duration-Duration-Context1
        SendMessageResult sentMessageItem = client.sendMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null, Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Message %s expires at %s", sentMessageItem.getMessageId(),
            sentMessageItem.getExpirationTime());
        // END: com.azure.storage.queue.QueueClient.sendMessageWithResponse#String-Duration-Duration-Duration-Context1
    }

    /**
     * Generates a code sample for using {@link QueueClient#sendMessageWithResponse(String, Duration, Duration,
     * Duration, Context)}
     */
    public void sendMessageWithLiveTimeOverload() {
        // BEGIN: com.azure.storage.queue.QueueClient.sendMessageWithResponse#String-Duration-Duration-Duration-Context2
        SendMessageResult enqueuedMessage = client.sendMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5), Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Message %s expires at %s", enqueuedMessage.getMessageId(),
            enqueuedMessage.getExpirationTime());
        // END: com.azure.storage.queue.QueueClient.sendMessageWithResponse#String-Duration-Duration-Duration-Context2
    }

    /**
     * Generates a code sample for using {@link QueueClient#receiveMessage()}
     */
    public void getMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.receiveMessage
        QueueMessageItem queueMessageItem = client.receiveMessage();
        System.out.println("Complete receiving the message: " + queueMessageItem.getMessageId());
        // END: com.azure.storage.queue.queueClient.receiveMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#receiveMessages(Integer)}
     */
    public void getMessageWithOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.receiveMessages#integer
        for (QueueMessageItem message : client.receiveMessages(5)) {
            System.out.printf("Received %s and it becomes visible at %s",
                message.getMessageId(), message.getTimeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.receiveMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#receiveMessages(Integer, Duration, Duration, Context)}
     */
    public void getMessageMaxOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.receiveMessages#integer-duration-duration-context
        for (QueueMessageItem message : client.receiveMessages(5, Duration.ofSeconds(60),
            Duration.ofSeconds(1), new Context(key1, value1))) {
            System.out.printf("Received %s and it becomes visible at %s",
                message.getMessageId(), message.getTimeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.receiveMessages#integer-duration-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessage()}
     */
    public void peekMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.peekMessage
        PeekedMessageItem peekedMessageItem = client.peekMessage();
        System.out.println("Complete peeking the message: " + peekedMessageItem.getMessageText());
        // END: com.azure.storage.queue.queueClient.peekMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages(Integer, Duration, Context)}
     */
    public void peekMessageMaxOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.peekMessages#integer-duration-context
        client.peekMessages(5, Duration.ofSeconds(1), new Context(key1, value1)).forEach(
            peekMessage -> System.out.printf("Peeked message %s has been received %d times",
                peekMessage.getMessageId(), peekMessage.getDequeueCount())
        );
        // END: com.azure.storage.queue.queueClient.peekMessages#integer-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
        QueueMessageItem queueMessageItem = client.receiveMessage();
        UpdateMessageResult result = client.updateMessage(queueMessageItem.getMessageId(),
            queueMessageItem.getPopReceipt(), "newText", null);
        System.out.println("Complete updating the message with the receipt " + result.getPopReceipt());
           // END: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessageWithResponse(String, String, String, Duration,
     * Duration, Context)}
     */
    public void updateMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Duration-Context
        QueueMessageItem queueMessageItem = client.receiveMessage();
        Response<UpdateMessageResult> response = client.updateMessageWithResponse(queueMessageItem.getMessageId(),
            queueMessageItem.getPopReceipt(), "newText", null, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.println("Complete updating the message with status code " + response.getStatusCode());
        // END: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessage#String-String
        QueueMessageItem queueMessageItem = client.receiveMessage();
        client.deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
        System.out.println("Complete deleting the message.");
        // END: com.azure.storage.queue.QueueClient.deleteMessage#String-String
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessageWithResponse(String, String, Duration,
     * Context)}
     */
    public void deleteMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Duration-Context
        QueueMessageItem queueMessageItem = client.receiveMessage();
        Response<Void> response = client.deleteMessageWithResponse(queueMessageItem.getMessageId(),
            queueMessageItem.getPopReceipt(), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the message with status code " + response.getStatusCode());
        // END: com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#delete()}
     */
    public void deleteQueue() {

        // BEGIN: com.azure.storage.queue.queueClient.delete
        client.delete();
        System.out.println("Complete deleting the queue.");
        // END: com.azure.storage.queue.queueClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteWithResponse(Duration, Context)}
     */
    public void deleteWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.deleteWithResponse#duration-context
        Response<Void> response = client.deleteWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.getStatusCode());
        // END: com.azure.storage.queue.queueClient.deleteWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getProperties()}
     */
    public void getProperties() {

        // BEGIN: com.azure.storage.queue.queueClient.getProperties
        QueueProperties properties = client.getProperties();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.getMetadata(),
            properties.getApproximateMessagesCount());
        // END: com.azure.storage.queue.queueClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.getPropertiesWithResponse#duration-context
        QueueProperties properties = client.getPropertiesWithResponse(Duration.ofSeconds(1),
            new Context(key1, value1)).getValue();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.getMetadata(),
            properties.getApproximateMessagesCount());
        // END: com.azure.storage.queue.queueClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadata() {

        // BEGIN: com.azure.storage.queue.queueClient.setMetadata#map
        client.setMetadata(Collections.singletonMap("queue", "metadataMap"));
        System.out.println("Setting metadata completed.");
        // END: com.azure.storage.queue.queueClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Duration, Context)} to set
     * metadata.
     */
    public void setMetadataWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.setMetadataWithResponse#map-duration-context
        client.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Setting metadata completed.");
        // END: com.azure.storage.queue.queueClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {

        // BEGIN: com.azure.storage.queue.queueClient.clearMetadata#map
        client.setMetadata(null);
        System.out.println("Clearing metadata completed.");
        // END: com.azure.storage.queue.queueClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Duration, Context)} to clear
     * metadata.
     */
    public void clearMetadataWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.clearMetadataWithResponse#map-duration-context
        Response<Void> response = client.setMetadataWithResponse(null, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Clearing metadata completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.queue.queueClient.clearMetadataWithResponse#map-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {

        // BEGIN: com.azure.storage.queue.queueClient.getAccessPolicy
        for (QueueSignedIdentifier permission : client.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", permission.getId(),
                permission.getAccessPolicy().getPermissions());
        }
        // END: com.azure.storage.queue.queueClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.queue.QueueClient.setAccessPolicy#List
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        QueueSignedIdentifier permission = new QueueSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicy(Collections.singletonList(permission));
        System.out.println("Setting access policies completed.");
        // END: com.azure.storage.queue.QueueClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicyWithResponse(List, Duration, Context)}
     */
    public void setAccessPolicyWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Duration-Context
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy().setPermissions("r")
            .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        QueueSignedIdentifier permission = new QueueSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        Response<Void> response = client.setAccessPolicyWithResponse(Collections.singletonList(permission),
            Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessages()}
     */
    public void clearMessages() {

        // BEGIN: com.azure.storage.queue.queueClient.clearMessages
        client.clearMessages();
        System.out.println("Clearing messages completed.");
        // END: com.azure.storage.queue.queueClient.clearMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessagesWithResponse(Duration, Context)}
     */
    public void clearMessagesWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.clearMessagesWithResponse#duration-context
        Response<Void> response = client.clearMessagesWithResponse(Duration.ofSeconds(1), new Context(key1, value1));
        System.out.printf("Clearing messages completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.queue.queueClient.clearMessagesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getQueueName()}
     */
    public void getName() {
        // BEGIN: com.azure.storage.queue.queueClient.getQueueName
        String queueName = client.getQueueName();
        System.out.println("The name of the queue is " + queueName);
        // END: com.azure.storage.queue.queueClient.getQueueName
    }

    /**
     * Code snippet for {@link QueueClient#generateSas(QueueServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.queue.QueueClient.generateSas#QueueServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        QueueSasPermission permission = new QueueSasPermission().setReadPermission(true);

        QueueServiceSasSignatureValues values = new QueueServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.queue.QueueClient.generateSas#QueueServiceSasSignatureValues
    }
}
