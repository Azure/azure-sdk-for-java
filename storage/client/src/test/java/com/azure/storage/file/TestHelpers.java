// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.StorageErrorException;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

class TestHelpers {
    static void assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    static void assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException exception = (StorageErrorException) throwable;
        assertEquals(expectedStatusCode, exception.response().statusCode());
    }

    static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            // Ignore the exception
        }
    }

    static void assertSharesAreEqual(ShareItem expected, ShareItem actual) {
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
}
