// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.models.ClearRange;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileCreateOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareFileSetPropertiesOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileAsyncApiTests extends FileShareTestBase {
    private ShareFileAsyncClient primaryFileAsyncClient;
    private ShareAsyncClient shareAsyncClient;
    private String shareName;
    private String filePath;
    private static Map<String, String> testMetadata;
    private static ShareFileHttpHeaders httpHeaders;
    private FileSmbProperties smbProperties;
    private static final String FILE_PERMISSION = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        filePath = generatePathName();
        shareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
        shareAsyncClient.create().block();
        primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getFileURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath);
        String fileURL = primaryFileAsyncClient.getFileUrl();
        assertEquals(expectURL, fileURL);
    }

    @Test
    public void createFile() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();
    }

    @Test
    public void createFileError() {
        StepVerifier.create(primaryFileAsyncClient.create(-1)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void createFileFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileCreateOptions options = new ShareFileCreateOptions(1024).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(options))
            .assertNext(it -> {
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                FileShareTestHelper.assertResponseStatusCode(it, 201);
            }).verifyComplete();
    }

    @Test
    public void createFileWithArgsFpk() {
        String filePermissionKey = shareAsyncClient.createPermission(FILE_PERMISSION).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, null,
            testMetadata)).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getLastModified());
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void createFileWithArgsFp() {
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, FILE_PERMISSION,
            testMetadata)).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                assertNotNull(it.getValue().getLastModified());
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void createFileWithArgsError() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(-1, null, null, null, testMetadata))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @Test
    public void createLease() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(DATA.getDefaultDataSizeLong() + 1, null, null,
            null, null, new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1);
    }

    @Test
    public void createLeaseFail() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(DATA.getDefaultDataSizeLong() + 1, null, null,
                null, null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    /*
     * Tests downloading a file using a default clientThatdoesn't have a HttpClient passed to it.
     */
//    @LiveOnly
//    @ParameterizedTest
//    @ValueSource(longs = {
//        0, // empty file
//        20, // small file
//        16 * 1024 * 1024, // medium file in several chunks
//        8 * 1026 * 1024 + 10, // medium file not aligned to block
//        50 * Constants.MB // large file requiring multiple requests
//    })
//    public void downloadFileBufferCopy(long fileSize) throws IOException {
//        ShareServiceAsyncClient shareServiceAsyncClient = new ShareServiceClientBuilder()
//            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
//            .buildAsyncClient();
//
//        ShareFileAsyncClient fileClient = shareServiceAsyncClient.getShareAsyncClient(shareName)
//            .createFile(filePath, fileSize).block();
//
//        File file = FileShareTestHelper.getRandomFile((int) fileSize);
//        fileClient.uploadFromFile(file.toPath().toString()).block();
//        File outFile = new File(generatePathName() + ".txt");
//        if (outFile.exists()) {
//            assertTrue(outFile.delete());
//        }
//        fileClient.downloadToFile(outFile.toPath().toString()).block();
//        FileShareTestHelper.compareFiles(file, outFile, 0, fileSize);
//
//        // cleanup
//        shareServiceAsyncClient.deleteShare(shareName).block();
//        outFile.delete();
//        file.delete();
//    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {
        0, // empty file
        20, // small file
        16 * 1024 * 1024, // medium file in several chunks
        8 * 1026 * 1024 + 10, // medium file not aligned to block
        50 * Constants.MB // large file requiring multiple requests
    })
    public void downloadFileBufferCopy(int fileSize) throws IOException {
        ShareServiceAsyncClient shareServiceAsyncClient = new ShareServiceClientBuilder()
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .buildAsyncClient();

        ShareFileAsyncClient fileClient = shareServiceAsyncClient.getShareAsyncClient(shareName)
            .createFile(filePath, fileSize).block();

        File file = FileShareTestHelper.getRandomFile(fileSize);
        assertNotNull(fileClient);
        fileClient.uploadFromFile(file.toPath().toString()).block();
        File outFile = new File(generatePathName() + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        fileClient.downloadToFile(outFile.toPath().toString()).block();
        assertTrue(FileShareTestHelper.compareFiles(file, outFile, 0, fileSize));

        //cleanup
        shareServiceAsyncClient.deleteShare(shareName).block();
        outFile.delete();
        file.delete();
    }

    @Test
    public void uploadAndDownloadData() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();

        StepVerifier.create(primaryFileAsyncClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null)).assertNext(response -> {
            assertTrue((response.getStatusCode() == 200) || (response.getStatusCode() == 206));
            ShareFileDownloadHeaders headers = response.getDeserializedHeaders();
            assertEquals(DATA.getDefaultDataSizeLong(), headers.getContentLength());
            assertNotNull(headers.getETag());
            assertNotNull(headers.getLastModified());
            assertNotNull(headers.getFilePermissionKey());
            assertNotNull(headers.getFileAttributes());
            assertNotNull(headers.getFileLastWriteTime());
            assertNotNull(headers.getFileCreationTime());
            assertNotNull(headers.getFileChangeTime());
            assertNotNull(headers.getFileParentId());
            assertNotNull(headers.getFileId());
            FluxUtil.collectBytesInByteBufferStream(response.getValue())
                .flatMap(actualData -> {
                    assertArrayEquals(DATA.getDefaultBytes(), actualData);
                    return Mono.empty();
                });
        }).verifyComplete();
    }

    @Test
    public void uploadAndDownloadDataWithArgs() {
        primaryFileAsyncClient.create(1024).block();

        StepVerifier.create(primaryFileAsyncClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong()).setOffset(1L)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1,
            DATA.getDefaultDataSizeLong()), true)).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 206);
                assertEquals(DATA.getDefaultDataSizeLong(), it.getDeserializedHeaders().getContentLength());
                FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(actualData -> {
                    assertArrayEquals(DATA.getDefaultBytes(), actualData);
                    return Mono.empty();
                });
            }).verifyComplete();
    }

    @Test
    public void uploadDataError() {
        StepVerifier.create(primaryFileAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong()))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void uploadLease() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
            DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())
            .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseId)))).expectNextCount(1);
    }

    @Test
    public void uploadLeaseFail() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())
                .setRequestConditions(new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("uploadDataLengthMismatchSupplier")
    public void uploadDataLengthMismatch(long size, String errMsg) {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), size).setOffset(0L))).verifyErrorSatisfies(it -> {
                assertInstanceOf(UnexpectedLengthException.class, it);
                assertTrue(it.getMessage().contains(errMsg));
            });
    }

    private static Stream<Arguments> uploadDataLengthMismatchSupplier() {
        return Stream.of(
            Arguments.of(6, "more than"),
            Arguments.of(8, "less than"));
    }

    @Test
    public void downloadDataError() {
        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 1023L), false))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void downloadLease() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null, null, new ShareRequestConditions()
            .setLeaseId(leaseId))).expectNextCount(1);
    }

    @Test
    public void downloadLeaseFail() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null, null, new ShareRequestConditions()
            .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);
    }

    @Test
    public void uploadAndClearRange() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileAsyncClient.create(fullInfoString.length()).block();
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block();

        StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 0)).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 201)).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 6L), false))
            .assertNext(it -> FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(data -> {
                for (byte b : data) {
                    assertEquals(b, 0);
                }
                return Mono.empty();
            })).verifyComplete();
    }

    @Test
    public void uploadAndClearRangeWithArgs() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileAsyncClient.create(fullInfoString.length()).block();
        primaryFileAsyncClient.uploadRange(Flux.just(fullInfoData), fullInfoString.length()).block();

        StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 1)).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 201);
        }).verifyComplete();
        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, 7L), false))
            .assertNext(it -> FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(data -> {
                for (byte b : data) {
                    assertEquals(b, 0);
                }
                return Mono.empty();
            })).verifyComplete();

        // cleanup
        fullInfoData.clear();
    }

    @Test
    public void clearRangeError() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileAsyncClient.create(fullInfoString.length()).block();
        primaryFileAsyncClient.uploadRange(Flux.just(fullInfoData), fullInfoString.length()).block();

        StepVerifier.create(primaryFileAsyncClient.clearRange(30)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE));
    }

    @Test
    public void clearRangeErrorArgs() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileAsyncClient.create(fullInfoString.length()).block();
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block();

        StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 20)).verifyErrorSatisfies(it ->
                FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE));

        // cleanup
        fullInfoData.clear();
    }

    @Test
    public void clearRangeLease() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(1, 0, new ShareRequestConditions()
            .setLeaseId(leaseId))).expectNextCount(1);
    }

    @Test
    public void clearRangeLeaseFail() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(1, 0, new ShareRequestConditions()
            .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);
    }

    @Test
    public void uploadFileDoesNotExist() {
        File uploadFile = new File(testFolder.getPath() + "/fakefile.txt");

        if (uploadFile.exists()) {
            assertTrue(uploadFile.delete());
        }
        StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile.getPath()))
            .verifyErrorSatisfies(it -> assertInstanceOf(NoSuchFileException.class, it.getCause()));

        // cleanup
        uploadFile.delete();
    }

    @Test
    public void uploadAndDownloadFileExists() throws IOException {
        String data = "Download file exists";
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        if (!downloadFile.exists()) {
            assertTrue(downloadFile.createNewFile());
        }
        primaryFileAsyncClient.create(data.length()).block();
        primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))),
            data.length()).block();

        StepVerifier.create(
            primaryFileAsyncClient.downloadToFile(downloadFile.getPath())).verifyErrorSatisfies(it ->
            assertInstanceOf(FileAlreadyExistsException.class, it.getCause()));

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void uploadAndDownloadToFileDoesNotExist() throws FileNotFoundException {
        String data = "Download file does not exist";
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        if (downloadFile.exists()) {
            assertTrue(downloadFile.delete());
        }

        primaryFileAsyncClient.create(data.length()).block();
        primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))),
            data.length()).block();

        StepVerifier.create(primaryFileAsyncClient.downloadToFile(downloadFile.getPath())).assertNext(it ->
            assertEquals(it.getContentLength(), data.length())).verifyComplete();
        Scanner scanner = new Scanner(downloadFile).useDelimiter("\\Z");
        assertEquals(data, scanner.next());
        scanner.close();

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void uploadFromFileLease() throws IOException {
        primaryFileAsyncClient.create(1024).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier.create(primaryFileAsyncClient.uploadFromFile(
            uploadFile, new ShareRequestConditions().setLeaseId(leaseId))).verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void uploadFromFileLeaseFail() throws IOException {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile, new ShareRequestConditions()
            .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void downloadToFileLease() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        primaryFileAsyncClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultFlux(),
            DATA.getDefaultDataSizeLong())).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        StepVerifier.create(primaryFileAsyncClient.downloadToFileWithResponse(
            downloadFile.toPath().toString(), null, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete();

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void downloadToFileLeaseFail() {
        primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong()).block();
        primaryFileAsyncClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultFlux(),
            DATA.getDefaultDataSizeLong())).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        StepVerifier.create(primaryFileAsyncClient.downloadToFileWithResponse(
            downloadFile.toPath().toString(), null, new ShareRequestConditions()
                .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);

        // cleanup
        downloadFile.delete();
    }

    @Disabled("Groovy version of this test was not asserting contents of result properly. Need to revisit this test.")
    @Test
    public void uploadRangeFromURL() {
        primaryFileAsyncClient.create(1024).block();
        String data = "The quick brown fox jumps over the lazy dog";
        long sourceOffset = 5;
        int length = 5;
        long destinationOffset = 0;

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block();
        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl()).buildFileAsyncClient();

        client.create(1024).block();
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileAsyncClient.getFileUrl()
            + "?" + sasToken).block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.download())).assertNext(it -> {
            String result = new String(it);
            for (int i = 0; i < length; i++) {
                assertEquals(result.charAt((int) (destinationOffset + i)), data.charAt((int) (sourceOffset + i)));
            }
        }).verifyComplete();
    }

    /*@RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void uploadRangeFromURLSourceErrorAndStatusCode() {
        ShareFileAsyncClient destinationClient = shareAsyncClient.getFileClient(generatePathName());

        StepVerifier.create(primaryFileAsyncClient.create(1024).then(destinationClient.create(1024))
            .then(destinationClient.uploadRangeFromUrl(5, 0, 0, primaryFileAsyncClient.getFileUrl())))
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertTrue(e.getStatusCode() == 401);
                assertTrue(e.getServiceMessage().contains("NoAuthenticationInformation"));
                assertTrue(e.getServiceMessage().contains("Server failed to authenticate the request. Please refer to the information in the www-authenticate header."));
            });
    }*/

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadRangeFromURLOAuth() {
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceClientAsyncSharedKey(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient = oAuthServiceClient.getShareAsyncClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create().block();
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(1024).block();

        String data = "The quick brown fox jumps over the lazy dog";
        int sourceOffset = 5;
        int length = 5;
        int destinationOffset = 0;

        fileClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block();
        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(fileClient.getShareName())
            .setFilePath(fileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        String fileNameDest = generatePathName();
        ShareFileAsyncClient fileClientDest = dirClient.getFileClient(fileNameDest);
        fileClientDest.create(1024).block();

        StepVerifier.create(fileClientDest.uploadRangeFromUrlWithResponse(length,
            destinationOffset, sourceOffset, fileClient.getFileUrl() + "?" + sasToken))
            .assertNext(r -> assertEquals(r.getStatusCode(), 201))
            .verifyComplete();

        StepVerifier.create(fileClientDest.downloadWithResponse(null)
            .flatMap(r -> {
                assertTrue(r.getStatusCode() == 200 || r.getStatusCode() == 206);
                ShareFileDownloadHeaders headers = r.getDeserializedHeaders();

                assertNotNull(headers.getETag());
                assertNotNull(headers.getLastModified());
                assertNotNull(headers.getFilePermissionKey());
                assertNotNull(headers.getFileAttributes());
                assertNotNull(headers.getFileLastWriteTime());
                assertNotNull(headers.getFileCreationTime());
                assertNotNull(headers.getFileChangeTime());
                assertNotNull(headers.getFileParentId());
                assertNotNull(headers.getFileId());

                return FluxUtil.collectBytesInByteBufferStream(r.getValue());
            }))
            .assertNext(bytes -> {
                //u
                assertEquals(bytes[0], 117);
            })
            .verifyComplete();
    }

    @Test
    public void uploadRangeFromURLLease() {
        primaryFileAsyncClient.create(1024).block();
        String data = "The quick brown fox jumps over the lazy dog";
        long sourceOffset = 5;
        int length = 5;
        long destinationOffset = 0;

        primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block();
        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl()).buildFileAsyncClient();

        client.create(1024).block();
        String leaseId = createLeaseClient(client).acquireLease().block();
        StepVerifier.create(client.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset,
            primaryFileAsyncClient.getFileUrl() + "?" + sasToken, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete();
    }

    @Test
    public void uploadRangeFromURLLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        String data = "The quick brown fox jumps over the lazy dog";
        long sourceOffset = 5;
        int length = 5;
        long destinationOffset = 0;

        primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block();
        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl()).buildFileAsyncClient();

        client.create(1024).block();
        createLeaseClient(client).acquireLease().block();
        StepVerifier.create(client.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset,
            primaryFileAsyncClient.getFileUrl().toString() + "?" + sasToken,
            new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void startCopy() {
        primaryFileAsyncClient.create(1024).block();
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileAsyncClient.getFileUrl();

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, new ShareFileCopyOptions(), null));
        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete().verify(Duration.ofMinutes(1));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithArgs(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String filePermissionKey = shareAsyncClient.createPermission(FILE_PERMISSION).block();
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey);
        }

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, smbProperties, setFilePermission ? FILE_PERMISSION : null,
                permissionType, ignoreReadOnly, setArchiveAttribute, null, null, null));
        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap "
        + "between the time subscribed and the time we start observing events.")
    @Test
    public void startCopyError() {
        primaryFileAsyncClient.create(1024).block();
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileAsyncClient.getFileUrl();

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, new ShareFileCopyOptions(), null));
        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    /*@RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void startCopySourceErrorAndStatusCode() {
        primaryFileAsyncClient.create(1024);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy("https://error.file.core.windows.net/garbage", new ShareFileCopyOptions(), null));

        StepVerifier.create(primaryFileAsyncClient.create(1024).thenMany(poller))
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertTrue(e.getStatusCode() == 400);
                assertTrue(e.getServiceMessage().contains("InvalidUri"));
                assertTrue(e.getServiceMessage().contains("The requested URI does not represent any resource on the server."));
            });
    }*/

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap "
        + "between the time subscribed and the time we start observing events.")
    @Test
    public void startCopyLease() {
        primaryFileAsyncClient.create(1024).block();
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, null, null, null, false, false, null, null,
                new ShareRequestConditions().setLeaseId(leaseId)));
        StepVerifier.create(poller).assertNext(it -> {
            assertNotNull(it.getValue().getCopyId());
        }).expectComplete().verify(Duration.ofMinutes(1));
    }

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap "
        + "between the time subscribed and the time we start observing events.")
    @Test
    public void startCopyLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, null, null, null, false, false, null, null,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())));
        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithOptions(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String filePermissionKey = shareAsyncClient.createPermission(FILE_PERMISSION).block();
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey);
        }
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(setFilePermission ? FILE_PERMISSION : null)
            .setIgnoreReadOnly(ignoreReadOnly)
            .setArchiveAttribute(setArchiveAttribute)
            .setPermissionCopyModeType(permissionType);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @Test
    public void startCopyWithOptionsIgnoreReadOnlyAndSetArchive() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setIgnoreReadOnly(true)
            .setArchiveAttribute(true);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsFilePermission() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);

        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(poller).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));

        FileSmbProperties properties = Objects.requireNonNull(primaryFileAsyncClient.getProperties().block())
            .getSmbProperties();

        FileShareTestHelper.compareDatesWithPrecision(properties.getFileCreationTime(),
            smbProperties.getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileLastWriteTime(),
            smbProperties.getFileLastWriteTime());
        assertEquals(properties.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-01-05")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void startCopyFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        Mono<ShareFileProperties> response =  primaryFileAsyncClient.create(1024)
            .then(setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null))
                .last())
            .flatMap(r -> {
                assertEquals(r.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
                return primaryFileAsyncClient.getProperties();
            });

        StepVerifier.create(response)
            .assertNext(r -> assertNotNull(r.getSmbProperties().getFilePermissionKey()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsChangeTime() {
        ShareFileInfo client = primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileChangeTime(testResourceNamer.now());

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        StepVerifier.create(poller).assertNext(it ->
            assertNotNull(it.getValue().getCopyId())).expectComplete().verify(Duration.ofMinutes(1));

        FileShareTestHelper.compareDatesWithPrecision(smbProperties.getFileChangeTime(),
            Objects.requireNonNull(primaryFileAsyncClient.getProperties().block()).getSmbProperties()
                .getFileChangeTime());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsCopySmbFilePropertiesPermissionKey() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String filePermissionKey = shareAsyncClient.createPermission(FILE_PERMISSION).block();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs)
            .setFilePermissionKey(filePermissionKey);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        StepVerifier.create(poller).assertNext(it ->
            assertNotNull(it.getValue().getCopyId())).expectComplete()
            .verify(Duration.ofMinutes(1));

        FileSmbProperties properties = Objects.requireNonNull(primaryFileAsyncClient.getProperties().block())
            .getSmbProperties();

        FileShareTestHelper.compareDatesWithPrecision(properties.getFileCreationTime(),
            smbProperties.getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileLastWriteTime(),
            smbProperties.getFileLastWriteTime());
        assertEquals(properties.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
    }

    @Test
    public void startCopyWithOptionsLease() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        StepVerifier.create(poller).assertNext(it ->
            assertNotNull(it.getValue().getCopyId())).expectComplete().verify(Duration.ofMinutes(1));
    }

    @Test
    public void startCopyWithOptionsInvalidLease() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = testResourceNamer.randomUuid();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions);

        // exception: LeaseNotPresentWithFileOperation
        assertThrows(ShareStorageException.class, () -> setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null)).blockFirst());
    }

    @Test
    public void startCopyWithOptionsMetadata() {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setMetadata(testMetadata);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        StepVerifier.create(poller).assertNext(it ->
            assertNotNull(it.getValue().getCopyId())).expectComplete().verify(Duration.ofMinutes(1));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsWithOriginalSmbProperties() {
        primaryFileAsyncClient.create(1024).block();
        ShareFileProperties initialProperties = primaryFileAsyncClient.getProperties().block();
        assertNotNull(initialProperties);
        OffsetDateTime creationTime = initialProperties.getSmbProperties().getFileCreationTime();
        OffsetDateTime lastWrittenTime = initialProperties.getSmbProperties().getFileLastWriteTime();
        OffsetDateTime changedTime = initialProperties.getSmbProperties().getFileChangeTime();
        EnumSet<NtfsFileAttributes> fileAttributes = initialProperties.getSmbProperties().getNtfsFileAttributes();

        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(true)
            .setLastWrittenOn(true)
            .setChangedOn(true)
            .setFileAttributes(true);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions)
            .setSmbPropertiesToCopy(list);

        PollerFlux<ShareFileCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        StepVerifier.create(poller).assertNext(it ->
            assertNotNull(it.getValue().getCopyId())).expectComplete().verify(Duration.ofMinutes(1));

        FileSmbProperties resultProperties = Objects.requireNonNull(primaryFileAsyncClient.getProperties().block())
            .getSmbProperties();

        FileShareTestHelper.compareDatesWithPrecision(creationTime, resultProperties.getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(lastWrittenTime, resultProperties.getFileLastWriteTime());
        FileShareTestHelper.compareDatesWithPrecision(changedTime, resultProperties.getFileChangeTime());
        assertEquals(fileAttributes, resultProperties.getNtfsFileAttributes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyWithCopySourceFileErrorSupplier")
    public void startCopyWithOptionsCopySourceFileError(boolean createdOn, boolean lastWrittenOn, boolean changedOn,
        boolean fileAttributes) {
        primaryFileAsyncClient.create(1024).block();
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(createdOn)
            .setLastWrittenOn(lastWrittenOn)
            .setChangedOn(changedOn)
            .setFileAttributes(fileAttributes);

        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFileChangeTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)
            .setSmbPropertiesToCopy(list);

        assertThrows(IllegalArgumentException.class, () -> setPlaybackPollerFluxPollInterval(
            primaryFileAsyncClient.beginCopy(sourceURL, options, null)));
    }

    @Disabled("TODO: Need to find a way of mocking pending copy status")
    @Test
    public void abortCopy() {
        //TODO: Need to find a way of mocking pending copy status
    }

    @Test
    public void deleteFile() {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse()).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteFileError() {
        StepVerifier.create(primaryFileAsyncClient.delete()).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteFileLease() {
        primaryFileAsyncClient.create(1024).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse(new ShareRequestConditions().setLeaseId(leaseId)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse(new ShareRequestConditions()
                .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFile() {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.deleteIfExistsWithResponse(null)).assertNext(it ->
            FileShareTestHelper.assertResponseStatusCode(it, 202)).verifyComplete();
    }

    @Test
    public void deleteFileThatDoesNotExist() {
        ShareFileAsyncClient client = primaryFileAsyncClient.getFileAsyncClient(generateShareName());
        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null).block();
        assertNotNull(response);
        assertFalse(response.getValue());
        assertEquals(response.getStatusCode(), 404);
        assertNotEquals(Boolean.TRUE, client.exists().block());
    }

    @Test
    public void deleteIfExistsFileThatWasAlreadyDeleted() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null, null).block();
        assertEquals(Boolean.TRUE, primaryFileAsyncClient.deleteIfExists().block());
        assertNotEquals(Boolean.TRUE, primaryFileAsyncClient.deleteIfExists().block());
    }

    @Test
    public void deleteIfExistsFileLease() {
        primaryFileAsyncClient.create(1024).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.deleteIfExistsWithResponse(new ShareRequestConditions()
            .setLeaseId(leaseId))).assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.deleteIfExistsWithResponse(new ShareRequestConditions()
            .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);
    }

    @Test
    public void getProperties() {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse()).assertNext(it -> {
            FileShareTestHelper.assertResponseStatusCode(it, 200);
            assertNotNull(it.getValue().getETag());
            assertNotNull(it.getValue().getLastModified());
            assertNotNull(it.getValue().getLastModified());
            assertNotNull(it.getValue().getSmbProperties());
            assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
            assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
            assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
            assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
            assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
            assertNotNull(it.getValue().getSmbProperties().getParentId());
            assertNotNull(it.getValue().getSmbProperties().getFileId());
        }).verifyComplete();
    }

    @Test
    public void getPropertiesLease() {
        primaryFileAsyncClient.create(1024).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();
        StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse(
            new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void getPropertiesLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse(
            new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryFileAsyncClient.getProperties())
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @Test
    public void setHttpHeadersFpk() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String filePermissionKey = shareAsyncClient.createPermission(FILE_PERMISSION).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @Test
    public void setHttpHeadersFp() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());

        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties,
            FILE_PERMISSION)).assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void setFileHttpHeadersFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        primaryFileAsyncClient.create(512);

        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileSetPropertiesOptions options = new ShareFileSetPropertiesOptions(512)
            .setFilePermissions(new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat));

        StepVerifier.create(primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.setPropertiesWithResponse(options)))
            .assertNext(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();
    }

    @Test
    public void setHttpHeadersLease() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null,
                new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void setHttpHeadersLeaseFail() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null,
                new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }


    @Test
    public void setHttpHeadersError() {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        StepVerifier.create(primaryFileAsyncClient.setProperties(-1, null, null, null)).verifyErrorSatisfies(it ->
            FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @Test
    public void setMetadata() {
        primaryFileAsyncClient.createWithResponse(1024, httpHeaders, null, null, testMetadata).block();
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        StepVerifier.create(primaryFileAsyncClient.getProperties()).assertNext(it ->
            assertEquals(testMetadata, it.getMetadata())).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(updatedMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200)).verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.getProperties()).assertNext(it ->
            assertEquals(updatedMetadata, it.getMetadata())).verifyComplete();
    }

    @Test
    public void setMetadataError() {
        primaryFileAsyncClient.create(1024).block();
        Map<String, String> errorMetadata = Collections.singletonMap("", "value");
        StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(errorMetadata))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void setMetadataLease() {
        primaryFileAsyncClient.create(1024).block();
        Map<String, String> metadata = Collections.singletonMap("key", "value");
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(metadata,
            new ShareRequestConditions().setLeaseId(leaseId))).expectNextCount(1).verifyComplete();
    }

    @Test
    public void setMetadataLeaseFail() {
        primaryFileAsyncClient.create(1024).block();
        Map<String, String> metadata = Collections.singletonMap("key", "value");
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(metadata,
            new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void listRanges() throws IOException {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileAsyncClient.uploadFromFile(uploadFile).block();

        StepVerifier.create(primaryFileAsyncClient.listRanges()).assertNext(it -> {
            assertEquals(0, it.getStart());
            assertEquals(1023, it.getEnd());
        }).verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesWithRange() throws IOException {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileAsyncClient.uploadFromFile(uploadFile).block();

        StepVerifier.create(primaryFileAsyncClient.listRanges(new ShareFileRange(0, 511L)))
            .assertNext(it -> {
                assertEquals(0, it.getStart());
                assertEquals(511, it.getEnd());
            }).verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesLease() throws IOException {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileAsyncClient.uploadFromFile(uploadFile).block();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.listRanges(null, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesLeaseFail() throws IOException {
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileAsyncClient.uploadFromFile(uploadFile).block();
        createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        StepVerifier.create(primaryFileAsyncClient.listRanges(null, new ShareRequestConditions()
                .setLeaseId(testResourceNamer.randomUuid()))).verifyError(ShareStorageException.class);

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listRangesDiffSupplier")
    public void listRangesDiff(List<FileRange> rangesToUpdate, List<FileRange> rangesToClear,
        List<FileRange> expectedRanges, List<ClearRange> expectedClearRanges) {
        String snapshotId = primaryFileAsyncClient.create(4 * Constants.MB)
            .then(primaryFileAsyncClient.upload(Flux.just(FileShareTestHelper.getRandomByteBuffer(4 * Constants.MB)),
                4 * Constants.MB))
            .then(primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName())
                .createSnapshot()
                .map(ShareSnapshotInfo::getSnapshot))
            .block();

        Flux.fromIterable(rangesToUpdate)
            .flatMap(it -> {
                int size = (int) (it.getEnd() - it.getStart() + 1);
                return primaryFileAsyncClient.uploadWithResponse(Flux.just(
                    FileShareTestHelper.getRandomByteBuffer(size)), size, it.getStart());
            }).blockLast();

        Flux.fromIterable(rangesToClear)
            .flatMap(it -> primaryFileAsyncClient.clearRangeWithResponse(it.getEnd() - it.getStart() + 1,
                it.getStart()))
            .blockLast();

        StepVerifier.create(primaryFileAsyncClient.listRangesDiff(snapshotId)).assertNext(it -> {
            assertEquals(it.getRanges().size(), expectedRanges.size());
            assertEquals(it.getClearRanges().size(), expectedClearRanges.size());

            for (int i = 0; i < expectedRanges.size(); i++) {
                FileRange actualRange = it.getRanges().get(i);
                FileRange expectedRange = expectedRanges.get(i);
                assertEquals(expectedRange.getStart(), actualRange.getStart());
                assertEquals(expectedRange.getEnd(), actualRange.getEnd());
            }

            for (int i = 0; i < expectedClearRanges.size(); i++) {
                ClearRange actualRange = it.getClearRanges().get(i);
                ClearRange expectedRange = expectedClearRanges.get(i);
                assertEquals(expectedRange.getStart(), actualRange.getStart());
                assertEquals(expectedRange.getEnd(), actualRange.getEnd());
            }
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("listRangesDiffWithRenameSupplier")
    public void listRangesDiffWithRename(Boolean renameSupport) throws IOException {
        //create a file
        String fileName = generateShareName();
        //upload some content and take snapshot
        ShareSnapshotInfo previousSnapshot = primaryFileAsyncClient.create(Constants.MB)
            .then(primaryFileAsyncClient.uploadFromFile(FileShareTestHelper.createRandomFileWithLength(Constants.KB, testFolder, fileName)))
            .then(primaryFileAsyncClient.uploadRange(Flux.just(FileShareTestHelper.getRandomByteBuffer(Constants.KB)), Constants.KB))
            .then(primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName()).createSnapshot())
            .block();

        //rename file
        ShareFileAsyncClient destFile = primaryFileAsyncClient.rename(generatePathName()).block();

        //take another snapshot
        primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName()).createSnapshot().block();

        //setup options
        ShareFileListRangesDiffOptions options = new ShareFileListRangesDiffOptions(previousSnapshot.getSnapshot());
        options.setRenameIncluded(renameSupport);

        //call
        if (renameSupport == null || !renameSupport) {
            StepVerifier.create(destFile.listRangesDiffWithResponse(options))
                .verifyErrorSatisfies(r -> {
                    ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                    assertEquals(ShareErrorCode.PREVIOUS_SNAPSHOT_NOT_FOUND, e.getErrorCode());
                });
        } else {
            StepVerifier.create(destFile.listRangesDiffWithResponse(options))
                .assertNext(r -> {
                    assertEquals(200, r.getStatusCode());
                    assertEquals(0, r.getValue().getRanges().size());
                })
                .verifyComplete();
        }

        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
        destFile.delete().block();
    }

    private static Stream<Arguments> listRangesDiffWithRenameSupplier() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(false),
            Arguments.of((Boolean) null));
    }

    @Test
    public void listHandles() {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.listHandles()).verifyComplete();
    }

    @Test
    public void listHandlesWithMaxResult() {
        primaryFileAsyncClient.create(1024).block();
        StepVerifier.create(primaryFileAsyncClient.listHandles(2)).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseHandleMin() {
        primaryFileAsyncClient.create(512).block();
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("1")).assertNext(it -> {
            assertEquals(it.getClosedHandles(), 0);
            assertEquals(it.getFailedHandles(), 0);
        }).verifyComplete();
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        primaryFileAsyncClient.create(512).block();
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("invalidHandleId"))
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseAllHandlesMin() {
        primaryFileAsyncClient.create(512).block();

        StepVerifier.create(primaryFileAsyncClient.forceCloseAllHandles())
            .assertNext(it -> {
                assertEquals(it.getClosedHandles(), 0);
                assertEquals(it.getFailedHandles(), 0);
            }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void renameFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        Mono<ShareFileProperties> response = primaryFileAsyncClient.create(512)
            .then(primaryFileAsyncClient.renameWithResponse(options))
            .flatMap(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                return r.getValue().getProperties();
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareFileAsyncClient shareSnapshotClient = fileBuilderHelper(shareName, filePath).snapshot(snapshot)
            .buildFileAsyncClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryFileAsyncClient.getShareName());
    }

    @Test
    public void getFilePath() {
        assertEquals(filePath, primaryFileAsyncClient.getFilePath());
    }

    @Test
    public void defaultAudience() {
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        fileClient.create(Constants.KB).block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(null) /* should default to "https://storage.azure.com/" */);

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        fileClient.create(Constants.KB).block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience(shareAsyncClient.getAccountName())));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void audienceError() {
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        fileClient.create(Constants.KB).block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(aadFileClient.exists())
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
            });
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", shareAsyncClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        fileClient.create(Constants.KB).block();
        ShareServiceAsyncClient oAuthServiceClient =
            getOAuthServiceAsyncClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(audience));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(aadFileClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-02-04")
    @Test
    public void listHandlesClientName() {
        ShareAsyncClient client = primaryFileServiceAsyncClient.getShareAsyncClient("testing");
        ShareDirectoryAsyncClient directoryClient = client.getDirectoryClient("dir1");
        ShareFileAsyncClient fileClient = directoryClient.getFileClient("test.txt");
        List<HandleItem> list = fileClient.listHandles().collectList().block();
        assertNotNull(list.get(0).getClientName());
    }
}
