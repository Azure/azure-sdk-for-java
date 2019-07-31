// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.queue.models.Logging;
import com.azure.storage.queue.models.Metrics;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
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
    private final ClientLogger logger = new ClientLogger(QueueServiceAsyncClientTests.class);

    private QueueServiceAsyncClient serviceClient;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = helper.setupClient((connectionString, endpoint) -> new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildAsyncClient(), true, logger);
        } else {
            serviceClient = helper.setupClient((connectionString, endpoint) -> new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildAsyncClient(), false, logger);
        }
    }

    @Override
    protected void afterTest() {
        serviceClient.listQueues(new QueuesSegmentOptions().prefix(queueName))
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
        StepVerifier.create(serviceClient.getQueueAsyncClient(queueName).enqueueMessage("Expecting an exception"));
    }

    @Override
    public void createQueue() {
        StepVerifier.create(serviceClient.createQueue(queueName).block().value().enqueueMessage("Testing service client creating a queue"))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();
    }

    @Override
    public void createQueueWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");
        QueueAsyncClient client = serviceClient.createQueue(queueName, metadata).block().value();

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
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

        StepVerifier.create(serviceClient.createQueue(queueName, metadata).block().value().enqueueMessage(messageText))
            .assertNext(response -> helper.assertResponseStatusCode(response, 201))
            .verifyComplete();

        StepVerifier.create(serviceClient.createQueue(queueName, metadata).block().value().peekMessages())
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
        }
    }

    @Override
    public void deleteExistingQueue() {
        QueueAsyncClient client = serviceClient.createQueue(queueName).block().value();
        serviceClient.deleteQueue(queueName).block();

        StepVerifier.create(client.enqueueMessage("Expecting an exception"));
    }

    @Override
    public void deleteNonExistentQueue() {
        try {
            serviceClient.deleteQueue(queueName);
        } catch (Exception exception) {
        }
    }

    @Override
    public void listQueues() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata()).block();
        }

        StepVerifier.create(serviceClient.listQueues(defaultSegmentOptions()))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
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
            serviceClient.createQueue(queue.name(), queue.metadata()).block();
        }

        StepVerifier.create(serviceClient.listQueues(defaultSegmentOptions().includeMetadata(true)))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
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

            serviceClient.createQueue(queue.name(), queue.metadata()).block();
        }

        StepVerifier.create(serviceClient.listQueues(defaultSegmentOptions().prefix(queueName + "prefix")))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .assertNext(result -> helper.assertQueuesAreEqual(testQueues.pop(), result))
            .verifyComplete();
    }

    @Override
    public void listQueuesWithLimit() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata()).block();
        }

        StepVerifier.create(serviceClient.listQueues(defaultSegmentOptions().maxResults(2)))
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
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(serviceClient.getProperties())
            .assertNext(response -> helper.assertQueueServicePropertiesAreEqual(updatedProperties, response.value()))
            .verifyComplete();

        StepVerifier.create(serviceClient.setProperties(originalProperties))
            .assertNext(response -> helper.assertResponseStatusCode(response, 202))
            .verifyComplete();

        StepVerifier.create(serviceClient.getProperties())
            .assertNext(response -> helper.assertQueueServicePropertiesAreEqual(originalProperties, response.value()))
            .verifyComplete();
    }
}
