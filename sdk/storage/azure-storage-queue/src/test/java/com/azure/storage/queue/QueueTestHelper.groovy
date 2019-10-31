// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.rest.Response
import com.azure.core.util.CoreUtils
import com.azure.core.util.Configuration
import com.azure.storage.queue.models.QueueAnalyticsLogging
import com.azure.storage.queue.models.QueueCorsRule
import com.azure.storage.queue.models.QueueErrorCode
import com.azure.storage.queue.models.QueueItem
import com.azure.storage.queue.models.QueueMetrics
import com.azure.storage.queue.models.QueueRetentionPolicy
import com.azure.storage.queue.models.QueueServiceProperties
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException

import java.time.Duration

class QueueTestHelper {
    static boolean assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        return expectedStatusCode == response.getStatusCode()
    }

    static boolean assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode, QueueErrorCode errMessage) {
        return throwable instanceof QueueStorageException &&
            ((QueueStorageException) throwable).getStatusCode() == expectedStatusCode &&
            ((QueueStorageException) throwable).getErrorCode() == errMessage
    }

    static boolean assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            return actual == null
        } else {
            if (!Objects.equals(expected.getName(), actual.getName())) {
                return false
            }
            if (expected.getMetadata() != null && !CoreUtils.isNullOrEmpty(actual.getMetadata())) {
                return expected.getMetadata() == actual.getMetadata()
            }
            return true
        }
    }

    static boolean assertQueueServicePropertiesAreEqual(QueueServiceProperties expected, QueueServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics()) &&
                assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics()) &&
                assertLoggingAreEqual(expected.getAnalyticsLogging(), actual.getAnalyticsLogging()) &&
                assertCorsAreEqual(expected.getCors(), actual.getCors())
        }
    }

    static boolean assertMetricsAreEqual(QueueMetrics expected, QueueMetrics actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.isEnabled(), actual.isEnabled()) &&
                Objects.equals(expected.isIncludeApis(), actual.isIncludeApis()) &&
                Objects.equals(expected.getVersion(), actual.getVersion()) &&
                assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy())
        }
    }

    static boolean assertLoggingAreEqual(QueueAnalyticsLogging expected, QueueAnalyticsLogging actual) {
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

    static boolean assertRetentionPoliciesAreEqual(QueueRetentionPolicy expected, QueueRetentionPolicy actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getDays(), actual.getDays()) &&
                Objects.equals(expected.isEnabled(), actual.isEnabled())
        }
    }

    static boolean assertCorsAreEqual(List<QueueCorsRule> expected, List<QueueCorsRule> actual) {
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

    static boolean assertCorRulesAreEqual(QueueCorsRule expected, QueueCorsRule actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getAllowedHeaders(), actual.getAllowedHeaders()) &&
                Objects.equals(expected.getAllowedMethods(), actual.getAllowedMethods()) &&
                Objects.equals(expected.getAllowedOrigins(), actual.getAllowedOrigins()) &&
                Objects.equals(expected.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds())
        }
    }

    static boolean assertPermissionsAreEqual(QueueSignedIdentifier expected, QueueSignedIdentifier actual) {
        if (expected == null) {
            return actual == null
        }
        if (expected.getAccessPolicy() == null) {
            return actual.getAccessPolicy() == null
        }
        return Objects.equals(expected.getId(), actual.getId()) &&
            Objects.equals(expected.getAccessPolicy().getPermissions(), actual.getAccessPolicy().getPermissions()) &&
            Objects.equals(expected.getAccessPolicy().getStartsOn(), actual.getAccessPolicy().getStartsOn()) &&
            Objects.equals(expected.getAccessPolicy().getExpiresOn(), actual.getAccessPolicy().getExpiresOn())
    }

    static void sleepInRecord(Duration time) {
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE")
        if ("RECORD".equalsIgnoreCase(azureTestMode)) {
            sleep(time)
        }
    }

    private static void sleep(Duration time) {
        try {
            Thread.sleep(time.toMillis())
        } catch (InterruptedException ignored) {
            // Ignore the error
        }
    }
}
