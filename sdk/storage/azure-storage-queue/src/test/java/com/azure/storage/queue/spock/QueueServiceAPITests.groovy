// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

import com.azure.storage.queue.models.*
import org.junit.Assert
import spock.lang.Unroll

class QueueServiceAPITests extends APISpec {

    def "Get queue client from queue service client"() {
        when:
        def queueClient = primaryQueueServiceClient.getQueueClient(testResourceName.randomName("queue", 16))
        queueClient.enqueueMessage("Expecting an exception")
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, "QueueNotFound")
    }

    def "Create queue from queue service client"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueue(testResourceName.randomName("queue", 16))
        def enqueueMessageResponse = queueClientResponse.value().enqueueMessage("Testing service client creating a queue")
        then:
        QueueTestHelper.assertResponseStatusCode(queueClientResponse, 201)
        QueueTestHelper.assertResponseStatusCode(enqueueMessageResponse, 201)
    }

    def "Create queues with duplicate name from queue service client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        primaryQueueServiceClient.createQueue(queueName)
        def queueClientResponse = primaryQueueServiceClient.createQueue(queueName)
        then:
        QueueTestHelper.assertResponseStatusCode(queueClientResponse, 204)
    }

    @Unroll
    def "Create queue with invalid name from queue service client"() {
        when:
        primaryQueueServiceClient.createQueue(queueName)
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMesage)
        where:
        queueName      | statusCode | errMesage
        "a_b"          | 400        | "InvalidResourceName"
        "-ab"          | 400        | "InvalidResourceName"
        "a--b"         | 400        | "InvalidResourceName"
        // null | 400 | "InvalidResourceName" TODO: Need to fix the RestProxy before having null parameter
        "Abc"          | 400        | "InvalidResourceName"
        "ab"           | 400        | "OutOfRangeInput"
        "verylong" * 8 | 400        | "OutOfRangeInput"
    }

    @Unroll
    def "Create queue maxOverload from queue service client"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueue(testResourceName.randomName("queue", 16), metadata)
        def enqueueMessageResponse = queueClientResponse.value().enqueueMessage("Testing service client creating a queue")
        then:
        QueueTestHelper.assertResponseStatusCode(queueClientResponse, 201)
        QueueTestHelper.assertResponseStatusCode(enqueueMessageResponse, 201)
        where:
        metadata                                       | _
        null                                           | _
        Collections.singletonMap("metadata", "value")  | _
        Collections.singletonMap("metadata", "va@lue") | _
    }

    def "Create queue with invalid metadata from queue service client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        primaryQueueServiceClient.createQueue(queueName, Collections.singletonMap("meta@data", "value"))
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, "Bad Request")
    }

    def "Create queues with same name and different metadata from queue service client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        def metadata1 = new HashMap<>()
        metadata1.put("metadata1", "value1")
        def metadata2 = new HashMap<>()
        metadata2.put("metadata2", "value2")
        when:
        primaryQueueServiceClient.createQueue(queueName, metadata1)
        primaryQueueServiceClient.createQueue(queueName, metadata2)
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 409, "QueueAlreadyExists")
    }

    def "Delete queue from queue service client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        when:
        def queueClient = primaryQueueServiceClient.createQueue(queueName).value()
        def deleteQueueResponse = primaryQueueServiceClient.deleteQueue(queueName)
        queueClient.enqueueMessage("Expecting exception as queue has been deleted.")
        then:
        QueueTestHelper.assertResponseStatusCode(deleteQueueResponse, 204)
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, "QueueNotFound")
    }

    def "Delete not exist queue from queue service client"() {
        when:
        primaryQueueServiceClient.deleteQueue(testResourceName.randomName("queue", 16))
        then:
        def e = thrown(StorageErrorException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, "QueueNotFound")
    }

    @Unroll
    def "List queues from queue service client"() {
        given:
        def queueName = testResourceName.randomName("queue", 16)
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            String version = Integer.toString(i)
            QueueItem queue = new QueueItem().name(queueName + version)
                .metadata(Collections.singletonMap("metadata" + version, "value" + version))
            testQueues.add(queue)
            primaryQueueServiceClient.createQueue(queue.name(), queue.metadata())
        }
        when:
        def queueListIter = primaryQueueServiceClient.listQueues(new QueuesSegmentOptions().maxResults(2))
        then:
        queueListIter.each {
            queueItem -> QueueTestHelper.assertQueuesAreEqual(queueItem, testQueues.pop())
        }
        where:
        options                                          | _
        new QueuesSegmentOptions().prefix("queue")       | _
        new QueuesSegmentOptions().maxResults(2)         | _
        new QueuesSegmentOptions().includeMetadata(true) | _
    }

    def "List empty queues from queue service client"() {
        when:
        primaryQueueServiceClient.getQueueClient(testResourceName.randomName("queue", 16))
        then:
        Assert.assertFalse(primaryQueueServiceClient.listQueues(new QueuesSegmentOptions()).iterator().hasNext())
    }

    def "Get and set properties from queue service client"() {
        given:
        def originalProperties = primaryQueueServiceClient.getProperties().value()
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
        def setResponse = primaryQueueServiceClient.setProperties(updatedProperties)
        def getResponseAfter = primaryQueueServiceClient.getProperties()
        then:
        QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, getResponseBefore.value())
        QueueTestHelper.assertResponseStatusCode(setResponse, 202)
        QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, getResponseAfter.value())
    }
}
