// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.storage.queue.models.Logging;
import com.azure.storage.queue.models.Metrics;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.RetentionPolicy;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QueueServiceAsyncClientTests extends QueueServiceClientTestsBase {
    private QueueServiceAsyncClient serviceClient;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = setupClient((connectionString, endpoint) -> QueueServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            serviceClient = setupClient((connectionString, endpoint) -> QueueServiceAsyncClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .build());
        }
    }

    @Override
    protected void afterTest() {
        serviceClient.listQueuesSegment()
            .collectList()
            .block()
            .forEach(queue -> {
                QueueAsyncClient client = serviceClient.getQueueAsyncClient(queue.name());
                try {
                    client.clearMessages().then(client.delete()).block();
                } catch (StorageErrorException ex) {
                    // Queue already delete, that's what we wanted anyways.
                }
            });
    }

    @Override
    public void getQueueDoesNotCreateAQueue() {
        StepVerifier.create(serviceClient.getQueueAsyncClient(queueName).enqueueMessage("Expecting an exception"))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void createQueue() {
        StepVerifier.create(serviceClient.createQueue(queueName).enqueueMessage("Testing service client creating a queue"))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createQueueWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");
        QueueAsyncClient client = serviceClient.createQueue(queueName, metadata);

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                TestHelpers.assertResponseStatusCode(response, 200);
                assertEquals(metadata, response.value().metadata());
            })
            .verifyComplete();
    }

    @Override
    public void createQueueTwiceSameMetadata() {
        final String messageText = "Testing service client creating the same queue twice does not modify the queue";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        StepVerifier.create(serviceClient.createQueue(queueName, metadata).enqueueMessage(messageText))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(serviceClient.createQueue(queueName, metadata).peekMessages())
            .assertNext(response -> assertEquals(messageText, response.messageText()))
            .verifyComplete();
    }

    @Override
    public void createQueueTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        try {
            serviceClient.createQueue(queueName);
            serviceClient.createQueue(queueName, metadata);
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void deleteExistingQueue() {
        QueueAsyncClient client = serviceClient.createQueue(queueName);
        serviceClient.deleteQueue(queueName);

        StepVerifier.create(client.enqueueMessage("Expecting an exception"))
            .verifyErrorSatisfies(throwable -> TestHelpers.assertExceptionStatusCode(throwable, 404));
    }

    @Override
    public void deleteNonExistentQueue() {
        try {
            serviceClient.deleteQueue(queueName);
        } catch (Exception exception) {
            TestHelpers.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void listQueues() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata());
        }

        StepVerifier.create(serviceClient.listQueuesSegment(defaultSegmentOptions()))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .verifyComplete();
    }

    @Override
    public void listQueuesIncludeMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            if (i % 2 == 0) {
                queue.metadata(metadata);
            }

            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata());
        }

        StepVerifier.create(serviceClient.listQueuesSegment(defaultSegmentOptions().includeMetadata(true)))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .verifyComplete();
    }

    @Override
    public void listQueuesWithPrefix() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem();
            if (i % 2 == 0) {
                queue.name(queueName + "prefix" + i);
                testQueues.add(queue);
            } else {
                queue.name(queueName + i);
            }

            serviceClient.createQueue(queue.name(), queue.metadata());
        }

        StepVerifier.create(serviceClient.listQueuesSegment(defaultSegmentOptions().prefix(queueName + "prefix")))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .verifyComplete();
    }

    @Override
    public void listQueuesWithLimit() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata());
        }

        StepVerifier.create(serviceClient.listQueuesSegment(defaultSegmentOptions().maxResults(2)))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .assertNext(queue -> TestHelpers.assertQueuesAreEqual(testQueues.pop(), queue))
            .verifyComplete();
    }

    @Override
    public void setProperties() {
        StorageServiceProperties originalProperties = serviceClient.getProperties().block().value();

        RetentionPolicy retentionPolicy = new RetentionPolicy().enabled(true)
            .days(3);

        Logging logging = new Logging().version("1.0")
            .delete(true)
            .write(true)
            .retentionPolicy(retentionPolicy);

        Metrics metrics = new Metrics().enabled(true)
            .includeAPIs(false)
            .retentionPolicy(retentionPolicy)
            .version("1.0");

        StorageServiceProperties updatedProperties = new StorageServiceProperties().logging(logging)
            .hourMetrics(metrics)
            .minuteMetrics(metrics)
            .cors(new ArrayList<>());

        StepVerifier.create(serviceClient.setProperties(updatedProperties))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(serviceClient.getProperties())
            .assertNext(response -> TestHelpers.assertQueueServicePropertiesAreEqual(updatedProperties, response.value()))
            .verifyComplete();

        StepVerifier.create(serviceClient.setProperties(originalProperties))
            .assertNext(response -> TestHelpers.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(serviceClient.getProperties())
            .assertNext(response -> TestHelpers.assertQueueServicePropertiesAreEqual(originalProperties, response.value()))
            .verifyComplete();
    }
}
