// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static com.azure.storage.queue.QueueApiTests.CREATE_METADATA;
import static com.azure.storage.queue.QueueApiTests.TEST_METADATA;
import static com.azure.storage.queue.QueueTestHelper.assertAsyncResponseStatusCode;
import static com.azure.storage.queue.QueueTestHelper.assertExceptionStatusCodeAndMessage;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueueAsyncApiTests extends QueueTestBase {
    private String queueName;
    private QueueAsyncClient queueAsyncClient;

    @BeforeEach
    public void setup() {
        queueName = getRandomName(60);
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient();
        queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName);
    }

    @Test
    public void getQueueUrl() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString())
            .getAccountName();
        String expectURL = String.format("https://%s.queue.core.windows.net/%s", accountName, queueName);

        assertEquals(expectURL, queueAsyncClient.getQueueUrl());
    }

    @Test
    public void ipBasedEndpoint() {
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .connectionString(getPrimaryConnectionString())
            .endpoint("http://127.0.0.1:10001/devstoreaccount1/myqueue")
            .buildAsyncClient();

        assertEquals("devstoreaccount1", queueAsyncClient.getAccountName());
        assertEquals("myqueue", queueAsyncClient.getQueueName());
    }

    @Test
    public void createQueueWithSharedKey() {
        assertAsyncResponseStatusCode(queueAsyncClient.createWithResponse(null), 201);
    }

    @Test
    public void createIfNotExistsQueueWithSharedKey() {
        assertAsyncResponseStatusCode(queueAsyncClient.createIfNotExistsWithResponse(null), 201);
    }

    @Test
    public void createIfNotExistsMin() {
        String queueName = getRandomName(60);
        QueueAsyncClient client = primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName);

        assertEquals(queueName, client.getQueueName());
        assertDoesNotThrow(client::createIfNotExists);
        assertNotNull(client.getProperties());
    }

    @Test
    public void createIfNotExistsQueueWithSameMetadata() {
        assertAsyncResponseStatusCode(queueAsyncClient.createIfNotExistsWithResponse(null), 201);
        // if metadata is the same response code is 204
        assertAsyncResponseStatusCode(queueAsyncClient.createIfNotExistsWithResponse(null), 204);
    }

    @Test
    public void createIfNotExistsQueueWithConflictingMetadata() {
        assertAsyncResponseStatusCode(queueAsyncClient.createIfNotExistsWithResponse(CREATE_METADATA), 201);
        // if metadata is the different response code is 409
        assertAsyncResponseStatusCode(queueAsyncClient.createIfNotExistsWithResponse(TEST_METADATA), 409);
    }

    @Test
    public void deleteExistingQueue() {
        queueAsyncClient.createWithResponse(null).block();
        assertAsyncResponseStatusCode(queueAsyncClient.deleteWithResponse(), 204);
    }

    @Test
    public void deleteQueueError() {
        StepVerifier.create(queueAsyncClient.deleteWithResponse())
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @Test
    public void deleteIfExistsQueue() {
        queueAsyncClient.createWithResponse(null).block();
        assertAsyncResponseStatusCode(queueAsyncClient.deleteIfExistsWithResponse(), 204);
    }

    @Test
    public void deleteIfExistsQueueThatDoesNotExist() {
        StepVerifier.create(queueAsyncClient.deleteIfExistsWithResponse())
            .assertNext(response -> {
                assertEquals(404, response.getStatusCode());
                assertFalse(response.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void getProperties() {
        queueAsyncClient.createWithResponse(TEST_METADATA).block();
        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(0, response.getValue().getApproximateMessagesCount());
                assertEquals(TEST_METADATA, response.getValue().getMetadata());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(queueAsyncClient.getProperties())
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueApiTests#setAndClearMetadataSupplier")
    public void setAndClearMetadata(Map<String, String> create, Map<String, String> set,
        Map<String, String> expectedCreate, Map<String, String> expectedSet) {
        queueAsyncClient.createWithResponse(create).block();

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(expectedCreate, response.getValue().getMetadata());
            })
            .verifyComplete();

        assertAsyncResponseStatusCode(queueAsyncClient.setMetadataWithResponse(set), 204);

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(expectedSet, response.getValue().getMetadata());
            })
            .verifyComplete();
    }

    @Test
    public void setMetadataQueueError() {
        StepVerifier.create(queueAsyncClient.setMetadataWithResponse(TEST_METADATA))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueApiTests#setInvalidMetadataSupplier")
    public void setInvalidMetadata(String invalidKey, int statusCode, QueueErrorCode errMessage) {
        queueAsyncClient.create().block();
        StepVerifier.create(queueAsyncClient.setMetadataWithResponse(Collections.singletonMap(invalidKey, "value")))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, statusCode, errMessage));
    }

    @Test
    public void getAccessPolicy() {
        queueAsyncClient.create().block();
        StepVerifier.create(queueAsyncClient.getAccessPolicy())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    public void getAccessPolicyDoesError() {
        StepVerifier.create(queueAsyncClient.getAccessPolicy())
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @Test
    public void setAccessPolicy() {
        queueAsyncClient.create().block();
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("raup")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        QueueSignedIdentifier permission = new QueueSignedIdentifier()
            .setId("testpermission")
            .setAccessPolicy(accessPolicy);

        assertAsyncResponseStatusCode(queueAsyncClient.setAccessPolicyWithResponse(
            Collections.singletonList(permission)), 204);
        StepVerifier.create(queueAsyncClient.getAccessPolicy())
            .assertNext(policy -> QueueTestHelper.assertPermissionsAreEqual(permission, policy))
            .verifyComplete();
    }

    @Test
    public void setInvalidAccessPolicy() {
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        QueueSignedIdentifier permission = new QueueSignedIdentifier()
            .setId("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .setAccessPolicy(accessPolicy);
        queueAsyncClient.create().block();

        StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission)))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 400,
                QueueErrorCode.INVALID_XML_DOCUMENT));
    }

    @Test
    public void setMultipleAccessPolicies() {
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        List<QueueSignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            permissions.add(new QueueSignedIdentifier()
                .setId("policy" + i)
                .setAccessPolicy(accessPolicy));
        }
        queueAsyncClient.create().block();

        assertAsyncResponseStatusCode(queueAsyncClient.setAccessPolicyWithResponse(permissions), 204);
        StepVerifier.create(queueAsyncClient.getAccessPolicy())
            .assertNext(it -> QueueTestHelper.assertPermissionsAreEqual(permissions.get(0), it))
            .assertNext(it -> QueueTestHelper.assertPermissionsAreEqual(permissions.get(1), it))
            .assertNext(it -> QueueTestHelper.assertPermissionsAreEqual(permissions.get(2), it))
            .verifyComplete();
    }

    @Test
    public void setTooManyAccessPolicies() {
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        List<QueueSignedIdentifier> permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            permissions.add(new QueueSignedIdentifier()
                .setId("policy" + i)
                .setAccessPolicy(accessPolicy));
        }
        queueAsyncClient.create().block();

        StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(permissions))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 400,
                QueueErrorCode.INVALID_XML_DOCUMENT));
    }

    @Test
    public void enqueueMessage() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";

        assertAsyncResponseStatusCode(queueAsyncClient.sendMessageWithResponse(expectMsg, null, null), 201);
        StepVerifier.create(queueAsyncClient.peekMessage())
            .assertNext(peekedMessageItem -> assertEquals(expectMsg, peekedMessageItem.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void enqueueMessageBinaryData() {
        queueAsyncClient.create().block();
        BinaryData expectMsg = BinaryData.fromString("test message");

        assertAsyncResponseStatusCode(queueAsyncClient.sendMessageWithResponse(expectMsg, null, null), 201);
        StepVerifier.create(queueAsyncClient.peekMessage())
            .assertNext(message -> assertArraysEqual(expectMsg.toBytes(), message.getBody().toBytes()))
            .verifyComplete();
    }

    @Test
    public void enqueueEmptyMessage() {
        queueAsyncClient.create().block();

        assertAsyncResponseStatusCode(queueAsyncClient.sendMessageWithResponse("", null, null), 201);
        StepVerifier.create(queueAsyncClient.peekMessage())
            .assertNext(message -> assertNull(message.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void enqueueTimeToLive() {
        queueAsyncClient.create().block();

        assertAsyncResponseStatusCode(queueAsyncClient.sendMessageWithResponse("test message", Duration.ofSeconds(0),
            Duration.ofSeconds(2)), 201);
    }

    @Test
    public void enqueueMessageEncodedMessage() {
        queueAsyncClient.create().block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);
        BinaryData expectMsg = BinaryData.fromString("test message");

        assertAsyncResponseStatusCode(encodingQueueClient.sendMessageWithResponse(expectMsg, null, null), 201);
        StepVerifier.create(queueAsyncClient.peekMessage())
            .assertNext(message -> assertEquals(Base64.getEncoder().encodeToString(expectMsg.toBytes()),
                message.getBody().toString()))
            .verifyComplete();
    }

    @Test
    public void dequeueMessageFromEmptyQueue() {
        queueAsyncClient.create().block();
        StepVerifier.create(queueAsyncClient.receiveMessage()).verifyComplete();
    }

    @Test
    public void dequeueMessage() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        queueAsyncClient.sendMessage(expectMsg).block();

        StepVerifier.create(queueAsyncClient.receiveMessage())
            .assertNext(message -> assertEquals(expectMsg, message.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void dequeueEncodedMessage() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(encodedMsg).block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessage())
            .assertNext(message -> assertEquals(expectMsg, message.getBody().toString()))
            .verifyComplete();
    }

    @Test
    public void dequeueFailsWithoutHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        queueAsyncClient.sendMessage(expectMsg).block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessage()).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void dequeueWithHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<String> queueUrl = new AtomicReference<>();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                badMessage.set(failure.getQueueMessageItem());
                queueUrl.set(failure.getQueueAsyncClient().getQueueUrl());
                return Mono.empty();
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessages(10))
            .assertNext(message -> {
                assertEquals(expectMsg, message.getBody().toString());
                assertNotNull(badMessage.get());
                assertEquals(expectMsg, badMessage.get().getBody().toString());
                assertEquals(queueAsyncClient.getQueueUrl(), queueUrl.get());
            })
            .verifyComplete();
    }

    @Test
    public void dequeueAndDeleteWithHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                QueueMessageItem item = failure.getQueueMessageItem();
                badMessage.set(item);
                return failure.getQueueAsyncClient().deleteMessage(item.getMessageId(), item.getPopReceipt());
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessages(10))
            .assertNext(message -> {
                assertEquals(expectMsg, message.getBody().toString());
                assertNotNull(badMessage.get());
                assertEquals(expectMsg, badMessage.get().getBody().toString());
            })
            .verifyComplete();
    }

    @Test
    public void dequeueAndDeleteWithSyncHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError(failure -> {
                QueueMessageItem item = failure.getQueueMessageItem();
                badMessage.set(item);
                failure.getQueueClient().deleteMessage(item.getMessageId(), item.getPopReceipt());
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessages(10))
            .assertNext(message -> {
                assertEquals(expectMsg, message.getBody().toString());
                assertNotNull(badMessage.get());
                assertEquals(expectMsg, badMessage.get().getBody().toString());
            })
            .verifyComplete();
    }

    @Test
    public void dequeueWithHandlerError() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(message -> {
                throw new IllegalStateException("KABOOM");
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.receiveMessages(10)).verifyError(IllegalStateException.class);
    }

    @Test
    public void dequeueMultipleMessages() {
        queueAsyncClient.create().block();
        String expectMsg1 = "test message 1";
        String expectMsg2 = "test message 2";
        queueAsyncClient.sendMessage(expectMsg1).block();
        queueAsyncClient.sendMessage(expectMsg2).block();

        StepVerifier.create(queueAsyncClient.receiveMessages(2))
            .assertNext(message -> assertEquals(expectMsg1, message.getMessageText()))
            .assertNext(message -> assertEquals(expectMsg2, message.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void dequeueTooManyMessages() {
        queueAsyncClient.create().block();
        StepVerifier.create(queueAsyncClient.receiveMessages(33)).verifyErrorSatisfies(ex ->
            assertExceptionStatusCodeAndMessage(ex, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE));
    }

    @Test
    public void enqueueDequeueNonUtfMessage() {
        queueAsyncClient.create().block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);
        byte[] content = new byte[]{(byte) 0xFF, 0x00}; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content)).block();

        StepVerifier.create(encodingQueueClient.receiveMessage())
            .assertNext(message -> assertArraysEqual(content, message.getBody().toBytes()))
            .verifyComplete();
    }

    @Test
    public void enqueuePeekNonUtfMessage() {
        queueAsyncClient.create().block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);
        byte[] content = new byte[]{(byte) 0xFF, 0x00}; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content)).block();

        StepVerifier.create(encodingQueueClient.peekMessage())
            .assertNext(message -> assertArraysEqual(content, message.getBody().toBytes()))
            .verifyComplete();
    }

    @Test
    public void peekMessageFromEmptyQueue() {
        queueAsyncClient.create().block();
        StepVerifier.create(queueAsyncClient.peekMessage()).verifyComplete();
    }

    @Test
    public void peekMessage() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        queueAsyncClient.sendMessage(expectMsg).block();

        StepVerifier.create(queueAsyncClient.peekMessage())
            .assertNext(message -> assertEquals(expectMsg, message.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void peekEncodedMessage() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(encodedMsg).block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.peekMessage())
            .assertNext(message -> assertEquals(expectMsg, message.getBody().toString()))
            .verifyComplete();
    }

    @Test
    public void peekFailsWithoutHandler() {
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage("test message").block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.peekMessage()).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void peekWithHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        AtomicReference<PeekedMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<String> queueUrl = new AtomicReference<>();
        AtomicReference<Exception> cause = new AtomicReference<>();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                badMessage.set(failure.getPeekedMessageItem());
                queueUrl.set(failure.getQueueAsyncClient().getQueueUrl());
                cause.set(failure.getCause());
                return Mono.empty();
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.peekMessages(10))
            .assertNext(message -> {
                assertEquals(expectMsg, message.getBody().toString());
                assertNotNull(badMessage.get());
                assertEquals(expectMsg, badMessage.get().getBody().toString());
                assertEquals(queueAsyncClient.getQueueUrl(), queueUrl.get());
                assertNotNull(cause.get());
            })
            .verifyComplete();
    }

    @Test
    public void peekWithSyncHandler() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        AtomicReference<PeekedMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<Exception> cause = new AtomicReference<>();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError(failure -> {
                badMessage.set(failure.getPeekedMessageItem());
                cause.set(failure.getCause());
                // call some sync API
                failure.getQueueClient().getProperties();
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.peekMessages(10))
            .assertNext(peekedMessageItem -> {
                assertEquals(expectMsg, peekedMessageItem.getBody().toString());
                assertNotNull(badMessage.get());
                assertEquals(expectMsg, badMessage.get().getBody().toString());
                assertNotNull(cause.get());
            })
            .verifyComplete();
    }

    @Test
    public void peekWithHandlerException() {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueAsyncClient.sendMessage(expectMsg).block();
        queueAsyncClient.sendMessage(encodedMsg).block();
        QueueAsyncClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(message -> {
                throw new IllegalStateException("KABOOM");
            })
            .buildAsyncClient().getQueueAsyncClient(queueName);

        StepVerifier.create(encodingQueueClient.peekMessages(10)).verifyError(IllegalStateException.class);
    }

    @Test
    public void peekMultipleMessages() {
        queueAsyncClient.create().block();
        String expectMsg1 = "test message 1";
        String expectMsg2 = "test message 2";
        queueAsyncClient.sendMessage(expectMsg1).block();
        queueAsyncClient.sendMessage(expectMsg2).block();

        StepVerifier.create(queueAsyncClient.peekMessages(2))
            .assertNext(peekedMessageItem -> assertEquals(expectMsg1, peekedMessageItem.getMessageText()))
            .assertNext(peekedMessageItem -> assertEquals(expectMsg2, peekedMessageItem.getMessageText()))
            .verifyComplete();
    }

    @Test
    public void peekTooManyMessages() {
        queueAsyncClient.create().block();

        StepVerifier.create(queueAsyncClient.peekMessages(33)).verifyErrorSatisfies(ex ->
            assertExceptionStatusCodeAndMessage(ex, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE));
    }

    @Test
    public void peekMessagesError() {
        StepVerifier.create(queueAsyncClient.peekMessage())
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @Test
    public void clearMessages() {
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage("test message 1").block();
        queueAsyncClient.sendMessage("test message 2").block();
        queueAsyncClient.sendMessage("test message 3").block();

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(3, response.getValue().getApproximateMessagesCount());
            })
            .verifyComplete();

        assertAsyncResponseStatusCode(queueAsyncClient.clearMessagesWithResponse(), 204);

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(0, response.getValue().getApproximateMessagesCount());
            })
            .verifyComplete();
    }

    @Test
    public void clearMessagesError() {
        StepVerifier.create(queueAsyncClient.clearMessagesWithResponse())
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, 404, QueueErrorCode.QUEUE_NOT_FOUND));
    }

    @Test
    public void deleteMessage() {
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage("test message 1").block();
        queueAsyncClient.sendMessage("test message 2").block();
        queueAsyncClient.sendMessage("test message 3").block();
        QueueMessageItem dequeueMsg = queueAsyncClient.receiveMessage().block();

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(3, response.getValue().getApproximateMessagesCount());
            })
            .verifyComplete();

        assertAsyncResponseStatusCode(queueAsyncClient.deleteMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt()), 204);

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(2, response.getValue().getApproximateMessagesCount());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueApiTests#invalidArgsSupplier")
    public void deleteMessageInvalidArgs(boolean messageId, boolean popReceipt, int statusCode, QueueErrorCode errMsg) {
        queueAsyncClient.create().block();
        String expectMsg = "test message";
        queueAsyncClient.sendMessage(expectMsg).block();
        QueueMessageItem message = queueAsyncClient.receiveMessage().block();
        String deleteMessageId = messageId ? message.getMessageId() : message.getMessageId() + "Random";
        String deletePopReceipt = popReceipt ? message.getPopReceipt() : message.getPopReceipt() + "Random";

        StepVerifier.create(queueAsyncClient.deleteMessageWithResponse(deleteMessageId, deletePopReceipt))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, statusCode, errMsg));
    }

    @Test
    public void updateMessage() {
        String updateMsg = "Updated test message";
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage("test message before update").block();

        QueueMessageItem dequeueMsg = queueAsyncClient.receiveMessage().block();

        assertAsyncResponseStatusCode(queueAsyncClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), updateMsg, Duration.ofSeconds(1)), 204);

        StepVerifier.create(queueAsyncClient.peekMessage().delaySubscription(getMessageUpdateDelay(2000)))
            .assertNext(peekedMessageItem -> assertEquals(updateMsg, peekedMessageItem.getMessageText()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.queue.QueueApiTests#invalidArgsSupplier")
    public void updateMessageInvalidArgs(boolean messageId, boolean popReceipt, int statusCode, QueueErrorCode errMsg) {
        queueAsyncClient.create().block();
        String updateMsg = "Updated test message";
        queueAsyncClient.sendMessage("test message before update").block();
        QueueMessageItem message = queueAsyncClient.receiveMessage().block();

        String updateMessageId = messageId ? message.getMessageId() : message.getMessageId() + "Random";
        String updatePopReceipt = popReceipt ? message.getPopReceipt() : message.getPopReceipt() + "Random";

        StepVerifier.create(queueAsyncClient.updateMessageWithResponse(updateMessageId, updatePopReceipt, updateMsg,
                Duration.ofSeconds(1)))
            .verifyErrorSatisfies(ex -> assertExceptionStatusCodeAndMessage(ex, statusCode, errMsg));
    }

    @Test
    public void updateMessageNoBody() {
        String messageText = "test message before update";
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage(messageText).block();

        QueueMessageItem dequeueMsg = queueAsyncClient.receiveMessage().block();

        assertAsyncResponseStatusCode(queueAsyncClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), null, Duration.ofSeconds(1), null), 204);

        sleepIfRunningAgainstService(2000);

        StepVerifier.create(queueAsyncClient.peekMessage()).assertNext(peekedMessageItem ->
                assertEquals(messageText, peekedMessageItem.getMessageText())).verifyComplete();
    }

    @Test
    public void updateMessageNullDuration() {
        String messageText = "test message before update";
        queueAsyncClient.create().block();
        queueAsyncClient.sendMessage(messageText).block();

        QueueMessageItem dequeueMsg = queueAsyncClient.receiveMessage().block();

        assertAsyncResponseStatusCode(queueAsyncClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), null, null), 204);

        sleepIfRunningAgainstService(2000);

        StepVerifier.create(queueAsyncClient.peekMessage()).assertNext(peekedMessageItem ->
            assertEquals(messageText, peekedMessageItem.getMessageText())).verifyComplete();
    }

    @Test
    public void getQueueName() {
        assertEquals(queueName, queueAsyncClient.getQueueName());
    }

    @Test
    public void builderBearerTokenValidation() throws MalformedURLException {
        URL url = new URL(queueAsyncClient.getQueueUrl());
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString();

        assertThrows(IllegalArgumentException.class, () -> new QueueClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAsyncClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        QueueAsyncClient queueAsyncClient = queueBuilderHelper().addPolicy(getPerCallVersionPolicy())
            .buildAsyncClient();

        queueAsyncClient.create().block();

        StepVerifier.create(queueAsyncClient.getPropertiesWithResponse()).assertNext(queuePropertiesResponse ->
            assertEquals("2017-11-09", queuePropertiesResponse.getHeaders().getValue("x-ms-version"))).verifyComplete();
    }
}
