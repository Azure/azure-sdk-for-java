// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

import com.azure.storage.queue.QueueClient
import com.azure.storage.queue.models.AccessPolicy
import com.azure.storage.queue.models.SignedIdentifier
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageErrorException
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class QueueAPITests extends APISpec {
    QueueClient queueClient

    static def testMetadata = Collections.singletonMap("metadata", "value")
    static def createMetadata = Collections.singletonMap("metadata1", "value")

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
        queueClient = primaryQueueServiceClient.getQueueClient(testResourceName.randomName(methodName, 60))
    }

    def "Create queue with shared key"() {
        expect:
        QueueTestHelper.assertResponseStatusCode(queueClient.createWithResponse(null, null), 201)
    }

    // TODO: Will implement the test after introduce the sas token generator
    @Ignore
    def "Create queue with sas token"() {

    }

    def "Delete exist queue"() {
        given:
        queueClient.create()
        when:
        def deleteQueueResponse = queueClient.deleteWithResponse(null)
        then:
        QueueTestHelper.assertResponseStatusCode(deleteQueueResponse, 204)

    }

    def "Delete queue error"() {
        when:
        queueClient.delete()
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        queueClient.createWithResponse(testMetadata, null)
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.value().approximateMessagesCount() == 0
        testMetadata.equals(getPropertiesResponse.value().metadata())
    }

    def "Get properties error"() {
        when:
        queueClient.getProperties()
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    @Unroll
    def "Set and clear metadata"() {
        given:
        queueClient.createWithResponse(matadataInCreate, null)
        when:
        def getPropertiesResponseBefore = queueClient.getPropertiesWithResponse(null)
        def setMetadataResponse = queueClient.setMetadataWithResponse(metadataInSet, null)
        def getPropertiesResponseAfter = queueClient.getPropertiesWithResponse(null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponseBefore, 200)
        expectMetadataInCreate.equals(getPropertiesResponseBefore.value().metadata())
        QueueTestHelper.assertResponseStatusCode(setMetadataResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponseAfter, 200)
        expectMetadataInSet.equals(getPropertiesResponseAfter.value().metadata)
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
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    @Unroll
    def "Set invalid meta"() {
        given:
        def invalidMetadata = Collections.singletonMap(invalidKey, "value")
        queueClient.create()
        when:
        queueClient.setMetadata(invalidMetadata)
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)
        where:
        invalidKey     | statusCode | errMessage
        "invalidMeta"  | 403        | StorageErrorCode.fromString("AuthenticationError")
        "invalid-meta" | 400        | StorageErrorCode.INVALID_METADATA
        "12345"        | 400        | StorageErrorCode.INVALID_METADATA
        ""             | 400        | StorageErrorCode.EMPTY_METADATA_KEY
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
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Set access policy"() {
        given:
        queueClient.create()
        def accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new SignedIdentifier()
            .id("testpermission")
            .accessPolicy(accessPolicy)
        when:
        def setAccessPolicyResponse = queueClient.setAccessPolicyWithResponse(Collections.singletonList(permission), null)
        def nextAccessPolicy = queueClient.getAccessPolicy().iterator().next()
        then:
        QueueTestHelper.assertResponseStatusCode(setAccessPolicyResponse, 204)
        QueueTestHelper.assertPermissionsAreEqual(permission, nextAccessPolicy)
    }

    def "Set invalid access policy"() {
        given:
        def accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permission = new SignedIdentifier()
            .id("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .accessPolicy(accessPolicy)
        queueClient.create()
        when:
        queueClient.setAccessPolicy(Collections.singletonList(permission))
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Set multiple access policies"() {
        given:
        def accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<>()
        for (int i = 0; i < 3; i++) {
            permissions.add(new SignedIdentifier()
                .id("policy" + i)
                .accessPolicy(accessPolicy))
        }
        queueClient.create()
        when:
        def setAccessPolicyResponse = queueClient.setAccessPolicyWithResponse(permissions, null)
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
        def accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<>()
        for (int i = 0; i < 6; i++) {
            permissions.add(new SignedIdentifier()
                .id("policy" + i)
                .accessPolicy(accessPolicy))
        }
        queueClient.create()
        when:
        queueClient.setAccessPolicyWithResponse(permissions, null)
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Enqueue message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse(expectMsg, null, null, null)
        def peekMsgIter = queueClient.peekMessages().iterator()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        expectMsg.equals(peekMsgIter.next().messageText())
        !peekMsgIter.hasNext()
    }

    def "Enqueue empty message"() {
        given:
        queueClient.create()
        def expectMsg = ""
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse(expectMsg, null, null, null)
        def peekMsgIter = queueClient.peekMessages().iterator()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        peekMsgIter.next().messageText() == null
        !peekMsgIter.hasNext()
    }

    def "Enqueue time to live"() {
        given:
        queueClient.create()
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse("test message",
            Duration.ofSeconds(0), Duration.ofSeconds(2), null)
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
    }

    def "Dequeue message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.enqueueMessage(expectMsg)
        when:
        def dequeueMsgResponse = queueClient.dequeueMessages().iterator().next()
        then:
        expectMsg.equals(dequeueMsgResponse.messageText())
    }

    def "Dequeue multiple messages"() {
        given:
        queueClient.create()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueClient.enqueueMessage(expectMsg1)
        queueClient.enqueueMessage(expectMsg2)
        when:
        def dequeueMsgIter = queueClient.dequeueMessages(2).iterator()
        then:
        expectMsg1.equals(dequeueMsgIter.next().messageText())
        expectMsg2.equals(dequeueMsgIter.next().messageText())
    }

    def "Dequeue too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.dequeueMessages(33).iterator().next()
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
    }

    def "Peek message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.enqueueMessage(expectMsg)
        when:
        def peekMsgIter = queueClient.peekMessages().iterator().next()
        then:
        expectMsg.equals(peekMsgIter.messageText())
    }

    def "Peek multiple messages"() {
        given:
        queueClient.create()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueClient.enqueueMessage(expectMsg1)
        queueClient.enqueueMessage(expectMsg2)
        when:
        def peekMsgIter = queueClient.peekMessages(2).iterator()
        then:
        expectMsg1.equals(peekMsgIter.next().messageText())
        expectMsg2.equals(peekMsgIter.next().messageText())
        !peekMsgIter.hasNext()
    }

    def "Peek too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.peekMessages(33).iterator().next()
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
    }

    def "Peek messages error"() {
        when:
        queueClient.peekMessages().iterator().next()
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Clear messages"() {
        given:
        queueClient.create()
        queueClient.enqueueMessage("test message 1")
        queueClient.enqueueMessage("test message 2")
        queueClient.enqueueMessage("test message 3")
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null)
        def clearMsgResponse = queueClient.clearMessagesWithResponse(null)
        def getPropertiesAfterResponse = queueClient.getPropertiesWithResponse(null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.value().approximateMessagesCount() == 3
        QueueTestHelper.assertResponseStatusCode(clearMsgResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        getPropertiesAfterResponse.value().approximateMessagesCount() == 0
    }

    def "Clear messages error"() {
        when:
        StepVerifier.create(queueClient.clearMessagesWithResponse(null))
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Delete message"() {
        given:
        queueClient.create()
        queueClient.enqueueMessage("test message 1")
        queueClient.enqueueMessage("test message 2")
        queueClient.enqueueMessage("test message 3")
        def dequeueMsg = queueClient.dequeueMessages().iterator().next()
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null)
        def deleteMsgResponse = queueClient.deleteMessageWithResponse(dequeueMsg.messageId(), dequeueMsg.popReceipt(), null)
        def getPropertiesAfterResponse = queueClient.getPropertiesWithResponse(null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.value().approximateMessagesCount() == 3
        QueueTestHelper.assertResponseStatusCode(deleteMsgResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesAfterResponse, 200)
        getPropertiesAfterResponse.value().approximateMessagesCount() == 2
    }

    @Unroll
    def "Delete message invalid args"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        queueClient.enqueueMessage(expectMsg)
        def dequeueMessageIter = queueClient.dequeueMessages().iterator().next()
        when:
        def deleteMessageId = messageId ? dequeueMessageIter.messageId() : dequeueMessageIter.messageId() + "Random"
        def deletePopReceipt = popReceipt ? dequeueMessageIter.popReceipt() : dequeueMessageIter.popReceipt() + "Random"
        queueClient.deleteMessage(deleteMessageId, deletePopReceipt)
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | StorageErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Update message"() {
        given:
        def updateMsg = "Updated test message"
        queueClient.create()
        queueClient.enqueueMessage("test message before update")

        def dequeueMsg = queueClient.dequeueMessages().iterator().next()
        when:
        def updateMsgResponse = queueClient.updateMessageWithResponse(updateMsg,
            dequeueMsg.messageId(), dequeueMsg.popReceipt(), Duration.ofSeconds(1), null)
        QueueTestHelper.sleepInRecord(Duration.ofSeconds(2))
        def peekMsgIter = queueClient.peekMessages().iterator().next()
        then:
        QueueTestHelper.assertResponseStatusCode(updateMsgResponse, 204)
        updateMsg.equals(peekMsgIter.messageText())
    }

    @Unroll
    def "Update message invalid args"() {
        given:
        queueClient.create()
        def updateMsg = "Updated test message"
        queueClient.enqueueMessage("test message before update")
        def dequeueMessageIter = queueClient.dequeueMessages().iterator().next()
        when:
        def updateMessageId = messageId ? dequeueMessageIter.messageId() : dequeueMessageIter.messageId() + "Random"
        def updatePopReceipt = popReceipt ? dequeueMessageIter.popReceipt() : dequeueMessageIter.popReceipt() + "Random"
        queueClient.updateMessage(updateMsg, updateMessageId, updatePopReceipt, Duration.ofSeconds(1))
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | StorageErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

}
