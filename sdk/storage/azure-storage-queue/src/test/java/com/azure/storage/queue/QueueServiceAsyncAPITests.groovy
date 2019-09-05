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
        primaryQueueServiceAsyncClient = setupQueueServiceClientBuilder().buildAsyncClient()
    }

    def "Get queue client"() {
        given:
        def queueAsyncClient = primaryQueueServiceAsyncClient.getQueueAsyncClient(generateResourceName())
        expect:
        queueAsyncClient instanceof QueueAsyncClient
    }

    def "Create queue"() {
        given:
        def queueName = generateResourceName()

        expect:
        StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, null)).assertNext {
            assert assertResponseStatusCode(it, 201)
        }.verifyComplete()
        StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName).enqueueMessageWithResponse("Testing service client creating a queue", null, null))
            .assertNext {
                assert assertResponseStatusCode(it, 201)
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
        def queueName = generateResourceName()

        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, metadata))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessageWithResponse("Testing service client creating a queue", null, null))

        then:
        createQueueVerifier.assertNext {
            assert assertResponseStatusCode(it, 201)
        }.verifyComplete()
        enqueueMessageVerifier.assertNext {
            assert assertResponseStatusCode(it, 201)
        }.verifyComplete()


        where:
        metadata                                       | _
        null                                           | _
        Collections.singletonMap("metadata", "value")  | _
        Collections.singletonMap("metadata", "va@lue") | _
    }

    def "Create queue with invalid metadata"() {
        given:
        def queueName = generateResourceName()

        when:
        def createQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.createQueueWithResponse(queueName, Collections.singletonMap("metadata!", "value")))

        then:
        createQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.INVALID_METADATA)
        }
    }

    def "Delete queue"() {
        given:
        def queueName = generateResourceName()
        primaryQueueServiceAsyncClient.createQueue(queueName).block()

        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(queueName))
        def enqueueMessageVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getQueueAsyncClient(queueName)
            .enqueueMessageWithResponse("Expecting exception as queue has been deleted.", null, null))

        then:
        deleteQueueVerifier.assertNext {
            assert assertResponseStatusCode(it, 204)
        }.verifyComplete()
        enqueueMessageVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    def "Delete queue error"() {
        when:
        def deleteQueueVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.deleteQueueWithResponse(generateResourceName()))

        then:
        deleteQueueVerifier.verifyErrorSatisfies {
            assert QueueTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.QUEUE_NOT_FOUND)
        }
    }

    @Unroll
    def "List queues"() {
        given:
        def queueName = generateResourceName()
        LinkedList<QueueItem> testQueues = new LinkedList<>()
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i)
            QueueItem queue = new QueueItem().name(queueName + version)
                .metadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceAsyncClient.createQueueWithResponse(queue.name(), queue.metadata()).block()
        }

        when:
        def queueListVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.listQueues(options.prefix(getTestName())))
        then:
        queueListVerifier.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.assertNext {
            assert QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
        }.verifyComplete()

        where:
        options                                          | _
        new QueuesSegmentOptions()                       | _
        new QueuesSegmentOptions().maxResults(2)         | _
        new QueuesSegmentOptions().includeMetadata(true) | _
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
        def setPropertiesVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.setPropertiesWithResponse(updatedProperties))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryQueueServiceAsyncClient.getProperties())

        then:
        getPropertiesBeforeVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, it)
        }
        setPropertiesVerifier.assertNext {
            assert assertResponseStatusCode(it, 202)
        }.verifyComplete()

        getPropertiesAfterVerifier.assertNext {
            assert QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, it)
        }.verifyComplete()
    }
}
