// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueueAsyncClientTests extends QueueClientTestsBase {
    private QueueAsyncClient client;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();

        if (interceptorManager.isPlaybackMode()) {
            client = setupClient((connectionString, endpoint) -> QueueAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            client = setupClient((connectionString, endpoint) -> QueueAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .queueName(queueName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .build());
        }
    }

    @Override
    protected void afterTest() {
        try {
            client.clearMessages().block();
            client.delete().block();
        } catch (StorageErrorException ex) {
            // Queue already delete, that's what we wanted anyways.
        }
    }

    @Override
    public void createWithSharedKey() {

    }

    @Override
    public void createWithSASToken() {

    }

    @Override
    public void createWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(metadata, response.value().metadata()))
            .verifyComplete();
    }

    @Override
    public void createTwiceSameMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();
    }

    @Override
    public void createTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.create(metadata))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void deleteExisting() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage("This queue will be deleted"))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void deleteNonExistent() {
        StepVerifier.create(client.delete())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void getProperties() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                assertEquals(0, response.value().approximateMessagesCount());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getPropertiesQueueDoesNotExist() {
        StepVerifier.create(client.getProperties())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void setMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.setMetadata(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(metadata, response.value().metadata()))
            .verifyComplete();
    }

    @Override
    public void setMetadataQueueDoesNotExist() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void setInvalidMetadata() {
        Map<String, String> badMetadata = Collections.singletonMap("%^&&*fheuhiew~~!--=", "bad metadata");

        StepVerifier.create(client.create(badMetadata))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void deleteMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(metadata, response.value().metadata()))
            .verifyComplete();

        StepVerifier.create(client.setMetadata(null))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(Collections.EMPTY_MAP, response.value().metadata()))
            .verifyComplete();
    }

    @Override
    public void getAccessPolicy() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void getAccessPolicyQueueDoesNotExist() {
        StepVerifier.create(client.getAccessPolicy())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void setAccessPolicy() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.now())
            .expiry(OffsetDateTime.now());

        SignedIdentifier permission = new SignedIdentifier()
            .id("test-permission")
            .accessPolicy(accessPolicy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .assertNext(response -> assertEquals(permission, response))
            .verifyComplete();
    }

    @Override
    public void setAccessPolicyQueueDoesNotExist() {
        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.now())
            .expiry(OffsetDateTime.now());

        SignedIdentifier permission = new SignedIdentifier()
            .id("test-permission")
            .accessPolicy(accessPolicy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void setInvalidAccessPolicy() {
        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.now())
            .expiry(OffsetDateTime.now());

        SignedIdentifier permission = new SignedIdentifier()
            .id("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .accessPolicy(accessPolicy);

        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void enqueueMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void enqueueEmptyMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertNull(peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void enqueueShortTimeToLiveMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofSeconds(2)))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages().delaySubscription(Duration.ofSeconds(5)))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void enqueueQueueDoesNotExist() {
        StepVerifier.create(client.enqueueMessage("this should fail"))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void dequeueMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages())
            .assertNext(dequeuedMessage -> assertEquals(messageText, dequeuedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void dequeueMultipleMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        String messageText2 = "test message 2";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage(messageText2))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages(2))
            .assertNext(dequeuedMessage -> assertEquals(messageText, dequeuedMessage.messageText()))
            .assertNext(dequeuedMessage -> assertEquals(messageText2, dequeuedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void dequeueTooManyMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages(64))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void dequeueQueueDoesNotExist() {
        StepVerifier.create(client.dequeueMessages())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void peekMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void peekMultipleMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        String messageText2 = "test message 2";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage(messageText2))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages(2))
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .assertNext(peekedMessage -> assertEquals(messageText2, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void peekTooManyMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.peekMessages(64))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void peekQueueDoesNotExist() {
        StepVerifier.create(client.peekMessages())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void clearMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();
        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();
        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(3, response.value().approximateMessagesCount()))
            .verifyComplete();

        StepVerifier.create(client.clearMessages())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(0, response.value().approximateMessagesCount()))
            .verifyComplete();
    }

    @Override
    public void clearMessagesQueueDoesNotExist() {
        StepVerifier.create(client.clearMessages())
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void deleteMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages().blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());
        StepVerifier.create(client.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()))
            .assertNext(voidResponse -> assertNull(voidResponse.value()))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> assertEquals(0, response.value().approximateMessagesCount()))
            .verifyComplete();
    }

    @Override
    public void deleteMessageInvalidPopReceipt() {
        StepVerifier.create(client.create())
            .assertNext(response -> assertNull(response.value()))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> assertNotNull(response.value()))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages(1, Duration.ofSeconds(5))
            .blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());

        StepVerifier.create(client.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt() + "random"))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));

        StepVerifier.create(client.getProperties().delayElement(Duration.ofSeconds(5)))
            .assertNext(response -> assertEquals(1, response.value().approximateMessagesCount()))
            .verifyComplete();
    }

    @Override
    public void deleteMessageQueueDoesNotExist() {
        StepVerifier.create(client.deleteMessage("queue", "doesn't exist"))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void updateMessage() {

    }

    @Override
    public void updateMessageInvalidPopReceipt() {

    }

    @Override
    public void updateMessageQueueDoesNotExist() {
        StepVerifier.create(client.updateMessage("queue", "doesn't", "exist", Duration.ofSeconds(5)))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }
}
