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

public abstract class QueueClientTestsBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(QueueClientTestsBase.class);
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
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net";
        String queueEndpoint = "https://teststorage.queue.core.windows.net/";

        if (!interceptorManager.isPlaybackMode()) {
            connectionString = ConfigurationManager.getConfiguration().get(azureStorageConnectionString);
            queueEndpoint = ConfigurationManager.getConfiguration().get(azureStorageQueueEndpoint);
        }

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
    public abstract void createWithSharedKey();

    @Test
    public abstract void createWithSASToken();

    @Test
    public abstract void createWithMetadata();

    @Test
    public abstract void createTwiceSameMetadata();

    @Test
    public abstract void createTwiceDifferentMetadata();

    @Test
    public abstract void deleteExisting();

    @Test
    public abstract void deleteNonExistent();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void getPropertiesQueueDoesNotExist();

    @Test
    public abstract void setMetadata();

    @Test
    public abstract void setMetadataQueueDoesNotExist();

    @Test
    public abstract void setInvalidMetadata();

    @Test
    public abstract void deleteMetadata();

    @Test
    public abstract void getAccessPolicy();

    @Test
    public abstract void getAccessPolicyQueueDoesNotExist();

    @Test
    public abstract void setAccessPolicy();

    @Test
    public abstract void setAccessPolicyQueueDoesNotExist();

    @Test
    public abstract void setInvalidAccessPolicy();

    @Test
    public abstract void setTooManyAccessPolicies();

    @Test
    public abstract void enqueueMessage();

    @Test
    public abstract void enqueueEmptyMessage();

    @Test
    public abstract void enqueueShortTimeToLiveMessage();

    @Test
    public abstract void enqueueQueueDoesNotExist();

    @Test
    public abstract void dequeueMessage();

    @Test
    public abstract void dequeueMultipleMessages();

    @Test
    public abstract void dequeueTooManyMessages();

    @Test
    public abstract void dequeueQueueDoesNotExist();

    @Test
    public abstract void peekMessage();

    @Test
    public abstract void peekMultipleMessages();

    @Test
    public abstract void peekTooManyMessages();

    @Test
    public abstract void peekQueueDoesNotExist();

    @Test
    public abstract void clearMessages();

    @Test
    public abstract void clearMessagesQueueDoesNotExist();

    @Test
    public abstract void deleteMessage();

    @Test
    public abstract void deleteMessageInvalidMessageId();

    @Test
    public abstract void deleteMessageInvalidPopReceipt();

    @Test
    public abstract void deleteMessageQueueDoesNotExist();

    @Test
    public abstract void updateMessage();

    @Test
    public abstract void updateMessageInvalidMessageId();

    @Test
    public abstract void updateMessageInvalidPopReceipt();

    @Test
    public abstract void updateMessageQueueDoesNotExist();
}
