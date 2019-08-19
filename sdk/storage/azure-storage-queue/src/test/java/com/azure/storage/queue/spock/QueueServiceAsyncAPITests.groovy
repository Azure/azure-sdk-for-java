// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

import com.azure.storage.queue.QueueAsyncClient
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
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName)).assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName).enqueueMessage("Testing service client creating a queue"))
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
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName, metadata))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessage("Testing service client creating a queue"))
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
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName, Collections.singletonMap("meta@data", "value")))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "Bad Request")
        }
    }

    def "Delete queue"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)
        primaryQueueServiceAsyncClient.createQueue(queueName).block()
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueue(queueName))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessage("Expecting exception as queue has been deleted."))
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
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueue(testResourceName.randomName(methodName, 16)))
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
            QueueItem queue = new QueueItem().name(queueName + version)
                .metadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceAsyncClient.createQueue(queue.name(), queue.metadata()).block()
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
        options                                                                                        | _
        new QueuesSegmentOptions().prefix("queueserviceasyncapitestslistqueues")                       | _
        new QueuesSegmentOptions().prefix("queueserviceasyncapitestslistqueues").maxResults(2)         | _
        new QueuesSegmentOptions().prefix("queueserviceasyncapitestslistqueues").includeMetadata(true) | _
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
        def originalProperties = primaryQueueServiceAsyncClient.getProperties().block().value()
        def retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3)
        def logging = new Logging().version("1.0")
            .delete(true)
            .write(true)
            .retentionPolicy(retentionPolicy)
        def metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0")
        def updatedProperties = new StorageServiceProperties().logging(logging)
            .hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(new ArrayList<>())
        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
        def setPropertiesVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.setProperties(updatedProperties))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())
        then:
        getPropertiesBeforeVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, it.value())
        }
        setPropertiesVerifier.assertNext {
            assert QueueTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, it.value())
        }.verifyComplete()
    }
}
