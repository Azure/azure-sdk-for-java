// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.storage.queue.models.CorsRule;
import com.azure.storage.queue.models.Logging;
import com.azure.storage.queue.models.Metrics;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.RetentionPolicy;
import com.azure.storage.queue.models.StorageServiceProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

    void assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.name(), actual.name());

            if (expected.metadata() != null && !ImplUtils.isNullOrEmpty(actual.metadata())) {
                assertEquals(expected.metadata(), actual.metadata());
            }
        }
    }

    void assertQueueServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics());
            assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics());
            assertLoggingAreEqual(expected.logging(), actual.logging());
            assertCorsAreEqual(expected.cors(), actual.cors());
        }
    }

    private void assertMetricsAreEqual(Metrics expected, Metrics actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.enabled(), actual.enabled());
            assertEquals(expected.includeAPIs(), actual.includeAPIs());
            assertEquals(expected.version(), actual.version());
            assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy());
        }
    }

    private void assertLoggingAreEqual(Logging expected, Logging actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.read(), actual.read());
            assertEquals(expected.write(), actual.write());
            assertEquals(expected.delete(), actual.delete());
            assertEquals(expected.version(), actual.version());
            assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy());
        }
    }

    private void assertRetentionPoliciesAreEqual(RetentionPolicy expected, RetentionPolicy actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.days(), actual.days());
            assertEquals(expected.enabled(), actual.enabled());
        }
    }

    private void assertCorsAreEqual(List<CorsRule> expected, List<CorsRule> actual) {
        if (expected == null) {
            assertTrue(ImplUtils.isNullOrEmpty(actual));
        } else {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertCorRulesAreEqual(expected.get(i), actual.get(i));
            }
        }
    }

    private void assertCorRulesAreEqual(CorsRule expected, CorsRule actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.allowedHeaders(), actual.allowedHeaders());
            assertEquals(expected.allowedMethods(), actual.allowedMethods());
            assertEquals(expected.allowedOrigins(), actual.allowedOrigins());
            assertEquals(expected.exposedHeaders(), actual.exposedHeaders());
            assertEquals(expected.maxAgeInSeconds(), actual.maxAgeInSeconds());
        }
    }

    QueuesSegmentOptions defaultSegmentOptions() {
        return new QueuesSegmentOptions().prefix(queueName);
    }
}
