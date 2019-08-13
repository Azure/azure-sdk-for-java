// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock


import com.azure.storage.queue.models.Logging
import com.azure.storage.queue.models.Metrics
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueuesSegmentOptions
import com.azure.storage.queue.models.RetentionPolicy
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageServiceProperties
import reactor.test.StepVerifier
import spock.lang.Unroll

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull

class QueueServiceAsyncAPITests extends APISpec {

    def setup() {
        primaryQueueServiceAsyncClient = queueServiceBuilderHelper(interceptorManager).buildAsyncClient()
    }
    
    def "Get queue client from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        def queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
        def enqueueMsgVerifier = StepVerifier.create(queueAsyncClient.enqueueMessage("Expecting an exception"))
        then:
        assertNotNull(queueAsyncClient)
        enqueueMsgVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Create queue from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        expect:
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName)).assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName).enqueueMessage("Testing service client creating a queue"))
            .assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
    }

    def "Create queues with duplicate name from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        primaryQueueServiceAsyncClient.createQueue(queueName).block()
        expect:
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName)).assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
    }

    @Unroll
    def "Create queue with invalid name from queue service async client"() {
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMesage)
        }
        where:
        queueName      | statusCode | errMesage
        "a_b"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "-ab"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "a--b"         | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        // null | 400 | "InvalidResourceName" TODO: Need to fix the RestProxy before having null parameter
        "Abc"          | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "ab"           | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        "verylong" * 8 | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
    }

    @Unroll
    def "Create queue maxOverload from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName, metadata))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessage("Testing service client creating a queue"))
        then:
        createQueueVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()
        enqueueMessageVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()


        where:
        metadata                                       | _
        null                                           | _
        Collections.singletonMap("metadata", "value")  | _
        Collections.singletonMap("metadata", "va@lue") | _
    }

    def "Create queue with invalid metadata from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName, Collections.singletonMap("meta@data", "value")))
        then:
        createQueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, "Bad Request")
        }
    }

    def "Create queues with same name and different metadata from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        def metadata1 = new HashMap<>()
        metadata1.put("metadata1", "value1")
        def metadata2 = new HashMap<>()
        metadata2.put("metadata2", "value2")
        primaryQueueServiceAsyncClient.createQueue(queueName, metadata1).block()
        when:
        def createAnotherQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueue(queueName, metadata2))
        then:
        createAnotherQueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 409, StorageErrorCode.QUEUE_ALREADY_EXISTS)
        }
    }

    def "Delete queue from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        primaryQueueServiceAsyncClient.createQueue(queueName).block()
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueue(queueName))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessage("Expecting exception as queue has been deleted."))
        then:
        deleteQueueVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 204)
        }.verifyComplete()
        enqueueMessageVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Delete not exist queue from queue service async client"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueue(testResourceName.randomName("queue", 16)))
        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "List queues from queue service async client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i)
            QueueItem queue = new QueueItem().name(queueName + version)
                .metadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceAsyncClient.createQueue(queue.name(), queue.metadata()).block()
        }
        when:
        def queueListVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(new QueuesSegmentOptions().maxResults(2)))
        then:
        queueListVerifier.assertNext {
            QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.verifyComplete()
        where:
        options                                          | _
        new QueuesSegmentOptions().prefix("queue")       | _
        new QueuesSegmentOptions().maxResults(2)         | _
        new QueuesSegmentOptions().includeMetadata(true) | _
    }

    def "List empty queues from queue service async client"() {
        when:
        def listQueueVerifier = StepVerifier.create((primaryQueueServiceAsyncClient.listQueues(new QueuesSegmentOptions())))
        then:
        listQueueVerifier.assertNext {
            assertFalse(it.iterator().hasNext())
        }
    }

    def "Get and set properties from queue service async client"() {
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
            QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, it.value())
        }
        setPropertiesVerifier.assertNext {
            QueueTestHelper.assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, it.value())
        }.verifyComplete()
    }
}
