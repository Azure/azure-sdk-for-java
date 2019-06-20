// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.file.models.FileRef;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Contains helper methods for unit tests.
 */
public class TestHelpers {
    static void assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.name(), actual.name());

            if (expected.metadata() != null && !ImplUtils.isNullOrEmpty(actual.metadata())) {
                assertEquals(expected.metadata(), actual.metadata());
            }
        }
    }

    public static void assertFileRefsAreEqual(FileRef expected, FileRef actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.name(), actual.name());
            assertEquals(expected.isDirectory(), actual.isDirectory());

            if (expected.fileProperties() != null && actual.fileProperties() != null) {
                assertEquals(expected.fileProperties().contentLength(), actual.fileProperties().contentLength());
            }
        }
    }

    static void assertQueueServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics());
            assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics());
            assertLoggingAreEqual(expected.logging(), actual.logging());
            assertCorsAreEqual(expected.cors(), actual.cors());
        }
    }

    private static void assertMetricsAreEqual(Metrics expected, Metrics actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.enabled(), actual.enabled());
            assertEquals(expected.includeAPIs(), actual.includeAPIs());
            assertEquals(expected.version(), actual.version());
            assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy());
        }
    }

    private static void assertLoggingAreEqual(Logging expected, Logging actual) {
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

    private static void assertRetentionPoliciesAreEqual(RetentionPolicy expected, RetentionPolicy actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.days(), actual.days());
            assertEquals(expected.enabled(), actual.enabled());
        }
    }

    private static void assertCorsAreEqual(List<CorsRule> expected, List<CorsRule> actual) {
        if (expected == null) {
            assertTrue(ImplUtils.isNullOrEmpty(actual));
        } else {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertCorRulesAreEqual(expected.get(i), actual.get(i));
            }
        }
    }

    private static void assertCorRulesAreEqual(CorsRule expected, CorsRule actual) {
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

    static void assertPermissionsAreEqual(SignedIdentifier expected, SignedIdentifier actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.accessPolicy().permission(), actual.accessPolicy().permission());
        assertEquals(expected.accessPolicy().start(), actual.accessPolicy().start());
        assertEquals(expected.accessPolicy().expiry(), actual.accessPolicy().expiry());
    }

    public static void assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    public static void assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException storageErrorException = (StorageErrorException) throwable;
        assertEquals(expectedStatusCode, storageErrorException.response().statusCode());
    }

    static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            // Ignore the errror
        }
    }
}
