// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.Logging;
import com.azure.storage.queue.models.Metrics;
import com.azure.storage.queue.models.PeekedMessage;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.RetentionPolicy;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueueServiceClientTests extends QueueServiceClientTestsBase {
    private QueueServiceClient serviceClient;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = setupClient((connectionString, endpoint) -> QueueServiceClient.builder()
                .connectionString(connectionString)
                .endpoint(endpoint)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            serviceClient = setupClient((connectionString, endpoint) -> QueueServiceClient.builder()
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
        serviceClient.listQueuesSegment(new QueuesSegmentOptions().prefix(queueName))
            .forEach(queueToDelete -> {
                try {
                    QueueClient client = serviceClient.getQueueClient(queueToDelete.name());
                    client.clearMessages();
                    client.delete();
                } catch (StorageErrorException ex) {
                    // Queue already delete, that's what we wanted anyways.
                }
            });
    }

    @Override
    public void getQueueDoesNotCreateAQueue() {
        try {
            serviceClient.getQueueClient(queueName).enqueueMessage("Expecting an exception");
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof StorageErrorException);
        }
    }

    @Override
    public void createQueue() {
        QueueClient client = serviceClient.createQueue(queueName);
        EnqueuedMessage message = client.enqueueMessage("Testing service client creating a queue").value();
        assertNotNull(message);
    }

    @Override
    public void createQueueWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");
        QueueClient client = serviceClient.createQueue(queueName, metadata);

        QueueProperties properties = client.getProperties().value();
        assertEquals(metadata.size(), properties.metadata().size());
        assertEquals(metadata, properties.metadata());
    }

    @Override
    public void createQueueTwiceSameMetadata() {
        final String messageText = "Testing service client creating the same queue twice does not modify the queue";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        EnqueuedMessage enqueuedMessage = serviceClient.createQueue(queueName, metadata).enqueueMessage(messageText).value();
        assertNotNull(enqueuedMessage);

        PeekedMessage peekedMessage = serviceClient.createQueue(queueName, metadata).peekMessages().iterator().next();
        assertEquals(messageText, peekedMessage.messageText());
    }

    @Override
    public void createQueueTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        try {
            serviceClient.createQueue(queueName);
            serviceClient.createQueue(queueName, metadata);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof StorageErrorException);
        }
    }

    @Override
    public void deleteExistingQueue() {
        QueueClient client = serviceClient.createQueue(queueName);
        serviceClient.deleteQueue(queueName);

        try {
            client.enqueueMessage("Expecting an exception");
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof StorageErrorException);
        }
    }

    @Override
    public void deleteNonExistentQueue() {
        try {
            serviceClient.deleteQueue(queueName);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof StorageErrorException);
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

        for (QueueItem queue : serviceClient.listQueuesSegment(defaultSegmentOptions())) {
            assertQueuesAreEqual(testQueues.pop(), queue);
        }
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

        for (QueueItem queue : serviceClient.listQueuesSegment(defaultSegmentOptions().includeMetadata(true))) {
            assertQueuesAreEqual(testQueues.pop(), queue);
        }
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

        for (QueueItem queue : serviceClient.listQueuesSegment(defaultSegmentOptions().prefix(queueName + "prefix"))) {
            assertQueuesAreEqual(testQueues.pop(), queue);
        }
    }

    @Override
    public void listQueuesWithLimit() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueue(queue.name(), queue.metadata());
        }

        for (QueueItem queue : serviceClient.listQueuesSegment(defaultSegmentOptions().maxResults(2))) {
            assertQueuesAreEqual(testQueues.pop(), queue);
        }
    }

    @Override
    public void setProperties() {
        StorageServiceProperties originalProperties = serviceClient.getProperties().value();

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

        VoidResponse setResponse = serviceClient.setProperties(updatedProperties);
        assertNull(setResponse.value());

        Response<StorageServiceProperties> getResponse = serviceClient.getProperties();
        assertQueueServicePropertiesAreEqual(updatedProperties, getResponse.value());

        setResponse = serviceClient.setProperties(originalProperties);
        assertNull(setResponse.value());

        getResponse = serviceClient.getProperties();
        assertQueueServicePropertiesAreEqual(originalProperties, getResponse.value());
    }
}
