// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.util.BinaryData
import com.azure.core.util.Context
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.queue.models.PeekedMessageItem
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueErrorCode
import com.azure.storage.queue.models.QueueMessageItem
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException
import reactor.core.publisher.Mono
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class QueueAPITests extends APISpec {
    QueueClient queueClient

    static def testMetadata = Collections.singletonMap("metadata", "value")
    static def createMetadata = Collections.singletonMap("metadata1", "value")
    String queueName

    def setup() {
        queueName = namer.getRandomName(60)
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient()
        queueClient = primaryQueueServiceClient.getQueueClient(queueName)
    }

    def "Get queue URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.queue.core.windows.net/%s", accountName, queueName)

        when:
        def queueURL = queueClient.getQueueUrl()

        then:
        expectURL == queueURL
    }

    def "IP based endpoint"() {
        when:
        def queueClient = new QueueClientBuilder()
            .connectionString(env.primaryAccount.connectionString)
            .endpoint("http://127.0.0.1:10001/devstoreaccount1/myqueue")
            .buildClient()

        then:
        queueClient.getAccountName() == "devstoreaccount1"
        queueClient.getQueueName() == "myqueue"
    }

    def "Create queue with shared key"() {
        expect:
        QueueTestHelper.assertResponseStatusCode(queueClient.createWithResponse(null, null, null), 201)
    }

    def "Delete exist queue"() {
        given:
        queueClient.create()
        when:
        def deleteQueueResponse = queueClient.deleteWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(deleteQueueResponse, 204)

    }

    def "Delete queue error"() {
        when:
        queueClient.delete()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        queueClient.createWithResponse(testMetadata, null, null)
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.getValue().getApproximateMessagesCount() == 0
        testMetadata == getPropertiesResponse.getValue().getMetadata()
    }

    def "Get properties error"() {
        when:
        queueClient.getProperties()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    @Unroll
    def "Set and clear metadata"() {
        given:
        queueClient.createWithResponse(matadataInCreate, null, null)
        when:
        def getPropertiesResponseBefore = queueClient.getPropertiesWithResponse(null, null)
        def setMetadataResponse = queueClient.setMetadataWithResponse(metadataInSet, null, null)
        def getPropertiesResponseAfter = queueClient.getPropertiesWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponseBefore, 200)
        expectMetadataInCreate == getPropertiesResponseBefore.getValue().getMetadata()
        QueueTestHelper.assertResponseStatusCode(setMetadataResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponseAfter, 200)
        expectMetadataInSet == getPropertiesResponseAfter.getValue().getMetadata()
        where:
        matadataInCreate | metadataInSet | expectMetadataInCreate | expectMetadataInSet
        null             | testMetadata  | Collections.emptyMap() | testMetadata
        createMetadata   | testMetadata  | createMetadata         | testMetadata
        createMetadata   | null          | createMetadata         | Collections.emptyMap()
        testMetadata     | testMetadata  | testMetadata           | testMetadata
        null             | null          | Collections.emptyMap() | Collections.emptyMap()
    }

    def "Set metadata queue error"() {
        when:
        queueClient.setMetadata(testMetadata)
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    @Unroll
    def "Set invalid meta"() {
        given:
        def invalidMetadata = Collections.singletonMap(invalidKey, "value")
        queueClient.create()
        when:
        queueClient.setMetadata(invalidMetadata)
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)
        where:
        invalidKey     | statusCode | errMessage
        "invalid-meta" | 400        | QueueErrorCode.INVALID_METADATA
        "12345"        | 400        | QueueErrorCode.INVALID_METADATA
        ""             | 400        | QueueErrorCode.EMPTY_METADATA_KEY
    }

    def "Get access policy"() {
        given:
        queueClient.create()
        when:
        def accessPolicies = queueClient.getAccessPolicy()
        then:
        !accessPolicies.iterator().hasNext()
    }

    def "Get access policy error"() {
        when:
        queueClient.getAccessPolicy().iterator().next()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    def "Set access policy"() {
        given:
        queueClient.create()
        def accessPolicy = new QueueAccessPolicy()
            .setPermissions("raup")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new QueueSignedIdentifier()
            .setId("testpermission")
            .setAccessPolicy(accessPolicy)
        when:
        def setAccessPolicyResponse = queueClient.setAccessPolicyWithResponse(Collections.singletonList(permission), null, null)
        def nextAccessPolicy = queueClient.getAccessPolicy().iterator().next()
        then:
        QueueTestHelper.assertResponseStatusCode(setAccessPolicyResponse, 204)
        QueueTestHelper.assertPermissionsAreEqual(permission, nextAccessPolicy)
    }

    def "Set invalid access policy"() {
        given:
        def accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permission = new QueueSignedIdentifier()
            .setId("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .setAccessPolicy(accessPolicy)
        queueClient.create()
        when:
        queueClient.setAccessPolicy(Collections.singletonList(permission))
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, QueueErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Set multiple access policies"() {
        given:
        def accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<QueueSignedIdentifier>()
        for (int i = 0; i < 3; i++) {
            permissions.add(new QueueSignedIdentifier()
                .setId("policy" + i)
                .setAccessPolicy(accessPolicy))
        }
        queueClient.create()
        when:
        def setAccessPolicyResponse = queueClient.setAccessPolicyWithResponse(permissions, null, Context.NONE)
        def nextAccessPolicy = queueClient.getAccessPolicy().iterator()
        then:
        QueueTestHelper.assertResponseStatusCode(setAccessPolicyResponse, 204)
        QueueTestHelper.assertPermissionsAreEqual(permissions[0], nextAccessPolicy.next())
        QueueTestHelper.assertPermissionsAreEqual(permissions[1], nextAccessPolicy.next())
        QueueTestHelper.assertPermissionsAreEqual(permissions[2], nextAccessPolicy.next())
        !nextAccessPolicy.hasNext()
    }

    def "Set too many access policies"() {
        given:
        def accessPolicy = new QueueAccessPolicy()
            .setPermissions("r")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<QueueSignedIdentifier>()
        for (int i = 0; i < 6; i++) {
            permissions.add(new QueueSignedIdentifier()
                .setId("policy" + i)
                .setAccessPolicy(accessPolicy))
        }
        queueClient.create()
        when:
        queueClient.setAccessPolicyWithResponse(permissions, null, Context.NONE)
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, QueueErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Enqueue message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        when:
        def enqueueMsgResponse = queueClient.sendMessageWithResponse(expectMsg, null, null, null, null)
        def peekedMessage = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        expectMsg == peekedMessage.getMessageText()
    }

    def "Enqueue message binary data"() {
        given:
        queueClient.create()
        def expectMsg = BinaryData.fromString("test message")
        when:
        def enqueueMsgResponse = queueClient.sendMessageWithResponse(expectMsg, null, null, null, null)
        def peekedMessage = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        expectMsg.toBytes() == peekedMessage.getBody().toBytes()
    }

    def "Enqueue empty message"() {
        given:
        queueClient.create()
        def expectMsg = ""
        when:
        def enqueueMsgResponse = queueClient.sendMessageWithResponse(expectMsg, null, null, null, null)
        def peekedMessage = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        peekedMessage.getMessageText() == null
    }

    def "Enqueue time to live"() {
        given:
        queueClient.create()
        when:
        def enqueueMsgResponse = queueClient.sendMessageWithResponse("test message",
            Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(5), null)
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
    }

    def "Enqueue message encoded message"() {
        given:
        queueClient.create()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        def expectMsg = BinaryData.fromString("test message")
        when:
        def enqueueMsgResponse = encodingQueueClient.sendMessageWithResponse(expectMsg, null, null, null, null)
        def peekedMessage = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        Base64.getEncoder().encodeToString(expectMsg.toBytes()) == peekedMessage.getBody().toString()
    }

    def "Dequeue message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.sendMessage(expectMsg)
        when:
        def messageItem = queueClient.receiveMessage()
        then:
        expectMsg == messageItem.getMessageText()
    }

    def "Dequeue encoded message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        when:
        def messageItem = encodingQueueClient.receiveMessage()
        then:
        expectMsg == messageItem.getBody().toString()
    }

    def "Dequeue fails without handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.sendMessage(expectMsg)
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        when:
        encodingQueueClient.receiveMessage()
        then:
        thrown(IllegalArgumentException.class)
    }

    def "Dequeue with handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        queueClient.sendMessage(expectMsg)
        QueueMessageItem badMessage = null
        String queueUrl = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ failure ->
                badMessage = failure.getQueueMessageItem()
                queueUrl = failure.getQueueAsyncClient().getQueueUrl()
                return Mono.empty()
            })
            .buildClient().getQueueClient(queueName)
        when:
        def messageItems = encodingQueueClient.receiveMessages(10).toList()
        then:
        messageItems.size() == 1
        messageItems[0].getBody().toString() == expectMsg
        badMessage != null
        badMessage.getBody().toString() == expectMsg
        queueUrl == queueClient.getQueueUrl()
    }

    def "Dequeue and delete with handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        queueClient.sendMessage(expectMsg)
        QueueMessageItem badMessage = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ failure ->
                badMessage = failure.getQueueMessageItem()
                return failure.getQueueAsyncClient().deleteMessage(badMessage.getMessageId(), badMessage.getPopReceipt())
            })
            .buildClient().getQueueClient(queueName)
        when:
        def messageItems = encodingQueueClient.receiveMessages(10).toList()
        then:
        messageItems.size() == 1
        messageItems[0].getBody().toString() == expectMsg
        badMessage != null
        badMessage.getBody().toString() == expectMsg
    }

    def "Dequeue and delete with sync handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        queueClient.sendMessage(expectMsg)
        QueueMessageItem badMessage = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError({ failure ->
                badMessage = failure.getQueueMessageItem()
                failure.getQueueClient().deleteMessage(badMessage.getMessageId(), badMessage.getPopReceipt())
            })
            .buildClient().getQueueClient(queueName)
        when:
        def messageItems = encodingQueueClient.receiveMessages(10).toList()
        then:
        messageItems.size() == 1
        messageItems[0].getBody().toString() == expectMsg
        badMessage != null
        badMessage.getBody().toString() == expectMsg
    }

    def "Dequeue with handler error"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        queueClient.sendMessage(expectMsg)
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ message ->
                throw new IllegalStateException("KABOOM")
            })
            .buildClient().getQueueClient(queueName)
        when:
        encodingQueueClient.receiveMessages(10).toList()
        then:
        thrown(IllegalStateException.class)
    }

    def "Dequeue multiple messages"() {
        given:
        queueClient.create()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueClient.sendMessage(expectMsg1)
        queueClient.sendMessage(expectMsg2)
        when:
        def dequeueMsgIter = queueClient.receiveMessages(2).iterator()
        then:
        expectMsg1 == dequeueMsgIter.next().getMessageText()
        expectMsg2 == dequeueMsgIter.next().getMessageText()
    }

    def "Dequeue too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.receiveMessages(33).iterator().next()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
    }

    def "Enqueue Dequeue non-UTF message"() {
        given:
        queueClient.create()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        byte[] content = [ 0xFF, 0x00 ]; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content))

        when:
        def messageItem = encodingQueueClient.receiveMessage()
        then:
        content == messageItem.getBody().toBytes()
    }

    def "Enqueue Peek non-UTF message"() {
        given:
        queueClient.create()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        byte[] content = [ 0xFF, 0x00 ]; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content))

        when:
        def messageItem = encodingQueueClient.peekMessage()
        then:
        content == messageItem.getBody().toBytes()
    }

    def "Peek message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.sendMessage(expectMsg)
        when:
        def peekedMessage = queueClient.peekMessage()
        then:
        expectMsg == peekedMessage.getMessageText()
    }

    def "Peek encoded message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(encodedMsg)
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        when:
        def peekedMessage = encodingQueueClient.peekMessage()
        then:
        expectMsg == peekedMessage.getBody().toString()
    }

    def "Peek fails without handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.sendMessage(expectMsg)
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildClient().getQueueClient(queueName)
        when:
        encodingQueueClient.peekMessage()
        then:
        thrown(IllegalArgumentException.class)
    }

    def "Peek with handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(expectMsg)
        queueClient.sendMessage(encodedMsg)
        PeekedMessageItem badMessage = null
        String queueUrl = null
        Exception cause = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ failure ->
                badMessage = failure.getPeekedMessageItem()
                queueUrl = failure.getQueueAsyncClient().getQueueUrl()
                cause = failure.getCause()
                return Mono.empty()
            })
            .buildClient().getQueueClient(queueName)
        when:
        def peekedMessages = encodingQueueClient.peekMessages(10, null, null).toList()
        then:
        peekedMessages.size() == 1
        peekedMessages[0].getBody().toString() == expectMsg
        badMessage != null
        badMessage.getBody().toString() == expectMsg
        queueUrl == queueClient.getQueueUrl()
        cause != null
    }

    def "Peek with sync handler"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(expectMsg)
        queueClient.sendMessage(encodedMsg)
        PeekedMessageItem badMessage = null
        Exception cause = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError({ failure ->
                badMessage = failure.getPeekedMessageItem()
                cause = failure.getCause()
                // call some sync API here
                failure.getQueueClient().getProperties()
            })
            .buildClient().getQueueClient(queueName)
        when:
        def peekedMessages = encodingQueueClient.peekMessages(10, null, null).toList()
        then:
        peekedMessages.size() == 1
        peekedMessages[0].getBody().toString() == expectMsg
        badMessage != null
        badMessage.getBody().toString() == expectMsg
        cause != null
    }

    def "Peek with handler exception"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueClient.sendMessage(expectMsg)
        queueClient.sendMessage(encodedMsg)
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ message ->
                throw new IllegalStateException("KABOOM")
            })
            .buildClient().getQueueClient(queueName)
        when:
        encodingQueueClient.peekMessages(10, null, null).toList()
        then:
        thrown(IllegalStateException.class)
    }

    def "Peek multiple messages"() {
        given:
        queueClient.create()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueClient.sendMessage(expectMsg1)
        queueClient.sendMessage(expectMsg2)
        when:
        def peekMsgIter = queueClient.peekMessages(2, Duration.ofSeconds(10), null).iterator()
        then:
        expectMsg1 == peekMsgIter.next().getMessageText()
        expectMsg2 == peekMsgIter.next().getMessageText()
        !peekMsgIter.hasNext()
    }

    def "Peek too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.peekMessages(33, null, null).iterator().next()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
    }

    def "Peek messages error"() {
        when:
        queueClient.peekMessage()
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    def "Clear messages"() {
        given:
        queueClient.create()
        queueClient.sendMessage("test message 1")
        queueClient.sendMessage("test message 2")
        queueClient.sendMessage("test message 3")
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null, null)
        def clearMsgResponse = queueClient.clearMessagesWithResponse(null, null)
        def getPropertiesAfterResponse = queueClient.getPropertiesWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.getValue().getApproximateMessagesCount() == 3
        QueueTestHelper.assertResponseStatusCode(clearMsgResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        getPropertiesAfterResponse.getValue().getApproximateMessagesCount() == 0
    }

    def "Clear messages error"() {
        when:
        queueClient.clearMessagesWithResponse(null, null)
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    def "Delete message"() {
        given:
        queueClient.create()
        queueClient.sendMessage("test message 1")
        queueClient.sendMessage("test message 2")
        queueClient.sendMessage("test message 3")
        def dequeueMsg = queueClient.receiveMessage()
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null, null)
        def deleteMsgResponse = queueClient.deleteMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(),
            null, null)
        def getPropertiesAfterResponse = queueClient.getPropertiesWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.getValue().getApproximateMessagesCount() == 3
        QueueTestHelper.assertResponseStatusCode(deleteMsgResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        getPropertiesAfterResponse.getValue().getApproximateMessagesCount() == 2
    }

    @Unroll
    def "Delete message invalid args"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.sendMessage(expectMsg)
        QueueMessageItem queueMessageItem = queueClient.receiveMessage()
        when:
        def deleteMessageId = messageId ? queueMessageItem.getMessageId() : queueMessageItem.getMessageId() + "Random"
        def deletePopReceipt = popReceipt ? queueMessageItem.getPopReceipt() : queueMessageItem.getPopReceipt() + "Random"
        queueClient.deleteMessage(deleteMessageId, deletePopReceipt)
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | QueueErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Update message"() {
        given:
        def updateMsg = "Updated test message"
        queueClient.create()
        queueClient.sendMessage("test message before update")

        def dequeueMsg = queueClient.receiveMessage()
        when:
        def updateMsgResponse = queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), updateMsg, Duration.ofSeconds(1), null,  null)
        sleepIfLive(2000)
        def peekMsgIter = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(updateMsgResponse, 204)
        updateMsg == peekMsgIter.getMessageText()
    }

    def "Update message no body"() {
        given:
        def messageText = "test message before update"
        queueClient.create()
        queueClient.sendMessage(messageText)

        def dequeueMsg = queueClient.receiveMessage()
        when:
        def updateMsgResponse = queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), null, Duration.ofSeconds(1), null,  null)
        sleepIfLive(2000)
        def peekMsgIter = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(updateMsgResponse, 204)
        messageText == peekMsgIter.getMessageText()
    }

    def "Update message null duration"() {
        given:
        def messageText = "test message before update"
        queueClient.create()
        queueClient.sendMessage(messageText)

        def dequeueMsg = queueClient.receiveMessage()
        when:
        def updateMsgResponse = queueClient.updateMessageWithResponse(dequeueMsg.getMessageId(),
            dequeueMsg.getPopReceipt(), null, null, null,  null)
        sleepIfLive(2000)
        def peekMsgIter = queueClient.peekMessage()
        then:
        QueueTestHelper.assertResponseStatusCode(updateMsgResponse, 204)
        messageText == peekMsgIter.getMessageText()
    }

    @Unroll
    def "Update message invalid args"() {
        given:
        queueClient.create()
        def updateMsg = "Updated test message"
        queueClient.sendMessage("test message before update")
        def dequeueMessageIter = queueClient.receiveMessage()
        when:
        def updateMessageId = messageId ? dequeueMessageIter.getMessageId() : dequeueMessageIter.getMessageId() + "Random"
        def updatePopReceipt = popReceipt ? dequeueMessageIter.getPopReceipt() : dequeueMessageIter.getPopReceipt() + "Random"
        queueClient.updateMessage(updateMessageId, updatePopReceipt, updateMsg, Duration.ofSeconds(1))
        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | QueueErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Get Queue Name"() {
        expect:
        queueName == queueClient.getQueueName()
    }

    def "Builder bearer token validation"() {
        setup:
        URL url = new URL(queueClient.getQueueUrl())
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString()
        def builder = new QueueClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        given:
        def queueClient = queueBuilderHelper()
            .addPolicy(getPerCallVersionPolicy()).buildClient()
        queueClient.create()

        when:
        def response = queueClient.getPropertiesWithResponse(null, null)

        then:
        notThrown(QueueStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }
}
