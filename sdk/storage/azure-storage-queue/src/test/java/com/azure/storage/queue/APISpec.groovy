// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.StorageTestBase
import com.azure.storage.queue.models.QueuesSegmentOptions

import java.time.Duration

class APISpec extends StorageTestBase {
    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    private static String PRIMARY_STORAGE = "AZURE_STORAGE_QUEUE_"
    StorageSharedKeyCredential primaryCredential
    String connectionString

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        primaryCredential = getCredential(PRIMARY_STORAGE)
        connectionString = isPlaybackMode()
            ? "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        QueueServiceClient cleanupQueueServiceClient = queueServiceBuilderHelper().buildClient()
        cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(testName.toLowerCase()),
            Duration.ofSeconds(30), Context.NONE).each {
            queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
        }
    }

    def queueServiceBuilderHelper() {
        def builder = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    def sasQueueServiceBuilderHelper(String endpointWithSas) {
        def builder = new QueueServiceClientBuilder()
            .endpoint(endpointWithSas)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    def queueBuilderHelper() {
        def builder = new QueueClientBuilder()
            .connectionString(connectionString)
            .queueName(generateResourceName("queue", 16))
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    def sasQueueBuilderHelper(String endpointWithSas) {
        def builder = new QueueClientBuilder()
            .endpoint(endpointWithSas)
            .httpClient(getHttpClient())

        if (isRecordMode()) {
            builder.addPolicy(getRecordPolicy())
        }

        return builder
    }

    String generateRandomName(int length) {
        return generateResourceName(testName, length)
    }
}
