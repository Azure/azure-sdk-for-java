// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.function.BiFunction;

import static org.junit.Assert.fail;

public abstract class QueueServiceClientTestsBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(QueueServiceClientTestsBase.class);
    private final String azureStorageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";
    private final String azureStorageQueueEndpoint = "AZURE_STORAGE_QUEUE_ENDPOINT";

    String queueName;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    <T> T setupClient(BiFunction<String, String, T> clientBuilder) {
        String connectionString = ConfigurationManager.getConfiguration().get(azureStorageConnectionString);
        String queueEndpoint = ConfigurationManager.getConfiguration().get(azureStorageQueueEndpoint);

        if (ImplUtils.isNullOrEmpty(connectionString) || ImplUtils.isNullOrEmpty(queueEndpoint)) {
            logger.asWarning().log("{} and {} must be set to build the testing client", azureStorageConnectionString, azureStorageQueueEndpoint);
            fail();
            return null;
        }

        return clientBuilder.apply(connectionString, queueEndpoint);
    }

    String getQueueName() {
        return testResourceNamer.randomName("queue", 16).toLowerCase();
    }

    @Test
    public abstract void getQueueDoesNotCreateAQueue();

    @Test
    public abstract void createQueue();

    @Test
    public abstract void createQueueWithMetadata();

    @Test
    public abstract void createQueueTwiceSameMetadata();

    @Test
    public abstract void createQueueTwiceDifferentMetadata();

    @Test
    public abstract void deleteExistingQueue();

    @Test
    public abstract void deleteNonExistentQueue();

    @Test
    public abstract void listQueues();

    @Test
    public abstract void listQueuesIncludeMetadata();

    @Test
    public abstract void listQueuesWithPrefix();

    @Test
    public abstract void listQueuesWithLimit();

    @Test
    public abstract void setProperties();

    QueuesSegmentOptions defaultSegmentOptions() {
        return new QueuesSegmentOptions().prefix(queueName);
    }
}
