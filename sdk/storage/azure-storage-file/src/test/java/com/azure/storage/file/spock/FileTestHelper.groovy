// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.http.rest.Response
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.storage.file.models.CorsRule
import com.azure.storage.file.models.FileRef
import com.azure.storage.file.models.FileServiceProperties
import com.azure.storage.file.models.Metrics
import com.azure.storage.file.models.RetentionPolicy
import com.azure.storage.file.models.ShareItem
import com.azure.storage.file.models.SignedIdentifier
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageErrorException
import com.azure.storage.file.models.StorageServiceProperties

import java.nio.file.Files
import java.nio.file.Paths
import java.security.NoSuchAlgorithmException
import java.time.Duration
import java.util.logging.Logging

class FileTestHelper {

    static boolean assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        return expectedStatusCode == response.statusCode()
    }

    static boolean assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode, String errMessage) {
        return assertExceptionStatusCode(throwable, expectedStatusCode) && assertExceptionErrorMessage(throwable, errMessage)
    }

    static boolean assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode, StorageErrorCode errMessage) {
        return assertExceptionStatusCode(throwable, expectedStatusCode) && assertExceptionErrorMessage(throwable, errMessage)
    }

    static boolean assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        if (!throwable instanceof StorageErrorException) {
            return false
        }
        StorageErrorException storageErrorException = (StorageErrorException) throwable
        return expectedStatusCode == storageErrorException.response().statusCode()
    }

    static boolean assertExceptionErrorMessage(Throwable throwable, String errMessage) {
        return throwable instanceof StorageErrorException && throwable.getMessage().contains(errMessage)
    }

    static boolean assertExceptionErrorMessage(Throwable throwable, StorageErrorCode errMessage) {
        return throwable instanceof StorageErrorException && throwable.getMessage().contains(errMessage.toString())
    }

    static boolean assertFileServicePropertiesAreEqual(StorageServiceProperties expected, StorageServiceProperties actual) {
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

    static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual, boolean includeMetadata, boolean includeSnapshot) {
        if (expected == null) {
            return actual == null
        } else {
            Objects.equals(expected.name(), actual.name())
            if (expected.properties() == null) {
                return actual.properties() == null
            }
            if (!Objects.equals(expected.properties().quota(), actual.properties().quota())) {
                return false
            }
            if (includeMetadata) {
                if (!Objects.equals(expected.metadata(), actual.metadata())) {
                    return false
                }
            }
            if (includeSnapshot) {
                if (!Objects.equals(expected.snapshot(), actual.snapshot())) {
                    return false
                }
            }
            return true
        }
    }

    static boolean assertFileServicePropertiesAreEqual(FileServiceProperties expected, FileServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.hourMetrics(), actual.hourMetrics()) &&
                assertMetricsAreEqual(expected.minuteMetrics(), actual.minuteMetrics()) &&
                assertCorsAreEqual(expected.cors(), actual.cors())
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
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }
        def randomFile = new File(folder + "/" + fileName)
        RandomAccessFile raf = new RandomAccessFile(randomFile, "rw")
        raf.setLength(size)
        raf.close()
        return randomFile.getPath()
    }

    static boolean assertFileRefName(FileRef fileRef, String name) {
        if (fileRef != null) {
            return Objects.equals(name, fileRef.name())
        }
        return true
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
