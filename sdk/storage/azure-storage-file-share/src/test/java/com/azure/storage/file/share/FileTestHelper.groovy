// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.rest.Response
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.share.models.ShareCorsRule
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareItem
import com.azure.storage.file.share.models.ShareMetrics
import com.azure.storage.file.share.models.ShareRetentionPolicy
import com.azure.storage.file.share.models.ShareServiceProperties
import com.azure.storage.file.share.models.ShareStorageException

import java.nio.file.Files
import java.nio.file.Path

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

    static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual,
                                        boolean includeMetadata, boolean includeSnapshot) {
        return assertSharesAreEqual(expected, actual, includeMetadata, includeSnapshot, false)
    }

    static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual,
                                        boolean includeMetadata, boolean includeSnapshot,
                                        boolean includeDeleted) {
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
                if (includeDeleted &&
                    (expected.getProperties().getDeletedTime() == null ^ actual.getProperties().getDeletedTime() == null)) {
                    return false;
                }
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

    /*
    We only allow int because anything larger than 2GB (which would require a long) is left to stress/perf.
     */
    static File getRandomFile(int size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)

        if (size > Constants.MB) {
            for (def i = 0; i < size / Constants.MB; i++) {
                def dataSize = Math.min(Constants.MB, size - i * Constants.MB)
                fos.write(getRandomBuffer(dataSize))
            }
        } else {
            fos.write(getRandomBuffer(size))
        }

        fos.close()
        return file
    }

    // TODO : Move this into a common package test class?
    static byte[] getRandomBuffer(int length) {
        final Random randGenerator = new Random()
        final byte[] buff = new byte[length]
        randGenerator.nextBytes(buff)
        return buff
    }

    static compareFiles(File file1, File file2, long offset, long count) {
        def pos = 0L
        def defaultBufferSize = 128 * Constants.KB
        def stream1 = new FileInputStream(file1)
        stream1.skip(offset)
        def stream2 = new FileInputStream(file2)

        try {
            // If the amount we are going to read is smaller than the default buffer size use that instead.
            def bufferSize = (int) Math.min(defaultBufferSize, count)

            while (pos < count) {
                // Number of bytes we expect to read.
                def expectedReadCount = (int) Math.min(bufferSize, count - pos)
                def buffer1 = new byte[expectedReadCount]
                def buffer2 = new byte[expectedReadCount]

                def readCount1 = stream1.read(buffer1)
                def readCount2 = stream2.read(buffer2)

                // Use Arrays.equals as it is more optimized than Groovy/Spock's '==' for arrays.
                assert readCount1 == readCount2 && Arrays.equals(buffer1, buffer2)

                pos += expectedReadCount
            }

            def verificationRead = stream2.read()
            return pos == count && verificationRead == -1
        } finally {
            stream1.close()
            stream2.close()
        }
    }
}
