// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.rest.Response
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareRetentionPolicy
import com.azure.storage.file.share.models.ShareCorsRule
import com.azure.storage.file.share.models.ShareItem
import com.azure.storage.file.share.models.ShareMetrics
import com.azure.storage.file.share.models.ShareServiceProperties
import com.azure.storage.file.share.models.ShareStorageException

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileTestHelper {
    private static final ClientLogger logger = new ClientLogger(FileTestHelper.class)

    static boolean assertResponseStatusCode(Response<?> response, int expectedStatusCode) {
        return expectedStatusCode == response.getStatusCode()
    }

    static <T extends Throwable> boolean assertExceptionStatusCodeAndMessage(T throwable, int expectedStatusCode, ShareErrorCode errMessage) {
        return assertExceptionStatusCode(throwable, expectedStatusCode) && assertExceptionErrorMessage(throwable, errMessage)
    }

    static boolean assertExceptionStatusCode(Throwable throwable, int expectedStatusCode) {
        return throwable instanceof ShareStorageException &&
            ((ShareStorageException) throwable).getStatusCode() == expectedStatusCode
    }

    static boolean assertExceptionErrorMessage(Throwable throwable, ShareErrorCode errMessage) {
        return throwable instanceof ShareStorageException &&
            ((ShareStorageException) throwable).getErrorCode() == errMessage
    }

    static boolean assertMetricsAreEqual(ShareMetrics expected, ShareMetrics actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.isEnabled(), actual.isEnabled()) &&
                Objects.equals(expected.isIncludeApis(), actual.isIncludeApis()) &&
                Objects.equals(expected.getVersion(), actual.getVersion()) &&
                assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy())
        }
    }

    static boolean assertRetentionPoliciesAreEqual(ShareRetentionPolicy expected, ShareRetentionPolicy actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getDays(), actual.getDays()) &&
                Objects.equals(expected.isEnabled(), actual.isEnabled())
        }
    }

    static boolean assertCorsAreEqual(List<ShareCorsRule> expected, List<ShareCorsRule> actual) {
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

    static boolean assertCorRulesAreEqual(ShareCorsRule expected, ShareCorsRule actual) {
        if (expected == null) {
            return actual == null
        } else {
            return Objects.equals(expected.getAllowedHeaders(), actual.getAllowedHeaders()) &&
                Objects.equals(expected.getAllowedMethods(), actual.getAllowedMethods()) &&
                Objects.equals(expected.getAllowedOrigins(), actual.getAllowedOrigins()) &&
                Objects.equals(expected.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds())
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

    static boolean assertFileServicePropertiesAreEqual(ShareServiceProperties expected, ShareServiceProperties actual) {
        if (expected == null) {
            return actual == null
        } else {
            return assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics()) &&
                assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics()) &&
                assertCorsAreEqual(expected.getCors(), actual.getCors())
        }
    }

    static String createRandomFileWithLength(int size, URL folder, String fileName) {
        def path = folder.getPath()
        if (path == null) {
            throw logger.logExceptionAsError(new RuntimeException("The folder path does not exist."))
        }

        Path folderPaths = new File(path).toPath()
        if (!Files.exists(folderPaths)) {
            Files.createDirectory(folderPaths)
        }
        def randomFile = new File(folderPaths.toString(), fileName)
        RandomAccessFile raf = new RandomAccessFile(randomFile, "rw")
        raf.setLength(size)
        raf.close()
        return randomFile.getPath()
    }

    static void deleteFilesIfExists(String folder) {
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
