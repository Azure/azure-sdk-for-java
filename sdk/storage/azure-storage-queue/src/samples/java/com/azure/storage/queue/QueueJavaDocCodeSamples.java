// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IpRange;
import com.azure.storage.common.SasProtocol;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SasTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.AccessPolicy;
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
     * Generates code sample for creating a {@link QueueClient} with {@link SasTokenCredential}
     *
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.credential
        QueueClient client = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SasTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link
     * SharedKeyCredential}
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
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String)}
     */
    public void enqueueMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessage#string
        EnqueuedMessage response = client.enqueueMessage("hello msg");
        System.out.println("Complete enqueuing the message with message Id" + response.getMessageId());
        // END: com.azure.storage.queue.queueClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration,
     * Duration, Context)}
     */
    public void enqueueMessageWithTimeoutOverload() {

        // BEGIN: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context1
        EnqueuedMessage enqueuedMessage = client.enqueueMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null, Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Message %s expires at %s", enqueuedMessage.getMessageId(), enqueuedMessage.getExpirationTime());
        // END: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context1
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration,
     * Duration, Context)}
     */
    public void enqueueMessageWithLiveTimeOverload() {
        // BEGIN: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context2
        EnqueuedMessage enqueuedMessage = client.enqueueMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5), Duration.ofSeconds(1), new Context(key1, value1)).getValue();
        System.out.printf("Message %s expires at %s", enqueuedMessage.getMessageId(), enqueuedMessage.getExpirationTime());
        // END: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#String-Duration-Duration-Duration-Context2
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages()}
     */
    public void dequeueMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                System.out.println("Complete dequeuing the message: " + dequeuedMessage.getMessageId());
            }
        );
        // END: com.azure.storage.queue.queueClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageWithOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer
        for (DequeuedMessage dequeuedMessage : client.dequeueMessages(5)) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.getMessageId(), dequeuedMessage.getTimeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer, Duration, Duration, Context)}
     */
    public void dequeueMessageMaxOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration-duration-context
        for (DequeuedMessage dequeuedMessage : client.dequeueMessages(5, Duration.ofSeconds(60),
            Duration.ofSeconds(1), new Context(key1, value1))) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.getMessageId(), dequeuedMessage.getTimeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages()}
     */
    public void peekMessage() {

        // BEGIN: com.azure.storage.queue.queueClient.peekMessages
        client.peekMessages().forEach(
            peekedMessage -> {
                System.out.println("Complete peeking the message: " + peekedMessage.getMessageText());
            }
        );
        // END: com.azure.storage.queue.queueClient.peekMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages(Integer, Duration, Context)}
     */
    public void peekMessageMaxOverload() {

        // BEGIN: com.azure.storage.queue.queueClient.peekMessages#integer-duration-context
        client.peekMessages(5, Duration.ofSeconds(1), new Context(key1, value1)).forEach(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.getMessageId(), peekMessage.getDequeueCount())
        );
        // END: com.azure.storage.queue.queueClient.peekMessages#integer-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
        client.dequeueMessages().forEach(

            dequeuedMessage -> {
                UpdatedMessage response = client.updateMessage("newText",
                    dequeuedMessage.getMessageId(), dequeuedMessage.getPopReceipt(), null);

                System.out.println("Complete updating the message.");
            }
        );
        // END: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessageWithResponse(String, String, String, Duration,
     * Duration, Context)}
     */
    public void updateMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Duration-Context
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<UpdatedMessage> response = client.updateMessageWithResponse("newText",
                    dequeuedMessage.getMessageId(), dequeuedMessage.getPopReceipt(), null,
                    Duration.ofSeconds(1), new Context(key1, value1));

                System.out.println("Complete updating the message with status code " + response.getStatusCode());
            }
        );
        // END: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessage#String-String
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                client.deleteMessage(dequeuedMessage.getMessageId(), dequeuedMessage.getPopReceipt());
                System.out.println("Complete deleting the message.");
            }
        );
        // END: com.azure.storage.queue.QueueClient.deleteMessage#String-String
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessageWithResponse(String, String, Duration,
     * Context)}
     */
    public void deleteMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Duration-Context
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<Void> response = client.deleteMessageWithResponse(dequeuedMessage.getMessageId(),
                    dequeuedMessage.getPopReceipt(), Duration.ofSeconds(1), new Context(key1, value1));
                System.out.println("Complete deleting the message with status code " + response.getStatusCode());
            }
        );
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
        System.out.printf("Setting metadata completed.");
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
        System.out.printf("Setting metadata completed.");
        // END: com.azure.storage.queue.queueClient.setMetadataWithResponse#map-duration-context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {

        // BEGIN: com.azure.storage.queue.queueClient.clearMetadata#map
        client.setMetadata(null);
        System.out.printf("Clearing metadata completed.");
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
        for (SignedIdentifier permission : client.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", permission.getId(),
                permission.getAccessPolicy().getPermission());
        }
        // END: com.azure.storage.queue.queueClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.queue.QueueClient.setAccessPolicy#List
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
        client.setAccessPolicy(Collections.singletonList(permission));
        System.out.printf("Setting access policies completed.");
        // END: com.azure.storage.queue.QueueClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicyWithResponse(List, Duration, Context)}
     */
    public void setAccessPolicyWithResponse() {

        // BEGIN: com.azure.storage.queue.queueClient.setAccessPolicyWithResponse#List-Duration-Context
        AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
            .setStart(OffsetDateTime.now(ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
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
        System.out.printf("Clearing messages completed.");
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
     * Code snippet for {@link QueueClient#generateSas(String, QueueSasPermission, OffsetDateTime, OffsetDateTime,
     * String, SasProtocol, IpRange)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.queue.queueClient.generateSas#String-QueueSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange
        QueueSasPermission permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IpRange ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SasProtocol sasProtocol = SasProtocol.HTTPS_HTTP;
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSas(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange);
        // END: com.azure.storage.queue.queueClient.generateSas#String-QueueSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange
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
}
