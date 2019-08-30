// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.spock

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
        return expectedStatusCode == response.statusCode()
    }

    static boolean assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode, StorageErrorCode errMessage) {
        return throwable instanceof StorageException &&
            ((StorageException) throwable).statusCode() == expectedStatusCode &&
            ((StorageException) throwable).errorCode() == errMessage
    }

    static boolean assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            return actual == null
        } else {
            if (!Objects.equals(expected.name(), actual.name())) {
                return false
            }
            if (expected.metadata() != null && !ImplUtils.isNullOrEmpty(actual.metadata())) {
                return expected.metadata().equals(actual.metadata())
            }
            return true
        }
    }

    static boolean assertQueueServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics()) &&
                assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics()) &&
                assertLoggingAreEqual(expected.logging(), actual.logging()) &&
                assertCorsAreEqual(expected.cors(), actual.cors())
        }
    }

    static boolean assertMetricsAreEqual(Metrics expected, Metrics actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.enabled(), actual.enabled()) &&
                Objects.equals(expected.includeAPIs(), actual.includeAPIs()) &&
                Objects.equals(expected.version(), actual.version()) &&
                assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy())
        }
    }

    static boolean assertLoggingAreEqual(Logging expected, Logging actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.read(), actual.read()) &&
                Objects.equals(expected.write(), actual.write()) &&
                Objects.equals(expected.delete(), actual.delete()) &&
                Objects.equals(expected.version(), actual.version()) &&
                assertRetentionPoliciesAreEqual(expected.retentionPolicy(), actual.retentionPolicy())
        }
    }

    static boolean assertRetentionPoliciesAreEqual(RetentionPolicy expected, RetentionPolicy actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.days(), actual.days()) &&
                Objects.equals(expected.enabled(), actual.enabled())
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
            return Objects.equals(expected.allowedHeaders(), actual.allowedHeaders()) &&
                Objects.equals(expected.allowedMethods(), actual.allowedMethods()) &&
                Objects.equals(expected.allowedOrigins(), actual.allowedOrigins()) &&
                Objects.equals(expected.maxAgeInSeconds(), actual.maxAgeInSeconds())
        }
    }

    static boolean assertPermissionsAreEqual(SignedIdentifier expected, SignedIdentifier actual) {
        if (expected == null) {
            return actual == null
        }
        if (expected.accessPolicy() == null) {
            return actual.accessPolicy() == null
        }
        return Objects.equals(expected.id(), actual.id()) &&
            Objects.equals(expected.accessPolicy().permission(), actual.accessPolicy().permission()) &&
            Objects.equals(expected.accessPolicy().start(), actual.accessPolicy().start()) &&
            Objects.equals(expected.accessPolicy().expiry(), actual.accessPolicy().expiry())
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
