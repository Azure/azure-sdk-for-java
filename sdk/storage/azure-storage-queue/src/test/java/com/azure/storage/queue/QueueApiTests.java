// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueAccessPolicy;
import com.azure.storage.queue.models.QueueAudience;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueueApiTests extends QueueTestBase {
    static final Map<String, String> TEST_METADATA = Collections.singletonMap("metadata", "value");
    static final Map<String, String> CREATE_METADATA = Collections.singletonMap("metadata1", "value");

    private String queueName;
    private QueueClient queueClient;

    @BeforeEach
    public void setup() {
        queueName = getRandomName(60);
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient();
        queueClient = primaryQueueServiceClient.getQueueClient(queueName);
    }

    @Test
    public void getQueueUrl() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(getPrimaryConnectionString())
            .getAccountName();
        String expectedUrl = String.format("https://%s.queue.core.windows.net/%s", accountName, queueName);

        assertEquals(expectedUrl, queueClient.getQueueUrl());
    }

    @Test
    public void ipBaseEndpoint() {
        QueueClient queueClient = new QueueClientBuilder()
            .connectionString(getPrimaryConnectionString())
            .endpoint("http://127.0.0.1:10001/devstoreaccount1/myqueue")
            .buildClient();

        assertEquals("devstoreaccount1", queueClient.getAccountName());
        assertEquals("myqueue", queueClient.getQueueName());
    }

    @Test
    public void createQueueWithSharedKey() {
        QueueTestHelper.assertResponseStatusCode(queueClient.createWithResponse(null, null, null), 201);
    }

    @Test
    public void createIfNotExistsQueueWithSharedKey() {
        QueueTestHelper.assertResponseStatusCode(queueClient.createIfNotExistsWithResponse(null, null, null), 201);
    }

    @Test
    public void createIfNotExistsMin() {
        String queueName = getRandomName(60);
        QueueClient client = primaryQueueServiceClient.getQueueClient(queueName);

        assertEquals(queueName, client.getQueueName());
        assertDoesNotThrow(client::createIfNotExists);
        assertNotNull(client.getProperties());
    }

    @Test
    public void createIfNotExistsWithSameMetadataOnAQueueClientThatAlreadyExists() {
        String queueName = getRandomName(60);
        QueueClient client = primaryQueueServiceClient.getQueueClient(queueName);

        assertEquals(201, client.createIfNotExistsWithResponse(null, null, null).getStatusCode());
        assertEquals(204, client.createIfNotExistsWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsWithConflictingMetadataOnAQueueClientThatAlreadyExists() {
        String queueName = getRandomName(60);
        QueueClient client = primaryQueueServiceClient.getQueueClient(queueName);

        Response<Boolean> initialResponse = client.createIfNotExistsWithResponse(TEST_METADATA, null, null);
        Response<Boolean> secondResponse = client.createIfNotExistsWithResponse(null, null, null);

        assertEquals(201, initialResponse.getStatusCode());
        assertTrue(initialResponse.getValue());
        assertEquals(409, secondResponse.getStatusCode());
        assertFalse(secondResponse.getValue());
    }

    @Test
    public void deleteExistingQueue() {
        queueClient.create();
        QueueTestHelper.assertResponseStatusCode(queueClient.deleteWithResponse(null, null), 204);
    }


    @Test
    public void deleteQueueError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class, queueClient::delete);
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsMin() {
        queueClient.create();
        assertTrue(queueClient.deleteIfExists());
    }

    @Test
    public void deleteIfExistsQueue() {
        queueClient.create();
        QueueTestHelper.assertResponseStatusCode(queueClient.deleteIfExistsWithResponse(null, null), 204);
    }

    @Test
    public void deleteIfExistsWithResponseOnAQueueClientThatDoesNotExist() {
        String queueName = getRandomName(60);
        QueueClient client = primaryQueueServiceClient.getQueueClient(queueName);

        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null);
        assertEquals(404, response.getStatusCode());
        assertFalse(response.getValue());
    }

    @Test
    public void getProperties() {
        queueClient.createWithResponse(TEST_METADATA, null, null);
        Response<QueueProperties> response = queueClient.getPropertiesWithResponse(null, null);

        QueueTestHelper.assertResponseStatusCode(response, 200);
        assertEquals(0, response.getValue().getApproximateMessagesCount());
        assertEquals(TEST_METADATA, response.getValue().getMetadata());
    }

    @Test
    public void getPropertiesError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.getPropertiesWithResponse(null, null));

        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("setAndClearMetadataSupplier")
    public void setAndClearMetadata(Map<String, String> create, Map<String, String> set,
        Map<String, String> expectedCreate, Map<String, String> expectedSet) {
        queueClient.createWithResponse(create, null, null);

        Response<QueueProperties> response = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, response.getStatusCode());
        assertEquals(expectedCreate, response.getValue().getMetadata());

        assertEquals(204, queueClient.setMetadataWithResponse(set, null, null).getStatusCode());

        response = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, response.getStatusCode());
        assertEquals(expectedSet, response.getValue().getMetadata());
    }

    public static Stream<Arguments> setAndClearMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, TEST_METADATA, Collections.emptyMap(), TEST_METADATA),
            Arguments.of(CREATE_METADATA, TEST_METADATA, CREATE_METADATA, TEST_METADATA),
            Arguments.of(CREATE_METADATA, null, CREATE_METADATA, Collections.emptyMap()),
            Arguments.of(TEST_METADATA, TEST_METADATA, TEST_METADATA, TEST_METADATA),
            Arguments.of(null, null, Collections.emptyMap(), Collections.emptyMap())
        );
    }

    @Test
    public void setMetadataQueueError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.setMetadata(TEST_METADATA));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("setInvalidMetadataSupplier")
    public void setInvalidMetadata(String invalidKey, int statusCode, QueueErrorCode errMessage) {
        queueClient.create();
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.setMetadata(Collections.singletonMap(invalidKey, "value")));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, statusCode, errMessage);
    }

    public static Stream<Arguments> setInvalidMetadataSupplier() {
        return Stream.of(
            Arguments.of("invalid-meta", 400, QueueErrorCode.INVALID_METADATA),
            Arguments.of("12345", 400, QueueErrorCode.INVALID_METADATA),
            Arguments.of("", 400, QueueErrorCode.EMPTY_METADATA_KEY)
        );
    }

    @Test
    public void getAccessPolicy() {
        queueClient.create();
        assertFalse(queueClient.getAccessPolicy().iterator().hasNext());
    }

    @Test
    public void getAccessPolicyError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.getAccessPolicy().iterator().next());
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @Test
    public void setAccessPolicy() {
        queueClient.create();
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("raup")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        QueueSignedIdentifier permission = new QueueSignedIdentifier()
            .setId("testpermission")
            .setAccessPolicy(accessPolicy);

        QueueTestHelper.assertResponseStatusCode(
            queueClient.setAccessPolicyWithResponse(Collections.singletonList(permission), null, null), 204);
        QueueTestHelper.assertPermissionsAreEqual(permission, queueClient.getAccessPolicy().iterator().next());
    }

    @Test
    public void setInvalidAccessPolicy() {
        queueClient.create();
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        QueueSignedIdentifier permission = new QueueSignedIdentifier()
            .setId("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .setAccessPolicy(accessPolicy);

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.setAccessPolicy(Collections.singletonList(permission)));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 400, QueueErrorCode.INVALID_XML_DOCUMENT);
    }

    @Test
    public void setMultipleAccessPolicies() {
        queueClient.create();
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        List<QueueSignedIdentifier> permissions = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            permissions.add(new QueueSignedIdentifier().setId("policy" + i).setAccessPolicy(accessPolicy));
        }

        assertEquals(204, queueClient.setAccessPolicyWithResponse(permissions, null, Context.NONE).getStatusCode());
        Iterator<QueueSignedIdentifier> nextAccessPolicy = queueClient.getAccessPolicy().iterator();
        QueueTestHelper.assertPermissionsAreEqual(permissions.get(0), nextAccessPolicy.next());
        QueueTestHelper.assertPermissionsAreEqual(permissions.get(1), nextAccessPolicy.next());
        QueueTestHelper.assertPermissionsAreEqual(permissions.get(2), nextAccessPolicy.next());
        assertFalse(nextAccessPolicy.hasNext());
    }

    @Test
    public void setTooManyAccessPolicies() {
        queueClient.create();
        QueueAccessPolicy accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));
        List<QueueSignedIdentifier> permissions = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            permissions.add(new QueueSignedIdentifier().setId("policy" + i).setAccessPolicy(accessPolicy));
        }

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.setAccessPolicyWithResponse(permissions, null, Context.NONE));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 400, QueueErrorCode.INVALID_XML_DOCUMENT);
    }

    @Test
    public void enqueueMessage() {
        queueClient.create();
        String expectMsg = "test message";

        assertEquals(201, queueClient.sendMessageWithResponse(expectMsg, null, null, null, null).getStatusCode());
        assertEquals(expectMsg, queueClient.peekMessage().getMessageText());
    }

    @Test
    public void enqueueMessageBinaryData() {
        queueClient.create();
        BinaryData expectMsg = BinaryData.fromString("test message");

        assertEquals(201, queueClient.sendMessageWithResponse(expectMsg, null, null, null, null).getStatusCode());
        assertArraysEqual(expectMsg.toBytes(), queueClient.peekMessage().getBody().toBytes());
    }

    @Test
    public void enqueueEmptyMessage() {
        queueClient.create();
        String expectMsg = "";

        assertEquals(201, queueClient.sendMessageWithResponse(expectMsg, null, null, null, null).getStatusCode());
        assertNull(queueClient.peekMessage().getMessageText());
    }

    @Test
    public void enqueueTimeToLive() {
        queueClient.create();

        assertEquals(201, queueClient.sendMessageWithResponse("test message", Duration.ofSeconds(0),
            Duration.ofSeconds(2), Duration.ofSeconds(5), null).getStatusCode());
    }

    @Test
    public void enqueueMessageEncodedMessage() {
        queueClient.create();
        QueueClient encodingClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient().getQueueClient(queueName);
        BinaryData expectMsg = BinaryData.fromString("test message");

        assertEquals(201, encodingClient.sendMessageWithResponse(expectMsg, null, null, null, null).getStatusCode());
        assertEquals(Base64.getEncoder().encodeToString(expectMsg.toBytes()),
            queueClient.peekMessage().getBody().toString());
    }

    @Test
    public void dequeueMessageFromEmptyQueue() {
        queueClient.create();
        assertNull(queueClient.receiveMessage());
    }

    @Test
    public void dequeueMessage() {
        queueClient.create();
        String expectMsg = "test message";
        queueClient.sendMessage(expectMsg);

        assertEquals(expectMsg, queueClient.receiveMessage().getMessageText());
    }

    @Test
    public void dequeueEncodedMessage() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        QueueClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient().getQueueClient(queueName);

        assertEquals(expectMsg, encodingQueueClient.receiveMessage().getBody().toString());
    }

    @Test
    public void dequeueFailsWithoutHandler() {
        queueClient.create();
        String expectMsg = "test message";
        queueClient.sendMessage(expectMsg);
        QueueClient encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64)
            .buildClient().getQueueClient(queueName);

        assertThrows(IllegalArgumentException.class, encodingQueueClient::receiveMessage);
    }

    @Test
    public void dequeueWithHandler() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        queueClient.sendMessage(expectMsg);
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<String> queueUrl = new AtomicReference<>();

        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                badMessage.set(failure.getQueueMessageItem());
                queueUrl.set(failure.getQueueAsyncClient().getQueueUrl());
                return Mono.empty();
            })
            .buildClient().getQueueClient(queueName);

        List<QueueMessageItem> messageItems = encodingQueueClient.receiveMessages(10).stream()
            .collect(Collectors.toList());

        assertEquals(1, messageItems.size());
        assertEquals(expectMsg, messageItems.get(0).getBody().toString());
        assertNotNull(badMessage.get());
        assertEquals(expectMsg, badMessage.get().getBody().toString());
        assertEquals(queueClient.getQueueUrl(), queueUrl.get());
    }

    @Test
    public void dequeueAndDeleteWithHandler() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        queueClient.sendMessage(expectMsg);
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                QueueMessageItem item = failure.getQueueMessageItem();
                badMessage.set(item);
                return failure.getQueueAsyncClient().deleteMessage(item.getMessageId(), item.getPopReceipt());
            })
            .buildClient().getQueueClient(queueName);

        List<QueueMessageItem> messageItems = encodingQueueClient.receiveMessages(10).stream()
            .collect(Collectors.toList());

        assertEquals(1, messageItems.size());
        assertEquals(expectMsg, messageItems.get(0).getBody().toString());
        assertNotNull(badMessage.get());
        assertEquals(expectMsg, badMessage.get().getBody().toString());
    }

    @Test
    public void dequeueAndDeleteWithSyncHandler() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        queueClient.sendMessage(expectMsg);
        AtomicReference<QueueMessageItem> badMessage = new AtomicReference<>();
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError(failure -> {
                QueueMessageItem item = failure.getQueueMessageItem();
                badMessage.set(item);
                failure.getQueueClient().deleteMessage(item.getMessageId(), item.getPopReceipt());
            })
            .buildClient().getQueueClient(queueName);

        List<QueueMessageItem> messageItems = encodingQueueClient.receiveMessages(10).stream()
            .collect(Collectors.toList());
        assertEquals(1, messageItems.size());
        assertEquals(expectMsg, messageItems.get(0).getBody().toString());
        assertNotNull(badMessage.get());
        assertEquals(expectMsg, badMessage.get().getBody().toString());
    }

    @Test
    public void dequeueWithHandlerError() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        queueClient.sendMessage(expectMsg);
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(message -> {
                throw new IllegalStateException("KABOOM");
            })
            .buildClient().getQueueClient(queueName);

        assertThrows(IllegalStateException.class, () -> encodingQueueClient.receiveMessages(10).iterator().next());
    }

    @Test
    public void dequeueMultipleMessages() {
        queueClient.create();
        String expectMsg1 = "test message 1";
        String expectMsg2 = "test message 2";
        queueClient.sendMessage(expectMsg1);
        queueClient.sendMessage(expectMsg2);

        Iterator<QueueMessageItem> dequeueMsgIter = queueClient.receiveMessages(2).iterator();
        assertEquals(expectMsg1, dequeueMsgIter.next().getMessageText());
        assertEquals(expectMsg2, dequeueMsgIter.next().getMessageText());
    }

    @Test
    public void dequeueTooManyMessages() {
        queueClient.create();

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.receiveMessages(33).iterator().next());
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 400,
            QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE);
    }

    @Test
    public void enqueueDequeueNonUtfMessage() {
        queueClient.create();
        QueueClient encodingQueueClient = getBase64Client();
        byte[] content = new byte[]{(byte) 0xFF, 0x00}; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content));

        assertArraysEqual(content, encodingQueueClient.receiveMessage().getBody().toBytes());
    }

    @Test
    public void enqueuePeekNonUtfMessage() {
        queueClient.create();
        QueueClient encodingQueueClient = getBase64Client();
        byte[] content = new byte[]{(byte) 0xFF, 0x00}; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content));

        assertArraysEqual(content, encodingQueueClient.peekMessage().getBody().toBytes());
    }

    @Test
    public void peekMessageFromEmptyQueue() {
        queueClient.create();
        assertNull(queueClient.peekMessage());
    }

    @Test
    public void peekMessage() {
        queueClient.create();
        String expectMsg = "test message";
        queueClient.sendMessage(expectMsg);

        assertEquals(expectMsg, queueClient.peekMessage().getBody().toString());
    }

    @Test
    public void peekEncodedMessage() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(encodedMsg);
        QueueClient encodingQueueClient = getBase64Client();

        assertEquals(expectMsg, encodingQueueClient.peekMessage().getBody().toString());
    }

    @Test
    public void peekFailsWithoutHandler() {
        queueClient.create();
        String expectMsg = "test message";
        queueClient.sendMessage(expectMsg);
        QueueClient encodingQueueClient = getBase64Client();

        assertThrows(IllegalArgumentException.class, encodingQueueClient::peekMessage);
    }

    @Test
    public void peekWithHandler() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(expectMsg);
        queueClient.sendMessage(encodedMsg);
        AtomicReference<PeekedMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<String> queueUrl = new AtomicReference<>();
        AtomicReference<Exception> cause = new AtomicReference<>();
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(failure -> {
                badMessage.set(failure.getPeekedMessageItem());
                queueUrl.set(failure.getQueueAsyncClient().getQueueUrl());
                cause.set(failure.getCause());
                return Mono.empty();
            })
            .buildClient().getQueueClient(queueName);

        List<PeekedMessageItem> peekedMessages = encodingQueueClient.peekMessages(10, null, null).stream()
            .collect(Collectors.toList());

        assertEquals(1, peekedMessages.size());
        assertEquals(expectMsg, peekedMessages.get(0).getBody().toString());
        assertNotNull(badMessage.get());
        assertEquals(expectMsg, badMessage.get().getBody().toString());
        assertEquals(queueClient.getQueueUrl(), queueUrl.get());
        assertNotNull(cause.get());
    }

    @Test
    public void peekWithSyncHandler() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(expectMsg);
        queueClient.sendMessage(encodedMsg);
        AtomicReference<PeekedMessageItem> badMessage = new AtomicReference<>();
        AtomicReference<Exception> cause = new AtomicReference<>();
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError(failure -> {
                badMessage.set(failure.getPeekedMessageItem());
                cause.set(failure.getCause());
                // call some sync API here
                failure.getQueueClient().getProperties();
            })
            .buildClient().getQueueClient(queueName);

        List<PeekedMessageItem> peekedMessages = encodingQueueClient.peekMessages(10, null, null).stream()
            .collect(Collectors.toList());

        assertEquals(1, peekedMessages.size());
        assertEquals(expectMsg, peekedMessages.get(0).getBody().toString());
        assertNotNull(badMessage.get());
        assertEquals(expectMsg, badMessage.get().getBody().toString());
        assertNotNull(cause.get());
    }

    @Test
    public void peekWithHandlerException() {
        queueClient.create();
        String expectMsg = "test message";
        String encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8));
        queueClient.sendMessage(expectMsg);
        queueClient.sendMessage(encodedMsg);
        QueueClient encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync(message -> {
                throw new IllegalStateException("KABOOM");
            })
            .buildClient().getQueueClient(queueName);

        assertThrows(IllegalStateException.class,
            () -> encodingQueueClient.peekMessages(10, null, null).iterator().next());
    }

    @Test
    public void peekMultipleMessages() {
        queueClient.create();
        String expectMsg1 = "test message 1";
        String expectMsg2 = "test message 2";
        queueClient.sendMessage(expectMsg1);
        queueClient.sendMessage(expectMsg2);

        Iterator<PeekedMessageItem> peekMsgIter = queueClient.peekMessages(2, Duration.ofSeconds(10), null).iterator();
        assertEquals(expectMsg1, peekMsgIter.next().getMessageText());
        assertEquals(expectMsg2, peekMsgIter.next().getMessageText());
        assertFalse(peekMsgIter.hasNext());
    }

    @Test
    public void peekTooManyMessages() {
        queueClient.create();

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.peekMessages(33, null, null).iterator().next());
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 400,
            QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE);
    }

    @Test
    public void peekMessageError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class, queueClient::peekMessage);
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @Test
    public void clearMessages() {
        queueClient.create();
        queueClient.sendMessage("test message 1");
        queueClient.sendMessage("test message 2");
        queueClient.sendMessage("test message 3");

        Response<QueueProperties> propertiesResponse = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, propertiesResponse.getStatusCode());
        assertEquals(3, propertiesResponse.getValue().getApproximateMessagesCount());

        assertEquals(204, queueClient.clearMessagesWithResponse(null, null).getStatusCode());

        propertiesResponse = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, propertiesResponse.getStatusCode());
        assertEquals(0, propertiesResponse.getValue().getApproximateMessagesCount());
    }

    @Test
    public void clearMessagesError() {
        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.clearMessagesWithResponse(null, null));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, 404, QueueErrorCode.QUEUE_NOT_FOUND);
    }

    @Test
    public void deleteMessage() {
        queueClient.create();
        queueClient.sendMessage("test message 1");
        queueClient.sendMessage("test message 2");
        queueClient.sendMessage("test message 3");
        QueueMessageItem dequeueMsg = queueClient.receiveMessage();

        Response<QueueProperties> propertiesResponse = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, propertiesResponse.getStatusCode());
        assertEquals(3, propertiesResponse.getValue().getApproximateMessagesCount());

        assertEquals(204, queueClient.deleteMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(),
            null, null).getStatusCode());

        propertiesResponse = queueClient.getPropertiesWithResponse(null, null);
        assertEquals(200, propertiesResponse.getStatusCode());
        assertEquals(2, propertiesResponse.getValue().getApproximateMessagesCount());
    }

    @ParameterizedTest
    @MethodSource("invalidArgsSupplier")
    public void deleteMessageInvalidArgs(boolean messageId, boolean popReceipt, int statusCode, QueueErrorCode errMsg) {
        queueClient.create();
        String expectMsg = "test message";
        queueClient.sendMessage(expectMsg);
        QueueMessageItem messageItem = queueClient.receiveMessage();
        String deleteMessageId = messageId ? messageItem.getMessageId() : messageItem.getMessageId() + "Random";
        String deletePopReceipt = popReceipt ? messageItem.getPopReceipt() : messageItem.getPopReceipt() + "Random";

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.deleteMessage(deleteMessageId, deletePopReceipt));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, statusCode, errMsg);
    }

    @Test
    public void updateMessage() {
        String updateMsg = "Updated test message";
        queueClient.create();
        queueClient.sendMessage("test message before update");

        QueueMessageItem dequeueMsg = queueClient.receiveMessage();

        assertEquals(204, queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(),
            updateMsg, Duration.ofSeconds(1), null, null).getStatusCode());

        sleepIfRunningAgainstService(2000);

        assertEquals(updateMsg, queueClient.peekMessage().getMessageText());
    }

    @Test
    public void updateMessageNoBody() {
        String messageText = "test message before update";
        queueClient.create();
        queueClient.sendMessage(messageText);

        QueueMessageItem dequeueMsg = queueClient.receiveMessage();

        assertEquals(204, queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(),
            null, Duration.ofSeconds(1), null, null).getStatusCode());

        sleepIfRunningAgainstService(2000);

        assertEquals(messageText, queueClient.peekMessage().getMessageText());
    }

    @Test
    public void updateMessageNullDuration() {
        String messageText = "test message before update";
        queueClient.create();
        queueClient.sendMessage(messageText);

        QueueMessageItem dequeueMsg = queueClient.receiveMessage();

        assertEquals(204, queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(),
            null, null, null, null).getStatusCode());

        sleepIfRunningAgainstService(2000);

        assertEquals(messageText, queueClient.peekMessage().getMessageText());
    }

    @ParameterizedTest
    @MethodSource("invalidArgsSupplier")
    public void updateMessageInvalidArgs(boolean messageId, boolean popReceipt, int statusCode, QueueErrorCode errMsg) {
        queueClient.create();
        String updateMsg = "Updated test message";
        queueClient.sendMessage("test message before update");
        QueueMessageItem messageItem = queueClient.receiveMessage();

        String updateMessageId = messageId ? messageItem.getMessageId() : messageItem.getMessageId() + "Random";
        String updatePopReceipt = popReceipt ? messageItem.getPopReceipt() : messageItem.getPopReceipt() + "Random";

        QueueStorageException exception = assertThrows(QueueStorageException.class,
            () -> queueClient.updateMessage(updateMessageId, updatePopReceipt, updateMsg, Duration.ofSeconds(1)));
        QueueTestHelper.assertExceptionStatusCodeAndMessage(exception, statusCode, errMsg);
    }

    public static Stream<Arguments> invalidArgsSupplier() {
        return Stream.of(
            Arguments.of(true, false, 400, QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE),
            Arguments.of(false, true, 404, QueueErrorCode.MESSAGE_NOT_FOUND),
            Arguments.of(false, false, 400, QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE)
        );
    }

    @Test
    public void updateMessageWithBase64Client() {
        String updateMsg = "Updated test message";
        QueueClient encodingQueueClient = getBase64Client();
        encodingQueueClient.create();
        encodingQueueClient.sendMessage("test message before update");

        QueueMessageItem dequeueMsg = encodingQueueClient.receiveMessage();

        assertEquals(204, encodingQueueClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), updateMsg, Duration.ofSeconds(1), null, null).getStatusCode());

        sleepIfRunningAgainstService(2000);

        assertEquals(updateMsg, encodingQueueClient.peekMessage().getMessageText());
    }

    @Test
    public void getQueueName() {
        assertEquals(queueName, queueClient.getQueueName());
    }

    @Test
    public void builderBearerTokenValidation() throws MalformedURLException {
        URL url = new URL(queueClient.getQueueUrl());
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString();

        assertThrows(IllegalArgumentException.class, () -> new QueueClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        QueueClient queueClient = queueBuilderHelper().addPolicy(getPerCallVersionPolicy()).buildClient();
        queueClient.create();

        assertEquals("2017-11-09", queueClient.getPropertiesWithResponse(null, null).getHeaders()
            .getValue("x-ms-version"));
    }

    private QueueClient getBase64Client() {
        return queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient()
            .getQueueClient(queueName);
    }

    @Test
    public void defaultAudience() {
        queueClient.createIfNotExists();
        QueueClient aadQueue = getOAuthQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl())
            .audience(null) // should default to "https://storage.azure.com/"
            .queueName(queueClient.getQueueName())
            .buildClient();

        assertNotNull(aadQueue.getProperties());
    }

    @Test
    public void storageAccountAudience() {
        queueClient.createIfNotExists();
        QueueClient aadQueue = getOAuthQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl())
            .audience(QueueAudience.createQueueServiceAccountAudience(queueClient.getAccountName()))
            .queueName(queueClient.getQueueName())
            .buildClient();

        assertNotNull(aadQueue.getProperties());
    }
    @RequiredServiceVersion(clazz = QueueServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        queueClient.createIfNotExists();
        QueueClient aadQueue = getOAuthQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl())
            .queueName(queueClient.getQueueName())
            .audience(QueueAudience.createQueueServiceAccountAudience("badaudience"))
            .buildClient();

        assertNotNull(aadQueue.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.queue.core.windows.net/", queueClient.getAccountName());
        QueueAudience audience = QueueAudience.fromString(url);

        queueClient.createIfNotExists();
        QueueClient aadQueue = getOAuthQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl())
            .audience(audience)
            .queueName(queueClient.getQueueName())
            .buildClient();

        assertNotNull(aadQueue.getProperties());
    }
}
