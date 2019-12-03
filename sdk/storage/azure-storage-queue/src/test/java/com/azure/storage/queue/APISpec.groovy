// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue


import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.util.Configuration
import com.azure.core.util.Context
import com.azure.storage.common.StorageTestBase
import com.azure.storage.queue.models.QueuesSegmentOptions

import java.time.Duration

class APISpec extends StorageTestBase {
    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    String connectionString

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        connectionString = (testMode == TestMode.PLAYBACK)
            ? "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        interceptorManager.close()

        if (testMode == TestMode.PLAYBACK) {
            return
        }

        QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
        cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().setPrefix(testName.toLowerCase()),
            Duration.ofSeconds(30), Context.NONE).each {
            queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.getName())
        }
    }

    def queueServiceBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def queueBuilderHelper(final InterceptorManager interceptorManager) {
        def queueName = generateResourceName("queue", 16)
        if (testMode == TestMode.RECORD) {
            return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(getHttpClient())
        } else {
            return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    String generateRandomName(int length) {
        return generateResourceName(testName, length)
    }
}
