// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.storage.queue.models.StorageErrorException;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueueServiceAsyncClientTests extends QueueServiceClientTestsBase {
    QueueServiceAsyncClient serviceClient;
    String queueName;

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
        try {
            serviceClient.deleteQueue(queueName);
        } catch (StorageErrorException ex) {
            // Queue already delete, that's what we wanted anyways.
        }
    }

    @Override
    public void getQueueDoesNotCreateAQueue() {
        StepVerifier.create(serviceClient.getQueueAsyncClient(queueName).enqueueMessage("Expecting an exception"))
            .verifyErrorSatisfies(throwable -> assertTrue(throwable instanceof StorageErrorException));
    }

    @Override
    public void createQueue() {
        QueueAsyncClient client = serviceClient.createQueue(queueName);
        client.enqueueMessage("Testing service client creating a queue");
    }

    @Override
    public void createQueueWithMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("metadata1", "value1");
        metadata.put("metadata2", "value2");
        QueueAsyncClient client = serviceClient.createQueue(queueName, metadata);

        StepVerifier.create(client.getProperties())
            .assertNext(response -> {
                assertEquals(metadata.size(), response.value().metadata().size());
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
            .assertNext(response -> assertNotNull(response.value()))
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
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof StorageErrorException);
        }
    }

    @Override
    public void deleteExistingQueue() {

    }

    @Override
    public void deleteNonExistentQueue() {

    }

    @Override
    public void listQueues() {

    }

    @Override
    public void setProperties() {

    }
}
