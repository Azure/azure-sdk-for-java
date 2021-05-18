// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.storage.queue.models.QueueAnalyticsLogging
import com.azure.storage.queue.models.QueueErrorCode
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueueMetrics
import com.azure.storage.queue.models.QueueRetentionPolicy
import com.azure.storage.queue.models.QueueServiceProperties
import com.azure.storage.queue.models.QueuesSegmentOptions
import reactor.test.StepVerifier
import spock.lang.ResourceLock
import spock.lang.Unroll

class QueueServiceAsyncAPITests extends APISpec {

    def setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper().buildAsyncClient()
    }

    def "Get queue client"() {
        given:
        def queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(namer.getRandomName(60))
        expect:
        queueAsyncClient instanceof QueueAsyncClient
    }

    def "Create queue"() {
        given:
        def queueName = namer.getRandomName(60)
        expect:
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, null)).assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName).sendMessageWithResponse("Testing service client creating a queue", null, null))
            .assertNext {
                assert QueueTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create queue with invalid name"() {
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMesage)
        }
        where:
        queueName      | statusCode | errMesage
        "a_b"          | 400        | QueueErrorCode.INVALID_RESOURCE_NAME
        "-ab"          | 400        | QueueErrorCode.INVALID_RESOURCE_NAME
        "a--b"         | 400        | QueueErrorCode.INVALID_RESOURCE_NAME
        "Abc"          | 400        | QueueErrorCode.INVALID_RESOURCE_NAME
        "ab"           | 400        | QueueErrorCode.OUT_OF_RANGE_INPUT
        "verylong" * 8 | 400        | QueueErrorCode.OUT_OF_RANGE_INPUT
    }

    def "Create null"() {
        when:
        primaryQueueServiceAsyncClient.createQueue(null).block()
        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Create queue maxOverload"() {
        given:
        def queueName = namer.getRandomName(60)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, metadata))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .sendMessageWithResponse("Testing service client creating a queue", null, null))
        then:
        createQueueVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        enqueueMessageVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()


        where:
        metadata                                       | _
        null                                           | _
        Collections.singletonMap("metadata", "value")  | _
        Collections.singletonMap("metadata", "va@lue") | _
    }

    def "Create queue with invalid metadata"() {
        given:
        def queueName = namer.getRandomName(60)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, Collections.singletonMap("metadata!", "value")))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, QueueErrorCode.INVALID_METADATA)
        }
    }

    def "Delete queue"() {
        given:
        def queueName = namer.getRandomName(60)
        primaryQueueServiceAsyncClient.createQueue(queueName).block()
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(queueName))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .sendMessageWithResponse("Expecting exception as queue has been deleted.", null, null))
        then:
        deleteQueueVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        enqueueMessageVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Delete queue error"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(namer.getRandomName(16)))
        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, QueueErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "List queues"() {
        given:
        def queueName = namer.getRandomName(60)
        LinkedList<QueueItem> testQueues = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i)
            QueueItem queue = new QueueItem().setName(queueName + version)
                .setMetadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceAsyncClient.createQueueWithResponse(queue.getName(), queue.getMetadata()).block()
        }
        when:
        def queueListVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(options.setPrefix(namer.getResourcePrefix())))
        then:
        queueListVerifier.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.verifyComplete()
        where:
        options                                             | _
        new QueuesSegmentOptions()                          | _
        new QueuesSegmentOptions().setMaxResultsPerPage(2)  | _
        new QueuesSegmentOptions().setIncludeMetadata(true) | _
    }

    def "List empty queues"() {
        expect:
        // Queue was never made with the prefix, should expect no queues to be listed.
        StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(new QueuesSegmentOptions().setPrefix(namer.getResourcePrefix())))
            .expectNextCount(0)
            .verifyComplete()
    }

    @ResourceLock("ServiceProperties")
    def "Get and set properties"() {
        given:
        def originalProperties = primaryQueueServiceAsyncClient.getProperties().block()
        def retentionPolicy = new QueueRetentionPolicy().setEnabled(true)
            .setDays(3)
        def logging = new QueueAnalyticsLogging().setVersion("1.0")
            .setDelete(true)
            .setWrite(true)
            .setRetentionPolicy(retentionPolicy)
        def metrics = new QueueMetrics().setEnabled(true)
            .setIncludeApis(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0")
        def updatedProperties = new QueueServiceProperties().setAnalyticsLogging(logging)
            .setHourMetrics(metrics)
            .setMinuteMetrics(metrics)
            .setCors(new ArrayList<>())
        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
        def setPropertiesVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.setPropertiesWithResponse(updatedProperties))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
        then:
        getPropertiesBeforeVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, it)
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, it)
        }.verifyComplete()
    }
}
