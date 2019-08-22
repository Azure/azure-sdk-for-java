// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.core.util.Context;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.UpdatedMessage;

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
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.sastoken
        QueueClient queueClient = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.sastoken
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.credential
        QueueClient queueClient = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.credential
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                    + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueClient queueClient = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.connectionstring
        return queueClient;
    }

    /**
     * Generates a code sample for using {@link QueueClient#create()}
     */
    public void createQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.create
        queueClient.create();
        System.out.println("Complete creating queue.");
        // END: com.azure.storage.queue.queueClient.create
    }

    /**
     * Generates a code sample for using {@link QueueClient#createWithResponse(Map, Context)}
     */
    public void createQueueMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.createWithResponse#map-Context
        VoidResponse response = queueClient.createWithResponse(Collections.singletonMap("queue", "metadataMap"),
            new Context(key1, value1));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.createWithResponse#map-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String)}
     */
    public void enqueueMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessage#string
        EnqueuedMessage response = queueClient.enqueueMessage("hello msg");
        System.out.println("Complete enqueuing the message with message Id" + response.messageId());
        // END: com.azure.storage.queue.queueClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration, Context)}
     */
    public void enqueueMessageWithTimeoutOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessageWithResponse#string-duration-duration-Context
        EnqueuedMessage enqueuedMessage = queueClient.enqueueMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null, new Context(key1, value1)).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.queueClient.enqueueMessageWithResponse#string-duration-duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration, Context)}
     */
    public void enqueueMessageWithLiveTimeOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessageWithResponseLiveTime#string-duration-duration-Context
        EnqueuedMessage enqueuedMessage = queueClient.enqueueMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5), new Context(key1, value1)).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.queueClient.enqueueMessageWithResponseLiveTime#string-duration-duration-Context
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
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageWithOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer
        for (DequeuedMessage dequeuedMessage : queueClient.dequeueMessages(5)) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration
        for (DequeuedMessage dequeuedMessage : queueClient.dequeueMessages(5, Duration.ofSeconds(60))) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration
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
     * Generates a code sample for using {@link QueueClient#peekMessages(Integer)}
     */
    public void peekMessageMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.peekMessages#integer
        queueClient.peekMessages(5).forEach(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.messageId(), peekMessage.dequeueCount())
        );
        // END: com.azure.storage.queue.queueClient.peekMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.updateMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                UpdatedMessage response = queueClient.updateMessage("newText",
                    dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), Duration.ofSeconds(5));
                System.out.println("Complete updating the message.");
            }
        );
        // END: com.azure.storage.queue.queueClient.updateMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessageWithResponse(String, String, String, Duration, Context)}
     */
    public void updateMessageWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.updateMessageWithResponse
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<UpdatedMessage> response = queueClient.updateMessageWithResponse("newText",
                    dequeuedMessage.messageId(), dequeuedMessage.popReceipt(),
                    Duration.ofSeconds(5), new Context(key1, value1));
                System.out.println("Complete updating the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.updateMessageWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.deleteMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                queueClient.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt());
                System.out.println("Complete deleting the message.");
            }
        );
        // END: com.azure.storage.queue.queueClient.deleteMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessageWithResponse(String, String, Context)}
     */
    public void deleteMessageWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.deleteMessageWithResponse#Context
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                VoidResponse response = queueClient.deleteMessageWithResponse(dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt(), new Context(key1, value1));
                System.out.println("Complete deleting the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.deleteMessageWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#delete()}
     */
    public void deleteQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.delete
        queueClient.delete();
        System.out.println("Complete deleting the queue.");
        // END: com.azure.storage.queue.queueClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteWithResponse(Context)}
     */
    public void deleteWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.deleteWithResponse#Context
        VoidResponse response = queueClient.deleteWithResponse(new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.deleteWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getProperties()}
     */
    public void getProperties() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.getProperties
        QueueProperties properties = queueClient.getProperties();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
            properties.approximateMessagesCount());
        // END: com.azure.storage.queue.queueClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueClient#getPropertiesWithResponse(Context)}
     */
    public void getPropertiesWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.getPropertiesWithResponse#Context
        QueueProperties properties = queueClient.getPropertiesWithResponse(new Context(key1, value1)).value();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
            properties.approximateMessagesCount());
        // END: com.azure.storage.queue.queueClient.getPropertiesWithResponse#Context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadata() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setMetadata#map
        queueClient.setMetadata(Collections.singletonMap("queue", "metadataMap"));
        System.out.printf("Setting metadata completed.");
        // END: com.azure.storage.queue.queueClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Context)} to set metadata.
     */
    public void setMetadataWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setMetadataWithResponse#map-Context
        queueClient.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"), new Context(key1, value1));
        System.out.printf("Setting metadata completed.");
        // END: com.azure.storage.queue.queueClient.setMetadataWithResponse#map-Context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMetadata#map
        queueClient.setMetadata(null);
        System.out.printf("Clearing metadata completed.");
        // END: com.azure.storage.queue.queueClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Context)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMetadataWithResponse#map-Context
        VoidResponse response = queueClient.setMetadataWithResponse(null, new Context(key1, value1));
        System.out.printf("Clearing metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.clearMetadataWithResponse#map-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.getAccessPolicy
        for (SignedIdentifier permission : queueClient.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", permission.id(),
                permission.accessPolicy().permission());
        }
        // END: com.azure.storage.queue.queueClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setAccessPolicy
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        queueClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.printf("Setting access policies completed.");
        // END: com.azure.storage.queue.queueClient.setAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicyWithResponse(List, Context)}
     */
    public void setAccessPolicyWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Context
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        VoidResponse response = queueClient.setAccessPolicyWithResponse(Collections.singletonList(permission),
            new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessages()}
     */
    public void clearMessages() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMessages
        queueClient.clearMessages();
        System.out.printf("Clearing messages completed.");
        // END: com.azure.storage.queue.queueClient.clearMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessagesWithResponse(Context)}
     */
    public void clearMessagesWithResponse() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMessagesWithResponse#Context
        VoidResponse response = queueClient.clearMessagesWithResponse(new Context(key1, value1));
        System.out.printf("Clearing messages completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.clearMessagesWithResponse#Context
    }
}
