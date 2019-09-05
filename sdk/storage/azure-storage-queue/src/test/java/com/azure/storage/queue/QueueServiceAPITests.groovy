// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue


import com.azure.storage.queue.models.Logging
import com.azure.storage.queue.models.Metrics
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueuesSegmentOptions
import com.azure.storage.queue.models.RetentionPolicy
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageException
import com.azure.storage.queue.models.StorageServiceProperties
import spock.lang.Unroll

class QueueServiceAPITests extends APISpec {

    def setup() {
        primaryQueueServiceClient = setupQueueServiceClientBuilder().buildClient()
    }

    def "Get queue client"() {
        given:
        def queueClient = primaryQueueServiceClient.getQueueClient(generateResourceName())

        expect:
        queueClient instanceof QueueClient
    }

    def "Create queue"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueueWithResponse(generateResourceName(),  null, null)
        def enqueueMessageResponse = queueClientResponse.value().enqueueMessageWithResponse("Testing service client creating a queue", null, null, null)

        then:
        assertResponseStatusCode(queueClientResponse, 201)
        assertResponseStatusCode(enqueueMessageResponse, 201)
    }

    @Unroll
    def "Create queue with invalid name"() {
        when:
        primaryQueueServiceClient.createQueue(queueName)

        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMesage)

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
        primaryQueueServiceClient.createQueue(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Create queue maxOverload"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueueWithResponse(generateResourceName(), metadata, null)
        def enqueueMessageResponse = queueClientResponse.value().enqueueMessageWithResponse("Testing service client creating a queue", null, null, null)

        then:
        assertResponseStatusCode(queueClientResponse, 201)
        assertResponseStatusCode(enqueueMessageResponse, 201)

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
        primaryQueueServiceClient.createQueueWithResponse(queueName, Collections.singletonMap("metadata!", "value"), null)

        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_METADATA)
    }

    def "Delete queue"() {
        given:
        def queueName = generateResourceName()

        when:
        def queueClient = primaryQueueServiceClient.createQueue(queueName)
        def deleteQueueResponse = primaryQueueServiceClient.deleteQueueWithResponse(queueName, null)
        queueClient.enqueueMessage("Expecting exception as queue has been deleted.")

        then:
        assertResponseStatusCode(deleteQueueResponse, 204)
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
    }

    def "Delete queue error"() {
        when:
        primaryQueueServiceClient.deleteQueueWithResponse(generateResourceName(), null)

        then:
        def e = thrown(StorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.QUEUE_NOT_FOUND)
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
            primaryQueueServiceClient.createQueueWithResponse(queue.name(), queue.metadata(), null)
        }

        when:
        def queueListIter = primaryQueueServiceClient.listQueues(options.prefix(getTestName()))

        then:
        queueListIter.each {
            QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
            primaryQueueServiceClient.deleteQueue(it.name())
        }
        testQueues.size() == 0

        where:
        options                                          | _
        new QueuesSegmentOptions()                       | _
        new QueuesSegmentOptions().maxResults(2)         | _
        new QueuesSegmentOptions().includeMetadata(true) | _
    }

    def "List empty queues"() {
        when:
        primaryQueueServiceClient.getQueueClient(generateResourceName())

        then:
        !primaryQueueServiceClient.listQueues(new QueuesSegmentOptions().prefix(getTestName())).iterator().hasNext()
    }

    def "Get and set properties"() {
        given:
        def originalProperties = primaryQueueServiceClient.getProperties()
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
        def getResponseBefore = primaryQueueServiceClient.getProperties()
        def setResponse = primaryQueueServiceClient.setPropertiesWithResponse(updatedProperties, null)
        def getResponseAfter = primaryQueueServiceClient.getProperties()
        then:
        QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, getResponseBefore)
        assertResponseStatusCode(setResponse, 202)
        QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, getResponseAfter)
    }
}
