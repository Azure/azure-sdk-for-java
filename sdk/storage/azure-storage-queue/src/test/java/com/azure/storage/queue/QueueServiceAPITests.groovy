// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.queue.models.QueueAnalyticsLogging
import com.azure.storage.queue.models.QueueErrorCode
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueueMetrics
import com.azure.storage.queue.models.QueueRetentionPolicy
import com.azure.storage.queue.models.QueueServiceProperties
import com.azure.storage.queue.models.QueuesSegmentOptions
import com.azure.storage.queue.models.QueueStorageException
import spock.lang.Unroll

class QueueServiceAPITests extends APISpec {

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper(interceptorManager).buildClient()
    }

    def "Get queue client"() {
        given:
        def queueClient = primaryQueueServiceClient.getQueueClient(testResourceName.randomName(methodName, 60))

        expect:
        queueClient instanceof QueueClient
    }

    def "Create queue"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueueWithResponse(testResourceName.randomName(methodName, 60),  null, null, null)
        def enqueueMessageResponse = queueClientResponse.getValue().sendMessageWithResponse("Testing service client creating a queue", null, null, null,null)

        then:
        QueueTestHelper.assertResponseStatusCode(queueClientResponse, 201)
        QueueTestHelper.assertResponseStatusCode(enqueueMessageResponse, 201)
    }

    @Unroll
    def "Create queue with invalid name"() {
        when:
        primaryQueueServiceClient.createQueue(queueName)

        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMesage)

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
        primaryQueueServiceClient.createQueue(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "Create queue maxOverload"() {
        when:
        def queueClientResponse = primaryQueueServiceClient.createQueueWithResponse(testResourceName.randomName(methodName, 60), metadata,null, null)
        def enqueueMessageResponse = queueClientResponse.getValue().sendMessageWithResponse("Testing service client creating a queue", null, null, null, null)

        then:
        QueueTestHelper.assertResponseStatusCode(queueClientResponse, 201)
        QueueTestHelper.assertResponseStatusCode(enqueueMessageResponse, 201)

        where:
        metadata                                       | _
        null                                           | _
        Collections.singletonMap("metadata", "value")  | _
        Collections.singletonMap("metadata", "va@lue") | _
    }

    def "Create queue with invalid metadata"() {
        given:
        def queueName = testResourceName.randomName(methodName, 16)

        when:
        primaryQueueServiceClient.createQueueWithResponse(queueName, Collections.singletonMap("metadata!", "value"), null, null)

        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 400, QueueErrorCode.INVALID_METADATA)
    }

    def "Delete queue"() {
        given:
        def queueName = testResourceName.randomName(methodName, 60)

        when:
        def queueClient = primaryQueueServiceClient.createQueue(queueName)
        def deleteQueueResponse = primaryQueueServiceClient.deleteQueueWithResponse(queueName, null, null)
        queueClient.sendMessage("Expecting exception as queue has been deleted.")

        then:
        QueueTestHelper.assertResponseStatusCode(deleteQueueResponse, 204)
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
    }

    def "Delete queue error"() {
        when:
        primaryQueueServiceClient.deleteQueueWithResponse(testResourceName.randomName(methodName, 60), null, null)

        then:
        def e = thrown(QueueStorageException)
        QueueTestHelper.assertExceptionStatusCodeAndMessage(e, 404, QueueErrorCode.QUEUE_NOT_FOUND)
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
            primaryQueueServiceClient.createQueueWithResponse(queue.getName(), queue.getMetadata(), null, null)
        }

        when:
        def queueListIter = primaryQueueServiceClient.listQueues(options, null, null)
        then:
        queueListIter.each {
            QueueTestHelper.assertQueuesAreEqual(it, testQueues.pop())
            primaryQueueServiceClient.deleteQueue(it.getName())
        }
        testQueues.size() == 0

        where:
        options                                                                                         | _
        new QueuesSegmentOptions().setPrefix("queueserviceapitestslistqueues")                          | _
        new QueuesSegmentOptions().setPrefix("queueserviceapitestslistqueues").setMaxResultsPerPage(2)  | _
        new QueuesSegmentOptions().setPrefix("queueserviceapitestslistqueues").setIncludeMetadata(true) | _
    }

    def "List empty queues"() {
        when:
        primaryQueueServiceClient.getQueueClient(testResourceName.randomName(methodName, 60))

        then:
        !primaryQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(methodName), null, null).iterator().hasNext()
    }

    def "Get and set properties"() {
        given:
        def originalProperties = primaryQueueServiceClient.getProperties()
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
        def getResponseBefore = primaryQueueServiceClient.getProperties()
        def setResponse = primaryQueueServiceClient.setPropertiesWithResponse(updatedProperties, null, null)
        def getResponseAfter = primaryQueueServiceClient.getProperties()

        then:
        QueueTestHelper.assertQueueServicePropertiesAreEqual(originalProperties, getResponseBefore)
        QueueTestHelper.assertResponseStatusCode(setResponse, 202)
        QueueTestHelper.assertQueueServicePropertiesAreEqual(updatedProperties, getResponseAfter)
    }


    def "Builder bearer token validation"() {
        setup:
        URL url = new URL(primaryQueueServiceClient.getQueueServiceUrl())
        String endpoint = new URL("http", url.getHost(), url.getPort(), url.getFile()).toString()
        def builder = new QueueServiceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }
}
