// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.util.BinaryData
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.queue.models.PeekedMessageItem
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueErrorCode
import com.azure.storage.queue.models.QueueMessageItem
import com.azure.storage.queue.models.QueueSignedIdentifier
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class QueueAysncAPITests extends APISpec {
    QueueAsyncClient queueAsyncClient

    static def testMetadata = Collections.singletonMap("metadata", "value")
    static def createMetadata = Collections.singletonMap("metadata1", "value")
    def queueName

    def setup() {
        queueName = namer.getRandomName(60)
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient()
        queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
    }

    def "Get queue URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.queue.core.windows.net/%s", accountName, queueName)

        when:
        def queueURL = queueAsyncClient.getQueueUrl()

        then:
        expectURL == queueURL
    }

    def "IP based endpoint"() {
        when:
        def queueAsyncClient = new QueueClientBuilder()
            .connectionString(environment.primaryAccount.connectionString)
            .endpoint("http://127.0.0.1:10001/devstoreaccount1/myqueue")
            .buildAsyncClient()

        then:
        queueAsyncClient.getAccountName() == "devstoreaccount1"
        queueAsyncClient.getQueueName() == "myqueue"
    }

    def "Create queue with shared key"() {
        expect:
        StepVerifier.create(queueAsyncClient.createWithResponse(null)).assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201) }
            .verifyComplete()
    }

    // TODO: Will implement the test after introduce the sas token generator
    @Ignore
    def "Create queue with sas token"() {

    }

    def "Delete exist queue"() {
        given:
        queueAsyncClient.createWithResponse(null).block()
        when:
        def deleteQueueVerifier = StepVerifier.create(queueAsyncClient.deleteWithResponse())
        then:
        deleteQueueVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204) }
            .verifyComplete()
    }

    def "Delete queue error"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(queueAsyncClient.deleteWithResponse())
        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Get properties"() {
        given:
        queueAsyncClient.createWithResponse(testMetadata).block()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getApproximateMessagesCount() == 0
            assert testMetadata == it.getValue().getMetadata()
        }.verifyComplete()
    }

    def "Get properties error"() {
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getProperties())
        then:
        getPropertiesVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "Set and clear metadata"() {
        given:
        queueAsyncClient.createWithResponse(matadataInCreate).block()
        when:
        def getPropertiesVerifierBefore = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadataWithResponse(metadataInSet))
        def getPropertiesVerifierAfter = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifierBefore.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert expectMetadataInCreate == it.getValue().getMetadata()
        }.verifyComplete()
        setMetadataVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204) }
            .verifyComplete()
        getPropertiesVerifierAfter.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert expectMetadataInSet == it.getValue().metadata
        }.verifyComplete()
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
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadataWithResponse(testMetadata))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "Set invalid meta"() {
        given:
        def invalidMetadata = Collections.singletonMap(invalidKey, "value")
        queueAsyncClient.create().block()
        when:
        def setMetadataVerifier = StepVerifier.create(queueAsyncClient.setMetadataWithResponse(invalidMetadata))
        then:
        setMetadataVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMessage)
        }
        where:
        invalidKey     | statusCode | errMessage
        "invalid-meta" | 400        | QueueErrorCode.INVALID_METADATA
        "12345"        | 400        | QueueErrorCode.INVALID_METADATA
        ""             | 400        | QueueErrorCode.EMPTY_METADATA_KEY
    }

    def "Get access policy"() {
        given:
        queueAsyncClient.create().block()
        when:
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        getAccessPolicyVerifier
            .expectNextCount(0)
            .verifyComplete()
    }

    def "Get access policy does error"() {
        when:
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        getAccessPolicyVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Set access policy"() {
        given:
        queueAsyncClient.create().block()
        def accessPolicy = new QueueAccessPolicy()
            .setPermissions("raup")
            .setStartsOn(OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 0, 0), ZoneOffset.UTC))
            .setExpiresOn(OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneOffset.UTC))
        def permission = new QueueSignedIdentifier()
            .setId("testpermission")
            .setAccessPolicy(accessPolicy)
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission)))
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        setAccessPolicyVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getAccessPolicyVerifier.assertNext {
            assert QueueTestHelper.assertPermissionsAreEqual(permission, it)
        }.verifyComplete()
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
        queueAsyncClient.create().block()
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(Collections.singletonList(permission)))
        then:
        setAccessPolicyVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, QueueErrorCode.INVALID_XML_DOCUMENT)
        }
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
        queueAsyncClient.create().block()
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(permissions))
        def getAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.getAccessPolicy())
        then:
        setAccessPolicyVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getAccessPolicyVerifier.assertNext {
            assert QueueTestHelper.assertPermissionsAreEqual(permissions[0], it)
        }.assertNext {
            assert QueueTestHelper.assertPermissionsAreEqual(permissions[1], it)
        }.assertNext {
            assert QueueTestHelper.assertPermissionsAreEqual(permissions[2], it)
        }.verifyComplete()
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
        queueAsyncClient.create().block()
        when:
        def setAccessPolicyVerifier = StepVerifier.create(queueAsyncClient.setAccessPolicyWithResponse(permissions))
        then:
        setAccessPolicyVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, QueueErrorCode.INVALID_XML_DOCUMENT)
        }
    }

    def "Enqueue message"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.sendMessageWithResponse(expectMsg, null, null))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        enqueueMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assert expectMsg == it.getMessageText()
        }.verifyComplete()
    }

    def "Enqueue message binary data"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = BinaryData.fromString("test message")
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.sendMessageWithResponse(expectMsg, null, null))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        enqueueMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assert expectMsg.toBytes() == it.getBody().toBytes()
        }.verifyComplete()
    }

    def "Enqueue empty message"() {
        given:
        queueAsyncClient.create().block()
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.sendMessageWithResponse("", null, null))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        enqueueMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assert it.getMessageText() == null
        }.verifyComplete()
    }

    def "Enqueue time to live"() {
        given:
        queueAsyncClient.create().block()
        when:
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.sendMessageWithResponse("test message",
            Duration.ofSeconds(0), Duration.ofSeconds(2)))
        then:
        enqueueMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
    }

    def "Enqueue message encoded message"() {
        given:
        queueAsyncClient.create().block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        def expectMsg = BinaryData.fromString("test message")
        when:
        def enqueueMsgVerifier = StepVerifier.create(encodingQueueClient.sendMessageWithResponse(expectMsg, null, null))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        enqueueMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assert Base64.getEncoder().encodeToString(expectMsg.toBytes()) == it.getBody().toString()
        }.verifyComplete()
    }

    def "Dequeue message from empty queue"() {
        given:
        queueAsyncClient.create().block()

        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.receiveMessage())

        then:
        dequeueMsgVerifier.verifyComplete()
    }

    def "Dequeue message"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.sendMessage(expectMsg).block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.receiveMessage())
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg == it.getMessageText()
        }.verifyComplete()
    }

    def "Dequeue encoded message"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(encodedMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessage())
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
        }.verifyComplete()
    }

    def "Dequeue fails without handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.sendMessage(expectMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessage())
        then:
        dequeueMsgVerifier.verifyError(IllegalArgumentException.class)
    }

    def "Dequeue with handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        QueueMessageItem badMessage = null
        String queueUrl = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ failure ->
                badMessage = failure.getQueueMessageItem()
                queueUrl = failure.getQueueAsyncClient().getQueueUrl()
                return Mono.empty()
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessages(10))
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
            assert badMessage != null
            assert badMessage.getBody().toString() == expectMsg
            assert queueUrl == queueAsyncClient.getQueueUrl()
        }.verifyComplete()
    }

    def "Dequeue and delete with handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        QueueMessageItem badMessage = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ failure ->
                badMessage = failure.getQueueMessageItem()
                return failure.getQueueAsyncClient().deleteMessage(badMessage.getMessageId(), badMessage.getPopReceipt())
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessages(10))
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
            assert badMessage != null
            assert badMessage.getBody().toString() == expectMsg
        }.verifyComplete()
    }

    def "Dequeue and delete with sync handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        QueueMessageItem badMessage = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError({ failure ->
                badMessage = failure.getQueueMessageItem()
                failure.getQueueClient().deleteMessage(badMessage.getMessageId(), badMessage.getPopReceipt())
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessages(10))
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
            assert badMessage != null
            assert badMessage.getBody().toString() == expectMsg
        }.verifyComplete()
    }

    def "Dequeue with handler error"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ message ->
                throw new IllegalStateException("KABOOM")
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessages(10))
        then:
        dequeueMsgVerifier.verifyError(IllegalStateException.class)
    }

    def "Dequeue multiple messages"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueAsyncClient.sendMessage(expectMsg1).block()
        queueAsyncClient.sendMessage(expectMsg2).block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.receiveMessages(2))
        then:
        dequeueMsgVerifier.assertNext {
            assert expectMsg1 == it.getMessageText()
        }.assertNext {
            assert expectMsg2 == it.getMessageText()
        }.verifyComplete()
    }

    def "Dequeue too many message"() {
        given:
        queueAsyncClient.create().block()
        when:
        def dequeueMsgVerifier = StepVerifier.create(queueAsyncClient.receiveMessages(33))
        then:
        dequeueMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
        }
    }

    def "Enqueue Dequeue non-UTF message"() {
        given:
        queueAsyncClient.create().block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        byte[] content = [ 0xFF, 0x00 ]; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content)).block()

        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.receiveMessage())
        then:
        dequeueMsgVerifier.assertNext {
            assert content == it.getBody().toBytes()
        }.verifyComplete()
    }

    def "Enqueue Peek non-UTF message"() {
        given:
        queueAsyncClient.create().block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        byte[] content = [ 0xFF, 0x00 ]; // Not a valid UTF-8 byte sequence.
        encodingQueueClient.sendMessage(BinaryData.fromBytes(content)).block()

        when:
        def dequeueMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessage())
        then:
        dequeueMsgVerifier.assertNext {
            assert content == it.getBody().toBytes()
        }.verifyComplete()
    }

    def "Peek message from empty queue"() {
        given:
        queueAsyncClient.create().block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        peekMsgVerifier.verifyComplete()
    }

    def "Peek message"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.sendMessage(expectMsg).block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        peekMsgVerifier.assertNext {
            assert expectMsg == it.getMessageText()
        }.verifyComplete()
    }

    def "Peek encoded message"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(encodedMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def peekMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessage())
        then:
        peekMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
        }.verifyComplete()
    }

    def "Peek fails without handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.sendMessage(expectMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper().messageEncoding(QueueMessageEncoding.BASE64).buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def peekMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessage())
        then:
        peekMsgVerifier.verifyError(IllegalArgumentException.class)
    }

    def "Peek with handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
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
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def peekMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessages(10))
        then:
        peekMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
            assert badMessage != null
            assert badMessage.getBody().toString() == expectMsg
            assert queueUrl == queueAsyncClient.getQueueUrl()
            assert cause != null
        }.verifyComplete()
    }

    def "Peek with sync handler"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        PeekedMessageItem badMessage = null
        Exception cause = null
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingError({ failure ->
                badMessage = failure.getPeekedMessageItem()
                cause = failure.getCause()
                // call some sync API
                failure.getQueueClient().getProperties()
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def peekMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessages(10))
        then:
        peekMsgVerifier.assertNext {
            assert expectMsg == it.getBody().toString()
            assert badMessage != null
            assert badMessage.getBody().toString() == expectMsg
            assert cause != null
        }.verifyComplete()
    }

    def "Peek with handler exception"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        def encodedMsg = Base64.getEncoder().encodeToString(expectMsg.getBytes(StandardCharsets.UTF_8))
        queueAsyncClient.sendMessage(expectMsg).block()
        queueAsyncClient.sendMessage(encodedMsg).block()
        def encodingQueueClient = queueServiceBuilderHelper()
            .messageEncoding(QueueMessageEncoding.BASE64)
            .processMessageDecodingErrorAsync({ message ->
                throw new IllegalStateException("KABOOM")
            })
            .buildAsyncClient().getQueueAsyncClient(queueName)
        when:
        def peekMsgVerifier = StepVerifier.create(encodingQueueClient.peekMessages(10))
        then:
        peekMsgVerifier.verifyError(IllegalStateException.class)
    }

    def "Peek multiple messages"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg1 = "test message 1"
        def expectMsg2 = "test message 2"
        queueAsyncClient.sendMessage(expectMsg1).block()
        queueAsyncClient.sendMessage(expectMsg2).block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages(2))
        then:
        peekMsgVerifier.assertNext {
            assert expectMsg1 == it.getMessageText()
        }.assertNext {
            assert expectMsg2 == it.getMessageText()
        }.verifyComplete()
    }

    def "Peek too many message"() {
        given:
        queueAsyncClient.create().block()
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessages(33))
        then:
        peekMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, QueueErrorCode.OUT_OF_RANGE_QUERY_PARAMETER_VALUE)
        }
    }

    def "Peek messages error"() {
        when:
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage())
        then:
        peekMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Clear messages"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.sendMessage("test message 1").block()
        queueAsyncClient.sendMessage("test message 2").block()
        queueAsyncClient.sendMessage("test message 3").block()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        def clearMsgVerifier = StepVerifier.create(queueAsyncClient.clearMessagesWithResponse())
        def getPropertiesAfterVerifier = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getApproximateMessagesCount() == 3
        }.verifyComplete()
        clearMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getApproximateMessagesCount() == 0
        }.verifyComplete()
    }

    def "Clear messages error"() {
        when:
        def clearMsgVerifier = StepVerifier.create(queueAsyncClient.clearMessagesWithResponse())
        then:
        clearMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Delete message"() {
        given:
        queueAsyncClient.create().block()
        queueAsyncClient.sendMessage("test message 1").block()
        queueAsyncClient.sendMessage("test message 2").block()
        queueAsyncClient.sendMessage("test message 3").block()
        def dequeueMsg = queueAsyncClient.receiveMessage().block()
        when:
        def getPropertiesVerifier = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        def deleteMsgVerifier = StepVerifier.create(queueAsyncClient.deleteMessageWithResponse(dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt()))
        def getPropertiesAfterVerifier = StepVerifier.create(queueAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getApproximateMessagesCount() == 3

        }.verifyComplete()
        deleteMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 200)
            assert it.getValue().getApproximateMessagesCount() == 2
        }.verifyComplete()
    }

    @Unroll
    def "Delete message invalid args"() {
        given:
        queueAsyncClient.create().block()
        def expectMsg = "test message"
        queueAsyncClient.sendMessage(expectMsg).block()
        QueueMessageItem queueMessageItem = queueAsyncClient.receiveMessage().block()
        when:
        def deleteMessageId = messageId ? queueMessageItem.getMessageId() : queueMessageItem.getMessageId() + "Random"
        def deletePopReceipt = popReceipt ? queueMessageItem.getPopReceipt() : queueMessageItem.getPopReceipt() + "Random"
        def deleteMsgVerifier = StepVerifier.create(queueAsyncClient.deleteMessageWithResponse(deleteMessageId, deletePopReceipt))
        then:
        deleteMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | QueueErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Update message"() {
        given:
        def updateMsg = "Updated test message"
        queueAsyncClient.create().block()
        queueAsyncClient.sendMessage("test message before update").block()

        def dequeueMsg = queueAsyncClient.receiveMessage().block()
        when:
        def updateMsgVerifier = StepVerifier.create(queueAsyncClient.updateMessageWithResponse(
            dequeueMsg.getMessageId(), dequeueMsg.getPopReceipt(), updateMsg, Duration.ofSeconds(1)))
        def peekMsgVerifier = StepVerifier.create(queueAsyncClient.peekMessage()
            .delaySubscription(getMessageUpdateDelay(2000)))
        then:
        updateMsgVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        peekMsgVerifier.assertNext {
            assert updateMsg == it.getMessageText()
        }.verifyComplete()
    }

    @Unroll
    def "Update message invalid args"() {
        given:
        queueAsyncClient.create().block()
        def updateMsg = "Updated test message"
        queueAsyncClient.sendMessage("test message before update").block()
        def dequeueMessage = queueAsyncClient.receiveMessage().block()
        when:
        def updateMessageId = messageId ? dequeueMessage.getMessageId() : dequeueMessage.getMessageId() + "Random"
        def updatePopReceipt = popReceipt ? dequeueMessage.getPopReceipt() : dequeueMessage.getPopReceipt() + "Random"
        def updateMsgVerifier = StepVerifier.create(queueAsyncClient.updateMessageWithResponse(updateMessageId, updatePopReceipt, updateMsg, Duration.ofSeconds(1)))
        then:
        updateMsgVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        messageId | popReceipt | statusCode | errMsg
        true      | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
        false     | true       | 404        | QueueErrorCode.MESSAGE_NOT_FOUND
        false     | false      | 400        | QueueErrorCode.INVALID_QUERY_PARAMETER_VALUE
    }

    def "Get Queue Name"() {
        expect:
        queueName == queueAsyncClient.getQueueName()
    }
}
