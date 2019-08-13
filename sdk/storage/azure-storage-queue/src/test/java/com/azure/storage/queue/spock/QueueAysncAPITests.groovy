// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

import com.azure.storage.queue.models.AccessPolicy
import com.azure.storage.queue.models.DequeuedMessage
import com.azure.storage.queue.models.SignedIdentifier
import org.junit.Assert
import org.junit.Ignore
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull

class QueueAysncAPITests extends APISpec {
    def queueAsyncClient

    static def testMetadata = Collections.singletonMap("metadata", "value")
    static def createMetadata = Collections.singletonMap("metadata1", "value")

    def setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper(interceptorManager).buildAsyncClient()
        queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(testResourceName.randomName("queue", 16))
    }

    def "Create queue with shared key from queue async client"() {
        when:
        def createQueueVerifier = StepVerifier.create(queueAsyncClient.create())

        then:
        createQueueVerifier.assertNext { QueueTestHelper.assertResponseStatusCode(it, 201) }
            .verifyComplete()
    }

    // TODO: Will implement the test after introduce the sas token generator
    @Ignore
    def "Create queue with sas token from queue async client"() {

    }

    def "Create async queue with same metadata from queue async client"() {
        when:
        def createQueueVerifier1 = StepVerifier.create(queueAsyncClient.create(testMetadata))
        def createQueueVerifier2 = StepVerifier.create(queueAsyncClient.create(testMetadata))
        then:
        createQueueVerifier1.assertNext { QueueTestHelper.assertResponseStatusCode(it, 201) }
            .verifyComplete()
        createQueueVerifier2.assertNext { QueueTestHelper.assertResponseStatusCode(it, 204) }
            .verifyComplete()
    }

    def "Create queue with diff metadata from queue async client"() {
        when:
        def createQueueVerifier1 = StepVerifier.create(queueAsyncClient.create())
        def createQueueVerifier2 = StepVerifier.create(queueAsyncClient.create(testMetadata))
        then:
        createQueueVerifier1.assertNext { QueueTestHelper.assertResponseStatusCode(it, 201) }
            .verifyComplete()
        createQueueVerifier2.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 409, "QueueAlreadyExists")
        }
    }

    def "Delete exist queue from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def deleteQueueVerifier = StepVerifier.create(queueAsyncClient.delete())
        QueueTestHelper.sleepInRecordMode(Duration.ofSeconds(30));
        def errorEnqueueVerifier = StepVerifier.create(queueAsyncClient.enqueueMessage("This should fail"))
        then:
        deleteQueueVerifier.assertNext { QueueTestHelper.assertResponseStatusCode(it, 204) }
            .verifyComplete()
        errorEnqueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Delete not existing queue from queue async client"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(queueAsyncClient.delete())
        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Get properties from queue async client"() {
        given:
        queueAsyncClient.create(testMetadata).block()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(0, it.value().approximateMessagesCount())
            assertEquals(testMetadata, it.value().metadata())
        }.verifyComplete()
    }

    def "Get properties does not exist from queue async client"() {
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    @Unroll
    def "Set and clear metadata from queue async client"() {
        given:
        queueAsyncClient.create(matadataInCreate).block()
        when:
        def getPropertiesVerifierBefore = StepVerifier.create(queueAsyncClient.getProperties())
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadata(metadataInSet))
        def getPropertiesVerifierAfter = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifierBefore.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(expectMetadataInCreate, it.value().metadata())
        }.verifyComplete()
        setMetadataVerifier.assertNext { QueueTestHelper.assertResponseStatusCode(it, 204) }
            .verifyComplete()
        getPropertiesVerifierAfter.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(expectMetadataInSet, it.value().metadata)
        }.verifyComplete()
        where:
        matadataInCreate | metadataInSet | expectMetadataInCreate | expectMetadataInSet
        null             | testMetadata  | Collections.emptyMap() | testMetadata
        createMetadata   | testMetadata  | createMetadata         | testMetadata
        createMetadata   | null          | createMetadata         | Collections.emptyMap()
        testMetadata     | testMetadata  | testMetadata           | testMetadata
        null             | null          | Collections.emptyMap() | Collections.emptyMap()
    }

    def "Set metadata queue not exist from queue async client"() {
        when:
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadata(testMetadata))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    @Unroll
    def "Set invalid meta from queue async client"() {
        given:
        def invalidMetadata = Collections.singletonMap(invalidKey, "value")
        queueAsyncClient.create().block()
        when:
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadata(invalidMetadata))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage)
        }
        where:
        invalidKey     | statusCode | errMessage
        "invalidMeta"  | 403        | "AuthenticationError"
        "invalid-meta" | 400        | "InvalidMetadata"
        "12345"        | 400        | "InvalidMetadata"
        ""             | 400        | "EmptyMetadataKey"
    }

    def "Get access policy from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        getAccessPolicyVerifier.verifyComplete()
    }

    def "Get access policy does not exist from queue async client"() {
        when:
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        getAccessPolicyVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Set access policy from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new SignedIdentifier()
            .id("testpermission")
            .accessPolicy(accessPolicy)
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        setAccessPolicyVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getAccessPolicyVerifier.assertNext {
            QueueTestHelper.assertPermissionsAreEqual(permission, it)
        }.verifyComplete()
    }

    def "Set access policy does not exist from queue async client"() {
        given:
        def accessPolicy = new AccessPolicy()
            .permission("raup")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new SignedIdentifier()
            .id("testpermission")
            .accessPolicy(accessPolicy)
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
        then:
        setAccessPolicyVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Set invalid access policy from queue async client"() {
        given:
        def accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))

        def permission = new SignedIdentifier()
            .id("theidofthispermissionislongerthanwhatisallowedbytheserviceandshouldfail")
            .accessPolicy(accessPolicy)
        queueAsyncClient.create().block()
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicy(Collections.singletonList(permission)))
        then:
        setAccessPolicyVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "InvalidXmlDocument")
        }
    }

    def "Set too many access policies from queue async client"() {
        given:
        def accessPolicy = new AccessPolicy()
            .permission("r")
            .start(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .expiry(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC));

        def permissions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            permissions.add(new SignedIdentifier()
                .id("policy" + i)
                .accessPolicy(accessPolicy));
        }
        queueAsyncClient.create().block()
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicy(permissions))
        then:
        setAccessPolicyVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "InvalidXmlDocument")
        }
    }

    def "Enqueue message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.enqueueMessage(expectMsg))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages())
        then:
        enqueueMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assertEquals(expectMsg, it.messageText())
            assertFalse(it.hasNext())
        }
    }

    def "Enqueue empty message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.enqueueMessage(""))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages())
        then:
        enqueueMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assertNull(it.messageText())
            assertFalse(it.hasNext())
        }
    }

    def "Enqueue short time to live from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.enqueueMessage("test message",
            Duration.ofSeconds(0), Duration.ofSeconds(2)))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages().delaySubscription(Duration.ofSeconds(5)))
        then:
        enqueueMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.verifyComplete()
    }

    def "Dequeue message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.enqueueMessage(expectMsg).block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.dequeueMessages())
        then:
        dequeueMsgVerifier.assertNext {
            Assert.assertEquals(expectMsg, it.messageText())
        }.verifyComplete()
    }

    def "Dequeue multiple messages from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueAsyncClient.enqueueMessage(expectMsg1).block()
        queueAsyncClient.enqueueMessage(expectMsg2).block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.dequeueMessages(2))
        then:
        dequeueMsgVerifier.assertNext {
            assertEquals(expectMsg1, it.messageText())
        }.assertNext {
            assertEquals(expectMsg2, it.messageText())
        }.verifyComplete()
    }

    def "Dequeue too many message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.dequeueMessages(33))
        then:
        dequeueMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "OutOfRangeQueryParameterValue")
        }
    }

    def "Dequeue messages do not exist from queue async client"() {
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.dequeueMessages())
        then:
        dequeueMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Peek message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.enqueueMessage(expectMsg).block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages())
        then:
        peekMsgVerifier.assertNext {
            Assert.assertEquals(expectMsg, it.messageText())
        }.verifyComplete()
    }

    def "Peek multiple messages from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueAsyncClient.enqueueMessage(expectMsg1).block()
        queueAsyncClient.enqueueMessage(expectMsg2).block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages(2))
        then:
        peekMsgVerifier.assertNext {
            assertEquals(expectMsg1, it.messageText())
        }.assertNext {
            assertEquals(expectMsg2, it.messageText())
        }.verifyComplete()
    }

    def "Peek too many message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages(33))
        then:
        peekMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "OutOfRangeQueryParameterValue")
        }
    }

    def "Peek messages do not exist from queue async client"() {
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages())
        then:
        peekMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Clear messages from queue async client"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.enqueueMessage("test message 1").block()
        queueAsyncClient.enqueueMessage("test message 2").block()
        queueAsyncClient.enqueueMessage("test message 3").block()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        def clearMsgVerifier = StepVerifier.create(queueAsyncClient.clearMessages())
        def getPropertiesAfterVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(3, it.value().approximateMessagesCount())
        }.verifyComplete()
        clearMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(0, it.value().approximateMessagesCount())
        }.verifyComplete()
    }

    def "Clear messages do not exist from queue async client"() {
        when:
        def clearMsgVerifier = StepVerifier.create(queueAsyncClient.clearMessages())
        then:
        clearMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Delete message from queue async client"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.enqueueMessage("test message 1").block()
        queueAsyncClient.enqueueMessage("test message 2").block()
        queueAsyncClient.enqueueMessage("test message 3").block()
        def dequeueMsg = queueAsyncClient.dequeueMessages().blockFirst()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        def deleteMsgVerifier = StepVerifier.create(queueAsyncClient.deleteMessage(dequeueMsg.messageId(), dequeueMsg.popReceipt()))
        def getPropertiesAfterVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(3, it.value().approximateMessagesCount())
        }.verifyComplete()
        deleteMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 200)
            assertEquals(2, it.value().approximateMessagesCount())
        }.verifyComplete()
    }

    @Unroll
    def "Delete message invalid args from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.enqueueMessage(expectMsg).block()
        def dequeueMessage = queueAsyncClient.dequeueMessages().blockFirst()
        when:
        def deleteMessageId = messageId ? dequeueMessage.messageId() : dequeueMessage.messageId() + "Random"
        def deletePopReceipt = popReceipt ? dequeueMessage.popReceipt() : dequeueMessage.popReceipt() + "Random"
        def deleteMsgVerifier = StepVerifier.create(queueAsyncClient.deleteMessage(deleteMessageId, deletePopReceipt))
        then:
        deleteMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        messageId | popReceipt | statusCode | errMsg
        true | false | 400 | "InvalidQueryParameterValue"
        false | true | 404 | "MessageNotFound"
        false | false | 400 | "InvalidQueryParameterValue"
    }

    def "Delete message does not exist from async client"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.enqueueMessage("test update message").block()
        def dequeueMsg = queueAsyncClient.dequeueMessages().blockFirst()
        queueAsyncClient.delete().block()
        QueueTestHelper.sleepInRecordMode(Duration.ofSeconds(30))
        when:
        def deleteMsgVerifier = StepVerifier.create(queueAsyncClient.deleteMessage(dequeueMsg.messageId(),
            dequeueMsg.popReceipt()))
        then:
        deleteMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }

    def "Update message from queue async client"() {
        given:
        def updateMsg = "Updated test message"
        queueAsyncClient.create().block()
        queueAsyncClient.enqueueMessage("test message before update").block()

        def dequeueMsg = queueAsyncClient.dequeueMessages().blockFirst()
        when:
        def updateMsgVerifier = StepVerifier.create(queueAsyncClient.updateMessage(updateMsg,
            dequeueMsg.messageId(), dequeueMsg.popReceipt(), Duration.ofSeconds(1)))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages().delaySubscription(Duration.ofSeconds(2)))
        then:
        updateMsgVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assertEquals(updateMsg, it.messageText())
        }.verifyComplete()
    }

    @Unroll
    def "Update message invalid args from queue async client"() {
        given:
        queueAsyncClient.create().block()
        def updateMsg = "Updated test message"
        queueAsyncClient.enqueueMessage("test message before update").block()
        def dequeueMessage = queueAsyncClient.dequeueMessages().blockFirst()
        when:
        def updateMessageId = messageId ? dequeueMessage.messageId(): dequeueMessage.messageId() + "Random"
        def updatePopReceipt = popReceipt ? dequeueMessage.popReceipt(): dequeueMessage.popReceipt() + "Random"
        def updateMsgVerifier = StepVerifier.create(queueAsyncClient.updateMessage(updateMsg, updateMessageId, updatePopReceipt, Duration.ofSeconds(1)))
        then:
        updateMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        messageId | popReceipt | statusCode | errMsg
        true | false | 400 | "InvalidQueryParameterValue"
        false | true | 404 | "MessageNotFound"
        false | false | 400 | "InvalidQueryParameterValue"
    }

    def "Update message does not exist from async client"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.enqueueMessage("test update message").block()
        def dequeueMsg = queueAsyncClient.dequeueMessages().blockFirst()
        queueAsyncClient.delete().block()
        QueueTestHelper.sleepInRecordMode(Duration.ofSeconds(30))
        when:
        def updateMsgVerifier = StepVerifier.create(queueAsyncClient.updateMessage("sometext", dequeueMsg.messageId(),
            dequeueMsg.popReceipt(), Duration.ofSeconds(1)))
        then:
        updateMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, "QueueNotFound")
        }
    }
}
