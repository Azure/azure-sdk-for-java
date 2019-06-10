// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.function.BiFunction;

import static org.junit.Assert.fail;

public abstract class QueueServiceClientTestsBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(QueueServiceClientTestsBase.class);

    final String azureStorageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";
    final String azureStorageQueueEndpoint = "AZURE_STORAGE_QUEUE_ENDPOINT";

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
            fail(String.format("%s and %s must be set to build the testing client", azureStorageConnectionString, azureStorageQueueEndpoint));
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
    public abstract void setProperties();

    // TODO (alzimmer): determine how, or if, to test getting statistics
}
