// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.test.TestMode
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.storage.common.TestBase
import com.azure.storage.queue.models.QueuesSegmentOptions

class APISpec extends TestBase {
    // Clients for API tests
    QueueServiceClient primaryQueueServiceClient
    QueueServiceAsyncClient primaryQueueServiceAsyncClient

    String connectionString

    /**
     * Setup the QueueServiceClient and QueueClient common used for the API tests.
     */
    def setup() {
        if (getTestMode() == TestMode.RECORD) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_QUEUE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;" +
                "EndpointSuffix=core.windows.net"
        }
    }

    /**
     * Clean up the test queues and messages for the account.
     */
    def cleanup() {
        if (getTestMode() == TestMode.RECORD) {
            QueueServiceClient cleanupQueueServiceClient = new QueueServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()

            cleanupQueueServiceClient.listQueues(new QueuesSegmentOptions().prefix(getTestName())).each {
                queueItem -> cleanupQueueServiceClient.deleteQueue(queueItem.name())
            }
        }
    }

    def setupQueueServiceClientBuilder() {
        return setupBuilder(new QueueServiceClientBuilder(), connectionString)
    }

    def setupQueueClientBuilder() {
        return setupBuilder(new QueueClientBuilder(), connectionString)
            .queueName(generateResourceName("queue", 16))
    }

    def generateResourceName() {
        return generateResourceName(getTestName(), 60)
    }
}
