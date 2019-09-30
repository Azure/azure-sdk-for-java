// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue


import com.azure.core.util.Context
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.queue.QueueClient
import com.azure.storage.queue.models.AccessPolicy
import com.azure.storage.queue.models.SignedIdentifier
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageException
import spock.lang.Unroll

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
        queueName = testResourceName.randomName(methodName, 60)
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
        queueClient = primaryQueueServiceClient.getQueueClient(queueName)
    }

    def "Get queue URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.queue.core.windows.net/%s", accoutName, queueName)

        when:
        def queueURL = queueClient.getQueueUrl().toString()

        then:
        expectURL.equals(queueURL)
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
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        queueClient.createWithResponse(testMetadata, null, null)
        when:
        def getPropertiesResponse = queueClient.getPropertiesWithResponse(null, null)
        then:
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.getValue().getApproximateMessagesCount() == 0
        testMetadata.equals(getPropertiesResponse.getValue().getMetadata())
    }

    def "Get properties error"() {
        when:
        queueClient.getProperties()
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
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
        expectMetadataInCreate.equals(getPropertiesResponseBefore.getValue().getMetadata())
        QueueTestHelper.assertResponseStatusCode(setMetadataResponse, 204)
        QueueTestHelper.assertResponseStatusCode(getPropertiesResponseAfter, 200)
        expectMetadataInSet.equals(getPropertiesResponseAfter.getValue().getMetadata())
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
        def e = thrown(StorageException)
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
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)
        where:
        invalidKey     | statusCode | errMessage
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
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Set access policy"() {
        given:
        queueClient.create()
        def accessPolicy = new AccessPolicy()
            .setPermission("raup")
            .setStart(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new SignedIdentifier()
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
        def accessPolicy = new AccessPolicy()
            .setPermission("r")
            .setStart(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permission = new SignedIdentifier()
            .setId("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .setAccessPolicy(accessPolicy)
        queueClient.create()
        when:
        queueClient.setAccessPolicy(Collections.singletonList(permission))
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Set multiple access policies"() {
        given:
        def accessPolicy = new AccessPolicy()
            .setPermission("r")
            .setStart(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<>()
        for (int i = 0; i < 3; i++) {
            permissions.add(new SignedIdentifier()
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
        def accessPolicy = new AccessPolicy()
            .setPermission("r")
            .setStart(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permissions = new ArrayList<>()
        for (int i = 0; i < 6; i++) {
            permissions.add(new SignedIdentifier()
                .setId("policy" + i)
                .setAccessPolicy(accessPolicy))
        }
        queueClient.create()
        when:
        queueClient.setAccessPolicyWithResponse(permissions, null, Context.NONE)
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_XML_DOCUMENT)
    }

    def "Enqueue message"() {
        given:
        queueClient.create()
        def expectMsg = "test message"
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse(expectMsg, null, null, null, null)
        def peekMsgIter = queueClient.peekMessages().iterator()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        expectMsg.equals(peekMsgIter.next().getMessageText())
        !peekMsgIter.hasNext()
    }

    def "Enqueue empty message"() {
        given:
        queueClient.create()
        def expectMsg = ""
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse(expectMsg, null, null, null, null)
        def peekMsgIter = queueClient.peekMessages().iterator()
        then:
        QueueTestHelper.assertResponseStatusCode(enqueueMsgResponse, 201)
        peekMsgIter.next().getMessageText() == null
        !peekMsgIter.hasNext()
    }

    def "Enqueue time to live"() {
        given:
        queueClient.create()
        when:
        def enqueueMsgResponse = queueClient.enqueueMessageWithResponse("test message",
            Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(5), null)
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
        expectMsg.equals(dequeueMsgResponse.getMessageText())
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
        expectMsg1.equals(dequeueMsgIter.next().getMessageText())
        expectMsg2.equals(dequeueMsgIter.next().getMessageText())
    }

    def "Dequeue too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.dequeueMessages(33).iterator().next()
        then:
        def e = thrown(StorageException)
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
        expectMsg.equals(peekMsgIter.getMessageText())
    }

    def "Peek multiple messages"() {
        given:
        queueClient.create()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueClient.enqueueMessage(expectMsg1)
        queueClient.enqueueMessage(expectMsg2)
        when:
        def peekMsgIter = queueClient.peekMessages(2, Duration.ofSeconds(1), null).iterator()
        then:
        expectMsg1.equals(peekMsgIter.next().getMessageText())
        expectMsg2.equals(peekMsgIter.next().getMessageText())
        !peekMsgIter.hasNext()
    }

    def "Peek too many message"() {
        given:
        queueClient.create()
        when:
        queueClient.peekMessages(33, null, null).iterator().next()
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
    }

    def "Peek messages error"() {
        when:
        queueClient.peekMessages().iterator().next()
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Clear messages"() {
        given:
        queueClient.create()
        queueClient.enqueueMessage("test message 1")
        queueClient.enqueueMessage("test message 2")
        queueClient.enqueueMessage("test message 3")
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
        def e = thrown(StorageException)
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
        queueClient.enqueueMessage(expectMsg)
        def dequeueMessageIter = queueClient.dequeueMessages().iterator().next()
        when:
        def deleteMessageId = messageId ? dequeueMessageIter.getMessageId() : dequeueMessageIter.getMessageId() + "Random"
        def deletePopReceipt = popReceipt ? dequeueMessageIter.getPopReceipt() : dequeueMessageIter.getPopReceipt() + "Random"
        queueClient.deleteMessage(deleteMessageId, deletePopReceipt)
        then:
        def e = thrown(StorageException)
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
            dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(), Duration.ofSeconds(1), null,  null)
        QueueTestHelper.sleepInRecord(Duration.ofSeconds(2))
        def peekMsgIter = queueClient.peekMessages().iterator().next()
        then:
        QueueTestHelper.assertResponseStatusCode(updateMsgResponse, 204)
        updateMsg.equals(peekMsgIter.getMessageText())
    }

    @Unroll
    def "Update message invalid args"() {
        given:
        queueClient.create()
        def updateMsg = "Updated test message"
        queueClient.enqueueMessage("test message before update")
        def dequeueMessageIter = queueClient.dequeueMessages().iterator().next()
        when:
        def updateMessageId = messageId ? dequeueMessageIter.getMessageId() : dequeueMessageIter.getMessageId() + "Random"
        def updatePopReceipt = popReceipt ? dequeueMessageIter.getPopReceipt() : dequeueMessageIter.getPopReceipt() + "Random"
        queueClient.updateMessage(updateMsg, updateMessageId, updatePopReceipt, Duration.ofSeconds(1))
        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | StorageErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Get Queue Name"() {
        expect:
        queueName == queueClient.getQueueName()
    }
}
