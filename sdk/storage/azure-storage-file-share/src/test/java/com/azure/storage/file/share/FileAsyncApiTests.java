// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ModeCopyMode;
import com.azure.storage.file.share.models.NfsFileType;
import com.azure.storage.common.test.shared.policy.MockPartialResponsePolicy;
import com.azure.storage.file.share.models.ClearRange;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.OwnerCopyMode;
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
import com.azure.storage.file.share.models.ShareFileSymbolicLinkInfo;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileCreateHardLinkOptions;
import com.azure.storage.file.share.options.ShareFileCreateOptions;
import com.azure.storage.file.share.options.ShareFileCreateSymbolicLinkOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareFileSetPropertiesOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import com.azure.storage.file.share.specialized.ShareLeaseAsyncClient;
import org.junit.jupiter.api.Assertions;
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
import reactor.util.function.Tuple4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("deprecation")
public class FileAsyncApiTests extends FileShareTestBase {
    private ShareFileAsyncClient primaryFileAsyncClient;
    private ShareAsyncClient shareAsyncClient;
    private String shareName;
    private String filePath;
    private static Map<String, String> testMetadata;
    private static ShareFileHttpHeaders httpHeaders;
    private FileSmbProperties smbProperties;
    private static final String FILE_PERMISSION
        = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        filePath = generatePathName();
        shareAsyncClient = shareBuilderHelper(shareName).buildAsyncClient();
        shareAsyncClient.create().block();
        primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en").setContentType("application/octet-stream");
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getFileURL() {
        String accountName
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath);
        String fileURL = primaryFileAsyncClient.getFileUrl();
        assertEquals(expectURL, fileURL);
    }

    @Test
    public void createFile() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();
    }

    @Test
    public void createFileError() {
        StepVerifier.create(primaryFileAsyncClient.create(-1))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void createFileFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileCreateOptions options = new ShareFileCreateOptions(1024).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(options)).assertNext(it -> {
            assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
            FileShareTestHelper.assertResponseStatusCode(it, 201);
        }).verifyComplete();
    }

    @Test
    public void createFileWithArgsFpk() {
        StepVerifier.create(shareAsyncClient.createPermission(FILE_PERMISSION).flatMap(filePermissionKey -> {
            smbProperties.setFileCreationTime(testResourceNamer.now())
                .setFileLastWriteTime(testResourceNamer.now())
                .setFilePermissionKey(filePermissionKey);
            return primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata);
        })).assertNext(it -> {
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
        smbProperties.setFileCreationTime(testResourceNamer.now()).setFileLastWriteTime(testResourceNamer.now());

        StepVerifier
            .create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, FILE_PERMISSION,
                testMetadata))
            .assertNext(it -> {
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
            })
            .verifyComplete();
    }

    @Test
    public void createFileWithArgsError() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(-1, null, null, null, testMetadata))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @Test
    public void createLease() {
        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient.createWithResponse(DATA.getDefaultDataSizeLong() + 1, null,
                    null, null, null, new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void createLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .then(primaryFileAsyncClient.createWithResponse(DATA.getDefaultDataSizeLong() + 1, null, null, null,
                    null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @LiveOnly
    @ParameterizedTest
    @ValueSource(
        ints = {
            0, // empty file
            20, // small file
            16 * 1024 * 1024, // medium file in several chunks
            8 * 1026 * 1024 + 10, // medium file not aligned to block
            50 * Constants.MB // large file requiring multiple requests
        })
    public void downloadFileBufferCopy(int fileSize) throws IOException {
        ShareServiceAsyncClient shareServiceAsyncClient
            = new ShareServiceClientBuilder().connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
                .buildAsyncClient();

        ShareFileAsyncClient fileClient
            = shareServiceAsyncClient.getShareAsyncClient(shareName).getFileClient(filePath);

        File file = getRandomFile(fileSize);
        assertNotNull(fileClient);

        File outFile = new File(generatePathName() + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        StepVerifier.create(fileClient.create(fileSize)
            .then(fileClient.uploadFromFile(file.toPath().toString()))
            .then(fileClient.downloadToFile(outFile.toPath().toString()))
            .then(Mono.defer(() -> {
                try {
                    assertTrue(compareFiles(file, outFile, 0, fileSize));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return Mono.empty();
            }))
            .then(shareServiceAsyncClient.deleteShare(shareName))).verifyComplete();

        file.delete();
        outFile.delete();
    }

    @Test
    public void uploadAndDownloadData() {
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(primaryFileAsyncClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())))
            .flatMap(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 201);
                return primaryFileAsyncClient.downloadWithResponse(null);
            })
            .flatMap(response -> {
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
                return FluxUtil.collectBytesInByteBufferStream(response.getValue()).flatMap(actualData -> {
                    assertArrayEquals(DATA.getDefaultBytes(), actualData);
                    return Mono.empty();
                });
            })).verifyComplete();
    }

    @Test
    public void uploadAndDownloadDataWithArgs() {
        StepVerifier.create(primaryFileAsyncClient.create(1024)
            .then(primaryFileAsyncClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong()).setOffset(1L))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();

        StepVerifier
            .create(
                primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 206);
                assertEquals(DATA.getDefaultDataSizeLong(), it.getDeserializedHeaders().getContentLength());
                FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(actualData -> {
                    assertArrayEquals(DATA.getDefaultBytes(), actualData);
                    return Mono.empty();
                });
            })
            .verifyComplete();
    }

    @Test
    public void uploadDataError() {
        StepVerifier.create(primaryFileAsyncClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong()))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void uploadLease() {
        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient.uploadRangeWithResponse(
                    new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())
                        .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseId)))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadLeaseFail() {
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .then(primaryFileAsyncClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())
                    .setRequestConditions(new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())))))
            .verifyError(ShareStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("uploadDataLengthMismatchSupplier")
    public void uploadDataLengthMismatch(long size, String errMsg) {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(primaryFileAsyncClient.uploadRangeWithResponse(
                    new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), size).setOffset(0L))))
            .verifyErrorSatisfies(it -> {
                assertInstanceOf(UnexpectedLengthException.class, it);
                assertTrue(it.getMessage().contains(errMsg));
            });
    }

    private static Stream<Arguments> uploadDataLengthMismatchSupplier() {
        return Stream.of(Arguments.of(6, "more than"), Arguments.of(8, "less than"));
    }

    @Test
    public void downloadDataError() {
        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 1023L), false))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void downloadLease() {
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .flatMap(leaseId -> primaryFileAsyncClient.downloadWithResponse(null, null,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void downloadLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .then(primaryFileAsyncClient.downloadWithResponse(null, null,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void uploadAndClearRange() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));

        Mono<ShareFileUploadInfo> createAndUpload = primaryFileAsyncClient.create(fullInfoString.length())
            .then(primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()));

        StepVerifier.create(createAndUpload.then(primaryFileAsyncClient.clearRangeWithResponse(7, 0)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 6L), false))
            .assertNext(it -> FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(data -> {
                for (byte b : data) {
                    assertEquals(0, b);
                }
                return Mono.empty();
            }))
            .verifyComplete();
    }

    @Test
    public void uploadAndClearRangeWithArgs() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(primaryFileAsyncClient.create(fullInfoString.length())
            .then(primaryFileAsyncClient.uploadRange(Flux.just(fullInfoData), fullInfoString.length()))
            .then(primaryFileAsyncClient.clearRangeWithResponse(7, 1))
            .doOnNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 201))
            .then(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, 7L), false))
            .flatMap(it -> FluxUtil.collectBytesInByteBufferStream(it.getValue()).flatMap(data -> {
                for (byte b : data) {
                    assertEquals(0, b);
                }
                return Mono.empty();
            }))).verifyComplete();

        // cleanup
        fullInfoData.clear();
    }

    @Test
    public void clearRangeError() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));

        StepVerifier
            .create(primaryFileAsyncClient.create(fullInfoString.length())
                .then(primaryFileAsyncClient.uploadRange(Flux.just(fullInfoData), fullInfoString.length()))
                .then(primaryFileAsyncClient.clearRange(30)))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE));
    }

    @Test
    public void clearRangeErrorArgs() {
        String fullInfoString = "please clear the range";
        ByteBuffer fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8));

        StepVerifier
            .create(primaryFileAsyncClient.create(fullInfoString.length())
                .then(primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()))
                .then(primaryFileAsyncClient.clearRangeWithResponse(7, 20)))
            .verifyErrorSatisfies(
                it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE));

        // cleanup
        fullInfoData.clear();
    }

    @Test
    public void clearRangeLease() {
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .flatMap(leaseId -> primaryFileAsyncClient.clearRangeWithResponse(1, 0,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void clearRangeLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .then(primaryFileAsyncClient.clearRangeWithResponse(1, 0,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
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

        StepVerifier
            .create(primaryFileAsyncClient.create(data.length())
                .then(primaryFileAsyncClient
                    .uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))), data.length()))
                .then(primaryFileAsyncClient.downloadToFile(downloadFile.getPath())))
            .verifyErrorSatisfies(it -> assertInstanceOf(FileAlreadyExistsException.class, it.getCause()));

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

        StepVerifier
            .create(primaryFileAsyncClient.create(data.length())
                .then(primaryFileAsyncClient
                    .uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))), data.length()))
                .then(primaryFileAsyncClient.downloadToFile(downloadFile.getPath())))
            .assertNext(it -> assertEquals(it.getContentLength(), data.length()))
            .verifyComplete();

        Scanner scanner = new Scanner(downloadFile).useDelimiter("\\Z");
        assertEquals(data, scanner.next());
        scanner.close();

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void uploadFromFileLease() throws IOException {
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier.create(primaryFileAsyncClient.create(1024)
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .flatMap(leaseId -> primaryFileAsyncClient.uploadFromFile(uploadFile,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void uploadFromFileLeaseFail() throws IOException {
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier
            .create(primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient.uploadFromFile(uploadFile,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals("LeaseIdMismatchWithFileOperation", e.getErrorCode().getValue());
            });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void downloadToFileLease() {
        Mono<String> leaseIdMono = primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(primaryFileAsyncClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())))
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease());

        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        StepVerifier.create(leaseIdMono
            .flatMap(leaseId -> primaryFileAsyncClient.downloadToFileWithResponse(downloadFile.toPath().toString(),
                null, new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void downloadToFileLeaseFail() {
        Mono<String> leaseIdMono = primaryFileAsyncClient.create(DATA.getDefaultDataSizeLong())
            .then(primaryFileAsyncClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong())))
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease());

        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        StepVerifier
            .create(leaseIdMono
                .flatMap(leaseId -> primaryFileAsyncClient.downloadToFileWithResponse(downloadFile.toPath().toString(),
                    null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void downloadToFileWithPartialDownloads() throws Exception {
        File uploadFile = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
        uploadFile.deleteOnExit();
        Files.write(uploadFile.toPath(), DATA.getDefaultBytes());

        File outFile = new File(generatePathName() + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }
        outFile.deleteOnExit();

        MockPartialResponsePolicy policy = new MockPartialResponsePolicy(5);

        // Create a ShareFileAsyncClient for download using the custom pipeline
        ShareFileAsyncClient downloadClient = getFileAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileAsyncClient.getFileUrl(), policy);

        // Upload the test data
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSize())
            .then(primaryFileAsyncClient.uploadFromFile(uploadFile.toString()))).verifyComplete();

        StepVerifier.create(downloadClient.downloadToFile(outFile.toString()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        byte[] downloadedData = Files.readAllBytes(outFile.toPath());
        Assertions.assertArrayEquals(DATA.getDefaultBytes(), downloadedData);

        // Assert that we retried the correct number of times (5)
        assertEquals(0, policy.getTriesRemaining());

        // Assert that the range headers that were retried match what was returned from MockPartialResponsePolicy
        List<String> expectedRanges = expectedHeaderRanges();
        List<String> actualRanges = policy.getRangeHeaders();
        assertEquals(expectedRanges, actualRanges);

        // Clean up
        Files.deleteIfExists(outFile.toPath());
        Files.deleteIfExists(uploadFile.toPath());
    }

    private List<String> expectedHeaderRanges() {
        List<String> expectedRanges = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            expectedRanges.add("bytes=" + i + "-" + (DATA.getDefaultDataSize() - 1));
        }
        return expectedRanges;
    }

    @Test
    public void downloadToFileRetryExhausted() throws Exception {
        File uploadFile = File.createTempFile(CoreUtils.randomUuid().toString(), ".txt");
        uploadFile.deleteOnExit();
        Files.write(uploadFile.toPath(), DATA.getDefaultBytes());

        File outFile = new File(generatePathName() + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }
        outFile.deleteOnExit();

        MockPartialResponsePolicy policy = new MockPartialResponsePolicy(6);

        // Create a ShareFileAsyncClient for download using the custom pipeline
        ShareFileAsyncClient downloadClient = getFileAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileAsyncClient.getFileUrl(), policy);

        // Upload the test data
        StepVerifier.create(primaryFileAsyncClient.create(DATA.getDefaultDataSize())
            .then(primaryFileAsyncClient.uploadFromFile(uploadFile.toString()))).verifyComplete();

        StepVerifier.create(downloadClient.downloadToFile(outFile.toString()))
            .verifyErrorSatisfies(throwable -> assertInstanceOf(IOException.class, throwable));

        // Assert that we retried the correct number of times (5) even though the retry policy allowed for 6 retries
        assertEquals(0, policy.getTriesRemaining());

        // Assert that the range headers that were retried match what was returned from MockPartialResponsePolicy
        List<String> expectedRanges = expectedHeaderRanges();
        List<String> actualRanges = policy.getRangeHeaders();
        assertEquals(expectedRanges, actualRanges);

        // Clean up
        Files.deleteIfExists(outFile.toPath());
        Files.deleteIfExists(uploadFile.toPath());
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
        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues().setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client
            = fileBuilderHelper(shareName, "destination").endpoint(primaryFileAsyncClient.getFileUrl())
                .buildFileAsyncClient();

        client.create(1024).block();
        client
            .uploadRangeFromUrl(length, destinationOffset, sourceOffset,
                primaryFileAsyncClient.getFileUrl() + "?" + sasToken)
            .block();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.download())).assertNext(it -> {
            String result = new String(it);
            for (int i = 0; i < length; i++) {
                assertEquals(result.charAt((int) (destinationOffset + i)), data.charAt((int) (sourceOffset + i)));
            }
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadRangeFromURLOAuth() {
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceClientAsyncSharedKey(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryAsyncClient dirClient
            = oAuthServiceClient.getShareAsyncClient(shareName).getDirectoryClient(generatePathName());

        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = dirClient.getFileClient(fileName);

        String data = "The quick brown fox jumps over the lazy dog";
        int sourceOffset = 5;
        int length = 5;
        int destinationOffset = 0;

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues().setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(fileClient.getShareName())
            .setFilePath(fileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        String fileNameDest = generatePathName();
        ShareFileAsyncClient fileClientDest = dirClient.getFileClient(fileNameDest);

        StepVerifier.create(dirClient.create()
            .then(fileClient.create(1024))
            .then(fileClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()))
            .then(fileClientDest.create(1024))
            .then(fileClientDest.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset,
                fileClient.getFileUrl() + "?" + sasToken))
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return fileClientDest.downloadWithResponse(null);
            })
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
            })).assertNext(bytes -> {
                //u
                assertEquals(bytes[0], 117);
            }).verifyComplete();
    }

    @Test
    public void uploadRangeFromURLLease() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String data = "The quick brown fox jumps over the lazy dog";
        long sourceOffset = 5;
        int length = 5;
        long destinationOffset = 0;

        Mono<ShareFileUploadInfo> uploadRangeMono = createFileMono
            .then(primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()));

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues().setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client
            = fileBuilderHelper(shareName, "destination").endpoint(primaryFileAsyncClient.getFileUrl())
                .buildFileAsyncClient();

        Mono<ShareFileInfo> createDestinationFileMono = client.create(1024);
        Mono<String> leaseIdMono = createDestinationFileMono.then(createLeaseClient(client).acquireLease());

        StepVerifier.create(uploadRangeMono.then(leaseIdMono)
            .flatMap(leaseId -> client.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset,
                primaryFileAsyncClient.getFileUrl() + "?" + sasToken,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void uploadRangeFromURLLeaseFail() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String data = "The quick brown fox jumps over the lazy dog";
        long sourceOffset = 5;
        int length = 5;
        long destinationOffset = 0;

        Mono<ShareFileUploadInfo> uploadRangeMono = createFileMono
            .then(primaryFileAsyncClient.uploadRange(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()));

        StorageSharedKeyCredential credential
            = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues().setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileAsyncClient client
            = fileBuilderHelper(shareName, "destination").endpoint(primaryFileAsyncClient.getFileUrl())
                .buildFileAsyncClient();

        Mono<ShareFileInfo> createDestinationFileMono = client.create(1024);
        Mono<String> leaseIdMono = createDestinationFileMono.then(createLeaseClient(client).acquireLease());

        StepVerifier
            .create(uploadRangeMono.then(leaseIdMono)
                .then(client.uploadRangeFromUrlWithResponse(length, destinationOffset, sourceOffset,
                    primaryFileAsyncClient.getFileUrl() + "?" + sasToken,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void startCopy() {
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .thenMany(setPlaybackPollerFluxPollInterval(
                    primaryFileAsyncClient.beginCopy(sourceURL, new ShareFileCopyOptions(), null))))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithArgs(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        String sourceURL = primaryFileAsyncClient.getFileUrl();

        StepVerifier.create(primaryFileAsyncClient.create(1024)
            .then(shareAsyncClient.createPermission(FILE_PERMISSION))
            .flatMapMany(filePermissionKey -> {
                smbProperties.setFileCreationTime(testResourceNamer.now())
                    .setFileLastWriteTime(testResourceNamer.now());
                if (setFilePermissionKey) {
                    smbProperties.setFilePermissionKey(filePermissionKey);
                }
                return setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, smbProperties,
                    setFilePermission ? FILE_PERMISSION : null, permissionType, ignoreReadOnly, setArchiveAttribute,
                    null, null, null));
            }))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
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
        StepVerifier.create(poller)
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap "
        + "between the time subscribed and the time we start observing events.")
    @Test
    public void startCopyLease() {
        primaryFileAsyncClient.create(1024).block();
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block();

        PollerFlux<ShareFileCopyInfo, Void> poller
            = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, null, null, null, false,
                false, null, null, new ShareRequestConditions().setLeaseId(leaseId)));
        StepVerifier.create(poller)
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
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

        PollerFlux<ShareFileCopyInfo, Void> poller
            = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, null, null, null, false,
                false, null, null, new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid())));
        StepVerifier.create(poller)
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithOptions(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        String sourceURL = primaryFileAsyncClient.getFileUrl();

        StepVerifier.create(primaryFileAsyncClient.create(1024)
            .then(shareAsyncClient.createPermission(FILE_PERMISSION))
            .flatMapMany(filePermissionKey -> {
                smbProperties.setFileCreationTime(testResourceNamer.now())
                    .setFileLastWriteTime(testResourceNamer.now());
                if (setFilePermissionKey) {
                    smbProperties.setFilePermissionKey(filePermissionKey);
                }
                ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbProperties(smbProperties)
                    .setFilePermission(setFilePermission ? FILE_PERMISSION : null)
                    .setIgnoreReadOnly(ignoreReadOnly)
                    .setArchiveAttribute(setArchiveAttribute)
                    .setPermissionCopyModeType(permissionType);
                return setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));
            }))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @Test
    public void startCopyWithOptionsIgnoreReadOnlyAndSetArchive() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions().setIgnoreReadOnly(true).setArchiveAttribute(true);

        PollerFlux<ShareFileCopyInfo, Void> poller
            = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(createFileMono.thenMany(poller))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsFilePermission() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);

        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs);

        ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        PollerFlux<ShareFileCopyInfo, Void> poller
            = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(createFileMono.thenMany(poller))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));

        StepVerifier.create(primaryFileAsyncClient.getProperties()).assertNext(properties -> {
            FileSmbProperties smbProps = properties.getSmbProperties();
            FileShareTestHelper.compareDatesWithPrecision(smbProps.getFileCreationTime(),
                smbProperties.getFileCreationTime());
            FileShareTestHelper.compareDatesWithPrecision(smbProps.getFileLastWriteTime(),
                smbProperties.getFileLastWriteTime());
            assertEquals(smbProps.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-01-05")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void startCopyFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileCopyOptions options = new ShareFileCopyOptions().setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        Mono<ShareFileProperties> response = primaryFileAsyncClient.create(1024)
            .then(setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null)).last())
            .flatMap(r -> {
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, r.getStatus());
                return primaryFileAsyncClient.getProperties();
            });

        StepVerifier.create(response)
            .assertNext(r -> assertNotNull(r.getSmbProperties().getFilePermissionKey()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsChangeTime() {
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        smbProperties.setFileChangeTime(testResourceNamer.now());

        ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        Mono<PollerFlux<ShareFileCopyInfo, Void>> pollerMono = createFileMono
            .thenReturn(setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null)));

        StepVerifier.create(pollerMono.flatMapMany(poller -> poller.take(1)))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));

        StepVerifier.create(primaryFileAsyncClient.getProperties())
            .assertNext(properties -> FileShareTestHelper.compareDatesWithPrecision(smbProperties.getFileChangeTime(),
                properties.getSmbProperties().getFileChangeTime()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsCopySmbFilePropertiesPermissionKey() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        Mono<String> filePermissionKeyMono = shareAsyncClient.createPermission(FILE_PERMISSION);

        Mono<ShareFileCopyOptions> optionsMono = filePermissionKeyMono.map(filePermissionKey -> {
            EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);
            smbProperties.setFileCreationTime(testResourceNamer.now())
                .setFileLastWriteTime(testResourceNamer.now())
                .setNtfsFileAttributes(ntfs)
                .setFilePermissionKey(filePermissionKey);
            return new ShareFileCopyOptions().setSmbProperties(smbProperties)
                .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);
        });

        Mono<PollerFlux<ShareFileCopyInfo, Void>> pollerMono = optionsMono.map(
            options -> setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null)));

        StepVerifier.create(createFileMono.thenMany(pollerMono.flatMapMany(poller -> poller.take(1))))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));

        StepVerifier.create(primaryFileAsyncClient.getProperties()).assertNext(properties -> {
            FileSmbProperties smbProps = properties.getSmbProperties();
            FileShareTestHelper.compareDatesWithPrecision(smbProps.getFileCreationTime(),
                smbProperties.getFileCreationTime());
            FileShareTestHelper.compareDatesWithPrecision(smbProps.getFileLastWriteTime(),
                smbProperties.getFileLastWriteTime());
            assertEquals(smbProps.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
        }).verifyComplete();
    }

    @Test
    public void startCopyWithOptionsLease() {
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        Mono<String> leaseIdMono
            = primaryFileAsyncClient.create(1024).then(createLeaseClient(primaryFileAsyncClient).acquireLease());

        StepVerifier.create(leaseIdMono.flatMapMany(leaseId -> {
            ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);
            ShareFileCopyOptions options = new ShareFileCopyOptions().setDestinationRequestConditions(conditions);
            return setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));
        })).assertNext(it -> assertNotNull(it.getValue().getCopyId())).expectComplete().verify(Duration.ofMinutes(1));
    }

    @Test
    public void startCopyWithOptionsInvalidLease() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        String leaseId = testResourceNamer.randomUuid();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);

        ShareFileCopyOptions options = new ShareFileCopyOptions().setDestinationRequestConditions(conditions);

        StepVerifier
            .create(createFileMono.thenMany(
                setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null))))
            .verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals("LeaseNotPresentWithFileOperation", e.getErrorCode().getValue());
            });
    }

    @Test
    public void startCopyWithOptionsMetadata() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions().setMetadata(testMetadata);

        PollerFlux<ShareFileCopyInfo, Void> poller
            = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));

        StepVerifier.create(createFileMono.thenMany(poller))
            .assertNext(it -> assertNotNull(it.getValue().getCopyId()))
            .expectComplete()
            .verify(Duration.ofMinutes(1));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsWithOriginalSmbProperties() {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        Mono<ShareFileProperties> initialPropertiesMono = createFileMono.then(primaryFileAsyncClient.getProperties());

        StepVerifier.create(initialPropertiesMono.flatMap(initialProperties -> {
            assertNotNull(initialProperties);
            OffsetDateTime creationTime = initialProperties.getSmbProperties().getFileCreationTime();
            OffsetDateTime lastWrittenTime = initialProperties.getSmbProperties().getFileLastWriteTime();
            OffsetDateTime changedTime = initialProperties.getSmbProperties().getFileChangeTime();
            EnumSet<NtfsFileAttributes> fileAttributes = initialProperties.getSmbProperties().getNtfsFileAttributes();

            String sourceURL = primaryFileAsyncClient.getFileUrl();
            return createLeaseClient(primaryFileAsyncClient).acquireLease().flatMap(leaseId -> {
                ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);
                CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList().setCreatedOn(true)
                    .setLastWrittenOn(true)
                    .setChangedOn(true)
                    .setFileAttributes(true);

                ShareFileCopyOptions options = new ShareFileCopyOptions().setDestinationRequestConditions(conditions)
                    .setSmbPropertiesToCopy(list);

                PollerFlux<ShareFileCopyInfo, Void> poller
                    = setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null));

                return poller.take(1).single().flatMap(response -> {
                    assertNotNull(response.getValue().getCopyId());
                    return primaryFileAsyncClient.getProperties().map(resultProperties -> {
                        FileSmbProperties smbProps = resultProperties.getSmbProperties();
                        FileShareTestHelper.compareDatesWithPrecision(creationTime, smbProps.getFileCreationTime());
                        FileShareTestHelper.compareDatesWithPrecision(lastWrittenTime, smbProps.getFileLastWriteTime());
                        FileShareTestHelper.compareDatesWithPrecision(changedTime, smbProps.getFileChangeTime());
                        assertEquals(fileAttributes, smbProps.getNtfsFileAttributes());
                        return resultProperties;
                    });
                });
            });
        })).expectNextCount(1).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyWithCopySourceFileErrorSupplier")
    public void startCopyWithOptionsCopySourceFileError(boolean createdOn, boolean lastWrittenOn, boolean changedOn,
        boolean fileAttributes) {
        Mono<ShareFileInfo> createFileMono = primaryFileAsyncClient.create(1024);
        String sourceURL = primaryFileAsyncClient.getFileUrl();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList().setCreatedOn(createdOn)
            .setLastWrittenOn(lastWrittenOn)
            .setChangedOn(changedOn)
            .setFileAttributes(fileAttributes);

        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFileChangeTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs);

        ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)
            .setSmbPropertiesToCopy(list);

        StepVerifier
            .create(createFileMono.then(Mono.fromRunnable(
                () -> setPlaybackPollerFluxPollInterval(primaryFileAsyncClient.beginCopy(sourceURL, options, null)))))
            .verifyError(IllegalArgumentException.class);
    }

    @Disabled("TODO: Need to find a way of mocking pending copy status")
    @Test
    public void abortCopy() {
        //TODO: Need to find a way of mocking pending copy status
    }

    @Test
    public void deleteFile() {
        StepVerifier.create(primaryFileAsyncClient.create(1024).then(primaryFileAsyncClient.deleteWithResponse()))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteFileError() {
        StepVerifier.create(primaryFileAsyncClient.delete())
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 404,
                ShareErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    public void deleteFileLease() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient
                    .deleteWithResponse(new ShareRequestConditions().setLeaseId(leaseId))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteFileLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient
                    .deleteWithResponse(new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void deleteIfExistsFile() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024).then(primaryFileAsyncClient.deleteIfExistsWithResponse(null)))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteFileThatDoesNotExist() {
        ShareFileAsyncClient client = primaryFileAsyncClient.getFileAsyncClient(generateShareName());
        StepVerifier.create(client.deleteIfExistsWithResponse(null, null)).assertNext(response -> {
            assertNotNull(response);
            assertFalse(response.getValue());
            assertEquals(404, response.getStatusCode());
        }).verifyComplete();

        StepVerifier.create(client.exists()).expectNext(Boolean.FALSE).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileThatWasAlreadyDeleted() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null, null)
            .then(primaryFileAsyncClient.deleteIfExists())
            .flatMap(deleted -> {
                assertEquals(Boolean.TRUE, deleted);
                return primaryFileAsyncClient.deleteIfExists();
            })).assertNext(deleted -> assertNotEquals(Boolean.TRUE, deleted)).verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLease() {
        Mono<String> leaseIdMono
            = primaryFileAsyncClient.create(1024).then(createLeaseClient(primaryFileAsyncClient).acquireLease());

        StepVerifier
            .create(leaseIdMono.flatMap(leaseId -> primaryFileAsyncClient
                .deleteIfExistsWithResponse(new ShareRequestConditions().setLeaseId(leaseId))))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 202))
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileLeaseFail() {
        Mono<String> leaseIdMono
            = primaryFileAsyncClient.create(1024).then(createLeaseClient(primaryFileAsyncClient).acquireLease());

        StepVerifier
            .create(leaseIdMono.flatMap(leaseId -> primaryFileAsyncClient
                .deleteIfExistsWithResponse(new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void getProperties() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024).then(primaryFileAsyncClient.getPropertiesWithResponse()))
            .assertNext(it -> {
                FileShareTestHelper.assertResponseStatusCode(it, 200);
                assertNotNull(it.getValue().getETag());
                assertNotNull(it.getValue().getLastModified());
                assertNotNull(it.getValue().getSmbProperties());
                assertNotNull(it.getValue().getSmbProperties().getFilePermissionKey());
                assertNotNull(it.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNotNull(it.getValue().getSmbProperties().getFileLastWriteTime());
                assertNotNull(it.getValue().getSmbProperties().getFileCreationTime());
                assertNotNull(it.getValue().getSmbProperties().getFileChangeTime());
                assertNotNull(it.getValue().getSmbProperties().getParentId());
                assertNotNull(it.getValue().getSmbProperties().getFileId());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesLease() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient
                    .getPropertiesWithResponse(new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void getPropertiesLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .then(primaryFileAsyncClient.getPropertiesWithResponse(
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        StepVerifier.create(primaryFileAsyncClient.getProperties())
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @Test
    public void setHttpHeadersFpk() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
            .then(shareAsyncClient.createPermission(FILE_PERMISSION))
            .flatMap(filePermissionKey -> {
                smbProperties.setFileCreationTime(testResourceNamer.now())
                    .setFileLastWriteTime(testResourceNamer.now())
                    .setFilePermissionKey(filePermissionKey);
                return primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null);
            })).assertNext(it -> {
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
        smbProperties.setFileCreationTime(testResourceNamer.now()).setFileLastWriteTime(testResourceNamer.now());
        StepVerifier
            .create(
                primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
                    .then(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties,
                        FILE_PERMISSION)))
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
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void setFileHttpHeadersFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        primaryFileAsyncClient.create(512);

        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileSetPropertiesOptions options = new ShareFileSetPropertiesOptions(512).setFilePermissions(
            new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat));

        StepVerifier
            .create(primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.setPropertiesWithResponse(options)))
            .assertNext(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                assertNotNull(r.getValue().getSmbProperties().getFilePermissionKey());
            })
            .verifyComplete();
    }

    @Test
    public void setHttpHeadersLease() {
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .flatMap(leaseId -> primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null,
                new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void setHttpHeadersLeaseFail() {
        StepVerifier
            .create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void setHttpHeadersError() {
        StepVerifier
            .create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
                .then(primaryFileAsyncClient.setProperties(-1, null, null, null)))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.OUT_OF_RANGE_INPUT));
    }

    @Test
    public void setMetadata() {
        Mono<Void> createFileMono
            = primaryFileAsyncClient.createWithResponse(1024, httpHeaders, null, null, testMetadata).then();

        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        StepVerifier.create(createFileMono.then(primaryFileAsyncClient.getProperties()))
            .assertNext(it -> assertEquals(testMetadata, it.getMetadata()))
            .verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(updatedMetadata))
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();

        StepVerifier.create(primaryFileAsyncClient.getProperties())
            .assertNext(it -> assertEquals(updatedMetadata, it.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void setMetadataError() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(primaryFileAsyncClient.setMetadataWithResponse(Collections.singletonMap("", "value"))))
            .verifyErrorSatisfies(it -> FileShareTestHelper.assertExceptionStatusCodeAndMessage(it, 400,
                ShareErrorCode.EMPTY_METADATA_KEY));
    }

    @Test
    public void setMetadataLease() {
        StepVerifier
            .create(primaryFileAsyncClient.create(1024)
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .flatMap(leaseId -> primaryFileAsyncClient.setMetadataWithResponse(
                    Collections.singletonMap("key", "value"), new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void setMetadataLeaseFail() {
        StepVerifier
            .create(
                primaryFileAsyncClient.create(1024)
                    .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                    .flatMap(leaseId -> primaryFileAsyncClient.setMetadataWithResponse(
                        Collections.singletonMap("key", "value"),
                        new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);
    }

    @Test
    public void listRanges() throws IOException {
        Mono<Void> createFileMono = primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).then();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        Mono<Void> uploadFileMono = primaryFileAsyncClient.uploadFromFile(uploadFile).then();

        StepVerifier.create(createFileMono.then(uploadFileMono).thenMany(primaryFileAsyncClient.listRanges()))
            .assertNext(it -> {
                assertEquals(0, it.getStart());
                assertEquals(1023, it.getEnd());
            })
            .verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesWithRange() throws IOException {
        Mono<Void> createFileMono = primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).then();
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        Mono<Void> uploadFileMono = primaryFileAsyncClient.uploadFromFile(uploadFile).then();

        StepVerifier.create(createFileMono.then(uploadFileMono)
            .thenMany(primaryFileAsyncClient.listRanges(new ShareFileRange(0, 511L)))).assertNext(it -> {
                assertEquals(0, it.getStart());
                assertEquals(511, it.getEnd());
            }).verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesLease() throws IOException {
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
            .then(primaryFileAsyncClient.uploadFromFile(uploadFile))
            .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
            .flatMapMany(
                leaseId -> primaryFileAsyncClient.listRanges(null, new ShareRequestConditions().setLeaseId(leaseId))))
            .expectNextCount(1)
            .verifyComplete();

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesLeaseFail() throws IOException {
        String fileName = generatePathName();
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);

        StepVerifier
            .create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null)
                .then(primaryFileAsyncClient.uploadFromFile(uploadFile))
                .then(createLeaseClient(primaryFileAsyncClient).acquireLease())
                .thenMany(primaryFileAsyncClient.listRanges(null,
                    new ShareRequestConditions().setLeaseId(testResourceNamer.randomUuid()))))
            .verifyError(ShareStorageException.class);

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listRangesDiffSupplier")
    public void listRangesDiff(List<FileRange> rangesToUpdate, List<FileRange> rangesToClear,
        List<FileRange> expectedRanges, List<ClearRange> expectedClearRanges) {
        Mono<String> snapshotIdMono = primaryFileAsyncClient.create(4 * Constants.MB)
            .then(primaryFileAsyncClient.upload(Flux.just(getRandomByteBuffer(4 * Constants.MB)), 4 * Constants.MB))
            .then(primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName())
                .createSnapshot()
                .map(ShareSnapshotInfo::getSnapshot));

        Flux<Void> updateRangesFlux = Flux.fromIterable(rangesToUpdate).flatMap(it -> {
            int size = (int) (it.getEnd() - it.getStart() + 1);
            return primaryFileAsyncClient.uploadWithResponse(Flux.just(getRandomByteBuffer(size)), size, it.getStart())
                .then();
        });

        Flux<Void> clearRangesFlux = Flux.fromIterable(rangesToClear)
            .flatMap(it -> primaryFileAsyncClient.clearRangeWithResponse(it.getEnd() - it.getStart() + 1, it.getStart())
                .then());

        StepVerifier.create(snapshotIdMono.flatMapMany(snapshotId -> updateRangesFlux.thenMany(clearRangesFlux)
            .thenMany(primaryFileAsyncClient.listRangesDiff(snapshotId)))).assertNext(it -> {
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
            .then(primaryFileAsyncClient
                .uploadFromFile(FileShareTestHelper.createRandomFileWithLength(Constants.KB, testFolder, fileName)))
            .then(primaryFileAsyncClient.uploadRange(Flux.just(getRandomByteBuffer(Constants.KB)), Constants.KB))
            .then(primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName())
                .createSnapshot())
            .block();

        //rename file
        ShareFileAsyncClient destFile = primaryFileAsyncClient.rename(generatePathName()).block();

        //take another snapshot
        primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName())
            .createSnapshot()
            .block();

        //setup options
        ShareFileListRangesDiffOptions options = new ShareFileListRangesDiffOptions(previousSnapshot.getSnapshot());
        options.setRenameIncluded(renameSupport);

        //call
        if (renameSupport == null || !renameSupport) {
            StepVerifier.create(destFile.listRangesDiffWithResponse(options)).verifyErrorSatisfies(r -> {
                ShareStorageException e = assertInstanceOf(ShareStorageException.class, r);
                assertEquals(ShareErrorCode.PREVIOUS_SNAPSHOT_NOT_FOUND, e.getErrorCode());
            });
        } else {
            StepVerifier.create(destFile.listRangesDiffWithResponse(options)).assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertEquals(0, r.getValue().getRanges().size());
            }).verifyComplete();
        }

        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
        destFile.delete().block();
    }

    private static Stream<Arguments> listRangesDiffWithRenameSupplier() {
        return Stream.of(Arguments.of(true), Arguments.of(false), Arguments.of((Boolean) null));
    }

    @Test
    public void listHandles() {
        StepVerifier.create(primaryFileAsyncClient.create(1024).thenMany(primaryFileAsyncClient.listHandles()))
            .verifyComplete();
    }

    @Test
    public void listHandlesWithMaxResult() {
        StepVerifier.create(primaryFileAsyncClient.create(1024).thenMany(primaryFileAsyncClient.listHandles(2)))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseHandleMin() {
        StepVerifier.create(primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.forceCloseHandle("1")))
            .assertNext(it -> {
                assertEquals(0, it.getClosedHandles());
                assertEquals(0, it.getFailedHandles());
            })
            .verifyComplete();
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        StepVerifier
            .create(primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.forceCloseHandle("invalidHandleId")))
            .verifyErrorSatisfies(it -> assertInstanceOf(ShareStorageException.class, it));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseAllHandlesMin() {
        StepVerifier.create(primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.forceCloseAllHandles()))
            .assertNext(it -> {
                assertEquals(0, it.getClosedHandles());
                assertEquals(0, it.getFailedHandles());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#filePermissionFormatSupplier")
    public void renameFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        Mono<ShareFileProperties> response
            = primaryFileAsyncClient.create(512).then(primaryFileAsyncClient.renameWithResponse(options)).flatMap(r -> {
                FileShareTestHelper.assertResponseStatusCode(r, 200);
                return r.getValue().getProperties();
            });

        StepVerifier.create(response)
            .assertNext(r -> assertNotNull(r.getSmbProperties().getFilePermissionKey()))
            .verifyComplete();
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1, 1, 1), ZoneOffset.UTC).toString();
        ShareFileAsyncClient shareSnapshotClient
            = fileBuilderHelper(shareName, filePath).snapshot(snapshot).buildFileAsyncClient();
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
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(null) /* should default to "https://storage.azure.com/" */);

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(fileClient.create(Constants.KB).then(aadFileClient.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void storageAccountAudience() {
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience(shareAsyncClient.getAccountName())));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(fileClient.create(Constants.KB).then(aadFileClient.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void audienceError() {
        String fileName = generatePathName();
        ShareFileAsyncClient fileClient = fileBuilderHelper(shareName, fileName).buildFileAsyncClient();
        ShareServiceAsyncClient oAuthServiceClient
            = getOAuthServiceAsyncClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(fileClient.create(Constants.KB).then(aadFileClient.exists())).verifyErrorSatisfies(r -> {
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
        ShareServiceAsyncClient oAuthServiceClient = getOAuthServiceAsyncClient(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP).audience(audience));

        ShareFileAsyncClient aadFileClient = oAuthServiceClient.getShareAsyncClient(shareName).getFileClient(fileName);

        StepVerifier.create(fileClient.create(Constants.KB).then(aadFileClient.exists()))
            .expectNext(true)
            .verifyComplete();
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-02-04")
    @Test
    public void listHandlesClientName() {
        StepVerifier.create(primaryFileServiceAsyncClient.getShareAsyncClient("testing")
            .getDirectoryClient("dir1")
            .getFileClient("test.txt")
            .listHandles()
            .collectList()).assertNext(list -> assertNotNull(list.get(0).getClientName())).verifyComplete();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void createNFS() {
        ShareFileCreateOptions options = new ShareFileCreateOptions(1024)
            .setPosixProperties(new FilePosixProperties().setOwner("345").setGroup("123").setFileMode("7777"));

        String shareName = generateShareName();
        Mono<Response<ShareFileInfo>> create = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
            ShareFileAsyncClient premiumFileClient = premiumShareClient.getFileClient(generatePathName());
            return premiumFileClient.createWithResponse(options);
        });

        StepVerifier.create(create).assertNext(r -> {
            ShareFileInfo response = r.getValue();
            assertEquals(NfsFileType.REGULAR, response.getPosixProperties().getFileType());
            assertEquals("345", response.getPosixProperties().getOwner());
            assertEquals("123", response.getPosixProperties().getGroup());
            assertEquals("7777", response.getPosixProperties().getFileMode());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void setPropertiesNFS() {
        ShareFileSetPropertiesOptions options = new ShareFileSetPropertiesOptions(1024)
            .setPosixProperties(new FilePosixProperties().setOwner("345").setGroup("123").setFileMode("7777"));

        String shareName = generateShareName();
        Mono<Response<ShareFileInfo>> create = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
            ShareFileAsyncClient premiumFileClient = premiumShareClient.getFileClient(generatePathName());
            return premiumFileClient.create(1024).then(premiumFileClient.setPropertiesWithResponse(options));
        });

        StepVerifier.create(create).assertNext(r -> {
            ShareFileInfo response = r.getValue();
            assertEquals("345", response.getPosixProperties().getOwner());
            assertEquals("123", response.getPosixProperties().getGroup());
            assertEquals("7777", response.getPosixProperties().getFileMode());
            assertNotNull(response.getPosixProperties().getLinkCount());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void getPropertiesNFS() {
        String shareName = generateShareName();
        Mono<Response<ShareFileProperties>> create
            = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
                ShareFileAsyncClient premiumFileClient = premiumShareClient.getFileClient(generatePathName());
                return premiumFileClient.create(1024).then(premiumFileClient.getPropertiesWithResponse());
            });

        StepVerifier.create(create).assertNext(r -> {
            ShareFileProperties response = r.getValue();

            assertEquals(NfsFileType.REGULAR, response.getPosixProperties().getFileType());
            assertEquals("0", response.getPosixProperties().getOwner());
            assertEquals("0", response.getPosixProperties().getGroup());
            assertEquals("0664", response.getPosixProperties().getFileMode());
            assertEquals(1, response.getPosixProperties().getLinkCount());

            FileShareTestHelper.assertSmbPropertiesNull(response.getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    private static Stream<Arguments> beginCopyNFSSupplier() {
        return Stream.of(Arguments.of(ModeCopyMode.SOURCE), Arguments.of(ModeCopyMode.OVERRIDE),
            Arguments.of((ModeCopyMode) null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @ParameterizedTest
    @MethodSource("beginCopyNFSSupplier")
    public void beginCopyNFS(ModeCopyMode modeAndOwnerCopyMode) {
        String shareName = generateShareName();

        String sourcePath = generatePathName() + "source";
        String destPath = generatePathName() + "dest";

        Mono<Tuple4<ShareFileProperties, String, String, String>> setup
            = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
                ShareFileAsyncClient premiumFileClientSource = premiumShareClient.getFileClient(sourcePath);
                ShareFileAsyncClient premiumFileClientDest = premiumShareClient.getFileClient(destPath);

                return premiumFileClientSource.create(1024)
                    .then(premiumFileClientSource.uploadRange(DATA.getDefaultFlux(), DATA.getDefaultDataSizeLong()))
                    .then(premiumFileClientSource
                        .setPropertiesWithResponse(new ShareFileSetPropertiesOptions(1024).setPosixProperties(
                            new FilePosixProperties().setOwner("999").setGroup("888").setFileMode("0111"))))
                    .then(premiumFileClientDest.create(1024))
                    .then(premiumFileClientSource.getProperties())
                    .flatMap(sourceProperties -> {
                        String owner;
                        String group;
                        String mode;

                        ShareFileCopyOptions options
                            = new ShareFileCopyOptions().setPosixProperties(new FilePosixProperties());

                        if (modeAndOwnerCopyMode == ModeCopyMode.OVERRIDE) {
                            owner = "54321";
                            group = "12345";
                            mode = "7777";
                            options.setModeCopyMode(ModeCopyMode.OVERRIDE);
                            options.setOwnerCopyMode(OwnerCopyMode.OVERRIDE);
                            options.getPosixProperties().setOwner(owner);
                            options.getPosixProperties().setGroup(group);
                            options.getPosixProperties().setFileMode(mode);
                        } else if (modeAndOwnerCopyMode == ModeCopyMode.SOURCE) {
                            options.setModeCopyMode(ModeCopyMode.SOURCE);
                            options.setOwnerCopyMode(OwnerCopyMode.SOURCE);
                            owner = sourceProperties.getPosixProperties().getOwner();
                            group = sourceProperties.getPosixProperties().getGroup();
                            mode = sourceProperties.getPosixProperties().getFileMode();
                        } else {
                            owner = "0";
                            group = "0";
                            mode = "0664";
                        }

                        return setPlaybackPollerFluxPollInterval(
                            premiumFileClientDest.beginCopy(premiumFileClientSource.getFileUrl(), options, null)).last()
                                .flatMap(ignore -> premiumFileClientDest.getProperties())
                                .flatMap(properties -> Mono.zip(Mono.just(properties), Mono.just(owner),
                                    Mono.just(group), Mono.just(mode)));
                    });
            });

        StepVerifier.create(setup).assertNext(r -> {
            assertEquals(r.getT2(), r.getT1().getPosixProperties().getOwner());
            assertEquals(r.getT3(), r.getT1().getPosixProperties().getGroup());
            assertEquals(r.getT4(), r.getT1().getPosixProperties().getFileMode());
            FileShareTestHelper.assertSmbPropertiesNull(r.getT1().getSmbProperties());
        }).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void createHardLink() {
        String shareName = generateShareName();

        Mono<Void> response = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
            ShareFileAsyncClient source = premiumShareClient.getFileClient(generatePathName());

            String leaseId = testResourceNamer.randomUuid();
            ShareLeaseAsyncClient leaseClient = createLeaseClient(premiumShareClient, leaseId);

            return source.create(1024).then(leaseClient.acquireLease()).flatMap(lease -> {
                ShareFileAsyncClient hardLink = premiumShareClient.getFileClient(generatePathName() + "hardlink");
                ShareFileCreateHardLinkOptions options = new ShareFileCreateHardLinkOptions(source.getFilePath())
                    .setRequestConditions(new ShareRequestConditions().setLeaseId(lease));
                return hardLink.createHardLinkWithResponse(options).flatMap(res -> {
                    ShareFileInfo info = res.getValue();
                    assertEquals(NfsFileType.REGULAR, info.getPosixProperties().getFileType());
                    assertEquals("0", info.getPosixProperties().getOwner());
                    assertEquals("0", info.getPosixProperties().getGroup());
                    assertEquals("0664", info.getPosixProperties().getFileMode());
                    assertEquals(2, info.getPosixProperties().getLinkCount());

                    assertNotNull(info.getSmbProperties().getFileCreationTime());
                    assertNotNull(info.getSmbProperties().getFileLastWriteTime());
                    assertNotNull(info.getSmbProperties().getFileChangeTime());
                    assertNotNull(info.getSmbProperties().getFileId());
                    assertNotNull(info.getSmbProperties().getParentId());

                    FileShareTestHelper.assertSmbPropertiesNull(info.getSmbProperties());
                    //cleanup
                    return leaseClient.releaseLease();
                });
            });
        });

        StepVerifier.create(response).verifyComplete();

        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void createGetSymbolicLink() {
        // Arrange
        String shareName = generateShareName();
        Mono<Void> testMono = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
            ShareFileAsyncClient source = premiumShareClient.getFileClient(generatePathName());
            ShareDirectoryAsyncClient directory = premiumShareClient.getRootDirectoryClient();
            ShareFileAsyncClient symlink = directory.getFileClient(generatePathName());

            Map<String, String> metadata = Collections.singletonMap("key", "value");
            String owner = "345";
            String group = "123";
            OffsetDateTime fileCreatedOn = OffsetDateTime.of(2024, 10, 15, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime fileLastWrittenOn = OffsetDateTime.of(2025, 5, 2, 0, 0, 0, 0, ZoneOffset.UTC);

            ShareFileCreateSymbolicLinkOptions options
                = new ShareFileCreateSymbolicLinkOptions(source.getFileUrl()).setMetadata(metadata)
                    .setFileCreationTime(fileCreatedOn)
                    .setFileLastWriteTime(fileLastWrittenOn)
                    .setOwner(owner)
                    .setGroup(group);

            // Act & Assert
            return source.create(1024).then(symlink.createSymbolicLinkWithResponse(options)).flatMap(response -> {
                assertEquals(NfsFileType.SYM_LINK, response.getValue().getPosixProperties().getFileType());
                assertEquals(owner, response.getValue().getPosixProperties().getOwner());
                assertEquals(group, response.getValue().getPosixProperties().getGroup());
                assertEquals(fileCreatedOn, response.getValue().getSmbProperties().getFileCreationTime());
                assertEquals(fileLastWrittenOn, response.getValue().getSmbProperties().getFileLastWriteTime());

                assertNull(response.getValue().getSmbProperties().getNtfsFileAttributes());
                assertNull(response.getValue().getSmbProperties().getFilePermissionKey());

                assertNotNull(response.getValue().getSmbProperties().getFileId());
                assertNotNull(response.getValue().getSmbProperties().getParentId());

                return symlink.getSymbolicLinkWithResponse();
            }).flatMap(getSymLinkResponse -> {
                assertNull(null, getSymLinkResponse.getValue().getETag());
                assertNull(null, getSymLinkResponse.getValue().getLastModified().toString());
                try {
                    if (getTestMode() != TestMode.PLAYBACK) {
                        assertEquals(source.getFileUrl(), URLDecoder.decode(getSymLinkResponse.getValue().getLinkText(),
                            StandardCharsets.UTF_8.toString()));
                    } else {
                        assertTrue(URLDecoder
                            .decode(getSymLinkResponse.getValue().getLinkText(), StandardCharsets.UTF_8.toString())
                            .contains(source.getFilePath()));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                return Mono.empty();
            });
        });

        StepVerifier.create(testMono).verifyComplete();
        //cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();

    }

    @Test
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    public void createGetSymbolicLinkError() {
        // Arrange
        String shareName = generateShareName();

        Mono<Boolean> createAndGetErrors = getPremiumNFSShareAsyncClient(shareName).flatMap(premiumShareClient -> {
            ShareDirectoryAsyncClient directory = premiumShareClient.getDirectoryClient(generatePathName());
            ShareFileAsyncClient source = directory.getFileClient(generatePathName());
            ShareFileAsyncClient symlink = directory.getFileClient(generatePathName());

            return symlink.createSymbolicLink(source.getFileUrl()).then(Mono.just(false)).onErrorResume(e -> {
                ShareStorageException createError = assertInstanceOf(ShareStorageException.class, e);
                assertEquals(ShareErrorCode.PARENT_NOT_FOUND, createError.getErrorCode());
                return symlink.getSymbolicLink().then(Mono.just(false));
            }).onErrorResume(e -> {
                ShareStorageException getError = assertInstanceOf(ShareStorageException.class, e);
                assertEquals(ShareErrorCode.PARENT_NOT_FOUND, getError.getErrorCode());
                return Mono.just(true);
            });
        });

        StepVerifier.create(createAndGetErrors).assertNext(Assertions::assertTrue).verifyComplete();

        // Cleanup
        premiumFileServiceAsyncClient.getShareAsyncClient(shareName).delete().block();
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2025-05-05")
    @Test
    public void createGetSymbolicLinkOAuth() {
        // Arrange
        ShareServiceAsyncClient oauthServiceClient = getOAuthPremiumServiceAsyncClient(
            new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));

        ShareProtocols enabledProtocol = ModelHelper.parseShareProtocols("NFS");
        Mono<Response<ShareFileSymbolicLinkInfo>> response = oauthServiceClient
            .createShareWithResponse(shareName, new ShareCreateOptions().setProtocols(enabledProtocol))
            .flatMap(shareClient -> {
                ShareDirectoryAsyncClient directory = shareClient.getValue().getDirectoryClient(generatePathName());
                ShareFileAsyncClient source = directory.getFileClient(generatePathName());
                ShareFileAsyncClient symlink = directory.getFileClient(generatePathName());
                return directory.create()
                    .then(source.create(1024))
                    .then(symlink.createSymbolicLink(source.getFileUrl()))
                    .then(symlink.getSymbolicLinkWithResponse());
            });

        StepVerifier.create(response)
            .assertNext(it -> FileShareTestHelper.assertResponseStatusCode(it, 200))
            .verifyComplete();

        oauthServiceClient.deleteShare(shareName).block();
    }

}
