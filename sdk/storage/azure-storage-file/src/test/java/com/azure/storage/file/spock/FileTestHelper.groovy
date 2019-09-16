// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.http.rest.Response
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.file.models.CorsRule
import com.azure.storage.file.models.FileServiceProperties
import com.azure.storage.file.models.Metrics
import com.azure.storage.file.models.RetentionPolicy
import com.azure.storage.file.models.ShareItem
import com.azure.storage.file.models.SignedIdentifier
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageException
import com.azure.storage.file.models.StorageServiceProperties

import java.nio.file.Files
import java.nio.file.Paths
import java.security.NoSuchAlgorithmException
import java.time.Duration

class FileTestHelper {
    private static final ClientLogger logger = new ClientLogger(FileTestHelper.class)

    static boolean assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        return expectedStatusCode == response.getStatusCode()
    }

    static <T extends Throwable> boolean assertExceptionStatusCodeAndMessage(T throwable, int expectedStatusCode, StorageErrorCode errMessage) {
        return assertExceptionStatusCode(throwable, expectedStatusCode) && assertExceptionErrorMessage(throwable, errMessage)
    }

    static boolean assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        return throwable instanceof StorageException &&
            ((StorageException) throwable).getStatusCode() == expectedStatusCode
    }

    static boolean assertExceptionErrorMessage(Throwable throwable, StorageErrorCode errMessage) {
        return throwable instanceof StorageException &&
            ((StorageException) throwable).getErrorCode() == errMessage
    }

    static boolean assertFileServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics()) &&
                assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics()) &&
                assertCorsAreEqual(expected.cors(), actual.cors())
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

    static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual, boolean includeMetadata, boolean includeSnapshot) {
        if (expected == null) {
            return actual == null
        } else {
            if (!Objects.equals(expected.getName(), actual.getName())) {
                return false
            }

            if (includeMetadata && !Objects.equals(expected.getMetadata(), actual.getMetadata())) {
                return false
            }
            if (includeSnapshot && !Objects.equals(expected.getSnapshot(), actual.getSnapshot())) {
                return false
            }

            if (expected.getProperties() == null) {
                return actual.getProperties() == null
            } else {
                return Objects.equals(expected.getProperties().getQuota(), actual.getProperties().getQuota())
            }
        }
    }

    static boolean assertFileServicePropertiesAreEqual(FileServiceProperties expected, FileServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics()) &&
                assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics()) &&
                assertCorsAreEqual(expected.getCors(), actual.getCors())
        }
    }

    static boolean assertTwoFilesAreSame(File f1, File f2) throws IOException, NoSuchAlgorithmException {
        List<String> uploadFileString = Files.readAllLines(f1.toPath())
        List<String> downloadFileString = Files.readAllLines(f2.toPath())
        if (uploadFileString != null && downloadFileString != null) {
            downloadFileString.removeAll(uploadFileString)
        }
        while (!downloadFileString.isEmpty()) {
            if (!downloadFileString.get(0).trim().isEmpty()) {
                return false
            }
            downloadFileString.remove(0)
        }
        return true
    }

    static String createRandomFileWithLength(int size, String folder, String fileName) {
        def path = Paths.get(folder)
        if (path == null) {
            throw logger.logExceptionAsError(new RuntimeException("The folder path does not exist."))
        }

        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        def randomFile = new File(folder, fileName)
        RandomAccessFile raf = new RandomAccessFile(randomFile, "rw")
        raf.setLength(size)
        raf.close()
        return randomFile.getPath()
    }

    static void deleteFolderIfExists(String folder) {
        // Clean up all temporary generated files
        def dir = new File(folder)
        if (dir.isDirectory()) {
            File[] children = dir.listFiles()
            for (int i = 0; i < children.length; i++) {
                Files.delete(children[i].toPath())
            }
        }
    }

    // TODO : Move this into a common package test class?
    static byte[] getRandomBuffer(int length) {
        final Random randGenerator = new Random()
        final byte[] buff = new byte[length]
        randGenerator.nextBytes(buff)
        return buff
    }
}
