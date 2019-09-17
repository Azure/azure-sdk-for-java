// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue


import com.azure.storage.queue.models.Logging
import com.azure.storage.queue.models.Metrics
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueuesSegmentOptions
import com.azure.storage.queue.models.RetentionPolicy
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageServiceProperties
import reactor.test.StepVerifier
import spock.lang.Unroll

class QueueServiceAsyncAPITests extends APISpec {

    def setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper(interceptorManager).buildAsyncClient()
    }

    def "Get queue client"() {
        given:
        def queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(testResourceName.randomName(methodName, 60))
        expect:
        queueAsyncClient instanceof QueueAsyncClient
    }

    def "Create queue"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)
        expect:
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, null)).assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName).enqueueMessageWithResponse("Testing service client creating a queue", null, null))
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
        "a_b"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "-ab"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "a--b"         | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "Abc"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "ab"           | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        "verylong" * 8 | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
    }

    def "Create null"() {
        when:
        primaryQueueServiceAsyncClient.createQueue(null)
        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Create queue maxOverload"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, metadata))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessageWithResponse("Testing service client creating a queue", null, null))
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
        def queueName = testResourceName.randomName(methodName, 60)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, Collections.singletonMap("metadata!", "value")))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.INVALID_METADATA)
        }
    }

    def "Delete queue"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)
        primaryQueueServiceAsyncClient.createQueue(queueName).block()
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(queueName))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessageWithResponse("Expecting exception as queue has been deleted.", null, null))
        then:
        deleteQueueVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        enqueueMessageVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Delete queue error"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(testResourceName.randomName(methodName, 16)))
        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "List queues"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)
        LinkedList<QueueItem> testQueues = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i)
            QueueItem queue = new QueueItem().setName(queueName + version)
                .setMetadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceAsyncClient.createQueueWithResponse(queue.getName(), queue.getMetadata()).block()
        }
        when:
        def queueListVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(options))
        then:
        queueListVerifier.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.verifyComplete()
        where:
        options                                                                                              | _
        new QueuesSegmentOptions().setPrefix("queueserviceasyncapitestslistqueues")                          | _
        new QueuesSegmentOptions().setPrefix("queueserviceasyncapitestslistqueues").setMaxResults(2)         | _
        new QueuesSegmentOptions().setPrefix("queueserviceasyncapitestslistqueues").setIncludeMetadata(true) | _
    }

    def "List empty queues"() {
        when:
        def listQueueVerifier = StepVerifier.create((primaryQueueServiceAsyncClient.listQueues(new QueuesSegmentOptions())))
        then:
        listQueueVerifier.assertNext {
            !it.iterator().hasNext()
        }
    }

    def "Get and set properties"() {
        given:
        def originalProperties = primaryQueueServiceAsyncClient.getProperties().block()
        def retentionPolicy = new RetentionPolicy().setEnabled(true)
            .setDays(3)
        def logging = new Logging().setVersion("1.0")
            .setDelete(true)
            .setWrite(true)
            .setRetentionPolicy(retentionPolicy)
        def metrics = new Metrics().setEnabled(true)
            .setIncludeAPIs(false)
            .setRetentionPolicy(retentionPolicy)
            .setVersion("1.0")
        def updatedProperties = new StorageServiceProperties().setLogging(logging)
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
        }
        setPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, it)
        }.verifyComplete()
    }
}
