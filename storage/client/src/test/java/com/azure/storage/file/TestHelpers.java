// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.Metrics;
import com.azure.storage.file.models.RetentionPolicy;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;

import java.time.Duration;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class TestHelpers {
    final String azureStorageConnectionString = "AZURE_STORAGE_CONNECTION_STRING";
    final String azureStorageFileEndpoint = "AZURE_STORAGE_FILE_ENDPOINT";

    <T> T setupClient(BiFunction<String, String, T> clientBuilder, ServiceLogger logger) {
        String connectionString = ConfigurationManager.getConfiguration().get(azureStorageConnectionString);
        String endpoint = ConfigurationManager.getConfiguration().get(azureStorageFileEndpoint);

        if (ImplUtils.isNullOrEmpty(connectionString) || ImplUtils.isNullOrEmpty(endpoint)) {
            logger.asWarning().log("{} and {} must be set to build the testing client", azureStorageConnectionString, azureStorageFileEndpoint);
            fail();
            return null;
        }

        return clientBuilder.apply(connectionString, endpoint);
    }

    void assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    void assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException exception = (StorageErrorException) throwable;
        assertEquals(expectedStatusCode, exception.response().statusCode());
    }

    void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            // Ignore the exception
        }
    }

    void assertSharesAreEqual(ShareItem expected, ShareItem actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.name(), actual.name());
            assertEquals(expected.properties().quota(), actual.properties().quota());

            if (expected.metadata() != null) {
                assertEquals(expected.metadata(), actual.metadata());
            }

            if (expected.snapshot() != null) {
                assertEquals(expected.snapshot(), actual.snapshot());
            }
        }
    }

    void assertFileServicePropertiesAreEqual(FileServiceProperties expected, FileServiceProperties actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics());
            assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics());
            assertCorsAreEqual(expected.cors(), actual.cors());
        }
    }

    void assertMetricsAreEqual(Metrics expected, Metrics actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.enabled(), actual.enabled());
            assertEquals(expected.includeAPIs(), actual.includeAPIs());
            assertEquals(expected.version(), actual.version());
            assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy());
        }
    }

    void assertRetentionPoliciesAreEqual(RetentionPolicy expected, RetentionPolicy actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.days(), actual.days());
            assertEquals(expected.enabled(), actual.enabled());
        }
    }

    void assertCorsAreEqual(List<CorsRule> expected, List<CorsRule> actual) {
        if (expected == null) {
            assertTrue(ImplUtils.isNullOrEmpty(actual));
        } else {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertCorRulesAreEqual(expected.get(i), actual.get(i));
            }
        }
    }

    void assertCorRulesAreEqual(CorsRule expected, CorsRule actual) {
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
}
