// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueueAsyncClientTests extends QueueClientTestsBase {
    private final ClientLogger logger = new ClientLogger(QueueAsyncClientTests.class);

    private QueueAsyncClient client;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            client = helper.setupClient((connectionString, endpoint) -> new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsyncClient(), true, logger);
        } else {
            client = helper.setupClient((connectionString, endpoint) -> new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsyncClient(), false, logger);
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
        // Need to find a way to get SAS tokens from the storage account
    }

    @Override
    public void createWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void createTwiceSameMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();
    }

    @Override
    public void createTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.create(metadata))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 409));
    }

    @Override
    public void deleteExisting() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage("This queue will be deleted"))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.delete())
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        helper.sleepInRecordMode(Duration.ofSeconds(30));

        StepVerifier.create(client.enqueueMessage("This should fail"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteNonExistent() {
        StepVerifier.create(client.delete())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void getProperties() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getPropertiesQueueDoesNotExist() {
        StepVerifier.create(client.getProperties())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void setMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.setMetadata(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void setMetadataQueueDoesNotExist() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.setMetadata(metadata))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void setInvalidMetadata() {
        Map<String, String> badMetadata = Collections.singletonMap("", "bad metadata");

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.setMetadata(badMetadata))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void deleteMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(client.create(metadata))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();

        StepVerifier.create(client.setMetadata(null))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(Collections.EMPTY_MAP, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void getAccessPolicy() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void getAccessPolicyQueueDoesNotExist() {
        StepVerifier.create(client.getAccessPolicy())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void setAccessPolicy() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier()
            .id("testpermission")
            .accessPolicy(accessPolicy);

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.getAccessPolicy())
            .assertNext(response -> helper.assertPermissionsAreEqual(permission, response))
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
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setInvalidAccessPolicy() {
        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier()
            .id("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .accessPolicy(accessPolicy);

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.setAccessPolicy(Collections.singletonList(permission)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void setTooManyAccessPolicies() {
        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        List<SignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            permissions.add(new SignedIdentifier()
                .id("policy" + i)
                .accessPolicy(accessPolicy));
        }

        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.setAccessPolicy(permissions))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void enqueueMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void enqueueEmptyMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertNull(peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void enqueueShortTimeToLiveMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofSeconds(2)))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages().delaySubscription(Duration.ofSeconds(5)))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Override
    public void enqueueQueueDoesNotExist() {
        StepVerifier.create(client.enqueueMessage("this should fail"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void dequeueMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages())
            .assertNext(dequeuedMessage -> assertEquals(messageText, dequeuedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void dequeueMultipleMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        String messageText2 = "test message 2";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage(messageText2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages(2))
            .assertNext(dequeuedMessage -> assertEquals(messageText, dequeuedMessage.messageText()))
            .assertNext(dequeuedMessage -> assertEquals(messageText2, dequeuedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void dequeueTooManyMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.dequeueMessages(64))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void dequeueQueueDoesNotExist() {
        StepVerifier.create(client.dequeueMessages())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void peekMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages())
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void peekMultipleMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        String messageText2 = "test message 2";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage(messageText2))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages(2))
            .assertNext(peekedMessage -> assertEquals(messageText, peekedMessage.messageText()))
            .assertNext(peekedMessage -> assertEquals(messageText2, peekedMessage.messageText()))
            .verifyComplete();
    }

    @Override
    public void peekTooManyMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.peekMessages(64))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void peekQueueDoesNotExist() {
        StepVerifier.create(client.peekMessages())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void clearMessages() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
        StepVerifier.create(client.enqueueMessage("test message"))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(3, response.value().approximateMessagesCount());
            })
            .verifyComplete();

        StepVerifier.create(client.clearMessages())
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
            })
            .verifyComplete();
    }

    @Override
    public void clearMessagesQueueDoesNotExist() {
        StepVerifier.create(client.clearMessages())
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages().blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());
        StepVerifier.create(client.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                helper.assertResponseStatusCode(response, 200);
                assertEquals(0, response.value().approximateMessagesCount());
            })
            .verifyComplete();
    }

    @Override
    public void deleteMessageInvalidMessageId() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = new DequeuedMessage();
        StepVerifier.create(client.dequeueMessages())
            .assertNext(response -> {
                assertEquals(messageText, response.messageText());
                dequeuedMessage.popReceipt(response.popReceipt()).messageId(response.messageId());
            })
            .verifyComplete();

        StepVerifier.create(client.deleteMessage(dequeuedMessage.messageId() + "random", dequeuedMessage.popReceipt()))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteMessageInvalidPopReceipt() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = new DequeuedMessage();
        StepVerifier.create(client.dequeueMessages())
            .assertNext(response -> {
                assertEquals(messageText, response.messageText());
                dequeuedMessage.popReceipt(response.popReceipt()).messageId(response.messageId());
            })
            .verifyComplete();

        StepVerifier.create(client.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt() + "random"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void deleteMessageQueueDoesNotExist() {
        StepVerifier.create(client.deleteMessage("invalid", "call"))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void updateMessage() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages().blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());

        String updatedMessageText = "updated test message";
        StepVerifier.create(client.updateMessage(updatedMessageText, dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), Duration.ofSeconds(1)))
            .assertNext(response -> helper.assertResponseStatusCode(response, 204))
            .verifyComplete();

        StepVerifier.create(client.peekMessages().delaySubscription(Duration.ofSeconds(2)))
            .assertNext(response -> assertEquals(updatedMessageText, response.messageText()))
            .verifyComplete();
    }

    @Override
    public void updateMessageInvalidMessageId() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages().blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());

        String updatedMessageText = "updated test message";
        StepVerifier.create(client.updateMessage(updatedMessageText, dequeuedMessage.messageId() + "random", dequeuedMessage.popReceipt(), Duration.ofSeconds(1)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void updateMessageInvalidPopReceipt() {
        StepVerifier.create(client.create())
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        String messageText = "test message";
        StepVerifier.create(client.enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        DequeuedMessage dequeuedMessage = client.dequeueMessages().blockFirst();
        assertEquals(messageText, dequeuedMessage.messageText());

        String updatedMessageText = "updated test message";
        StepVerifier.create(client.updateMessage(updatedMessageText, dequeuedMessage.messageId(), dequeuedMessage.popReceipt() + "random", Duration.ofSeconds(1)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }

    @Override
    public void updateMessageQueueDoesNotExist() {
        StepVerifier.create(client.updateMessage("queue", "doesn't", "exist", Duration.ofSeconds(5)))
            .verifyErrorSatisfies(throwable -> helper.assertExceptionStatusCode(throwable, 400));
    }
}
