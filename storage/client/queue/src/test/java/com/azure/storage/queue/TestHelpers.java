// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.queue.models.CorsRule;
import com.azure.storage.queue.models.Logging;
import com.azure.storage.queue.models.Metrics;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.RetentionPolicy;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.StorageErrorException;
import com.azure.storage.queue.models.StorageServiceProperties;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Contains helper methods for unit tests.
 */
class TestHelpers {
    private final String azureStorageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";
    private final String azureStorageQueueEndpoint = "AZURE_STORAGE_QUEUE_ENDPOINT";

    <T> T setupClient(BiFunction<String, String, T> clientBuilder, boolean isPlaybackMode, ClientLogger logger) {
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net";
        String queueEndpoint = "https://teststorage.queue.core.windows.net/";

        if (!isPlaybackMode) {
            connectionString = ConfigurationManager.getConfiguration().get(azureStorageConnectionString);
            queueEndpoint = ConfigurationManager.getConfiguration().get(azureStorageQueueEndpoint);
        }

        if (ImplUtils.isNullOrEmpty(connectionString) && ImplUtils.isNullOrEmpty(queueEndpoint)) {
            logger.warning("{} and {} must be set to build the testing client", azureStorageConnectionString, azureStorageQueueEndpoint);
            fail();
            return null;
        }

        return clientBuilder.apply(connectionString, queueEndpoint);
    }

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

    void assertPermissionsAreEqual(SignedIdentifier expected, SignedIdentifier actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.accessPolicy().permission(), actual.accessPolicy().permission());
        assertEquals(expected.accessPolicy().start(), actual.accessPolicy().start());
        assertEquals(expected.accessPolicy().expiry(), actual.accessPolicy().expiry());
    }

    void assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    void assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException storageErrorException = (StorageErrorException) throwable;
        assertEquals(expectedStatusCode, storageErrorException.response().statusCode());
    }

    void sleepInRecordMode(Duration duration) {
        String azureTestMode = ConfigurationManager.getConfiguration().get("AZURE_TEST_MODE");
        if ("RECORD".equalsIgnoreCase(azureTestMode)) {
            sleep(duration);
        }
    }

    void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            // Ignore the error
        }
    }
}
