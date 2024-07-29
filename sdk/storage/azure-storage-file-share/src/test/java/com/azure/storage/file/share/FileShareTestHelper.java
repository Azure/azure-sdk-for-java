// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.rest.Response;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import com.azure.storage.file.share.models.ClearRange;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareCorsRule;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareMetrics;
import com.azure.storage.file.share.models.ShareRetentionPolicy;
import com.azure.storage.file.share.models.ShareRootSquash;
import com.azure.storage.file.share.models.ShareServiceProperties;
import com.azure.storage.file.share.models.ShareStorageException;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class FileShareTestHelper {

    private static final ClientLogger LOGGER = new ClientLogger(FileShareTestHelper.class);

    protected static void assertExceptionStatusCodeAndMessage(Throwable throwable, int expectedStatusCode,
        ShareErrorCode errMessage) {
        ShareStorageException exception = assertInstanceOf(ShareStorageException.class, throwable);
        assertEquals(expectedStatusCode, exception.getStatusCode());
        assertEquals(errMessage, exception.getErrorCode());
    }

    protected static <T> Response<T> assertResponseStatusCode(Response<T> response, int expectedStatusCode) {
        assertEquals(expectedStatusCode, response.getStatusCode());
        return response;
    }

    /**
     * Compares the two timestamps to the minute
     * @param expectedTime expected time
     * @param actualTime actual time
     * @return whether timestamps match (excluding seconds)
     */
    protected static boolean compareDatesWithPrecision(OffsetDateTime expectedTime, OffsetDateTime actualTime) {
        return expectedTime.truncatedTo(ChronoUnit.MINUTES).isEqual(actualTime.truncatedTo(ChronoUnit.MINUTES));
    }

    protected static List<FileRange> createFileRanges(long... offsets) {
        List<FileRange> fileRanges = new ArrayList<>();
        if (offsets == null || offsets.length == 0) {
            return fileRanges;
        }
        for (int i = 0; i < offsets.length / 2; i++) {
            FileRange range = new FileRange().setStart(offsets[i * 2]).setEnd(offsets[i * 2 + 1]);
            fileRanges.add(range);
        }
        return fileRanges;
    }

    protected static List<ClearRange> createClearRanges(long... offsets) {
        List<ClearRange> clearRanges = new ArrayList<>();
        if (offsets == null || offsets.length == 0) {
            return clearRanges;
        }
        for (int i = 0; i < offsets.length / 2; i++) {
            ClearRange range = new ClearRange().setStart(offsets[i * 2]).setEnd(offsets[i * 2 + 1]);
            clearRanges.add(range);
        }
        return clearRanges;
    }

    protected static InputStream getInputStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    protected static byte[] getRandomBuffer(int length) {
        final byte[] buff = new byte[length];
        ThreadLocalRandom.current().nextBytes(buff);
        return buff;
    }

    protected static File getRandomFile(int size) throws IOException {
        File file = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
        file.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(file);

        if (size > Constants.MB) {
            int mbWrites = size / Constants.MB;
            int remainder = size % Constants.MB;

            for (int i = 0; i < mbWrites; i++) {
                fos.write(getRandomBuffer(Constants.MB));
            }

            if (remainder > 0) {
                fos.write(getRandomBuffer(remainder));
            }
        } else {
            fos.write(getRandomBuffer(size));
        }

        fos.close();
        return file;
    }

    protected static boolean compareFiles(File file1, File file2, long offset, long count) throws IOException {
        return StorageCommonTestUtils.compareFiles(file1, file2, offset, count);
    }

    protected static ByteBuffer getRandomByteBuffer(int length) {
        return ByteBuffer.wrap(FileShareTestHelper.getRandomBuffer(length));
    }

    protected static boolean assertMetricsAreEqual(ShareMetrics expected, ShareMetrics actual) {
        if (expected == null) {
            return actual == null;
        } else {
            return Objects.equals(expected.isEnabled(), actual.isEnabled())
                && Objects.equals(expected.isIncludeApis(), actual.isIncludeApis())
                && Objects.equals(expected.getVersion(), actual.getVersion())
                && assertRetentionPoliciesAreEqual(expected.getRetentionPolicy(), actual.getRetentionPolicy());
        }
    }

    protected static boolean assertRetentionPoliciesAreEqual(ShareRetentionPolicy expected,
        ShareRetentionPolicy actual) {
        if (expected == null) {
            return actual == null;
        } else {
            return Objects.equals(expected.getDays(), actual.getDays())
                && Objects.equals(expected.isEnabled(), actual.isEnabled());
        }
    }

    protected static boolean assertFileServicePropertiesAreEqual(ShareServiceProperties expected,
        ShareServiceProperties actual) {
        if (expected == null) {
            return actual == null;
        } else {
            return assertMetricsAreEqual(expected.getHourMetrics(), actual.getHourMetrics())
                && assertMetricsAreEqual(expected.getMinuteMetrics(), actual.getMinuteMetrics())
                && assertCorsAreEqual(expected.getCors(), actual.getCors());
        }
    }

    static boolean assertCorsAreEqual(List<ShareCorsRule> expected, List<ShareCorsRule> actual) {
        if (expected == null) {
            return actual == null;
        } else {
            if (expected.size() != actual.size()) {
                return false;
            }
            for (int i = 0; i < expected.size(); i++) {
                if (!assertCorRulesAreEqual(expected.get(i), actual.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    static boolean assertCorRulesAreEqual(ShareCorsRule expected, ShareCorsRule actual) {
        if (expected == null) {
            return actual == null;
        } else {
            return Objects.equals(expected.getAllowedHeaders(), actual.getAllowedHeaders())
                && Objects.equals(expected.getAllowedMethods(), actual.getAllowedMethods())
                && Objects.equals(expected.getAllowedOrigins(), actual.getAllowedOrigins())
                && Objects.equals(expected.getMaxAgeInSeconds(), actual.getMaxAgeInSeconds());
        }
    }

    protected static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual, boolean includeMetadata,
        boolean includeSnapshot) {
        return assertSharesAreEqual(expected, actual, includeMetadata, includeSnapshot, false);
    }

    protected static boolean assertSharesAreEqual(ShareItem expected, ShareItem actual, boolean includeMetadata,
        boolean includeSnapshot, boolean includeDeleted) {
        if (expected == null) {
            return actual == null;
        } else {
            if (!Objects.equals(expected.getName(), actual.getName())) {
                return false;
            }

            if (includeMetadata && !Objects.equals(expected.getMetadata(), actual.getMetadata())) {
                return false;
            }
            if (includeSnapshot && !Objects.equals(expected.getSnapshot(), actual.getSnapshot())) {
                return false;
            }

            if (expected.getProperties() == null) {
                return actual.getProperties() == null;
            } else {
                if (includeDeleted && (expected.getProperties().getDeletedTime() == null
                    ^ actual.getProperties().getDeletedTime() == null)) {
                    return false;
                }
                return Objects.equals(expected.getProperties().getQuota(), actual.getProperties().getQuota());
            }
        }
    }

    protected static String createRandomFileWithLength(int size, URL folder, String fileName) throws IOException {
        String path = folder.getPath();
        if (path == null) {
            throw LOGGER.logExceptionAsError(new RuntimeException("The folder path does not exist."));
        }

        Path folderPaths = new File(path).toPath();
        if (!Files.exists(folderPaths)) {
            Files.createDirectory(folderPaths);
        }
        File randomFile = new File(folderPaths.toString(), fileName);
        RandomAccessFile raf = new RandomAccessFile(randomFile, "rw");
        raf.setLength(size);
        raf.close();
        return randomFile.getPath();
    }


    protected static void deleteFileIfExists(String folder, String fileName) throws IOException {
        // Clean up all temporary generated files
        File dir = new File(folder);
        if (dir.isDirectory()) {
            Path filePath = dir.toPath().resolve(fileName);
            Files.deleteIfExists((filePath));
        }
    }

    protected static String getPermissionFromFormat(FilePermissionFormat filePermissionFormat) {
        if (filePermissionFormat == null || filePermissionFormat == FilePermissionFormat.SDDL) {
            return "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527" +
                "-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S" +
                ":NO_ACCESS_CONTROL";
        } else {
            return "AQAUhGwAAACIAAAAAAAAABQAAAACAFgAAwAAAAAAFAD/AR8AAQEAAAAAAAUSAAAAAAAYAP8BHwABAgAAAAAABS" +
                "AAAAAgAgAAAAAkAKkAEgABBQAAAAAABRUAAABZUbgXZnJdJWRjOwuMmS4AAQUAAAAAAAUVAAAAoGXPfnhLm1/nfIdwr" +
                "/1IAQEFAAAAAAAFFQAAAKBlz354S5tf53yHcAECAAA=";
        }
    }


//    protected static byte[] getBytes(InputStream is) {
//        ByteArrayOutputStream answer = new ByteArrayOutputStream();
//        // reading the content of the stream within a byte buffer
//        byte[] byteBuffer = new byte[8192];
//        int nbByteRead /* = 0*/;
//        try (is) {
//            while ((nbByteRead = is.read(byteBuffer)) != -1) {
//                // appends buffer
//                answer.write(byteBuffer, 0, nbByteRead);
//            }
//        } catch (IOException e) {
//            throw LOGGER.logExceptionAsError(new RuntimeException(e));
//        }
//        return answer.toByteArray();
//    }

    protected static boolean isAllWhitespace(String input) {
        return input.matches("\\s*");
    }

    /**
     * Copies the InputStream contents to the destination byte channel.
     * @param src Bytes source.
     * @param dst Bytes destination.
     * @param copySize Size of array to copy contents with.
     * @return Total number of bytes read from src.
     */
    protected static int copy(InputStream src, SeekableByteChannel dst, int copySize) throws IOException {
        int read;
        int totalRead = 0;
        byte[] temp = new byte[copySize];
        while ((read = src.read(temp)) != -1) {
            totalRead += read;
            int written = 0;
            while (written < read) {
                written += dst.write(ByteBuffer.wrap(temp, written, read - written));
            }
        }
        return totalRead;
    }

    /**
     * Copies the InputStream contents to the destination byte channel.
     * @param src Bytes source.
     * @param dst Bytes destination.
     * @param copySize Size of array to copy contents with.
     * @return Total number of bytes read from src.
     */
    static int copy(SeekableByteChannel src, OutputStream dst, int copySize) throws IOException {
        int read;
        int totalRead = 0;
        byte[] temp = new byte[copySize];
        ByteBuffer bb = ByteBuffer.wrap(temp);
        while ((read = src.read(bb)) != -1) {
            totalRead += read;
            dst.write(temp, 0, read);
            bb.clear();
        }
        return totalRead;
    }

    protected static Stream<Arguments> startCopyWithCopySourceFileErrorSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, false),
            Arguments.of(false, true, false, false),
            Arguments.of(false, false, true, false),
            Arguments.of(false, false, false, true)
        );
    }

    protected static Stream<Arguments> listRangesDiffSupplier() {
        return Stream.of(
            Arguments.of(FileShareTestHelper.createFileRanges(), FileShareTestHelper.createFileRanges(),
                FileShareTestHelper.createFileRanges(), FileShareTestHelper.createClearRanges()),
            Arguments.of(FileShareTestHelper.createFileRanges(0, 511), FileShareTestHelper.createFileRanges(),
                FileShareTestHelper.createFileRanges(0, 511), FileShareTestHelper.createClearRanges()),
            Arguments.of(FileShareTestHelper.createFileRanges(), FileShareTestHelper.createFileRanges(0, 511),
                FileShareTestHelper.createFileRanges(), FileShareTestHelper.createClearRanges(0, 511)),
            Arguments.of(FileShareTestHelper.createFileRanges(0, 511), FileShareTestHelper.createFileRanges(512, 1023),
                FileShareTestHelper.createFileRanges(0, 511),
                FileShareTestHelper.createClearRanges(512, 1023)),
            Arguments.of(FileShareTestHelper.createFileRanges(0, 511, 1024, 1535),
                FileShareTestHelper.createFileRanges(512, 1023, 1536, 2047),
                FileShareTestHelper.createFileRanges(0, 511, 1024, 1535),
                FileShareTestHelper.createClearRanges(512, 1023, 1536, 2047))
        );
    }

    protected static Stream<Arguments> listFilesAndDirectoriesSupplier() {
        return Stream.of(
            Arguments.of(new String[]{"a", "b", "c"}, new String[]{"d", "e"}),
            Arguments.of(new String[]{"a", "c", "e"}, new String[]{"b", "d"}));
    }

    protected static Stream<Arguments> startCopyArgumentsSupplier() {
        return Stream.of(
            Arguments.of(true, false, false, false, PermissionCopyModeType.OVERRIDE),
            Arguments.of(false, true, false, false, PermissionCopyModeType.OVERRIDE),
            Arguments.of(false, false, true, false, PermissionCopyModeType.SOURCE),
            Arguments.of(false, false, false, true, PermissionCopyModeType.SOURCE));
    }

    protected static Stream<Arguments> getPropertiesPremiumSupplier() {
        return Stream.of(
            Arguments.of(Constants.HeaderConstants.SMB_PROTOCOL, null),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.ALL_SQUASH),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.NO_ROOT_SQUASH),
            Arguments.of(Constants.HeaderConstants.NFS_PROTOCOL, ShareRootSquash.ROOT_SQUASH)
        );
    }

    protected static Stream<Arguments> getStatisticsSupplier() {
        return Stream.of(
            Arguments.of(0, 0),
            Arguments.of(Constants.KB, 1),
            Arguments.of(Constants.GB, 1),
            Arguments.of((long) 3 * Constants.GB, 3));
    }

    protected static Stream<Arguments> createFileInvalidArgsSupplier() {
        return Stream.of(Arguments.of("testfile:", 1024, 400, ShareErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("fileName", -1, 400, ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    protected static Stream<Arguments> createFileMaxOverloadInvalidArgsSupplier() {
        return Stream.of(Arguments.of("testfile:", 1024, null, Collections.singletonMap("testmetadata", "value"),
                ShareErrorCode.INVALID_RESOURCE_NAME),
            Arguments.of("fileName", -1, null, Collections.singletonMap("testmetadata", "value"),
                ShareErrorCode.OUT_OF_RANGE_INPUT),
            Arguments.of("fileName", 1024, new ShareFileHttpHeaders().setContentMd5(new byte[0]),
                Collections.singletonMap("testmetadata", "value"), ShareErrorCode.INVALID_HEADER_VALUE),
            Arguments.of("fileName", 1024, null, Collections.singletonMap("", "value"),
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    protected static Stream<Arguments> createFileServiceShareWithInvalidArgsSupplier() {
        return Stream.of(
            Arguments.of(Collections.singletonMap("invalid#", "value"), 1, 400, ShareErrorCode.INVALID_METADATA),
            Arguments.of(Collections.singletonMap("testmetadata", "value"), -1, 400,
                ShareErrorCode.INVALID_HEADER_VALUE));
    }
}
