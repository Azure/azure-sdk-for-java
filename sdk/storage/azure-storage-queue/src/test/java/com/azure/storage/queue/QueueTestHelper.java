// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.storage.queue.models.QueueAnalyticsLogging;
import com.azure.storage.queue.models.QueueCorsRule;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueItem;
import com.azure.storage.queue.models.QueueMetrics;
import com.azure.storage.queue.models.QueueRetentionPolicy;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueSignedIdentifier;
import com.azure.storage.queue.models.QueueStorageException;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueueTestHelper {
    static <T> Response<T> assertResponseStatusCode(Response<T> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode());
        return response;
    }

    static <T> void assertAsyncResponseStatusCode(Mono<Response<T>> response, int expectedStatusCode) {
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(expectedStatusCode, r.getStatusCode()))
            .verifyComplete();
    }

    static QueueStorageException throwsStorageException(Executable executable) {
        return assertThrows(QueueStorageException.class, executable);
    }

    static void assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode,
        QueueErrorCode errMessage) {
        QueueStorageException exception = assertInstanceOf(QueueStorageException.class, throwable);
        assertEquals(expectedStatusCode, exception.getStatusCode());
        assertEquals(errMessage, exception.getErrorCode());
    }

    static void assertQueuesAreEqual(QueueItem expected, QueueItem actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getName(), actual.getName());
            if (expected.getMetadata() != null && !CoreUtils.isNullOrEmpty(actual.getMetadata())) {
                assertEquals(expected.getMetadata(), actual.getMetadata());
            }
        }
    }

    static void assertQueueServicePropertiesAreEqual(QueueServiceProperties expected, QueueServiceProperties actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics());
            assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics());
            assertLoggingAreEqual(expected.getAnalyticsLogging(), actual.getAnalyticsLogging());
            assertCorsAreEqual(expected.getCors(), actual.getCors());
        }
    }

    static void assertMetricsAreEqual(QueueMetrics expected, QueueMetrics actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.isEnabled(), actual.isEnabled());
            assertEquals(expected.isIncludeApis(), actual.isIncludeApis());
            assertEquals(expected.getVersion(), actual.getVersion());
            assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy());
        }
    }

    static void assertLoggingAreEqual(QueueAnalyticsLogging expected, QueueAnalyticsLogging actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.isRead(), actual.isRead());
            assertEquals(expected.isWrite(), actual.isWrite());
            assertEquals(expected.isDelete(), actual.isDelete());
            assertEquals(expected.getVersion(), actual.getVersion());
            assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy());
        }
    }

    static void assertRetentionPoliciesAreEqual(QueueRetentionPolicy expected, QueueRetentionPolicy actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getDays(), actual.getDays());
            assertEquals(expected.isEnabled(), actual.isEnabled());
        }
    }

    static void assertCorsAreEqual(List<QueueCorsRule> expected, List<QueueCorsRule> actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertCorRulesAreEqual(expected.get(i), actual.get(i));
            }
        }
    }

    static void assertCorRulesAreEqual(QueueCorsRule expected, QueueCorsRule actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.getAllowedHeaders(), actual.getAllowedHeaders());
            assertEquals(expected.getAllowedMethods(), actual.getAllowedMethods());
            assertEquals(expected.getAllowedOrigins(), actual.getAllowedOrigins());
            assertEquals(expected.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds());
        }
    }

    static void assertPermissionsAreEqual(QueueSignedIdentifier expected, QueueSignedIdentifier actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        if (expected.getAccessPolicy() == null) {
            assertNull(actual.getAccessPolicy());
        } else {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getAccessPolicy().getPermissions(), actual.getAccessPolicy().getPermissions());
            assertEquals(expected.getAccessPolicy().getStartsOn(), actual.getAccessPolicy().getStartsOn());
            assertEquals(expected.getAccessPolicy().getExpiresOn(), actual.getAccessPolicy().getExpiresOn());
        }
    }

    static <T> List<T> pagedResponseToList(PagedIterable<T> response) {
        return response.stream().collect(Collectors.toList());
    }
}
