// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
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
        // BEGIN: com.azure.storage.queue.QueueClient.instantiation
        QueueClient client = new QueueClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.storage.queue.QueueClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link QueueClient}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.QueueClient.instantiation.sastoken
        QueueClient client = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.QueueClient.instantiation.sastoken
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.QueueClient.instantiation.credential
        QueueClient client = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.queue.QueueClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.QueueClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                    + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueClient client = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.QueueClient.instantiation.connectionstring
        return client;
    }

    /**
     * Generates a code sample for using {@link QueueClient#create()}
     */
    public void createQueue() {
        // BEGIN: com.azure.storage.queue.QueueClient.create
        client.create();
        System.out.println("Complete creating queue.");
        // END: com.azure.storage.queue.QueueClient.create
    }

    /**
     * Generates a code sample for using {@link QueueClient#createWithResponse(Map, Context)}
     */
    public void createQueueMaxOverload() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.createWithResponse#map-Context
        VoidResponse response = client.createWithResponse(Collections.singletonMap("queue", "metadataMap"),
            new Context(key1, value1));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.QueueClient.createWithResponse#map-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String)}
     */
    public void enqueueMessage() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.enqueueMessage#string
        EnqueuedMessage response = client.enqueueMessage("hello msg");
        System.out.println("Complete enqueuing the message with message Id" + response.messageId());
        // END: com.azure.storage.queue.QueueClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration, Context)}
     */
    public void enqueueMessageWithTimeoutOverload() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#string-duration-duration-Context
        EnqueuedMessage enqueuedMessage = client.enqueueMessageWithResponse("Hello, Azure",
            Duration.ofSeconds(5), null, new Context(key1, value1)).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse#string-duration-duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessageWithResponse(String, Duration, Duration, Context)}
     */
    public void enqueueMessageWithLiveTimeOverload() {
        // BEGIN: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration-Context
        EnqueuedMessage enqueuedMessage = client.enqueueMessageWithResponse("Goodbye, Azure",
            null, Duration.ofSeconds(5), new Context(key1, value1)).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.QueueClient.enqueueMessageWithResponse-liveTime#String-Duration-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages()}
     */
    public void dequeueMessage() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.dequeueMessages
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                System.out.println("Complete dequeuing the message: " + dequeuedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.QueueClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageWithOverload() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.dequeueMessages#integer
        for (DequeuedMessage dequeuedMessage : client.dequeueMessages(5)) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.QueueClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageMaxOverload() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.dequeueMessages#integer-duration
        for (DequeuedMessage dequeuedMessage : client.dequeueMessages(5, Duration.ofSeconds(60))) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.QueueClient.dequeueMessages#integer-duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages()}
     */
    public void peekMessage() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.peekMessages
        client.peekMessages().forEach(
            peekedMessage -> {
                System.out.println("Complete peeking the message: " + peekedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.QueueClient.peekMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages(Integer)}
     */
    public void peekMessageMaxOverload() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.peekMessages#integer
        client.peekMessages(5).forEach(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.messageId(), peekMessage.dequeueCount())
        );
        // END: com.azure.storage.queue.QueueClient.peekMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
        client.dequeueMessages().forEach(

            dequeuedMessage -> {
                UpdatedMessage response = client.updateMessage("newText",
                    dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), null);

                System.out.println("Complete updating the message.");
            }
        );
        // END: com.azure.storage.queue.QueueClient.updateMessage#String-String-String-Duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessageWithResponse(String, String, String, Duration, Context)}
     */
    public void updateMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Context
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<UpdatedMessage> response = client.updateMessageWithResponse("newText",
                    dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), null, new Context(key1, value1));

                System.out.println("Complete updating the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.QueueClient.updateMessageWithResponse#String-String-String-Duration-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessage#String-String
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                client.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt());
                System.out.println("Complete deleting the message.");
            }
        );
        // END: com.azure.storage.queue.QueueClient.deleteMessage#String-String
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessageWithResponse(String, String, Context)}
     */
    public void deleteMessageWithResponse() {
        // BEGIN: com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Context
        client.dequeueMessages().forEach(
            dequeuedMessage -> {
                VoidResponse response = client.deleteMessageWithResponse(dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt(), new Context(key1, value1));
                System.out.println("Complete deleting the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.QueueClient.deleteMessageWithResponse#String-String-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#delete()}
     */
    public void deleteQueue() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.delete
        client.delete();
        System.out.println("Complete deleting the queue.");
        // END: com.azure.storage.queue.QueueClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteWithResponse(Context)}
     */
    public void deleteWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.deleteWithResponse#Context
        VoidResponse response = client.deleteWithResponse(new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.QueueClient.deleteWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getProperties()}
     */
    public void getProperties() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.getProperties
        QueueProperties properties = client.getProperties();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
            properties.approximateMessagesCount());
        // END: com.azure.storage.queue.QueueClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueClient#getPropertiesWithResponse(Context)}
     */
    public void getPropertiesWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.getPropertiesWithResponse#Context
        QueueProperties properties = client.getPropertiesWithResponse(new Context(key1, value1)).value();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
            properties.approximateMessagesCount());
        // END: com.azure.storage.queue.QueueClient.getPropertiesWithResponse#Context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadata() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.setMetadata#map
        client.setMetadata(Collections.singletonMap("queue", "metadataMap"));
        System.out.printf("Setting metadata completed.");
        // END: com.azure.storage.queue.QueueClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Context)} to set metadata.
     */
    public void setMetadataWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.setMetadataWithResponse#map-Context
        client.setMetadataWithResponse(Collections.singletonMap("queue", "metadataMap"), new Context(key1, value1));
        System.out.printf("Setting metadata completed.");
        // END: com.azure.storage.queue.QueueClient.setMetadataWithResponse#map-Context
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.clearMetadata#map
        client.setMetadata(null);
        System.out.printf("Clearing metadata completed.");
        // END: com.azure.storage.queue.QueueClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadataWithResponse(Map, Context)} to clear metadata.
     */
    public void clearMetadataWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.clearMetadataWithResponse#map-Context
        VoidResponse response = client.setMetadataWithResponse(null, new Context(key1, value1));
        System.out.printf("Clearing metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.QueueClient.clearMetadataWithResponse#map-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.getAccessPolicy
        for (SignedIdentifier permission : client.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", permission.id(),
                permission.accessPolicy().permission());
        }
        // END: com.azure.storage.queue.QueueClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.queue.QueueClient.setAccessPolicy#List
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        client.setAccessPolicy(Collections.singletonList(permission));
        System.out.printf("Setting access policies completed.");
        // END: com.azure.storage.queue.QueueClient.setAccessPolicy#List
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicyWithResponse(List, Context)}
     */
    public void setAccessPolicyWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.setAccessPolicyWithResponse#List-Context
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        VoidResponse response = client.setAccessPolicyWithResponse(Collections.singletonList(permission),
            new Context(key1, value1));
        System.out.printf("Setting access policies completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.QueueClient.setAccessPolicyWithResponse#List-Context
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessages()}
     */
    public void clearMessages() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.clearMessages
        client.clearMessages();
        System.out.printf("Clearing messages completed.");
        // END: com.azure.storage.queue.QueueClient.clearMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessagesWithResponse(Context)}
     */
    public void clearMessagesWithResponse() {
        
        // BEGIN: com.azure.storage.queue.QueueClient.clearMessagesWithResponse#Context
        VoidResponse response = client.clearMessagesWithResponse(new Context(key1, value1));
        System.out.printf("Clearing messages completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.QueueClient.clearMessagesWithResponse#Context
    }

    /**
     * Code snippet for {@link QueueClient#generateSAS(String, QueueSASPermission, OffsetDateTime, OffsetDateTime,
     * String, SASProtocol, IPRange)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.QueueClient.generateSAS
        QueueSASPermission permissions = new QueueSASPermission()
            .read(true)
            .add(true)
            .update(true)
            .process(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange);
        // END: com.azure.storage.blob.QueueClient.generateSAS
    }
}
