// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.rest.Response
import com.azure.core.implementation.util.ImplUtils
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.storage.queue.models.CorsRule
import com.azure.storage.queue.models.Logging
import com.azure.storage.queue.models.Metrics
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.RetentionPolicy
import com.azure.storage.queue.models.SignedIdentifier
import com.azure.storage.queue.models.StorageErrorCode
import com.azure.storage.queue.models.StorageException
import com.azure.storage.queue.models.StorageServiceProperties

import java.time.Duration

class QueueTestHelper {
    static boolean assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        return expectedStatusCode == response.getStatusCode()
    }

    static boolean assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode, StorageErrorCode errMessage) {
        return throwable instanceof StorageException &&
            ((StorageException) throwable).getStatusCode() == expectedStatusCode &&
            ((StorageException) throwable).getErrorCode() == errMessage
    }

    static boolean assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            return actual == null
        } else {
            if (!Objects.equals(expected.getName(), actual.getName())) {
                return false
            }
            if (expected.getMetadata() != null && !ImplUtils.isNullOrEmpty(actual.getMetadata())) {
                return expected.getMetadata().equals(actual.getMetadata())
            }
            return true
        }
    }

    static boolean assertQueueServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics()) &&
                assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics()) &&
                assertLoggingAreEqual(expected.getLogging(), actual.getLogging()) &&
                assertCorsAreEqual(expected.getCors(), actual.getCors())
        }
    }

    static boolean assertMetricsAreEqual(Metrics expected, Metrics actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.isEnabled(), actual.isEnabled()) &&
                Objects.equals(expected.isIncludeAPIs(), actual.isIncludeAPIs()) &&
                Objects.equals(expected.getVersion(), actual.getVersion()) &&
                assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy())
        }
    }

    static boolean assertLoggingAreEqual(Logging expected, Logging actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.isRead(), actual.isRead()) &&
                Objects.equals(expected.isWrite(), actual.isWrite()) &&
                Objects.equals(expected.isDelete(), actual.isDelete()) &&
                Objects.equals(expected.getVersion(), actual.getVersion()) &&
                assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy())
        }
    }

    static boolean assertRetentionPoliciesAreEqual(RetentionPolicy expected, RetentionPolicy actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getDays(), actual.getDays()) &&
                Objects.equals(expected.isEnabled(), actual.isEnabled())
        }
    }

    static boolean assertCorsAreEqual(List<CorsRule> expected, List<CorsRule> actual) {
        if (expected == null) {
            return actual == null
        } else {
            if (expected.size() != actual.size()) {
                return false
            }
            for (int i = 0; i < expected.size(); i++) {
                if (!assertCorRulesAreEqual(expected.get(i), actual.get(i))) {
                    return false
                }
            }
            return true
        }
    }

    static boolean assertCorRulesAreEqual(CorsRule expected, CorsRule actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getAllowedHeaders(), actual.getAllowedHeaders()) &&
                Objects.equals(expected.getAllowedMethods(), actual.getAllowedMethods()) &&
                Objects.equals(expected.getAllowedOrigins(), actual.getAllowedOrigins()) &&
                Objects.equals(expected.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds())
        }
    }

    static boolean assertPermissionsAreEqual(SignedIdentifier expected, SignedIdentifier actual) {
        if (expected == null) {
            return actual == null
        }
        if (expected.getAccessPolicy() == null) {
            return actual.getAccessPolicy() == null
        }
        return Objects.equals(expected.getId(), actual.getId()) &&
            Objects.equals(expected.getAccessPolicy().getPermission(), actual.getAccessPolicy().getPermission()) &&
            Objects.equals(expected.getAccessPolicy().getStart(), actual.getAccessPolicy().getStart()) &&
            Objects.equals(expected.getAccessPolicy().getExpiry(), actual.getAccessPolicy().getExpiry())
    }

    static void sleepInRecord(Duration time) {
        String azureTestMode = ConfigurationManager.getConfiguration().get("AZURE_TEST_MODE")
        if ("RECORD".equalsIgnoreCase(azureTestMode)) {
            sleep(time)
        }
    }

    private static void sleep(Duration time) {
        try {
            Thread.sleep(time.toMillis())
        } catch (InterruptedException ex) {
            // Ignore the error
        }
    }
}
