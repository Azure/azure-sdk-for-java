// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.logging.ClientLogger;
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
import static org.junit.Assert.fail;

public class QueueServiceClientTests extends QueueServiceClientTestsBase {
    private final ClientLogger logger = new ClientLogger(QueueServiceClientTests.class);

    private QueueServiceClient serviceClient;

    @Override
    protected void beforeTest() {
        queueName = getQueueName();
        helper = new TestHelpers();

        if (interceptorManager.isPlaybackMode()) {
            serviceClient = helper.setupClient((connectionString, endpoint) -> new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient(), true, logger);
        } else {
            serviceClient = helper.setupClient((connectionString, endpoint) -> new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .buildClient(), false, logger);
        }
    }

    @Override
    protected void afterTest() {
        serviceClient.listQueues(new QueuesSegmentOptions().prefix(queueName))
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
            fail("getQueueClient doesn't create a queue in Azure Storage.");
        } catch (Exception exception) {
            helper.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void createQueue() {
        QueueClient client = serviceClient.createQueue(queueName);
        Response<EnqueuedMessage> response = client.enqueueMessageWithResponse("Testing service client creating a queue", null, null, null);
        helper.assertResponseStatusCode(response, 201);
    }

    @Override
    public void createQueueWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");
        QueueClient client = serviceClient.createQueueWithResponse(queueName, metadata, null).value();

        Response<QueueProperties> propertiesResponse = client.getPropertiesWithResponse(null);
        helper.assertResponseStatusCode(propertiesResponse, 200);
        assertEquals(metadata, propertiesResponse.value().metadata());
    }

    @Override
    public void createQueueTwiceSameMetadata() {
        final String messageText = "Testing service client creating the same queue twice does not modify the queue";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        EnqueuedMessage enqueuedMessage = serviceClient.createQueueWithResponse(queueName, metadata, null).value().enqueueMessage(messageText);
        assertNotNull(enqueuedMessage);

        PeekedMessage peekedMessage = serviceClient.createQueueWithResponse(queueName, metadata, null).value().peekMessages().iterator().next();
        assertEquals(messageText, peekedMessage.messageText());
    }

    @Override
    public void createQueueTwiceDifferentMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");

        try {
            serviceClient.createQueue(queueName);
            serviceClient.createQueueWithResponse(queueName, metadata, null);
            fail("Creating a queue twice with different metadata should throw an exception.");
        } catch (Exception exception) {
            helper.assertExceptionStatusCode(exception, 409);
        }
    }

    @Override
    public void deleteExistingQueue() {
        QueueClient client = serviceClient.createQueue(queueName);
        serviceClient.deleteQueue(queueName);

        try {
            client.enqueueMessage("Expecting an exception");
            fail("Attempting to enqueue a message on a client that has been delete should throw an exception.");
        } catch (Exception exception) {
            helper.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void deleteNonExistentQueue() {
        try {
            serviceClient.deleteQueue(queueName);
            fail("Attempting to delete a queue that doesn't exist should throw an exception.");
        } catch (Exception exception) {
            helper.assertExceptionStatusCode(exception, 404);
        }
    }

    @Override
    public void listQueues() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueueWithResponse(queue.name(), queue.metadata(), null);
        }

        for (QueueItem queue : serviceClient.listQueues(defaultSegmentOptions())) {
            helper.assertQueuesAreEqual(testQueues.pop(), queue);
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
            serviceClient.createQueueWithResponse(queue.name(), queue.metadata(), null);
        }

        for (QueueItem queue : serviceClient.listQueues(defaultSegmentOptions().includeMetadata(true))) {
            helper.assertQueuesAreEqual(testQueues.pop(), queue);
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

            serviceClient.createQueueWithResponse(queue.name(), queue.metadata(), null);
        }

        for (QueueItem queue : serviceClient.listQueues(defaultSegmentOptions().prefix(queueName + "prefix"))) {
            helper.assertQueuesAreEqual(testQueues.pop(), queue);
        }
    }

    @Override
    public void listQueuesWithLimit() {
        LinkedList<QueueItem> testQueues = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            QueueItem queue = new QueueItem().name(queueName + i);
            testQueues.add(queue);
            serviceClient.createQueueWithResponse(queue.name(), queue.metadata(), null);
        }

        for (QueueItem queue : serviceClient.listQueues(defaultSegmentOptions().maxResults(2))) {
            helper.assertQueuesAreEqual(testQueues.pop(), queue);
        }
    }

    @Override
    public void setProperties() {
        StorageServiceProperties originalProperties = serviceClient.getProperties();

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

        VoidResponse setResponse = serviceClient.setPropertiesWithResponse(updatedProperties, null);
        helper.assertResponseStatusCode(setResponse, 202);

        Response<StorageServiceProperties> getResponse = serviceClient.getPropertiesWithResponse(null);
        helper.assertQueueServicePropertiesAreEqual(updatedProperties, getResponse.value());

        setResponse = serviceClient.setPropertiesWithResponse(originalProperties, null);
        helper.assertResponseStatusCode(setResponse, 202);

        getResponse = serviceClient.getPropertiesWithResponse(null);
        helper.assertQueueServicePropertiesAreEqual(originalProperties, getResponse.value());
    }
}
