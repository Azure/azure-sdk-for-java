// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.models.CorsRule;
import com.azure.storage.file.models.FileRef;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.Metrics;
import com.azure.storage.file.models.RetentionPolicy;
import com.azure.storage.file.models.ShareItem;
import com.azure.storage.file.models.SignedIdentifier;
import com.azure.storage.file.models.StorageErrorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class FileTestHelpers {
    private static final String AZURE_TEST_MODE = "AZURE_TEST_MODE";

    static <T> T setupClient(BiFunction<String, String, T> clientBuilder, boolean isPlayback, ClientLogger logger) {
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net";
        String endpoint = "https://teststorage.file.core.windows.net/";

        if (!isPlayback) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING");
            endpoint = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");
        }

        if (ImplUtils.isNullOrEmpty(connectionString) && ImplUtils.isNullOrEmpty(endpoint)) {
            logger.warning("Connection string or endpoint must be set to buildClient the testing client");
            fail();
            return null;
        }

        return clientBuilder.apply(connectionString, endpoint);
    }

    static ShareClientBuilder createShareClientWithSnapshot(InterceptorManager interceptorManager, String shareName, String snapshot) {
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net";
        String endpoint = "https://teststorage.file.core.windows.net/";

        ShareClientBuilder shareClientBuilder;
        if (!interceptorManager.isPlaybackMode()) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_CONNECTION_STRING");
            endpoint = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_ENDPOINT");
            shareClientBuilder = new ShareClientBuilder()
                .endpoint(endpoint)
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
                .shareName(shareName)
                .snapshot(snapshot);
        } else {
            shareClientBuilder = new ShareClientBuilder()
                .endpoint(endpoint)
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .shareName(shareName)
                .snapshot(snapshot);
        }
        return shareClientBuilder;
    }

    static void assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.statusCode());
    }

    static void assertResponseListStatusCode(Response<?> response, List<Integer> expectedStatusCodeList) {
        for (Integer expectedStatusCode: expectedStatusCodeList) {
            if (expectedStatusCode == response.statusCode()) {
                return;
            }
        }
        fail("The response status code did not include in the list.");
    }

    static void assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException exception = (StorageErrorException) throwable;
        assertEquals(expectedStatusCode, exception.response().statusCode());
    }

    static void assertExceptionErrorMessage(Throwable throwable, String message) {
        assertTrue(throwable instanceof StorageErrorException);
        StorageErrorException exception = (StorageErrorException) throwable;
        assertTrue(exception.getMessage().contains(message));
    }

    static void assertExceptionStatusCode(Runnable thrower, int expectedStatusCode) {
        try {
            thrower.run();
            fail();
        } catch (Exception ex) {
            assertTrue(ex instanceof StorageErrorException);
            StorageErrorException exception = (StorageErrorException) ex;
            assertEquals(expectedStatusCode, exception.response().statusCode());
        }
    }

    static void sleepInRecordMode(Duration duration) {
        if (getTestMode() == TestMode.RECORD) {
            sleep(duration);
        }
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

    static void assertFileServicePropertiesAreEqual(FileServiceProperties expected, FileServiceProperties actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics());
            assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics());
            assertCorsAreEqual(expected.cors(), actual.cors());
        }
    }

    static void assertFileRefsAreEqual(FileRef expected, FileRef actual) {
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

    static void assertPermissionsAreEqual(SignedIdentifier expected, SignedIdentifier actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.accessPolicy().permission(), actual.accessPolicy().permission());
        assertEquals(expected.accessPolicy().start(), actual.accessPolicy().start());
        assertEquals(expected.accessPolicy().expiry(), actual.accessPolicy().expiry());
    }

    static TestMode getTestMode() {
        final Logger logger = LoggerFactory.getLogger(TestBase.class);
        final String azureTestMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE);

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                }

                return TestMode.PLAYBACK;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE);
        }
        return TestMode.PLAYBACK;
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

    static void assertTwoFilesAreSame(File f1, File f2) throws IOException, NoSuchAlgorithmException {
        List<String> uploadFileString = Files.readAllLines(f1.toPath());
        List<String> downloadFileString = Files.readAllLines(f2.toPath());
        if (uploadFileString != null && downloadFileString != null) {
            downloadFileString.removeAll(uploadFileString);
        }
        while (!downloadFileString.isEmpty()) {
            Assert.assertTrue("The download file is supposed to be the same as the upload file.", downloadFileString.get(0).isEmpty());
            downloadFileString.remove(0);
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
}
