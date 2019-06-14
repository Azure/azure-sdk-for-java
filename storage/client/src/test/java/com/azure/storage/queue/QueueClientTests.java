// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.UpdatedMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class QueueClientTests extends QueueClientTestsBase {
    private QueueClient client;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();

        if (interceptorManager.isPlaybackMode()) {
            client = setupClient((connectionString, endpoint) -> QueueClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            client = setupClient((connectionString, endpoint) -> QueueClient.builder()
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
            client.clearMessages();
            client.delete();
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

        TestHelpers.assertResponseStatusCode(client.create(metadata), 201);

        QueueProperties properties = client.getProperties().value();
        assertEquals(metadata, properties.metadata());
    }

    @Override
    public void createTwiceSameMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        TestHelpers.assertResponseStatusCode(client.create(metadata), 201);
        TestHelpers.assertResponseStatusCode(client.create(metadata), 204);
    }

    @Override
    public void createTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        TestHelpers.assertResponseStatusCode(client.create(), 201);

        try {
            client.create(metadata);
            fail("Creating a queue twice with different metadata values should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void deleteExisting() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);
        TestHelpers.assertResponseStatusCode(client.enqueueMessage("This queue will be deleted"), 201);
        TestHelpers.assertResponseStatusCode(client.delete(), 204);

        TestHelpers.sleep(Duration.ofSeconds(30));

        try {
            client.enqueueMessage("This should fail");
            fail("Attempting to work with a queue that has been deleted should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteNonExistent() {
        try {
            client.delete();
            fail("Attempting to delete a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void getProperties() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        TestHelpers.assertResponseStatusCode(client.create(metadata), 201);

        Response<QueueProperties> response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(0, response.value().approximateMessagesCount());
        assertEquals(metadata, response.value().metadata());
    }

    @Override
    public void getPropertiesQueueDoesNotExist() {
        try {
            client.getProperties();
            fail("Attempting to get properties of a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void setMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        TestHelpers.assertResponseStatusCode(client.create(), 201);

        TestHelpers.assertResponseStatusCode(client.setMetadata(metadata), 204);

        Response<QueueProperties> response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(0, response.value().approximateMessagesCount());
        assertEquals(metadata, response.value().metadata());
    }

    @Override
    public void setMetadataQueueDoesNotExist() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        try {
            client.setMetadata(metadata);
            fail("Attempting to set metadata on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void setInvalidMetadata() {
        Map<String, String> badMetadata = Collections.singletonMap("", "bad metadata");

        TestHelpers.assertResponseStatusCode(client.create(), 201);
        try {
            client.setMetadata(badMetadata);
            fail("Attempting to set invalid metadata on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void deleteMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        TestHelpers.assertResponseStatusCode(client.create(metadata), 201);

        Response<QueueProperties> response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(0, response.value().approximateMessagesCount());
        assertEquals(metadata, response.value().metadata());

        TestHelpers.assertResponseStatusCode(client.setMetadata(null), 204);

        response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(0, response.value().approximateMessagesCount());
        assertEquals(Collections.EMPTY_MAP, response.value().metadata());
    }

    @Override
    public void getAccessPolicy() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        Iterable<SignedIdentifier> accessPolicies = client.getAccessPolicy();
        assertFalse(accessPolicies.iterator().hasNext());
    }

    @Override
    public void getAccessPolicyQueueDoesNotExist() {
        try {
            client.getAccessPolicy().iterator().hasNext();
            fail("Attempting to get access policies on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void setAccessPolicy() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        SignedIdentifier permission = new SignedIdentifier()
            .id("testpermission")
            .accessPolicy(accessPolicy);

        TestHelpers.assertResponseStatusCode(client.setAccessPolicy(Collections.singletonList(permission)), 204);

        Iterator<SignedIdentifier> accessPolicies = client.getAccessPolicy().iterator();
        TestHelpers.assertPermissionsAreEqual(permission, accessPolicies.next());
        assertFalse(accessPolicies.hasNext());
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

        try {
            client.setAccessPolicy(Collections.singletonList(permission));
            fail("Attempting to set access policies on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
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

        TestHelpers.assertResponseStatusCode(client.create(), 201);

        try {
            client.setAccessPolicy(Collections.singletonList(permission));
            fail("Attempting to set invalid access policies on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void setTooManyAccessPolicies() {
        List<SignedIdentifier> permissions = new ArrayList<>();

        AccessPolicy accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        for (int i = 0; i < 6; i++) {
            permissions.add(new SignedIdentifier()
                .id("policy" + i)
                .accessPolicy(accessPolicy));
        }

        TestHelpers.assertResponseStatusCode(client.create(), 201);

        try {
            client.setAccessPolicy(permissions);
            fail("Attempting to set more than five access policies on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void enqueueMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<PeekedMessage> response = client.peekMessages().iterator();
        assertEquals(messageText, response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void enqueueEmptyMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<PeekedMessage> response = client.peekMessages().iterator();
        assertNull(response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void enqueueShortTimeToLiveMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText, Duration.ofSeconds(0), Duration.ofSeconds(2)), 201);

        TestHelpers.sleep(Duration.ofSeconds(5));
        Iterator<PeekedMessage> response = client.peekMessages().iterator();
        assertFalse(response.hasNext());
    }

    @Override
    public void enqueueQueueDoesNotExist() {
        try {
            client.enqueueMessage("This should fail");
            fail("Attempting to enqueue a message on a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void dequeueMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        assertEquals(messageText, response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void dequeueMultipleMessages() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        String messageText2 = "test message 2";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText2), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages(2).iterator();
        assertEquals(messageText, response.next().messageText());
        assertEquals(messageText2, response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void dequeueTooManyMessages() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        try {
            client.dequeueMessages(64).iterator().hasNext();
            fail("Attempting to get more than 32 messages from a queue should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void dequeueQueueDoesNotExist() {
        try {
            client.dequeueMessages().iterator().hasNext();
            fail("Attempting to get messages from a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void peekMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<PeekedMessage> response = client.peekMessages().iterator();
        assertEquals(messageText, response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void peekMultipleMessages() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        String messageText2 = "test message 2";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText2), 201);

        Iterator<PeekedMessage> response = client.peekMessages(2).iterator();
        assertEquals(messageText, response.next().messageText());
        assertEquals(messageText2, response.next().messageText());
        assertFalse(response.hasNext());
    }

    @Override
    public void peekTooManyMessages() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        try {
            client.peekMessages(64).iterator().hasNext();
            fail("Attempting to peek more than 32 messages from a queue should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void peekQueueDoesNotExist() {
        try {
            client.peekMessages().iterator().hasNext();
            fail("Attempting to peek messages from a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void clearMessages() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        for (int i = 0; i < 3; i++) {
            TestHelpers.assertResponseStatusCode(client.enqueueMessage("test message"), 201);
        }

        Response<QueueProperties> response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(3, response.value().approximateMessagesCount());

        TestHelpers.assertResponseStatusCode(client.clearMessages(), 204);

        response = client.getProperties();
        TestHelpers.assertResponseStatusCode(response, 200);
        assertEquals(0, response.value().approximateMessagesCount());
    }

    @Override
    public void clearMessagesQueueDoesNotExist() {
        try {
            client.clearMessages();
            fail("Attempting to clear messages of a queue that doesn't exist should throw an exception");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertFalse(response.hasNext());
        assertEquals(messageText, message.messageText());

        TestHelpers.assertResponseStatusCode(client.deleteMessage(message.messageId(), message.popReceipt()), 204);

        Response<QueueProperties> propertiesResponse = client.getProperties();
        TestHelpers.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(0, propertiesResponse.value().approximateMessagesCount());
    }

    @Override
    public void deleteMessageInvalidMessageId() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertFalse(response.hasNext());
        assertEquals(messageText, message.messageText());

        try {
            client.deleteMessage(message.messageId() + "random", message.popReceipt());
            fail("Attempting to delete a message with an invalid ID should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteMessageInvalidPopReceipt() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertFalse(response.hasNext());
        assertEquals(messageText, message.messageText());

        try {
            client.deleteMessage(message.messageId(), message.popReceipt() + "random");
            fail("Attempting to delete a message with an invalid popReceipt should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void deleteMessageQueueDoesNotExist() {
        try {
            client.deleteMessage("invalid", "call");
            fail("Attempting to delete a message from a queue that doesn't exist should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void updateMessage() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertEquals(messageText, message.messageText());
        assertFalse(response.hasNext());

        String updatedMessageText = "updated test message";
        Response<UpdatedMessage> updatedMessageResponse = client.updateMessage(updatedMessageText, message.messageId(), message.popReceipt(), Duration.ofSeconds(1));
        TestHelpers.assertResponseStatusCode(updatedMessageResponse, 204);

        TestHelpers.sleep(Duration.ofSeconds(2));

        Iterator<PeekedMessage> peekedMessageIterator = client.peekMessages().iterator();
        PeekedMessage peekedMessage = peekedMessageIterator.next();
        assertEquals(updatedMessageText, peekedMessage.messageText());
        assertFalse(peekedMessageIterator.hasNext());
    }

    @Override
    public void updateMessageInvalidMessageId() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertEquals(messageText, message.messageText());
        assertFalse(response.hasNext());

        String updatedMessageText = "updated test message";
        try {
            client.updateMessage(updatedMessageText, message.messageId() + "random", message.popReceipt(), Duration.ofSeconds(1));
            fail("Attempting to update a message with an invalid ID should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void updateMessageInvalidPopReceipt() {
        TestHelpers.assertResponseStatusCode(client.create(), 201);

        String messageText = "test message";
        TestHelpers.assertResponseStatusCode(client.enqueueMessage(messageText), 201);

        Iterator<DequeuedMessage> response = client.dequeueMessages().iterator();
        DequeuedMessage message = response.next();
        assertEquals(messageText, message.messageText());
        assertFalse(response.hasNext());

        String updatedMessageText = "updated test message";
        try {
            client.updateMessage(updatedMessageText, message.messageId(), message.popReceipt() + "random", Duration.ofSeconds(1));
            fail("Attempting to update a message with an invalid popReceipt should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }

    @Override
    public void updateMessageQueueDoesNotExist() {
        try {
            client.updateMessage("queue", "doesn't", "exist", Duration.ofSeconds(5));
            fail("Attempting to update a message on a queue that doesn't exist should throw an exception.");
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 400);
        }
    }
}
