// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy;
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import com.azure.storage.file.share.models.ClearRange;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FileRange;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.models.ShareAudience;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileHandleAccessRights;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFileMetadataInfo;
import com.azure.storage.file.share.models.ShareFilePermission;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareFileRangeList;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileCreateOptions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareFileSetPropertiesOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileApiTests extends FileShareTestBase {
    private ShareFileClient primaryFileClient;
    private ShareClient shareClient;
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
        shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
        testMetadata = Collections.singletonMap("testmetadata", "value");
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream");
        smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL));
    }

    @Test
    public void getFileURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath);

        String fileURL = primaryFileClient.getFileUrl();
        assertEquals(expectURL, fileURL);
    }

    @Test
    public void getShareSnapshotURL() {
        String accountName = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount()
            .getConnectionString()).getAccountName();
        String expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath);

        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot();
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot();
        ShareFileClient newFileClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getFileClient(filePath);
        String fileURL = newFileClient.getFileUrl();

        assertEquals(expectURL, fileURL);

        String snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s/%s?sharesnapshot=%s", accountName,
            shareName, filePath, shareSnapshotInfo.getSnapshot());
        ShareFileClient client = getFileClient(StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString()), snapshotEndpoint);

        assertEquals(client.getFileUrl(), snapshotEndpoint);
    }

    @Test
    public void exists() {
        primaryFileClient.create(Constants.KB);
        assertTrue(primaryFileClient.exists());
    }

    @Test
    public void doesNotExist() {
        assertFalse(primaryFileClient.exists());
    }

    @Test
    public void existsError() {
        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .sasToken("sig=dummyToken").buildFileClient();

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.exists());
        assertEquals(e.getResponse().getStatusCode(), 403);
    }

    @Test
    public void createFile() {
        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null, null,
            null, null), 201);
    }

    private static Stream<Arguments> filePermissionFormatSupplier() {
        return Stream.of(
            Arguments.of(FilePermissionFormat.SDDL),
            Arguments.of(FilePermissionFormat.BINARY),
            Arguments.of((Object) null));
    }
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("filePermissionFormatSupplier")
    public void createFileFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileCreateOptions options = new ShareFileCreateOptions(1024).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        Response<ShareFileInfo> bagResponse = primaryFileClient.createWithResponse(options, null, null);
        Response<ShareFileInfo> nonBagResponse = primaryFileClient.createWithResponse(1024, null,
            null, permission, filePermissionFormat, null, null, null, null);

        FileShareTestHelper.assertResponseStatusCode(bagResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(nonBagResponse, 201);

        assertNotNull(bagResponse.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(nonBagResponse.getValue().getSmbProperties().getFilePermissionKey());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void createFile4TB() {
        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(4 * Constants.TB, null, null,
            null, null, null,
            null), 201);
    }

    @Test
    public void createFileError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.create(-1));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @Test
    public void createFileWithArgsFpk() {
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        Response<ShareFileInfo> resp = primaryFileClient
            .createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 201);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void createFileWithArgsFp() {
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        Response<ShareFileInfo> resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties,
            FILE_PERMISSION, testMetadata, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 201);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void createChangeTime() {
        OffsetDateTime changeTime = testResourceNamer.now();
        primaryFileClient.createWithResponse(512, null, new FileSmbProperties().setFileChangeTime(changeTime),
            null, null, null, null, null);
        FileShareTestHelper.compareDatesWithPrecision(primaryFileClient.getProperties().getSmbProperties()
            .getFileChangeTime(), changeTime);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void createFileOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);


        Response<ShareFileInfo> result = fileClient.createWithResponse(Constants.KB, null, null, null, null, null,
            null);
        assertEquals(fileClient.getShareName(), shareName);
        String[] filePath = fileClient.getFilePath().split("/");
        assertEquals(fileName, filePath[1]); // compare with filename
        assertEquals(result.getValue().getETag(), result.getHeaders().getValue(HttpHeaderName.ETAG));
    }

    @Test
    public void createFileWithArgsError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.createWithResponse(-1, null, null, null,
                testMetadata, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createFilePermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties smbProperties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        assertThrows(IllegalArgumentException.class, () ->
                primaryFileClient.createWithResponse(1024, null, smbProperties, permission, null, null, null));
    }

    private static Stream<Arguments> permissionAndKeySupplier() {
        return Stream.of(Arguments.of("filePermissionKey", FILE_PERMISSION),
            Arguments.of(null, new String(FileShareTestHelper.getRandomBuffer(9 * Constants.KB))));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createFileTrailingDot(boolean allowTrailingDot) {
        shareClient = getShareClient(shareName, allowTrailingDot, null);
        ShareDirectoryClient rootDirectory = shareClient.getRootDirectoryClient();
        String fileName = generatePathName();
        String fileNameWithDot = fileName + ".";
        ShareFileClient fileClient = rootDirectory.getFileClient(fileNameWithDot);
        fileClient.create(1024);

        List<String> foundFiles = new ArrayList<>();
        for (ShareFileItem fileRef : rootDirectory.listFilesAndDirectories()) {
            foundFiles.add(fileRef.getName());
        }

        if (allowTrailingDot) {
            assertEquals(fileNameWithDot, foundFiles.get(0));
        } else {
            assertEquals(fileName, foundFiles.get(0));
        }
    }

    @Test
    public void uploadAndDownloadData() {
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null,
            null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), DATA.getDefaultDataSizeLong());
        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @Test
    public void uploadAndDownloadDataWithArgs() {
        primaryFileClient.create(1024);
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1L),
            null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient
            .downloadWithResponse(stream, new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true, null, null);

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());
        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadAndDownloadDataOAuth() {
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(DATA.getDefaultDataSizeLong());

        Response<ShareFileUploadInfo> uploadResponse = fileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = fileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), DATA.getDefaultDataSizeLong());

        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @Test
    public void parallelUploadAndDownloadData() {
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()),
            null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null,
            null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), DATA.getDefaultDataSizeLong());

        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @Test
    public void parallelUploadAndDownloadDataWithArgs() {
        primaryFileClient.create(1024);
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1L),
            null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream,
            new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true, null, null);

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @Test
    public void parallelUploadInputStreamNoLength() {
        primaryFileClient.create(DATA.getDefaultDataSize());
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(DATA.getDefaultInputStream()), null, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        primaryFileClient.download(os);
        assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void parallelUploadInputStreamBadLength() {
        int[] lengths = new int[]{0, -100, DATA.getDefaultDataSize() - 1, DATA.getDefaultDataSize() + 1};
        for (int length : lengths) {
            primaryFileClient.create(DATA.getDefaultDataSize());
            assertThrows(Exception.class, () ->
                primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
                length), null, null));
        }
    }

    @Test
    public void uploadSuccessfulRetry() {
        primaryFileClient.create(DATA.getDefaultDataSize());
        ShareFileClient clientWithFailure = getFileClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(), new TransientFailureInjectingHttpPipelinePolicy());
        clientWithFailure.uploadWithResponse(new ShareFileUploadOptions(DATA.getDefaultInputStream()), null, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        primaryFileClient.download(os);
        assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void uploadRangeAndDownloadData() {
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null,
            null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), DATA.getDefaultDataSizeLong());

        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @Test
    public void uploadRangeAndDownloadDataWithArgs() {
        primaryFileClient.create(1024);
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1L),
            null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream,
            new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true, null, null);

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @LiveOnly
    @Test
    public void uploadAndDownloadAndUploadAgain() {
        byte[] randomData = getRandomByteArray(20 * Constants.MB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        String pathName = generatePathName();
        ShareFileClient fileClient = shareClient.getFileClient(pathName);
        fileClient.create(20 * Constants.MB);

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong((long) Constants.MB)
            .setMaxSingleUploadSizeLong(2L * Constants.MB)
            .setMaxConcurrency(5);
        ShareFileUploadOptions parallelUploadOptions = new ShareFileUploadOptions(input)
            .setParallelTransferOptions(parallelTransferOptions);

        fileClient.uploadWithResponse(parallelUploadOptions, null, null);

        StorageFileInputStream inputStreamResult = fileClient.openInputStream();

        // Upload the downloaded content to a different location
        String pathName2 = generatePathName();

        parallelUploadOptions = new ShareFileUploadOptions(inputStreamResult)
            .setParallelTransferOptions(parallelTransferOptions);

        ShareFileClient fileClient2 = shareClient.getFileClient(pathName2);
        fileClient2.create(20 * Constants.MB);
        fileClient2.uploadWithResponse(parallelUploadOptions, null, null);
    }

    @Test
    public void downloadAllNull() {
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        primaryFileClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse response = primaryFileClient.downloadWithResponse(stream, null, null, null);
        byte[] body = stream.toByteArray();
        ShareFileDownloadHeaders headers = response.getDeserializedHeaders();

        assertArrayEquals(DATA.getDefaultBytes(), body);
        CoreUtils.isNullOrEmpty(headers.getMetadata());
        assertNotNull(headers.getContentLength());
        assertNotNull(headers.getContentType());
        assertNull(headers.getContentMd5());
        assertNull(headers.getContentEncoding());
        assertNull(headers.getCacheControl());
        assertNull(headers.getContentDisposition());
        assertNull(headers.getContentLanguage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void downloadEmptyFile(int fileSize) {
        primaryFileClient.create(fileSize);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        primaryFileClient.download(outStream);
        byte[] result = outStream.toByteArray();
        assertEquals(result.length, fileSize);
        if (fileSize > 0) {
            assertEquals(0, result[0]);
        }
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */
    @Test
    public void downloadWithRetryRange() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in FileClient.download().
         */

        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        primaryFileClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        ShareFileClient fc2 = getFileClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(), new MockRetryRangeResponsePolicy("bytes=2-6"));


        ShareFileRange range = new ShareFileRange(2, 6L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(3);
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> fc2.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileDownloadOptions()
                .setRange(range).setRetryOptions(options), null, null));

        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    public void downloadRetryDefault() {
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        primaryFileClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        ShareFileClient failureClient = getFileClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(), new MockFailureResponsePolicy(5));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        failureClient.download(outStream);
        String bodyStr = outStream.toString();

        assertEquals(bodyStr, DATA.getDefaultText());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void downloadTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.create(DATA.getDefaultDataSizeLong());
        shareFileClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        shareFileClient.download(outStream);
        String downloadedData = outStream.toString();
        assertEquals(downloadedData, DATA.getDefaultText());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void downloadOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);

        fileClient.create(DATA.getDefaultDataSizeLong());
        fileClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        ShareFileProperties properties = fileClient.getProperties();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse response = fileClient.downloadWithResponse(stream, null, null, null);
        byte[] body = stream.toByteArray();
        ShareFileDownloadHeaders headers = response.getDeserializedHeaders();

        assertArrayEquals(body, DATA.getDefaultBytes());
        CoreUtils.isNullOrEmpty(headers.getMetadata());
        assertEquals(headers.getContentLength(), properties.getContentLength());
        assertEquals(headers.getContentType(), properties.getContentType());
        assertEquals(headers.getContentMd5(), properties.getContentMd5());
        assertEquals(headers.getContentEncoding(), properties.getContentEncoding());
        assertEquals(headers.getCacheControl(), properties.getCacheControl());
        assertEquals(headers.getContentDisposition(), properties.getContentDisposition());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void uploadRange4TB() {
        long fileSize = 4 * Constants.TB;
        primaryFileClient.create(fileSize);
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong())
                .setOffset(fileSize - DATA.getDefaultDataSizeLong()), null, null); /* Upload to end of file. */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream,
            new ShareFileRange(fileSize - DATA.getDefaultDataSizeLong(), fileSize), true, null, null);

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {
        4 * Constants.MB, // max put range
        5 * Constants.MB})
    public void uploadBufferedRangeGreaterThanMaxPutRange(long length) {
        primaryFileClient.create(length);
        ByteArrayInputStream data = new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer((int) length));
        assertDoesNotThrow(() -> primaryFileClient.upload(data, length, null));

    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void uploadRangeTrailingDot() {
        primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(DATA.getDefaultDataSizeLong());

        ShareFileUploadRangeOptions options = new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong());
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(options, null, null);
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(),
            null, null, null);

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        FileShareTestHelper.assertResponseStatusCode(downloadResponse, 200);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadRangeOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(DATA.getDefaultDataSizeLong());

        Response<ShareFileUploadInfo> uploadResponse = fileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = fileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), DATA.getDefaultDataSizeLong());

        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @ParameterizedTest
    @MethodSource("bufferedUploadVariousPartitions")
    public void bufferedUploadVariousPartitions(Long length, Long uploadChunkLength) {
        primaryFileClient.create(length);
        ByteArrayInputStream data = new ByteArrayInputStream(FileShareTestHelper
            .getRandomBuffer(Math.toIntExact(length)));
        assertNotNull(primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength)));
    }

    private static Stream<Arguments> bufferedUploadVariousPartitions() {
        return Stream.of(
            Arguments.of(1024L, null),
            Arguments.of(1024L, 1024L),
            Arguments.of(1024L, 256L),
            Arguments.of(4L * Constants.MB, null),
            Arguments.of(4L * Constants.MB, 1024L),
            Arguments.of(20L * Constants.MB, null),
            Arguments.of(20L * Constants.MB, 4L * Constants.MB)
        );
    }

    @Test
    public void bufferedUploadErrorPartitionTooBig() {
        long length = 20 * Constants.MB;
        long uploadChunkLength = 20 * Constants.MB;
        primaryFileClient.create(length);
        ByteArrayInputStream data = new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer((int) length));

        assertThrows(Exception.class, () -> primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength)));
    }

    @Test
    public void uploadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
                DATA.getDefaultDataSizeLong()).setOffset(1L), null, null));

        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void parallelUploadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void uploadRangeDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void uploadDataRetryOnTransientFailure() {
        ShareFileClient clientWithFailure = getFileClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(), new TransientFailureInjectingHttpPipelinePolicy());

        primaryFileClient.create(1024);
        clientWithFailure.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        primaryFileClient.downloadWithResponse(os, new ShareFileRange(0, DATA.getDefaultDataSizeLong() - 1), null, null,
            null);
        assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void uploadAndClearRange() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = FileShareTestHelper.getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());
        primaryFileClient.clearRange(7);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        primaryFileClient.downloadWithResponse(stream, new ShareFileRange(0, 6L), false, null, null);

        for (byte b : stream.toByteArray()) {
            assertEquals(0, b);
        }
    }

    @Test
    public void uploadAndClearRangeWithArgs() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = FileShareTestHelper.getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());
        primaryFileClient.clearRangeWithResponse(7, 1, null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, 7L), false, null, null);

        for (byte b : stream.toByteArray()) {
            assertEquals(0, b);
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void clearRangeTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.clearRangeWithResponse(
            DATA.getDefaultDataSizeLong(), 0, null, null), 201);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadAndClearRangeOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);

        String fullInfoString = "please clear the range";
        InputStream fullInfoData = FileShareTestHelper.getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        fileClient.create(fullInfoString.length());
        fileClient.uploadRange(fullInfoData, fullInfoString.length());

        fileClient.clearRange(7);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        fileClient.downloadWithResponse(stream, new ShareFileRange(0, 6L), false, null, null);

        for (byte b : stream.toByteArray()) {
            assertEquals(0, b);
        }
    }

    @Test
    public void clearRangeError() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = FileShareTestHelper.getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.clearRange(30));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE);
    }

    @Test
    public void clearRangeErrorArgs() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = FileShareTestHelper.getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());

        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.clearRangeWithResponse(7, 20, null, null));

        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE);
    }

    @ParameterizedTest
    @MethodSource("uploadDataLengthMismatchSupplier")
    public void uploadDataLengthMismatch(int size, String errMsg) {
        primaryFileClient.create(1024);
        UnexpectedLengthException e = assertThrows(UnexpectedLengthException.class,
            () -> primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
                DATA.getDefaultInputStream(), size), null, Context.NONE));

        assertTrue(e.getMessage().contains(errMsg));
    }

    private static Stream<Arguments> uploadDataLengthMismatchSupplier() {
        return Stream.of(
            Arguments.of(6, "more than"),
            Arguments.of(8, "less than"));
    }

    @ParameterizedTest
    @MethodSource("uploadDataLengthMismatchSupplier")
    public void parallelUploadDataLengthMismatch(int size, String errMsg) {
        primaryFileClient.create(1024);

        UnexpectedLengthException e = assertThrows(UnexpectedLengthException.class,
            () -> primaryFileClient.upload(DATA.getDefaultInputStream(), size, null));
        assertTrue(e.getMessage().contains(errMsg));
    }

    @ParameterizedTest
    @MethodSource("uploadDataLengthMismatchSupplier")
    public void uploadRangeLengthMismatch(int size, String errMsg) {
        primaryFileClient.create(1024);
        UnexpectedLengthException e = assertThrows(UnexpectedLengthException.class,
            () -> primaryFileClient.uploadRange(DATA.getDefaultInputStream(), size));
        assertTrue(e.getMessage().contains(errMsg));
    }

    @Test
    public void downloadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileRange(0, 1023L),
                false, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void uploadFileDoesNotExist() {
        File uploadFile = new File(testFolder.getPath() + "/fakefile.txt");
        if (uploadFile.exists()) {
            assert uploadFile.delete();
        }
        UncheckedIOException e = assertThrows(UncheckedIOException.class,
            () -> primaryFileClient.uploadFromFile(uploadFile.getPath()));
        assertInstanceOf(NoSuchFileException.class, e.getCause());

        // cleanup
        uploadFile.delete();
    }

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
        ShareServiceClient shareServiceClient = new ShareServiceClientBuilder()
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .buildClient();

        ShareFileClient fileClient = shareServiceClient.getShareClient(shareName)
            .createFile(filePath, fileSize);

        File file = FileShareTestHelper.getRandomFile(fileSize);
        fileClient.uploadFromFile(file.toPath().toString());
        File outFile = new File(generatePathName() + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        fileClient.downloadToFile(outFile.toPath().toString());
        assertTrue(FileShareTestHelper.compareFiles(file, outFile, 0, fileSize));

        //cleanup
        shareServiceClient.deleteShare(shareName);
        outFile.delete();
        file.delete();
    }

    @Test
    public void uploadAndDownloadFileExists() throws IOException {
        String data = "Download file exists";
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        if (!downloadFile.exists()) {
            assertTrue(downloadFile.createNewFile());
        }

        primaryFileClient.create(data.length());
        primaryFileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes(StandardCharsets.UTF_8)),
            data.length());
        UncheckedIOException e = assertThrows(UncheckedIOException.class,
            () -> primaryFileClient.downloadToFile(downloadFile.getPath()));
        assertInstanceOf(FileAlreadyExistsException.class, e.getCause());

        // cleanup
        downloadFile.delete();
    }

    @Test
    public void uploadAndDownloadToFileDoesNotExist() throws IOException {
        String data = "Download file DoesNotExist";
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), prefix));

        if (downloadFile.exists()) {
            assertTrue(downloadFile.createNewFile());
        }

        primaryFileClient.create(data.length());
        primaryFileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes(StandardCharsets.UTF_8)),
            data.length());
        primaryFileClient.downloadToFile(downloadFile.getPath());

        Scanner scanner = new Scanner(downloadFile).useDelimiter("\\Z");
        assertEquals(data, scanner.next());
        scanner.close();
        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), downloadFile.getName());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void uploadRangePreserveFileLastWrittenOn() {
        FileLastWrittenMode[] modes = {FileLastWrittenMode.NOW, FileLastWrittenMode.PRESERVE};
        for (FileLastWrittenMode mode : modes) {
            primaryFileClient.create(Constants.KB);
            ShareFileProperties initialProps = primaryFileClient.getProperties();
            primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
                new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(Constants.KB)),
                Constants.KB).setLastWrittenMode(mode), null, null);
            ShareFileProperties resultProps = primaryFileClient.getProperties();

            if (mode.equals(FileLastWrittenMode.PRESERVE)) {
                assertEquals(initialProps.getSmbProperties().getFileLastWriteTime(), resultProps.getSmbProperties()
                    .getFileLastWriteTime());
            } else {
                assertNotEquals(initialProps.getSmbProperties().getFileLastWriteTime(), resultProps.getSmbProperties()
                    .getFileLastWriteTime());
            }
        }
    }

    @Disabled("the groovy test was not testing this test properly. need to investigate this test further.")
    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "ü1ü" /* Something that needs to be url encoded. */
    })
    public void uploadRangeFromURL(String pathSuffix) {
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient();
        primaryFileClient.create(1024);
        String data = "The quick brown fox jumps over the lazy dog";
        int sourceOffset = 5;
        int length = 5;
        int destinationOffset = 0;

        primaryFileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes()), data.length());
        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(
            ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        ShareFileClient client = fileBuilderHelper(shareName, "destination" + pathSuffix)
            .endpoint(primaryFileClient.getFileUrl().toString())
            .buildFileClient();


        client.create(1024);
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileClient.getFileUrl() + "?"
            + sasToken);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        client.download(stream);
        String result = new String(stream.toByteArray());

        for (int i = 0; i < length; i++) {
            //the groovy test was not asserting this line properly. need to come back and investigate this test further.
            assertEquals(result.charAt(destinationOffset + i), data.charAt(sourceOffset + i));
        }
    }

    /*@RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void uploadRangeFromURLSourceErrorAndStatusCode() {
        primaryFileClient.create(1024);
        ShareFileClient destinationClient = shareClient.getFileClient(generatePathName());
        destinationClient.create(1024);

        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> destinationClient.uploadRangeFromUrl(5, 0, 0, primaryFileClient.getFileUrl()));

        assertTrue(e.getStatusCode() == 401);
        assertTrue(e.getServiceMessage().contains("NoAuthenticationInformation"));
        assertTrue(e.getServiceMessage().contains("Server failed to authenticate the request. Please refer to the information in the www-authenticate header."));
    }*/

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadRangeFromURLOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClientSharedKey(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(1024);

        String data = "The quick brown fox jumps over the lazy dog";
        int sourceOffset = 5;
        int length = 5;
        int destinationOffset = 0;

        fileClient.uploadRange(FileShareTestHelper.getInputStream(data.getBytes()), data.length());
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
        ShareFileClient fileClientDest = dirClient.getFileClient(fileNameDest);
        fileClientDest.create(1024);

        Response<ShareFileUploadRangeFromUrlInfo> uploadResponse = fileClientDest.uploadRangeFromUrlWithResponse(length,
            destinationOffset, sourceOffset, fileClient.getFileUrl() + "?" + sasToken, null, null);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = fileClientDest.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        FileShareTestHelper.assertResponseStatusCode(uploadResponse, 201);
        assertTrue(downloadResponse.getStatusCode() == 200 || downloadResponse.getStatusCode() == 206);
        assertEquals(headers.getContentLength(), 1024);

        assertNotNull(headers.getETag());
        assertNotNull(headers.getLastModified());
        assertNotNull(headers.getFilePermissionKey());
        assertNotNull(headers.getFileAttributes());
        assertNotNull(headers.getFileLastWriteTime());
        assertNotNull(headers.getFileCreationTime());
        assertNotNull(headers.getFileChangeTime());
        assertNotNull(headers.getFileParentId());
        assertNotNull(headers.getFileId());

        //u
        assertEquals(stream.toByteArray()[0], 117);

    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void uploadRangeFromUrlPreserveFileLastWrittenOn() {
        FileLastWrittenMode[] modes = {FileLastWrittenMode.NOW, FileLastWrittenMode.PRESERVE};

        primaryFileClient.create(Constants.KB);
        ShareFileClient destinationClient = shareClient.getFileClient(generatePathName());
        destinationClient.create(Constants.KB);
        ShareFileProperties initialProps = destinationClient.getProperties();
        primaryFileClient.uploadRange(new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(Constants.KB)),
            Constants.KB);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential
            .fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString());
        String sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode();

        for (FileLastWrittenMode mode : modes) {
            destinationClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(Constants.KB,
                primaryFileClient.getFileUrl() + "?" + sasToken).setLastWrittenMode(mode), null, null);
            ShareFileProperties resultProps = destinationClient.getProperties();
            if (mode.equals(FileLastWrittenMode.PRESERVE)) {
//                assertEquals(initialProps.getSmbProperties().getFileLastWriteTime(), resultProps.getSmbProperties()
//                    .getFileLastWriteTime());
                assertTrue(FileShareTestHelper.compareDatesWithPrecision(
                    initialProps.getSmbProperties().getFileLastWriteTime(),
                    resultProps.getSmbProperties().getFileLastWriteTime()));
            } else {
                assertNotEquals(initialProps.getSmbProperties().getFileLastWriteTime(), resultProps.getSmbProperties()
                    .getFileLastWriteTime());
            }
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void uploadRangeFromUrlTrailingDot() {
        shareClient = getShareClient(shareName, true, true);
        ShareDirectoryClient directoryClient = shareClient.getRootDirectoryClient();
        ShareFileClient sourceClient = directoryClient.getFileClient(generatePathName() + ".");
        sourceClient.create(Constants.KB);

        ShareFileClient destinationClient = directoryClient.getFileClient(generatePathName() + ".");
        destinationClient.create(Constants.KB);

        sourceClient.uploadRange(new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(Constants.KB)),
            Constants.KB);
        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions);
        String sasToken = shareClient.generateSas(sasValues);

        Response<ShareFileUploadRangeFromUrlInfo> res = destinationClient.uploadRangeFromUrlWithResponse(
            new ShareFileUploadRangeFromUrlOptions(Constants.KB, sourceClient.getFileUrl() + "?" + sasToken), null,
            null);

        FileShareTestHelper.assertResponseStatusCode(res, 201);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void uploadRangeFromUrlTrailingDotFail() {
        shareClient = getShareClient(shareName, true, false);
        ShareDirectoryClient directoryClient = shareClient.getRootDirectoryClient();
        ShareFileClient sourceClient = directoryClient.getFileClient(generatePathName() + ".");
        sourceClient.create(DATA.getDefaultDataSizeLong());

        ShareFileClient destinationClient = directoryClient.getFileClient(generatePathName() + ".");
        destinationClient.create(DATA.getDefaultDataSizeLong());

        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());
        // error thrown: CannotVerifyCopySource
        assertThrows(ShareStorageException.class, () -> destinationClient
            .uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(DATA.getDefaultDataSizeLong(),
                sourceClient.getFileUrl()), null, null));
    }

    @Test
    public void openInputStreamWithRange() throws IOException {
        primaryFileClient.create(1024);
        ShareFileRange shareFileRange = new ShareFileRange(5L, 10L);
        byte[] dataBytes = "long test string".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStreamData = new ByteArrayInputStream(dataBytes);
        primaryFileClient.upload(inputStreamData, dataBytes.length, null);
        int totalBytesRead = 0;
        StorageFileInputStream stream = primaryFileClient.openInputStream(shareFileRange);
        while (stream.read() != -1) {
            totalBytesRead++;
        }
        stream.close();
        assertEquals(6, totalBytesRead);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "ü1ü" /* Something that needs to be url encoded. */
    })
    public void startCopy(String pathSuffix) {
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient();
        primaryFileClient.create(1024);
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        String sourceURL = primaryFileClient.getFileUrl();
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, new ShareFileCopyOptions(),
            null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse.getValue().getCopyId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithArgs(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey);
        }

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, smbProperties,
            setFilePermission ? FILE_PERMISSION : null, permissionType, ignoreReadOnly,
            setArchiveAttribute, null, null, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse.getValue().getCopyId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void startCopyTrailingDot() {
        shareClient = getShareClient(shareName, true, true);
        ShareFileClient sourceClient = shareClient.getFileClient(generatePathName() + ".");
        sourceClient.create(1024);

        ShareFileClient destClient = shareClient.getFileClient(generatePathName() + ".");
        destClient.create(1024);

        byte[] data = FileShareTestHelper.getRandomBuffer(Constants.KB);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        sourceClient.uploadRange(inputStream, Constants.KB);

        SyncPoller<ShareFileCopyInfo, Void> poller = destClient.beginCopy(sourceClient.getFileUrl(),
            new ShareFileCopyOptions(), null);
        poller.waitForCompletion();
        assertEquals(poller.poll().getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void startCopyTrailingDotFail() {
        shareClient = getShareClient(shareName, true, false);
        ShareFileClient sourceClient = shareClient.getFileClient(generatePathName() + ".");
        sourceClient.create(1024);

        ShareFileClient destClient = shareClient.getFileClient(generatePathName() + ".");
        destClient.create(1024);
        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> destClient.beginCopy(sourceClient.getFileUrl(), new ShareFileCopyOptions(), null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap between"
        + " the time subscribed and the time we start observing events.")
    @Test
    public void startCopyError() {
        primaryFileClient.create(1024);
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy("some url", testMetadata, null);
        ShareStorageException e = assertThrows(ShareStorageException.class, poller::waitForCompletion);
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE);
    }

    /*@RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-08-04")
    @Test
    public void startCopySourceErrorAndStatusCode() {
        primaryFileClient.create(1024);

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> {
            SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy("https://error.file.core.windows.net/garbage", testMetadata, null);
            poller.waitForCompletion();
        });

        assertTrue(e.getStatusCode() == 400);
        assertTrue(e.getServiceMessage().contains("InvalidUri"));
        assertTrue(e.getServiceMessage().contains("The requested URI does not represent any resource on the server."));
    }*/

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyArgumentsSupplier")
    public void startCopyWithOptions(boolean setFilePermissionKey, boolean setFilePermission, boolean ignoreReadOnly,
        boolean setArchiveAttribute, PermissionCopyModeType permissionType) {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
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

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @Test
    public void startCopyWithOptionsIgnoreReadOnlyAndSetArchive() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setIgnoreReadOnly(true)
            .setArchiveAttribute(true);

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsFilePermission() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
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

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        FileSmbProperties properties = primaryFileClient.getProperties().getSmbProperties();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileCreationTime(),
            smbProperties.getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileLastWriteTime(),
            smbProperties.getFileLastWriteTime());
        assertEquals(properties.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsChangeTime() {
        ShareFileInfo client = primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        // We recreate file properties for each test since we need to store the times for the test with
        // testResourceNamer.now()
        smbProperties.setFileChangeTime(testResourceNamer.now());
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE);

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        FileShareTestHelper.compareDatesWithPrecision(smbProperties.getFileChangeTime(),
            primaryFileClient.getProperties().getSmbProperties().getFileChangeTime());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsCopySmbFilePropertiesPermissionKey() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
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

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        FileSmbProperties properties = primaryFileClient.getProperties().getSmbProperties();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileCreationTime(),
            smbProperties.getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(properties.getFileLastWriteTime(),
            smbProperties.getFileLastWriteTime());
        assertEquals(properties.getNtfsFileAttributes(), smbProperties.getNtfsFileAttributes());
    }

    @Test
    public void startCopyWithOptionLease() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileClient).acquireLease();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions);
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @Test
    public void startCopyWithOptionsInvalidLease() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        String leaseId = testResourceNamer.randomUuid();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions);
        // exception: LeaseNotPresentWithFileOperation
        assertThrows(ShareStorageException.class, () -> primaryFileClient.beginCopy(sourceURL, options, null));
    }

    @Test
    public void startCopyWithOptionsMetadata() {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setMetadata(testMetadata);

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void startCopyWithOptionsWithOriginalSmbProperties() {
        primaryFileClient.create(1024);
        ShareFileProperties initialProperties = primaryFileClient.getProperties();
        OffsetDateTime creationTime = initialProperties.getSmbProperties().getFileCreationTime();
        OffsetDateTime lastWrittenTime = initialProperties.getSmbProperties().getFileLastWriteTime();
        OffsetDateTime changedTime = initialProperties.getSmbProperties().getFileChangeTime();
        EnumSet<NtfsFileAttributes> fileAttributes = initialProperties.getSmbProperties().getNtfsFileAttributes();

        String sourceURL = primaryFileClient.getFileUrl();
        String leaseId = createLeaseClient(primaryFileClient).acquireLease();
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId);
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(true)
            .setLastWrittenOn(true)
            .setChangedOn(true)
            .setFileAttributes(true);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions)
            .setSmbPropertiesToCopy(list);

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        ShareFileProperties resultProperties = primaryFileClient.getProperties();

        assertNotNull(pollResponse.getValue().getCopyId());
        assertEquals(pollResponse.getStatus(), LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        FileShareTestHelper.compareDatesWithPrecision(creationTime, resultProperties.getSmbProperties()
            .getFileCreationTime());
        FileShareTestHelper.compareDatesWithPrecision(lastWrittenTime, resultProperties.getSmbProperties()
            .getFileLastWriteTime());
        FileShareTestHelper.compareDatesWithPrecision(changedTime, resultProperties.getSmbProperties()
            .getFileChangeTime());
        assertEquals(fileAttributes, resultProperties.getSmbProperties().getNtfsFileAttributes());
    }

    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#startCopyWithCopySourceFileErrorSupplier")
    public void startCopyWithOptionsCopySourceFileError(boolean createdOn, boolean lastWrittenOn, boolean changedOn,
        boolean fileAttributes) {
        primaryFileClient.create(1024);
        String sourceURL = primaryFileClient.getFileUrl();
        EnumSet<NtfsFileAttributes> ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE);
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(createdOn)
            .setLastWrittenOn(lastWrittenOn)
            .setChangedOn(changedOn)
            .setFileAttributes(fileAttributes);

        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFileChangeTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs);

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(FILE_PERMISSION)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)
            .setSmbPropertiesToCopy(list);

        assertThrows(IllegalArgumentException.class, () -> primaryFileClient.beginCopy(sourceURL, options, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void startCopyOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient sourceClient = dirClient.getFileClient(generatePathName());
        sourceClient.create(DATA.getDefaultDataSizeLong());
        ShareFileClient destClient = dirClient.getFileClient(generatePathName());
        destClient.create(DATA.getDefaultDataSizeLong());
        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());

        String sourceURL = sourceClient.getFileUrl();
        SyncPoller<ShareFileCopyInfo, Void> poller = sourceClient.beginCopy(sourceURL, new ShareFileCopyOptions(),
            null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse.getValue().getCopyId());
    }

    @Test
    public void abortCopy() {
        int fileSize = Constants.MB;
        byte[] bytes = new byte[fileSize];
        ByteArrayInputStream data = new ByteArrayInputStream(bytes);
        ShareFileClient primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
        primaryFileClient.create(fileSize);
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileClient dest = fileBuilderHelper(shareName, filePath).buildFileClient();
        dest.create(fileSize);
        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(sourceURL, new ShareFileCopyOptions(), null);

        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse);
        assertNotNull(pollResponse.getValue());
        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        assertThrows(ShareStorageException.class, () -> dest.abortCopy(pollResponse.getValue().getCopyId()));
    }

    @Test
    public void abortCopyLease() {
        int fileSize = Constants.MB;
        byte[] bytes = new byte[fileSize];
        ByteArrayInputStream data = new ByteArrayInputStream(bytes);
        ShareFileClient primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
        primaryFileClient.create(fileSize);
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileClient dest = fileBuilderHelper(shareName, filePath).buildFileClient();
        dest.create(fileSize);

        // obtain lease
        String leaseId = createLeaseClient(primaryFileClient).acquireLease();
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(
            sourceURL, new ShareFileCopyOptions().setDestinationRequestConditions(requestConditions), null);

        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse);
        assertNotNull(pollResponse.getValue());
        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        assertThrows(ShareStorageException.class, () -> dest.abortCopyWithResponse(pollResponse.getValue().getCopyId(),
            requestConditions, null, null));
    }

    @Test
    public void abortCopyInvalidLease() {
        int fileSize = Constants.MB;
        byte[] bytes = new byte[fileSize];
        ByteArrayInputStream data = new ByteArrayInputStream(bytes);
        ShareFileClient primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
        primaryFileClient.create(fileSize);
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileClient dest = fileBuilderHelper(shareName, filePath).buildFileClient();
        dest.create(fileSize);

        // create invalid lease
        String leaseId = testResourceNamer.randomUuid();
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId);

        // revisit this test. the groovy test was throwing exception at beginCopy, but not testing abortCopy.
        // exception: LeaseNotPresentWithFileOperation
        assertThrows(ShareStorageException.class, () -> {
            SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(
                sourceURL, new ShareFileCopyOptions().setDestinationRequestConditions(requestConditions), null);

            PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
            assertNotNull(pollResponse);
            assertNotNull(pollResponse.getValue());
            dest.abortCopyWithResponse(pollResponse.getValue().getCopyId(),
                requestConditions, null, null);
        });
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void abortCopyTrailingDot() {
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[Constants.MB]);
        String fileName = generatePathName() + ".";
        ShareFileClient primaryFileClient = getFileClient(shareName, fileName, true, null);
        primaryFileClient.create(Constants.MB);
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null);
        String sourceURL = primaryFileClient.getFileUrl();
        ShareFileClient dest = fileBuilderHelper(shareName, fileName).buildFileClient();
        dest.create(Constants.MB);
        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(sourceURL, new ShareFileCopyOptions(), null);

        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse);
        assertNotNull(pollResponse.getValue());
        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        assertThrows(ShareStorageException.class, () -> dest.abortCopy(pollResponse.getValue().getCopyId()));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void abortCopyOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient sourceClient = dirClient.getFileClient(fileName);
        sourceClient.create(DATA.getDefaultDataSizeLong());
        sourceClient.uploadWithResponse(new ShareFileUploadOptions(DATA.getDefaultInputStream()), null, null);
        String sourceURL = sourceClient.getFileUrl();

        ShareFileClient destClient = dirClient.getFileClient(generatePathName());
        destClient.create(DATA.getDefaultDataSizeLong());
        SyncPoller<ShareFileCopyInfo, Void> poller = destClient.beginCopy(sourceURL, new ShareFileCopyOptions(), null);
        PollResponse<ShareFileCopyInfo> pollResponse = poller.poll();
        assertNotNull(pollResponse);
        assertNotNull(pollResponse.getValue());
        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        assertThrows(ShareStorageException.class, () -> destClient.abortCopy(pollResponse.getValue().getCopyId()));
    }

    @Test
    public void abortCopyError() {
        // Exception thrown: "InvalidQueryParameterValue"
        assertThrows(ShareStorageException.class, () -> primaryFileClient.abortCopy("randomId"));
    }

    @Test
    public void deleteFile() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.deleteWithResponse(null, null), 202);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void deleteFileTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.create(1024);
        FileShareTestHelper.assertResponseStatusCode(shareFileClient.deleteWithResponse(null, null), 202);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void deleteFileOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(Constants.KB);
        FileShareTestHelper.assertResponseStatusCode(fileClient.deleteWithResponse(null, null), 202);
    }

    @Test
    public void deleteFileError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.deleteWithResponse(null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void deleteIfExistsFile() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.deleteIfExistsWithResponse(null, null, null),
            202);
    }

    @Test
    public void deleteIfExistsFileMin() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        primaryFileClient.deleteIfExists();
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        ShareFileClient client = shareClient.getFileClient(generateShareName());
        Response<Boolean> response = client.deleteIfExistsWithResponse(null, null, null);
        assertFalse(response.getValue());
        FileShareTestHelper.assertResponseStatusCode(response, 404);
        assertFalse(client.exists());
    }

    @Test
    public void deleteIfExistsFileThatWasAlreadyDeleted() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        assertTrue(primaryFileClient.deleteIfExists());
        assertFalse(primaryFileClient.deleteIfExists());
    }

    @Test
    public void getProperties() {
        primaryFileClient.create(1024);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        Response<ShareFileProperties> resp = primaryFileClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void getPropertiesTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.create(1024);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());
        Response<ShareFileProperties> resp = shareFileClient.getPropertiesWithResponse(null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void getPropertiesOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());

        ShareFileInfo createInfo = fileClient.create(Constants.KB);
        ShareFileProperties properties = fileClient.getProperties();
        assertEquals(createInfo.getETag(), properties.getETag());
        assertEquals(createInfo.getLastModified(), properties.getLastModified());
        assertEquals(createInfo.getSmbProperties().getFilePermissionKey(),
            properties.getSmbProperties().getFilePermissionKey());
        assertEquals(createInfo.getSmbProperties().getNtfsFileAttributes(),
            properties.getSmbProperties().getNtfsFileAttributes());
        assertEquals(createInfo.getSmbProperties().getFileLastWriteTime(),
            properties.getSmbProperties().getFileLastWriteTime());
        assertEquals(createInfo.getSmbProperties().getFileCreationTime(),
            properties.getSmbProperties().getFileCreationTime());
        assertEquals(createInfo.getSmbProperties().getFileChangeTime(),
            properties.getSmbProperties().getFileChangeTime());
        assertEquals(createInfo.getSmbProperties().getParentId(), properties.getSmbProperties().getParentId());
        assertEquals(createInfo.getSmbProperties().getFileId(), properties.getSmbProperties().getFileId());
    }

    @Test
    public void getPropertiesError() {
        ShareStorageException ex = assertThrows(ShareStorageException.class, () -> primaryFileClient.getProperties());
        assertTrue(ex.getMessage().contains("ResourceNotFound"));
    }

    @Test
    public void setHttpHeadersFpk() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        String filePermissionKey = shareClient.createPermission(FILE_PERMISSION);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);

        Response<ShareFileInfo> resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties,
            null, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @Test
    public void setHttpHeadersFp() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now());

        Response<ShareFileInfo> resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties,
            FILE_PERMISSION, null, null);

        FileShareTestHelper.assertResponseStatusCode(resp, 200);
        assertNotNull(resp.getValue().getETag());
        assertNotNull(resp.getValue().getLastModified());
        assertNotNull(resp.getValue().getSmbProperties());
        assertNotNull(resp.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(resp.getValue().getSmbProperties().getNtfsFileAttributes());
        assertNotNull(resp.getValue().getSmbProperties().getFileLastWriteTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileCreationTime());
        assertNotNull(resp.getValue().getSmbProperties().getFileChangeTime());
        assertNotNull(resp.getValue().getSmbProperties().getParentId());
        assertNotNull(resp.getValue().getSmbProperties().getFileId());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("filePermissionFormatSupplier")
    public void setFileHttpHeadersFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        primaryFileClient.create(512);

        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileSetPropertiesOptions options = new ShareFileSetPropertiesOptions(512)
            .setFilePermissions(new ShareFilePermission().setPermission(permission).setPermissionFormat(filePermissionFormat));

        Response<ShareFileInfo> bagResponse = primaryFileClient.setPropertiesWithResponse(options, null, null);
        Response<ShareFileInfo> nonBagResponse = primaryFileClient.setPropertiesWithResponse(1024,
            null, null, permission, null, filePermissionFormat, null, null);

        FileShareTestHelper.assertResponseStatusCode(bagResponse, 200);
        FileShareTestHelper.assertResponseStatusCode(nonBagResponse, 200);

        assertNotNull(bagResponse.getValue().getSmbProperties().getFilePermissionKey());
        assertNotNull(nonBagResponse.getValue().getSmbProperties().getFilePermissionKey());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void setHttpHeadersChangeTime() {
        primaryFileClient.create(512);
        OffsetDateTime changeTime = testResourceNamer.now();
        primaryFileClient.setProperties(512, null, new FileSmbProperties().setFileChangeTime(changeTime), null);
        FileShareTestHelper.compareDatesWithPrecision(primaryFileClient.getProperties().getSmbProperties()
            .getFileChangeTime(), changeTime);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void setHttpHeadersTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);

        shareFileClient.create(1024);
        OffsetDateTime changeTime = testResourceNamer.now();
        shareFileClient.setProperties(512, null, new FileSmbProperties().setFileChangeTime(changeTime), null);
        FileShareTestHelper.compareDatesWithPrecision(shareFileClient.getProperties().getSmbProperties()
            .getFileChangeTime(), changeTime);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void setHttpHeadersOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);
        httpHeaders = new ShareFileHttpHeaders()
            .setContentType("application/octet-stream")
            .setContentDisposition("attachment")
            .setCacheControl("no-transform")
            .setContentEncoding("gzip")
            .setContentLanguage("en");

        Response<ShareFileInfo> res = fileClient.setPropertiesWithResponse(Constants.KB, httpHeaders, null, null, null,
            null);
        ShareFileProperties properties = fileClient.getProperties();

        FileShareTestHelper.assertResponseStatusCode(res, 200);
        assertNotNull(res.getValue().getETag());
        assertEquals(res.getValue().getETag(), res.getHeaders().getValue(HttpHeaderName.ETAG));
        assertEquals(properties.getContentType(), "application/octet-stream");
        assertEquals(properties.getContentDisposition(), "attachment");
        assertEquals(properties.getCacheControl(), "no-transform");
        assertEquals(properties.getContentEncoding(), "gzip");
        assertNull(properties.getContentMd5());
    }

    @Test
    public void setHttpHeadersError() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.setPropertiesWithResponse(-1, null, null, null, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @Test
    public void setMetadata() {
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");
        ShareFileProperties getPropertiesBefore = primaryFileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = primaryFileClient
            .setMetadataWithResponse(updatedMetadata, null, null);
        ShareFileProperties getPropertiesAfter = primaryFileClient.getProperties();
        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void setMetadataTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        ShareFileProperties getPropertiesBefore = shareFileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = shareFileClient.setMetadataWithResponse(updatedMetadata,
            null, null);
        ShareFileProperties getPropertiesAfter = shareFileClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void setMetadataOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.createWithResponse(Constants.KB, null, null, null, testMetadata, null, null);

        ShareFileProperties getPropertiesBefore = fileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = fileClient.setMetadataWithResponse(updatedMetadata,
            null, null);
        ShareFileProperties getPropertiesAfter = fileClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        FileShareTestHelper.assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @Test
    public void setMetadataError() {
        primaryFileClient.create(1024);
        Map<String, String> errorMetadata = Collections.singletonMap("", "value");

        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.setMetadataWithResponse(errorMetadata, null, null));
        FileShareTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    @Test
    public void listRanges() throws IOException {
        String fileName = generatePathName();
        primaryFileClient.create(1024);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);

        primaryFileClient.listRanges().forEach(it -> {
            assertEquals(0, it.getStart());
            assertEquals(1023, it.getEnd());
        });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void listRangesTrailingDot() throws IOException {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(1024);

        String fileName = generatePathName() + ".";
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);

        primaryFileClient.listRanges().forEach(it -> {
            assertEquals(0, it.getStart());
            assertEquals(1023, it.getEnd());
        });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesWithRange() throws IOException {
        String fileName = generatePathName();
        primaryFileClient.create(1024);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).forEach(it -> {
            assertEquals(0, it.getStart());
            assertEquals(511, it.getEnd());
        });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesSnapshot() throws IOException {
        String fileName = generatePathName();
        primaryFileClient.create(1024);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        ShareSnapshotInfo snapInfo = shareClient.createSnapshot();
        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .snapshot(snapInfo.getSnapshot())
            .buildFileClient();

        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).forEach(it -> {
            assertEquals(0, it.getStart());
            assertEquals(511, it.getEnd());
        });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesSnapshotFail() throws IOException {
        String fileName = generateShareName();
        primaryFileClient.create(1024);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .snapshot("2020-08-07T16:58:02.0000000Z")
            .buildFileClient();

        assertThrows(ShareStorageException.class, () ->
            primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).forEach(it -> {
                assertEquals(0, it.getStart());
                assertEquals(511, it.getEnd());
            }));

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void listRangesOAuth() throws IOException {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(Constants.KB);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        fileClient.uploadFromFile(uploadFile);

        fileClient.listRanges().forEach(it -> {
            assertEquals(0, it.getStart());
            assertEquals(1023, it.getEnd());
        });

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @ParameterizedTest
    @MethodSource("com.azure.storage.file.share.FileShareTestHelper#listRangesDiffSupplier")
    public void listRangesDiff(List<FileRange> rangesToUpdate, List<FileRange> rangesToClear,
        List<FileRange> expectedRanges, List<ClearRange> expectedClearRanges) {
        primaryFileClient.create(4 * Constants.MB);
        primaryFileClient.uploadRange(new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(4 * Constants.MB)),
            4 * Constants.MB);
        String snapshotId = primaryFileServiceClient.getShareClient(primaryFileClient.getShareName())
            .createSnapshot()
            .getSnapshot();

        rangesToUpdate.forEach(it -> {
            long size = it.getEnd() - it.getStart() + 1;
            primaryFileClient.uploadRangeWithResponse(
                new ShareFileUploadRangeOptions(new ByteArrayInputStream(
                    FileShareTestHelper.getRandomBuffer((int) size)), size).setOffset(it.getStart()), null, null);
        });

        rangesToClear.forEach(it -> {
            long size = it.getEnd() - it.getStart() + 1;
            primaryFileClient.clearRangeWithResponse(size, it.getStart(), null, null);
        });

        ShareFileRangeList rangeDiff = primaryFileClient.listRangesDiff(snapshotId);
        assertEquals(expectedRanges.size(), rangeDiff.getRanges().size());
        assertEquals(expectedClearRanges.size(), rangeDiff.getClearRanges().size());

        for (int i = 0; i < expectedRanges.size(); i++) {
            FileRange actualRange = rangeDiff.getRanges().get(i);
            FileRange expectedRange = expectedRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }

        for (int i = 0; i < expectedClearRanges.size(); i++) {
            ClearRange actualRange = rangeDiff.getClearRanges().get(i);
            ClearRange expectedRange = expectedClearRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void listRangesDiffOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);
        fileClient.uploadRange(new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(Constants.KB)),
            Constants.KB);
        String snapshotId = primaryFileServiceClient.getShareClient(fileClient.getShareName())
            .createSnapshot()
            .getSnapshot();

        List<FileRange> rangesToUpdate = FileShareTestHelper.createFileRanges();
        List<FileRange> rangesToClear = FileShareTestHelper.createFileRanges();
        List<FileRange> expectedRanges = FileShareTestHelper.createFileRanges();
        List<FileRange> expectedClearRanges = FileShareTestHelper.createFileRanges();

        rangesToUpdate.forEach(it -> {
            long size = it.getEnd() - it.getStart() + 1;
            fileClient.uploadWithResponse(new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer((int) size)),
                size, it.getStart(), null, null);
        });

        rangesToClear.forEach(it -> {
            long size = it.getEnd() - it.getStart() + 1;
            fileClient.clearRangeWithResponse(size, it.getStart(), null, null);
        });

        ShareFileRangeList rangeDiff = fileClient.listRangesDiff(snapshotId);
        assertEquals(expectedRanges.size(), rangeDiff.getRanges().size());
        assertEquals(expectedClearRanges.size(), rangeDiff.getClearRanges().size());
        for (int i = 0; i < expectedRanges.size(); i++) {
            FileRange actualRange = rangeDiff.getRanges().get(i);
            FileRange expectedRange = expectedRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }

        for (int i = 0; i < expectedClearRanges.size(); i++) {
            ClearRange actualRange = rangeDiff.getClearRanges().get(i);
            FileRange expectedRange = expectedClearRanges.get(i);
            assertEquals(expectedRange.getStart(), actualRange.getStart());
            assertEquals(expectedRange.getEnd(), actualRange.getEnd());
        }
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void listRangesDiffWithRange() throws IOException {
        String fileName = generateShareName();
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong());
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        ShareSnapshotInfo snapInfo = shareClient.createSnapshot();

        primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong()).setOffset(1024L), null, null);

        FileRange range = primaryFileClient.listRangesDiffWithResponse(
            new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRange(new ShareFileRange(1025, 1026L)), null,
            null).getValue().getRanges().get(0);
        assertEquals(1025, range.getStart());
        assertEquals(1026, range.getEnd());

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2020-02-10")
    @Test
    public void listRangesDiffLease() throws IOException {
        String fileName = generateShareName();
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong());
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        ShareSnapshotInfo snapInfo = shareClient.createSnapshot();
        primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong()).setOffset(1024L), null, null);
        String leaseId = createLeaseClient(primaryFileClient).acquireLease();

        FileRange range = primaryFileClient.listRangesDiffWithResponse(
            new ShareFileListRangesDiffOptions(snapInfo.getSnapshot())
                .setRequestConditions(new ShareRequestConditions().setLeaseId(leaseId)), null, null)
            .getValue().getRanges().get(0);
        assertEquals(1024, range.getStart());
        assertEquals(1030, range.getEnd());
        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("listRangesDiffWithRenameSupplier")
    public void listRangesDiffWithRename(Boolean renameSupport) throws IOException {
        //create a file
        String fileName = generateShareName();
        primaryFileClient.create(Constants.MB);

        //upload some content
        ByteArrayInputStream content = new ByteArrayInputStream(FileShareTestHelper.getRandomBuffer(Constants.KB));
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(Constants.KB, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        primaryFileClient.uploadRange(content, Constants.KB);

        //take snapshot
        ShareSnapshotInfo previousSnapshot = shareClient.createSnapshot();

        //rename file
        ShareFileClient destFile = primaryFileClient.rename(generatePathName());

        //take another snapshot
        shareClient.createSnapshot();

        //setup options
        ShareFileListRangesDiffOptions options = new ShareFileListRangesDiffOptions(previousSnapshot.getSnapshot());
        options.setRenameIncluded(renameSupport);

        //call
        if (renameSupport == null || !renameSupport) {
            ShareStorageException e = assertThrows(ShareStorageException.class,
                () -> destFile.listRangesDiffWithResponse(options, null, null));
            assertEquals(ShareErrorCode.PREVIOUS_SNAPSHOT_NOT_FOUND, e.getErrorCode());
        } else {
            Response<ShareFileRangeList> response = destFile.listRangesDiffWithResponse(options, null, null);
            assertEquals(200, response.getStatusCode());
            assertEquals(0, response.getValue().getRanges().size());
        }

        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
        destFile.delete();
    }

    private static Stream<Arguments> listRangesDiffWithRenameSupplier() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(false),
            Arguments.of((Boolean) null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void listRangesDiffTrailingDot() throws IOException {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        String fileNameWithDot = generateShareName() + ".";
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong());
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileNameWithDot);
        primaryFileClient.uploadFromFile(uploadFile);
        ShareSnapshotInfo snapInfo = shareClient.createSnapshot();
        ShareFileUploadRangeOptions options = new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSizeLong()).setOffset(1024L);
        primaryFileClient.uploadRangeWithResponse(options, null, null);
        FileRange range = primaryFileClient.listRangesDiffWithResponse(
            new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRange(new ShareFileRange(1025L, 1026L)), null,
            null).getValue().getRanges().get(0);

        assertEquals(1025, range.getStart());
        assertEquals(1026, range.getEnd());

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileNameWithDot);
    }

    @Test
    public void listRangesDiffLeaseFail() throws IOException {
        String fileName = generateShareName();
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong());
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);
        ShareSnapshotInfo snapInfo = shareClient.createSnapshot();
        primaryFileClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), 1024L, null,
            null);

        assertThrows(ShareStorageException.class, () -> primaryFileClient.listRangesDiffWithResponse(
            new ShareFileListRangesDiffOptions(snapInfo.getSnapshot())
                .setRequestConditions(new ShareRequestConditions()
                    .setLeaseId(testResourceNamer.randomUuid())), null, null).getValue().getRanges().get(0));

        // cleanup
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listRangesDiffFail() throws IOException {
        String fileName = generateShareName();
        primaryFileClient.create(1024);
        String uploadFile = FileShareTestHelper.createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);

        assertThrows(ShareStorageException.class, () ->
            primaryFileClient.listRangesDiffWithResponse(
                new ShareFileListRangesDiffOptions("2020-08-07T16:58:02.0000000Z"), null, null).getValue().getRanges()
                .get(0));
        FileShareTestHelper.deleteFileIfExists(testFolder.getPath(), fileName);
    }

    @Test
    public void listHandles() {
        primaryFileClient.create(1024);
        assertEquals(0, primaryFileClient.listHandles().stream().count());
    }

    @Test
    public void listHandlesWithMaxResult() {
        primaryFileClient.create(1024);
        assertEquals(0, primaryFileClient.listHandles(2, null, null).stream().count());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void listHandlesTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(1024);
        assertEquals(0, primaryFileClient.listHandles().stream().count());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void listHandlesOAuth() {
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB);
        assertEquals(0, fileClient.listHandles().stream().count());
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2023-01-03")
    @Test
    public void listHandlesAccessRights() {
        ShareClient shareClient = primaryFileServiceClient.getShareClient("myshare");
        ShareDirectoryClient directoryClient = shareClient.getDirectoryClient("mydirectory");
        ShareFileClient fileClient = directoryClient.getFileClient("myfile");
        List<HandleItem> list = fileClient.listHandles().stream().collect(Collectors.toList());
        assertEquals(list.get(0).getAccessRights().get(0), ShareFileHandleAccessRights.WRITE);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2023-01-03")
    @Test
    public void forceCloseHandleMin() {
        primaryFileClient.create(512);
        CloseHandlesInfo handlesClosedInfo = primaryFileClient.forceCloseHandle("1");
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        primaryFileClient.create(512);
        assertThrows(ShareStorageException.class, () -> primaryFileClient.forceCloseHandle("invalidHandleId"));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void forceCloseHandleTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(512);
        CloseHandlesInfo handlesClosedInfo = primaryFileClient.forceCloseHandle("1");
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void forceCloseHandleOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName)
            .getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(512);
        CloseHandlesInfo handlesClosedInfo = fileClient.forceCloseHandle("1");
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2019-07-07")
    @Test
    public void forceCloseAllHandlesMin() {
        primaryFileClient.create(512);
        CloseHandlesInfo handlesClosedInfo = primaryFileClient.forceCloseAllHandles(null, null);
        assertEquals(0, handlesClosedInfo.getClosedHandles());
        assertEquals(0, handlesClosedInfo.getFailedHandles());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameMin() {
        primaryFileClient.create(512);
        assertNotNull(primaryFileClient.rename(generatePathName()));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @ParameterizedTest
    @ValueSource(strings = {"\u200B", "\u200C", "\u200D", "\uFEFF"})
    public void renameWithUnicodeChars(String specialChar) {
        ShareFileClient fileClient = shareClient.getFileClient("test-file-source" + specialChar + " pdf.txt");
        fileClient.create(512);
        ShareFileClient destClient = fileClient.rename("test-file-destination" + specialChar + " pdf.txt");
        assertNotNull(destClient);
        assertTrue(Utility.urlEncode(destClient.getFileUrl()).contains(Utility.urlEncode(specialChar)));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameWithResponse() {
        primaryFileClient.create(512);
        Response<ShareFileClient> resp = primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()), null, null);
        ShareFileClient renamedClient = resp.getValue();
        assertNotNull(renamedClient.getProperties());
        assertThrows(ShareStorageException.class, () -> primaryFileClient.getProperties());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-02-12")
    @Test
    public void renameSasToken() {
        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions);
        String sas = shareClient.generateSas(sasValues);
        ShareFileClient client = getFileClient(sas, primaryFileClient.getFileUrl());
        primaryFileClient.create(1024);
        String fileName = generatePathName();
        ShareFileClient destClient = client.rename(fileName);
        assertNotNull(destClient.getProperties());
        assertEquals(fileName, destClient.getFilePath());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameDifferentDirectory() {
        primaryFileClient.create(512);
        ShareDirectoryClient dc = shareClient.getDirectoryClient(generatePathName());
        dc.create();
        ShareFileClient destinationPath = dc.getFileClient(generatePathName());
        ShareFileClient resultClient = primaryFileClient.rename(destinationPath.getFilePath());
        assertTrue(destinationPath.exists());
        assertEquals(destinationPath.getFilePath(), resultClient.getFilePath());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void renameReplaceIfExists(boolean replaceIfExists) {
        primaryFileClient.create(512);
        ShareFileClient destination = shareClient.getFileClient(generatePathName());
        destination.create(512);
        boolean exception = false;
        try {
            primaryFileClient.renameWithResponse(new ShareFileRenameOptions(destination.getFilePath())
                .setReplaceIfExists(replaceIfExists), null, null);
        } catch (ShareStorageException ignored) {
            exception = true;
        }
        assertEquals(replaceIfExists, !exception);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void renameIgnoreReadOnly(boolean ignoreReadOnly) {
        primaryFileClient.create(512);
        FileSmbProperties props = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY));
        ShareFileClient destinationFile = shareClient.getFileClient(generatePathName());
        destinationFile.createWithResponse(512L, null, props, null, null, null, null, null);
        boolean exception = false;

        try {
            primaryFileClient.renameWithResponse(new ShareFileRenameOptions(destinationFile.getFilePath())
                .setIgnoreReadOnly(ignoreReadOnly).setReplaceIfExists(true), null, null);
        } catch (ShareStorageException ignored) {
            exception = true;
        }
        assertEquals(exception, !ignoreReadOnly);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameFilePermission() {
        primaryFileClient.create(512);
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";

        ShareFileClient destClient = primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission), null, null).getValue();

        assertNotNull(destClient.getProperties().getSmbProperties().getFilePermissionKey());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-11-04")
    @ParameterizedTest
    @MethodSource("filePermissionFormatSupplier")
    public void renameFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        primaryFileClient.create(512);

        String permission = FileShareTestHelper.getPermissionFromFormat(filePermissionFormat);

        ShareFileRenameOptions options = new ShareFileRenameOptions(generatePathName()).setFilePermission(permission)
            .setFilePermissionFormat(filePermissionFormat);

        Response<ShareFileClient> destClientResponse = primaryFileClient.renameWithResponse(options, null, null);

        FileShareTestHelper.assertResponseStatusCode(destClientResponse, 200);
        assertNotNull(destClientResponse.getValue().getProperties().getSmbProperties().getFilePermissionKey());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameFilePermissionAndKeySet() {
        primaryFileClient.create(512);
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        // permission and key cannot both be set
        assertThrows(ShareStorageException.class, () ->  primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission)
            .setSmbProperties(new FileSmbProperties().setFilePermissionKey("filePermissionkey")), null, null)
            .getValue());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void renameFileSmbProperties() {
        primaryFileClient.create(512);
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        String permissionKey = shareClient.createPermission(filePermission);
        OffsetDateTime fileCreationTime = testResourceNamer.now().minusDays(5);
        OffsetDateTime fileLastWriteTime = testResourceNamer.now().minusYears(2);
        OffsetDateTime fileChangeTime = testResourceNamer.now();
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setFilePermissionKey(permissionKey)
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.ARCHIVE, NtfsFileAttributes.READ_ONLY))
            .setFileCreationTime(fileCreationTime)
            .setFileLastWriteTime(fileLastWriteTime)
            .setFileChangeTime(fileChangeTime);

        ShareFileClient destClient = primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setSmbProperties(smbProperties), null, null).getValue();
        ShareFileProperties destProperties = destClient.getProperties();

        assertEquals(destProperties.getSmbProperties().getNtfsFileAttributes(), EnumSet.of(NtfsFileAttributes.ARCHIVE,
            NtfsFileAttributes.READ_ONLY));
        assertNotNull(destProperties.getSmbProperties().getFileCreationTime());
        assertNotNull(destProperties.getSmbProperties().getFileLastWriteTime());
        FileShareTestHelper.compareDatesWithPrecision(destProperties.getSmbProperties().getFileChangeTime(),
            fileChangeTime);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameMetadata() {
        primaryFileClient.create(512);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        Response<ShareFileClient> resp = primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()).setMetadata(updatedMetadata), null, null);

        ShareFileClient renamedClient = resp.getValue();
        ShareFileProperties getPropertiesAfter = renamedClient.getProperties();
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2022-11-02")
    @Test
    public void renameTrailingDot() {
        shareClient = getShareClient(shareName, true, true);

        ShareDirectoryClient rootDirectory = shareClient.getRootDirectoryClient();
        ShareFileClient primaryFileClient = rootDirectory.getFileClient(generatePathName() + ".");
        primaryFileClient.create(1024);

        Response<ShareFileClient> response = primaryFileClient
            .renameWithResponse(new ShareFileRenameOptions(generatePathName() + "."), null, null);

        FileShareTestHelper.assertResponseStatusCode(response, 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameError() {
        primaryFileClient = shareClient.getFileClient(generatePathName());
        assertThrows(ShareStorageException.class, () -> primaryFileClient.rename(generatePathName()));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameSourceAC() {
        primaryFileClient.create(512);
        String leaseID = setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(leaseID);

        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()).setSourceRequestConditions(src), null, null), 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameSourceACFail() {
        primaryFileClient.create(512);
        setupFileLeaseCondition(primaryFileClient, GARBAGE_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(GARBAGE_LEASE_ID);

        assertThrows(ShareStorageException.class, () ->
            primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
                .setSourceRequestConditions(src), null, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameDestAC() {
        primaryFileClient.create(512);
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        String leaseID = setupFileLeaseCondition(destFile, RECEIVED_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(leaseID);

        FileShareTestHelper.assertResponseStatusCode(primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(pathName).setDestinationRequestConditions(src).setReplaceIfExists(true), null,
            null), 200);
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameDestACFail() {
        primaryFileClient.create(512);
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        setupFileLeaseCondition(destFile, GARBAGE_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(GARBAGE_LEASE_ID);

        // should be throwing ShareStorageException, but test-proxy causes an error with mismatched requests
        assertThrows(RuntimeException.class, () -> destFile.renameWithResponse(new ShareFileRenameOptions(pathName)
            .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null));
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-06-08")
    @Test
    public void renameContentType() {
        primaryFileClient.create(512);
        Response<ShareFileClient> resp = primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()).setContentType("mytype"), null, null);

        ShareFileClient renamedClient = resp.getValue();
        ShareFileProperties props = renamedClient.getProperties();
        assertEquals(props.getContentType(), "mytype");
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2021-04-10")
    @Test
    public void renameOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());

        fileClient.create(512);
        String fileRename = generatePathName();
        Response<ShareFileClient> resp = fileClient.renameWithResponse(new ShareFileRenameOptions(fileRename), null,
            null);

        ShareFileClient renamedClient = resp.getValue();
        renamedClient.getProperties();
        assertEquals(fileRename, renamedClient.getFilePath()); // compare with new filename

        assertThrows(ShareStorageException.class, fileClient::getProperties);
    }

    @Test
    public void getSnapshotId() {
        String snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString();

        ShareFileClient shareSnapshotClient = fileBuilderHelper(shareName, filePath).snapshot(snapshot)
            .buildFileClient();
        assertEquals(snapshot, shareSnapshotClient.getShareSnapshotId());
    }

    @Test
    public void getShareName() {
        assertEquals(shareName, primaryFileClient.getShareName());
    }

    @Test
    public void getFilePath() {
        assertEquals(filePath, primaryFileClient.getFilePath());
    }

    private static Stream<Arguments> getNonEncodedFileNameSupplier() {
        return Stream.of(
            Arguments.of("test%test"),
            Arguments.of("%Россия 한국 中国!"),
            Arguments.of("%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點")
        );
    }

    @ParameterizedTest
    @MethodSource("getNonEncodedFileNameSupplier")
    public void getNonEncodedFileName(String fileName) {
        ShareFileClient fileClient = shareClient.getFileClient(fileName);
        assertEquals(fileName, fileClient.getFilePath());
        fileClient.create(1024);
        assertTrue(fileClient.exists());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        primaryFileClient.create(512);

        ShareFileClient fileClient = fileBuilderHelper(primaryFileClient.getShareName(),
            primaryFileClient.getFilePath()).addPolicy(getPerCallVersionPolicy()).buildFileClient();

        Response<ShareFileProperties> response = fileClient.getPropertiesWithResponse(null, null);
        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }

    @Test
    public void defaultAudience() {
        String fileName = generatePathName();
        ShareFileClient fileClient = fileBuilderHelper(shareName, fileName).buildFileClient();
        fileClient.create(Constants.KB);
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(null) /* should default to "https://storage.azure.com/" */);

        ShareFileClient aadFileClient = oAuthServiceClient.getShareClient(shareName).getFileClient(fileName);
        assertTrue(aadFileClient.exists());
    }

    @Test
    public void storageAccountAudience() {
        String fileName = generatePathName();
        ShareFileClient fileClient = fileBuilderHelper(shareName, fileName).buildFileClient();
        fileClient.create(Constants.KB);
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience(shareClient.getAccountName())));

        ShareFileClient aadFileClient = oAuthServiceClient.getShareClient(shareName).getFileClient(fileName);
        assertTrue(aadFileClient.exists());
    }

    @Test
    public void audienceError() {
        String fileName = generatePathName();
        ShareFileClient fileClient = fileBuilderHelper(shareName, fileName).buildFileClient();
        fileClient.create(Constants.KB);
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(ShareAudience.createShareServiceAccountAudience("badAudience")));

        ShareFileClient aadFileClient = oAuthServiceClient.getShareClient(shareName).getFileClient(fileName);
        ShareStorageException e = assertThrows(ShareStorageException.class, aadFileClient::exists);
        assertEquals(ShareErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.file.core.windows.net/", shareClient.getAccountName());
        ShareAudience audience = ShareAudience.fromString(url);

        String fileName = generatePathName();
        ShareFileClient fileClient = fileBuilderHelper(shareName, fileName).buildFileClient();
        fileClient.create(Constants.KB);
        ShareServiceClient oAuthServiceClient =
            getOAuthServiceClient(new ShareServiceClientBuilder()
                .shareTokenIntent(ShareTokenIntent.BACKUP)
                .audience(audience));

        ShareFileClient aadFileClient = oAuthServiceClient.getShareClient(shareName).getFileClient(fileName);
        assertTrue(aadFileClient.exists());
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "2024-02-04")
    @Test
    public void listHandlesClientName() {
        ShareClient client = primaryFileServiceClient.getShareClient("testing");
        ShareDirectoryClient directoryClient = client.getDirectoryClient("dir1");
        ShareFileClient fileClient = directoryClient.getFileClient("test.txt");
        List<HandleItem> list = fileClient.listHandles().stream().collect(Collectors.toList());
        assertNotNull(list.get(0).getClientName());
    }
}
