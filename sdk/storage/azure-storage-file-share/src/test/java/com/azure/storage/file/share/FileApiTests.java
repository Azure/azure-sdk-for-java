// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy;
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy;
import com.azure.storage.file.share.models.*;

import com.azure.storage.file.share.options.ShareFileCopyOptions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareFileSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static com.azure.storage.file.share.FileTestHelper.assertExceptionStatusCodeAndMessage;
import static com.azure.storage.file.share.FileTestHelper.assertResponseStatusCode;
import static com.azure.storage.file.share.FileTestHelper.compareFiles;
import static com.azure.storage.file.share.FileTestHelper.createClearRanges;
import static com.azure.storage.file.share.FileTestHelper.createFileRanges;
import static com.azure.storage.file.share.FileTestHelper.createRandomFileWithLength;
import static com.azure.storage.file.share.FileTestHelper.deleteFileIfExists;
import static com.azure.storage.file.share.FileTestHelper.getRandomBuffer;
import static com.azure.storage.file.share.FileTestHelper.getRandomByteBuffer;
import static com.azure.storage.file.share.FileTestHelper.getRandomFile;
import static org.junit.jupiter.api.Assertions.*;

class FileApiTests extends FileShareTestBase {
    ShareFileClient primaryFileClient;
    ShareClient shareClient;
    String shareName;
    String filePath;
    static Map<String, String> testMetadata;
    static ShareFileHttpHeaders httpHeaders;
    FileSmbProperties smbProperties;
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL";

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
        assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.SHARE_NOT_FOUND);
    }

    @Test
    public void createFile() {
        assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null, null, null, null), 201);
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    public void createFile4TB() {
        assertResponseStatusCode(primaryFileClient.createWithResponse(4 * Constants.TB, null, null, null, null, null, null), 201);
    }

    @Test
    public void createFileError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.create(-1));
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @Test
    public void createFileWithArgsFpk() {
        String filePermissionKey = shareClient.createPermission(filePermission);
        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey);
        Response<ShareFileInfo> resp = primaryFileClient
            .createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata, null, null);

        assertResponseStatusCode(resp, 201);
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
            filePermission, testMetadata, null, null);

        assertResponseStatusCode(resp, 201);
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

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createChangeTime() {
        OffsetDateTime changeTime = testResourceNamer.now();
        primaryFileClient.createWithResponse(512, null, new FileSmbProperties().setFileChangeTime(changeTime),
            null, null, null, null, null);
        compareDatesWithPrecision(primaryFileClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void createFileOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);


        Response<ShareFileInfo> result = fileClient.createWithResponse(Constants.KB, null, null, null, null, null, null);
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

        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @ParameterizedTest
    @MethodSource("permissionAndKeySupplier")
    public void createFilePermissionAndKeyError(String filePermissionKey, String permission) {
        FileSmbProperties smbProperties = new FileSmbProperties().setFilePermissionKey(filePermissionKey);
        assertThrows(IllegalArgumentException.class, () ->
                primaryFileClient.createWithResponse(1024, null, smbProperties, permission, null,
                    null, null));
    }

    private static Stream<List<String>> permissionAndKeySupplier() {
        return Stream.of(
            Arrays.asList("filePermissionKey", filePermission),
            Arrays.asList(null, new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))));
    }

    @DisabledIf("olderThan20221102ServiceVersion")
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
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        assertResponseStatusCode(uploadResponse, 201);
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
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1L), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient
            .downloadWithResponse(stream, new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true, null, null);

        assertResponseStatusCode(uploadResponse, 201);
        assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());
        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void uploadAndDownloadDataOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create();
        String fileName = generatePathName();
        ShareFileClient fileClient = dirClient.getFileClient(fileName);
        fileClient.create(DATA.getDefaultDataSizeLong());

        Response<ShareFileUploadInfo> uploadResponse = fileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = fileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        assertResponseStatusCode(uploadResponse, 201);
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
        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()),
            null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();

        assertResponseStatusCode(uploadResponse, 201);
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
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1L), null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, DATA.getDefaultDataSizeLong()), true, null, null);

        assertResponseStatusCode(uploadResponse, 201);
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
            primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), length), null, null);
        }
    }

    @Test
    public void uploadSuccessfulRetry() {
        primaryFileClient.create(DATA.getDefaultDataSize());
        ShareFileClient clientWithFailure = getFileClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());


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
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null);
        ShareFileDownloadHeaders headers = downloadResponse.getDeserializedHeaders();


        assertResponseStatusCode(uploadResponse, 201);
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

        assertResponseStatusCode(uploadResponse, 201);
        assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), (long) DATA.getDefaultDataSizeLong());

        assertArrayEquals(DATA.getDefaultBytes(), stream.toByteArray());
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
        assertNotNull(headers.getContentMd5());
        assertNotNull(headers.getContentEncoding());
        assertNotNull(headers.getCacheControl());
        assertNotNull(headers.getContentDisposition());
        assertNotNull(headers.getContentLanguage());
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
            assertNotEquals(0, result[0]);
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
        ShareFileClient fc2 = getFileClient(ENVIRONMENT.getPrimaryAccount().getCredential(), primaryFileClient.getFileUrl(),
            new MockRetryRangeResponsePolicy("bytes=2-6"));


        ShareFileRange range = new ShareFileRange(2, 6L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(3);
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> fc2.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileDownloadOptions()
                .setRange(range).setRetryOptions(options), null, null));

        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        assertInstanceOf(IOException.class, e);
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

    @DisabledIf("olderThan20221102ServiceVersion")
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

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void downloadOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
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

    @DisabledIf("olderThan20200210ServiceVersion")
    @Test
    public void uploadRange4TB() {
        long fileSize = 4 * Constants.TB;
        primaryFileClient.create(fileSize);

        Response<ShareFileUploadInfo> uploadResponse = primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(fileSize - DATA.getDefaultDataSizeLong()), null, null); /* Upload to end of file. */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ShareFileDownloadResponse downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(fileSize - DATA.getDefaultDataSizeLong(), fileSize), true, null, null)


        assertResponseStatusCode(uploadResponse, 201);
        assertResponseStatusCode(downloadResponse, 206);
        assertEquals(downloadResponse.getDeserializedHeaders().getContentLength(), DATA.getDefaultDataSizeLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {
        4 * Constants.MB, // max put range
        5 * Constants.MB})
    public void uploadBufferedRangeGreaterThanMaxPutRange(long length) {
        primaryFileClient.create(length);
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomBuffer((int) length));

        assertDoesNotThrow(() -> primaryFileClient.upload(data, length, null));

    }

    @DisabledIf("olderThan20221102ServiceVersion")
    @Test
    public void uploadRangeTrailingDot() {
        primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(DATA.getDefaultDataSizeLong());

        ShareFileUploadRangeOptions options = new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong())
        def uploadResponse = primaryFileClient.uploadRangeWithResponse(options, null, null)
        def downloadResponse = primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(), null, null, null)


        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 200)
        downloadResponse.getDeserializedHeaders().getContentLength() == DATA.getDefaultDataSizeLong()
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void uploadRange() oAuth() {
        given:
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        def fileName = generatePathName()
        def fileClient = dirClient.getFileClient(fileName)

        fileClient.create(DATA.getDefaultDataSizeLong())


        def uploadResponse = fileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null)
        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        def downloadResponse = fileClient.downloadWithResponse(stream, null, null, null, null)
        def headers = downloadResponse.getDeserializedHeaders()


        assertResponseStatusCode(uploadResponse, 201);
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
    public void bufferedUpload() various partitions() {
        given:
        primaryFileClient.create(length)
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomBuffer(length))


        primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength))


        notThrown(Exception)

        where:
        length            | uploadChunkLength
        1024              | null
        1024              | 1024
        1024              | 256
        4 * Constants.MB  | null
        4 * Constants.MB  | 1024
        20 * Constants.MB | null
        20 * Constants.MB | 4 * Constants.MB
    }

    @ParameterizedTest
    public void bufferedUpload() error partition too big() {
        given:
        primaryFileClient.create(length)
        ByteArrayInputStream data = new ByteArrayInputStream(getRandomBuffer(length))


        primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength))


        thrown(Exception)

        where:
        length            | uploadChunkLength
        20 * Constants.MB | 20 * Constants.MB
    }

    @Test
    public void uploadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(),
                DATA.getDefaultDataSizeLong()).setOffset(1L), null, null));

        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void parallelUploadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
        primaryFileClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), null));
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void uploadRangeDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()));
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    public void uploadDataRetryOnTransientFailure() {
        ShareFileClient clientWithFailure = getFileClient(
            ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryFileClient.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        );

        primaryFileClient.create(1024);

        clientWithFailure.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        primaryFileClient.downloadWithResponse(os, new ShareFileRange(0, DATA.getDefaultDataSizeLong() - 1), null, null, null);
        assertArrayEquals(os.toByteArray(), DATA.getDefaultBytes());
    }

    @Test
    public void uploadAndClearRange() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
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
        InputStream fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());


        primaryFileClient.clearRangeWithResponse(7, 1, null, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, 7L), false, null, null);

        for (byte b : stream.toByteArray()) {
            assertEquals(0, b);
        }
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    @Test
    public void clearRangeTrailingDot() {
        ShareFileClient primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        primaryFileClient.create(DATA.getDefaultDataSizeLong());
        assertResponseStatusCode(primaryFileClient.clearRangeWithResponse(DATA.getDefaultDataSizeLong(), 0, null, null), 201);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
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
        InputStream fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
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
        InputStream fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());


        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.clearRange(30));
        assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE);
    }

    @Test
    public void clearRangeErrorArgs() {
        String fullInfoString = "please clear the range";
        InputStream fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8));
        primaryFileClient.create(fullInfoString.length());
        primaryFileClient.uploadRange(fullInfoData, fullInfoString.length());

        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.clearRangeWithResponse(7, 20, null, null));

        assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE);
    }

    @ParameterizedTest
    public void uploadData() length mismatch() {
        given:
        primaryFileClient.create(1024)


        primaryFileClient.uploadWithResponse(DATA.getDefaultInputStream(), size, 0, null, Context.NONE)


        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    @ParameterizedTest
    public void parallelUpload() data length mismatch() {
        given:
        primaryFileClient.create(1024)


        primaryFileClient.upload(DATA.getDefaultInputStream(), size, null)


        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    @ParameterizedTest
    public void uploadRange() length mismatch() {
        given:
        primaryFileClient.create(1024)


        primaryFileClient.uploadRange(DATA.getDefaultInputStream(), size)


        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    @Test
    public void downloadDataError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileRange(0, 1023L),
                false, null, null));
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    public void uploadFile() does not exist() {
        given:
        File uploadFile = new File(testFolder.getPath() + "/fakefile.txt")

        if (uploadFile.exists()) {
            assert uploadFile.delete()
        }


        primaryFileClient.uploadFromFile(uploadFile.getPath())


        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof NoSuchFileException

            cleanup:
        uploadFile.delete()
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */

    @LiveOnly
    @ParameterizedTest
    public void downloadFile() buffer copy() {

        ShareServiceClientBuilder shareServiceClient = new ShareServiceClientBuilder()
            .connectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
            .buildClient()

        def fileClient = shareServiceClient.getShareClient(shareName)
            .createFile(filePath, fileSize)

        def file = getRandomFile(fileSize)
        fileClient.uploadFromFile(file.toPath().toString())
        File outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }


        fileClient.downloadToFile(outFile.toPath().toString())


        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        shareServiceClient.deleteShare(shareName)
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
    }

    public void uploadAnd() download file exists() {
        given:
        def data = "Download file exists"
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        if (!downloadFile.exists()) {
            assert downloadFile.createNewFile()
        }

        primaryFileClient.create(data.length())
        primaryFileClient.upload(getInputStream(data.getBytes(StandardCharsets.UTF_8)), data.length())


        primaryFileClient.downloadToFile(downloadFile.getPath())


        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

            cleanup:
        downloadFile.delete()
    }

    public void uploadAnd() download to file does not exist() {
        given:
        def data = "Download file does not exist"
        File downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        if (downloadFile.exists()) {
            assert downloadFile.delete()
        }

        primaryFileClient.create(data.length())
        primaryFileClient.upload(getInputStream(data.getBytes(StandardCharsets.UTF_8)), data.length())


        primaryFileClient.downloadToFile(downloadFile.getPath())


        Scanner scanner = new Scanner(downloadFile).useDelimiter("\\Z")
        data == scanner.next()
        scanner.close()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), downloadFile.getName())
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @ParameterizedTest
    public void uploadRange() preserve file last written on() {

        primaryFileClient.create(Constants.KB)
        def initialProps = primaryFileClient.getProperties()


        primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions
            (new ByteArrayInputStream(getRandomBuffer(Constants.KB)), Constants.KB).setLastWrittenMode(mode), null, null)
        def resultProps = primaryFileClient.getProperties()


        if (mode.equals(FileLastWrittenMode.PRESERVE)) {
            assert initialProps.getSmbProperties().getFileLastWriteTime() == resultProps.getSmbProperties()
                .getFileLastWriteTime()
        } else {
            assert initialProps.getSmbProperties().getFileLastWriteTime() != resultProps.getSmbProperties()
                .getFileLastWriteTime()
        }

        where:
        mode                         | _
        FileLastWrittenMode.NOW      | _
        FileLastWrittenMode.PRESERVE | _
    }

    @ParameterizedTest
    public void uploadRange() from URL() {
        given:
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient()
        primaryFileClient.create(1024)
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileClient.upload(getInputStream(data.getBytes()), data.length())
        def credential = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
        ShareServiceSasSignatureValues sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()


        ShareFileClient client = fileBuilderHelper(shareName, "destination" + pathSuffix)
            .endpoint(primaryFileClient.getFileUrl().toString())
            .buildFileClient()

        client.create(1024)
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileClient.getFileUrl() + "?" + sasToken)


        ByteArrayOutputStream stream = new ByteArrayOutputStream()
        client.download(stream)
        String result = new String(stream.toByteArray())

        for (int i = 0; i < length; i++) {
            result.charAt(destinationOffset + i) == data.charAt(sourceOffset + i)
        }
        where:
        pathSuffix || _
        ""         || _
        "ü1ü"      || _ /* Something that needs to be url encoded. */
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @ParameterizedTest
    public void uploadRange() from Url preserve file last written on() {

        primaryFileClient.create(Constants.KB)
        def destinationClient = shareClient.getFileClient(generatePathName())
        destinationClient.create(Constants.KB)
        def initialProps = destinationClient.getProperties()

        primaryFileClient.uploadRange(new ByteArrayInputStream(getRandomBuffer(Constants.KB)), Constants.KB)

        def credential = StorageSharedKeyCredential.fromConnectionString(ENVIRONMENT.getPrimaryAccount().getConnectionString())
        ShareServiceSasSignatureValues sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(testResourceNamer.now().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()


        destinationClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(Constants.KB,
            primaryFileClient.getFileUrl() + "?" + sasToken).setLastWrittenMode(mode), null, null)
        def resultProps = destinationClient.getProperties()


        if (mode.equals(FileLastWrittenMode.PRESERVE)) {
            assert initialProps.getSmbProperties().getFileLastWriteTime() == resultProps.getSmbProperties()
                .getFileLastWriteTime()
        } else {
            assert initialProps.getSmbProperties().getFileLastWriteTime() != resultProps.getSmbProperties()
                .getFileLastWriteTime()
        }

        where:
        mode                         | _
        FileLastWrittenMode.NOW      | _
        FileLastWrittenMode.PRESERVE | _
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void uploadRange() from Url trailing dot() {

        shareClient = getShareClient(shareName, true, true)

        def directoryClient = shareClient.getRootDirectoryClient()
        def sourceClient = directoryClient.getFileClient(generatePathName() + ".")
        sourceClient.create(Constants.KB)

        def destinationClient = directoryClient.getFileClient(generatePathName() + ".")
        destinationClient.create(Constants.KB)

        sourceClient.uploadRange(new ByteArrayInputStream(getRandomBuffer(Constants.KB)), Constants.KB)

        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)

        def expiryTime = testResourceNamer.now().plusDays(1)
        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions)
        def sasToken = shareClient.generateSas(sasValues)


        def res = destinationClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(Constants.KB,
            sourceClient.getFileUrl() + "?" + sasToken), null, null)


        assertResponseStatusCode(res, 201)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void uploadRange() from Url trailing dot fail() {

        shareClient = getShareClient(shareName, true, false)

        def directoryClient = shareClient.getRootDirectoryClient()
        def sourceClient = directoryClient.getFileClient(generatePathName() + ".")
        sourceClient.create(DATA.getDefaultDataSizeLong())

        def destinationClient = directoryClient.getFileClient(generatePathName() + ".")
        destinationClient.create(DATA.getDefaultDataSizeLong())

        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong())


        destinationClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(DATA.getDefaultDataSizeLong(),
            sourceClient.getFileUrl()), null, null)


        // error thrown: CannotVerifyCopySource
        thrown(ShareStorageException)
    }

    public void openInput() stream with range() {

        primaryFileClient.create(1024)
        ShareFileRange shareFileRange = new ShareFileRange(5L, 10L)
        def dataBytes = "long test string".getBytes(StandardCharsets.UTF_8)
        ByteArrayInputStream inputStreamData = new ByteArrayInputStream(dataBytes)


        primaryFileClient.upload(inputStreamData, dataBytes.size(), null)
        def totalBytesRead = 0
        def stream = primaryFileClient.openInputStream(shareFileRange)
        while (stream.read() != -1) {
            totalBytesRead++
        }
        stream.close()

        assert totalBytesRead == 6
    }

    @ParameterizedTest
    public void startCopy()() {
        given:
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient()
        primaryFileClient.create(1024)
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileClient.getFileUrl()


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL,
            null,
            null)

        def pollResponse = poller.poll()


        assert pollResponse.getValue().getCopyId() != null

        where:
        pathSuffix || _
        ""         || _
        "ü1ü"      || _ /* Something that needs to be url encoded. */
    }

    @ParameterizedTest
    public void startCopy() with args() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey)
        }


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, smbProperties,
            setFilePermission ? filePermission : null, permissionType, ignoreReadOnly,
            setArchiveAttribute, null, null, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null

        where:
        setFilePermissionKey | setFilePermission | ignoreReadOnly | setArchiveAttribute | permissionType
        true                 | false             | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | true              | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | false             | true           | false               | PermissionCopyModeType.SOURCE
        false                | false             | false          | true                | PermissionCopyModeType.SOURCE
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void startCopy() trailing dot() {
        given:
        shareClient = getShareClient(shareName, true, true)

        def sourceClient = shareClient.getFileClient(generatePathName() + ".")
        sourceClient.create(1024)

        def destClient = shareClient.getFileClient(generatePathName() + ".")
        destClient.create(1024)

        def data = getRandomBuffer(Constants.KB)
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data)

        sourceClient.uploadRange(inputStream, Constants.KB)

        expect:
        def poller = destClient.beginCopy(sourceClient.getFileUrl(), null, null)
        poller.waitForCompletion()
        poller.poll().getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    @Test
    public void startCopyTrailingDotFail() {
        shareClient = getShareClient(shareName, true, false);
        ShareFileClient sourceClient = shareClient.getFileClient(generatePathName() + ".");
        sourceClient.create(1024);

        ShareFileClient destClient = shareClient.getFileClient(generatePathName() + ".");
        destClient.create(1024);

        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        ShareStorageException e = assertThrows(ShareStorageException.class, () -> destClient.beginCopy(sourceClient.getFileUrl(), null, null));
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    @Disabled("There is a race condition in Poller where it misses the first observed event if there is a gap between the time subscribed and the time we start observing events.")
    @Test
    public void startCopyError() {
        primaryFileClient.create(1024);

        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy("some url", testMetadata, null);
        ShareStorageException e = assertThrows(ShareStorageException.class, poller::waitForCompletion);
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE);
    }

    @ParameterizedTest
    public void startCopyWithOptions() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey)
        }
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(setFilePermission ? filePermission : null)
            .setIgnoreReadOnly(ignoreReadOnly)
            .setArchiveAttribute(setArchiveAttribute)
            .setPermissionCopyModeType(permissionType)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        where:
        setFilePermissionKey | setFilePermission | ignoreReadOnly | setArchiveAttribute | permissionType
        true                 | false             | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | true              | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | false             | true           | false               | PermissionCopyModeType.SOURCE
        false                | false             | false          | true                | PermissionCopyModeType.SOURCE
    }

    public void startCopy() with options IgnoreReadOnly and SetArchive() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setIgnoreReadOnly(true)
            .setArchiveAttribute(true)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    public void startCopy() with options file permission() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE)

        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(filePermission)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()
        def properties = primaryFileClient.getProperties().getSmbProperties()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        compareDatesWithPrecision(properties.getFileCreationTime(), smbProperties.getFileCreationTime())
        compareDatesWithPrecision(properties.getFileLastWriteTime(), smbProperties.getFileLastWriteTime())
        properties.getNtfsFileAttributes() == smbProperties.getNtfsFileAttributes()
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    public void startCopy() with options change time() {
        given:
        def client = primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties.setFileChangeTime(testResourceNamer.now())

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(filePermission)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        compareDatesWithPrecision(smbProperties.getFileChangeTime(), primaryFileClient.getProperties().getSmbProperties().getFileChangeTime())
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    public void startCopy() with options copy smbFileProperties permission key() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def filePermissionKey = shareClient.createPermission(filePermission)
        def ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE)
        // We recreate file properties for each test since we need to store the times for the test with testResourceNamer.now()
        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs)
            .setFilePermissionKey(filePermissionKey)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()
        def properties = primaryFileClient.getProperties().getSmbProperties()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        compareDatesWithPrecision(properties.getFileCreationTime(), smbProperties.getFileCreationTime())
        compareDatesWithPrecision(properties.getFileLastWriteTime(), smbProperties.getFileLastWriteTime())
        properties.getNtfsFileAttributes() == smbProperties.getNtfsFileAttributes()
    }

    public void startCopy() with options lease() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def leaseId = createLeaseClient(primaryFileClient).acquireLease()
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
    }

    public void startCopy() with options invalid lease() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def leaseId = namer.getRandomUuid()
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions)


        primaryFileClient.beginCopy(sourceURL, options, null)


        // exception: LeaseNotPresentWithFileOperation
        thrown(ShareStorageException)
    }

    public void startCopy() with options metadata() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setMetadata(testMetadata)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)
        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    public void startCopy() with options with original smb properties() {
        given:
        primaryFileClient.create(1024)
        def initialProperties = primaryFileClient.getProperties()
        def creationTime = initialProperties.getSmbProperties().getFileCreationTime()
        def lastWrittenTime = initialProperties.getSmbProperties().getFileLastWriteTime()
        def changedTime = initialProperties.getSmbProperties().getFileChangeTime()
        def fileAttributes = initialProperties.getSmbProperties().getNtfsFileAttributes()

        def sourceURL = primaryFileClient.getFileUrl()
        def leaseId = createLeaseClient(primaryFileClient).acquireLease()
        ShareRequestConditions conditions = new ShareRequestConditions().setLeaseId(leaseId)
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(true)
            .setLastWrittenOn(true)
            .setChangedOn(true)
            .setFileAttributes(true)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setDestinationRequestConditions(conditions)
            .setSmbPropertiesToCopy(list)


        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, options, null)

        def pollResponse = poller.poll()
        def resultProperties = primaryFileClient.getProperties()


        pollResponse.getValue().getCopyId() != null
        pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        compareDatesWithPrecision(creationTime, resultProperties.getSmbProperties().getFileCreationTime())
        compareDatesWithPrecision(lastWrittenTime, resultProperties.getSmbProperties().getFileLastWriteTime())
        compareDatesWithPrecision(changedTime, resultProperties.getSmbProperties().getFileChangeTime())
        fileAttributes == resultProperties.getSmbProperties().getNtfsFileAttributes()
    }

    public void startCopy() with options copy source file error() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def ntfs = EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE)
        CopyableFileSmbPropertiesList list = new CopyableFileSmbPropertiesList()
            .setCreatedOn(createdOn)
            .setLastWrittenOn(lastWrittenOn)
            .setChangedOn(changedOn)
            .setFileAttributes(fileAttributes)


        smbProperties
            .setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFileChangeTime(testResourceNamer.now())
            .setNtfsFileAttributes(ntfs)

        ShareFileCopyOptions options = new ShareFileCopyOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(filePermission)
            .setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)
            .setSmbPropertiesToCopy(list)


        primaryFileClient.beginCopy(sourceURL, options, null)


        thrown(IllegalArgumentException)

        where:
        createdOn    | lastWrittenOn | changedOn | fileAttributes
        true         | false         | false     | false
        false        | true          | false     | false
        false        | false         | true      | false
        false        | false         | false     | true
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void startCopy() oAuth() {
        given:
        def oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder()
            .shareTokenIntent(ShareTokenIntent.BACKUP))
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        def sourceClient = dirClient.getFileClient(generatePathName())
        sourceClient.create(DATA.getDefaultDataSizeLong())
        def destClient = dirClient.getFileClient(generatePathName())
        destClient.create(DATA.getDefaultDataSizeLong())

        sourceClient.uploadRange(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong())

        def sourceURL = sourceClient.getFileUrl()


        SyncPoller<ShareFileCopyInfo, Void> poller = sourceClient.beginCopy(sourceURL, null, null)

        def pollResponse = poller.poll()


        pollResponse.getValue().getCopyId() != null
    }

    public void abortCopy()() {
        given:
        def fileSize = Constants.MB
        byte bytes = new byte[fileSize]
        ByteArrayInputStream data = new ByteArrayInputStream(bytes)
        def primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
        primaryFileClient.create(fileSize)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null)

        def sourceURL = primaryFileClient.getFileUrl()

        def dest = fileBuilderHelper(shareName, filePath).buildFileClient()
        dest.create(fileSize)


        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(sourceURL, null, null)

        def pollResponse = poller.poll()

        assert pollResponse != null
        assert pollResponse.getValue() != null
        dest.abortCopy(pollResponse.getValue().getCopyId())


        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        thrown(ShareStorageException)
    }

    public void abortCopy() lease() {
        given:
        def fileSize = Constants.MB
        byte bytes = new byte[fileSize]
        ByteArrayInputStream data = new ByteArrayInputStream(bytes)
        def primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
        primaryFileClient.create(fileSize)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null)

        def sourceURL = primaryFileClient.getFileUrl()

        def dest = fileBuilderHelper(shareName, filePath).buildFileClient()
        dest.create(fileSize)

        // obtain lease
        def leaseId = createLeaseClient(primaryFileClient).acquireLease()
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId)

        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(
            sourceURL, new ShareFileCopyOptions().setDestinationRequestConditions(requestConditions), null)

        def pollResponse = poller.poll()

        assert pollResponse != null
        assert pollResponse.getValue() != null
        dest.abortCopyWithResponse(pollResponse.getValue().getCopyId(), requestConditions, null, null)


        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        thrown(ShareStorageException)
    }

    public void abortCopy() invalid lease() {
        given:
        def fileSize = Constants.MB
        byte bytes = new byte[fileSize]
        ByteArrayInputStream data = new ByteArrayInputStream(bytes)
        def primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
        primaryFileClient.create(fileSize)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null)

        def sourceURL = primaryFileClient.getFileUrl()

        def dest = fileBuilderHelper(shareName, filePath).buildFileClient()
        dest.create(fileSize)

        // create invalid lease
        def leaseId = namer.getRandomUuid()
        ShareRequestConditions requestConditions = new ShareRequestConditions().setLeaseId(leaseId)

        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(
            sourceURL, new ShareFileCopyOptions().setDestinationRequestConditions(requestConditions), null)

        def pollResponse = poller.poll()

        assert pollResponse != null
        assert pollResponse.getValue() != null
        dest.abortCopyWithResponse(pollResponse.getValue().getCopyId(), requestConditions, null, null)


        // exception: LeaseNotPresentWithFileOperation
        thrown(ShareStorageException)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void abortCopy() trailing dot() {
        given:
        ByteArrayInputStream data = new ByteArrayInputStream(new byte[Constants.MB])
        def fileName = generatePathName() + "."
        def primaryFileClient = getFileClient(shareName, fileName, true, null)

        primaryFileClient.create(Constants.MB)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data), null, null)

        def sourceURL = primaryFileClient.getFileUrl()

        def dest = fileBuilderHelper(shareName, fileName).buildFileClient()
        dest.create(Constants.MB)


        SyncPoller<ShareFileCopyInfo, Void> poller = dest.beginCopy(sourceURL, null, null)

        def pollResponse = poller.poll()

        assert pollResponse != null
        assert pollResponse.getValue() != null
        dest.abortCopy(pollResponse.getValue().getCopyId())


        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        thrown(ShareStorageException)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void abortCopy() oAuth() {
        given:
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        def fileName = generatePathName()
        def sourceClient = dirClient.getFileClient(fileName)
        sourceClient.create(DATA.getDefaultDataSizeLong())
        sourceClient.uploadWithResponse(new ShareFileUploadOptions(DATA.getDefaultInputStream()), null, null)

        def sourceURL = sourceClient.getFileUrl()

        def destClient = dirClient.getFileClient(generatePathName())
        destClient.create(DATA.getDefaultDataSizeLong())


        SyncPoller<ShareFileCopyInfo, Void> poller = destClient.beginCopy(sourceURL, null, null)

        def pollResponse = poller.poll()

        assert pollResponse != null
        assert pollResponse.getValue() != null
        destClient.abortCopy(pollResponse.getValue().getCopyId())


        // This exception is intentional. It is difficult to test abortCopy in a deterministic way.
        // Exception thrown: "NoPendingCopyOperation"
        thrown(ShareStorageException)
    }

    public void abortCopy() error() {

        primaryFileClient.abortCopy("randomId")


        // Exception thrown: "InvalidQueryParameterValue"
        thrown(ShareStorageException)
    }

    public void deleteFile()() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)

        expect:
        assertResponseStatusCode(primaryFileClient.deleteWithResponse(null, null), 202)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void deleteFile() trailing dot() {
        given:
        def shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null)

        shareFileClient.create(1024)

        expect:
        assertResponseStatusCode(shareFileClient.deleteWithResponse(null, null), 202)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void deleteFileOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create()
        def fileName = generatePathName()
        def fileClient = dirClient.getFileClient(fileName)
        fileClient.create(Constants.KB)

        expect:
        assertResponseStatusCode(fileClient.deleteWithResponse(null, null), 202)
    }

    @Test
    public void deleteFileError() {
        ShareStorageException e = assertThrows(ShareStorageException.class,
            () -> primaryFileClient.deleteWithResponse(null, null));
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND);
    }

    public void deleteIfExistsFile() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)

        expect:
        assertResponseStatusCode(primaryFileClient.deleteIfExistsWithResponse(null, null, null), 202)
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
        assertResponseStatusCode(response, 404);
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


        assertResponseStatusCode(resp, 200);
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

    @DisabledIf("olderThan20221102ServiceVersion")
    public void getProperties() trailing dot() {
        given:
        def shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null)

        shareFileClient.create(1024)


        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
        def resp = shareFileClient.getPropertiesWithResponse(null, null)


        assertResponseStatusCode(resp, 200);
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

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void getPropertiesOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create();
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());


        def createInfo = fileClient.create(Constants.KB)
        def properties = fileClient.getProperties()


        createInfo.getETag() == properties.getETag()
        createInfo.getLastModified() == properties.getLastModified()
        createInfo.getSmbProperties().getFilePermissionKey() == properties.getSmbProperties().getFilePermissionKey()
        createInfo.getSmbProperties().getNtfsFileAttributes() == properties.getSmbProperties().getNtfsFileAttributes()
        createInfo.getSmbProperties().getFileLastWriteTime() == properties.getSmbProperties().getFileLastWriteTime()
        createInfo.getSmbProperties().getFileCreationTime() == properties.getSmbProperties().getFileCreationTime()
        createInfo.getSmbProperties().getFileChangeTime() == properties.getSmbProperties().getFileChangeTime()
        createInfo.getSmbProperties().getParentId() == properties.getSmbProperties().getParentId()
        createInfo.getSmbProperties().getFileId() == properties.getSmbProperties().getFileId()
    }

    public void getProperties() error() {

        primaryFileClient.getProperties()


        def ex = thrown(ShareStorageException)
        ex.getMessage().contains("ResourceNotFound")
    }

    public void setHttpHeaders() fpk() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)
        def filePermissionKey = shareClient.createPermission(filePermission)

        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())
            .setFilePermissionKey(filePermissionKey)

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null, null, null)

        assertResponseStatusCode(resp, 200);
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

    public void setHttpHeaders() fp() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)

        smbProperties.setFileCreationTime(testResourceNamer.now())
            .setFileLastWriteTime(testResourceNamer.now())

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission, null, null)

        assertResponseStatusCode(resp, 200);
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

    @DisabledIf("olderThan20210608ServiceVersion")
    public void setHttpHeaders() change time() {

        primaryFileClient.create(512)
        def changeTime = testResourceNamer.now()


        primaryFileClient.setProperties(512, null, new FileSmbProperties().setFileChangeTime(changeTime), null)


        compareDatesWithPrecision(primaryFileClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void setHttpHeaders() trailing dot() {

        def shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null)

        shareFileClient.create(1024)
        def changeTime = testResourceNamer.now()


        shareFileClient.setProperties(512, null, new FileSmbProperties().setFileChangeTime(changeTime), null)


        compareDatesWithPrecision(shareFileClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void setHttpHeadersOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        String dirName = generatePathName();
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(dirName);
        dirClient.create()
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB)
        httpHeaders = new ShareFileHttpHeaders()
            .setContentType("application/octet-stream")
            .setContentDisposition("attachment")
            .setCacheControl("no-transform")
            .setContentEncoding("gzip")
            .setContentLanguage("en")

        def res = fileClient.setPropertiesWithResponse(Constants.KB, httpHeaders, null, null, null, null)
        def properties = fileClient.getProperties()

        expect:
        assertResponseStatusCode(res, 200)
        res.getValue().getETag() == res.getHeaders().getValue(HttpHeaderName.ETAG)
        properties.getContentType() == "application/octet-stream"
        properties.getContentDisposition() == "attachment"
        properties.getCacheControl() == "no-transform"
        properties.getContentEncoding() == "gzip"
        assertNotNull(properties.getContentMd5());
    }

    @Test
    public void setHttpHeadersError() {
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null);
        ShareStorageException e = assertThrows(ShareStorageException.class, () ->
            primaryFileClient.setPropertiesWithResponse(-1, null, null, null, null, null));

        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT);
    }

    @Test
    public void setMetadata() {
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null, null)
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");


        ShareFileProperties getPropertiesBefore = primaryFileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = primaryFileClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareFileProperties getPropertiesAfter = primaryFileClient.getProperties();

        testMetadata == getPropertiesBefore.getMetadata()
        assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    @Test
    public void setMetadataTrailingDot() {
        ShareFileClient shareFileClient = getFileClient(shareName, generatePathName() + ".", true, null);
        shareFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null, null);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        ShareFileProperties getPropertiesBefore = shareFileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = shareFileClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareFileProperties getPropertiesAfter = shareFileClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void setMetadataOAuth() {
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create();
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.createWithResponse(Constants.KB, null, null, null, testMetadata, null, null);

        ShareFileProperties getPropertiesBefore = fileClient.getProperties();
        Response<ShareFileMetadataInfo> setPropertiesResponse = fileClient.setMetadataWithResponse(updatedMetadata, null, null);
        ShareFileProperties getPropertiesAfter = fileClient.getProperties();

        assertEquals(testMetadata, getPropertiesBefore.getMetadata());
        assertResponseStatusCode(setPropertiesResponse, 200);
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @Test
    public void setMetadataError() {
        primaryFileClient.create(1024);
        Map<String, String> errorMetadata = Collections.singletonMap("", "value");


        ShareStorageException e = assertThrows(ShareStorageException.class, () -> primaryFileClient.setMetadataWithResponse(errorMetadata, null, null));
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY);
    }

    public void listRanges()() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        expect:
        primaryFileClient.listRanges().each {
            assert it.getStart() == 0
            assert it.getEnd() == 1023
        }

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void listRanges() trailing dot() {
        given:
        def primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null)
        primaryFileClient.create(1024)

        def fileName = namer.getRandomName(60) + "."
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        expect:
        primaryFileClient.listRanges().each {
            assert it.getStart() == 0
            assert it.getEnd() == 1023
        }

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    public void listRanges() with range() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        expect:
        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).each {
            assert it.getStart() == 0
            assert it.getEnd() == 511
        }

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    public void listRanges() snapshot() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .snapshot(snapInfo.getSnapshot())
            .buildFileClient()

        expect:
        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).each {
            assert it.getStart() == 0
            assert it.getEnd() == 511
        }

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @Test
    public void listRangesSnapshotFail() {
        String fileName = generateShareName();
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .snapshot("2020-08-07T16:58:02.0000000Z")
            .buildFileClient()


        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).each {
            assert it.getStart() == 0
            assert it.getEnd() == 511
        }


        def e = thrown(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void listRanges() oAuth() {
        given:
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        def fileName = generatePathName()
        def fileClient = dirClient.getFileClient(fileName)
        fileClient.create(Constants.KB)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        fileClient.uploadFromFile(uploadFile)

        expect:
        fileClient.listRanges().each {
            assert it.getStart() == 0
            assert it.getEnd() == 1023
        }

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    @ParameterizedTest
    public void listRanges() diff() {

        primaryFileClient.create(4 * Constants.MB)
        primaryFileClient.upload(new ByteArrayInputStream(getRandomBuffer(4 * Constants.MB)), 4 * Constants.MB)
        def snapshotId = primaryFileServiceClient.getShareClient(primaryFileClient.getShareName())
            .createSnapshot()
            .getSnapshot()

        rangesToUpdate.forEach({
            def size = it.getEnd() - it.getStart() + 1
            primaryFileClient.uploadWithResponse(new ByteArrayInputStream(getRandomBuffer((int) size)), size,
                it.getStart(), null, null)
        })

        rangesToClear.forEach({
            def size = it.getEnd() - it.getStart() + 1
            primaryFileClient.clearRangeWithResponse(size, it.getStart(), null, null)
        })


        def rangeDiff = primaryFileClient.listRangesDiff(snapshotId)


        rangeDiff.getRanges().size() == expectedRanges.size()
        rangeDiff.getClearRanges().size() == expectedClearRanges.size()

        for (def i = 0; i < expectedRanges.size(); i++) {
            def actualRange = rangeDiff.getRanges().get(i)
            def expectedRange = expectedRanges.get(i)
            expectedRange.getStart() == actualRange.getStart()
            expectedRange.getEnd() == actualRange.getEnd()
        }

        for (def i = 0; i < expectedClearRanges.size(); i++) {
            def actualRange = rangeDiff.getClearRanges().get(i)
            def expectedRange = expectedClearRanges.get(i)
            expectedRange.getStart() == actualRange.getStart()
            expectedRange.getEnd() == actualRange.getEnd()
        }

        where:
        rangesToUpdate                       | rangesToClear                           | expectedRanges                       | expectedClearRanges
        createFileRanges()                   | createFileRanges()                      | createFileRanges()                   | createClearRanges()
        createFileRanges(0, 511)             | createFileRanges()                      | createFileRanges(0, 511)             | createClearRanges()
        createFileRanges()                   | createFileRanges(0, 511)                | createFileRanges()                   | createClearRanges(0, 511)
        createFileRanges(0, 511)             | createFileRanges(512, 1023)             | createFileRanges(0, 511)             | createClearRanges(512, 1023)
        createFileRanges(0, 511, 1024, 1535) | createFileRanges(512, 1023, 1536, 2047) | createFileRanges(0, 511, 1024, 1535) | createClearRanges(512, 1023, 1536, 2047)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void listRanges() diff oAuth() {

        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB)
        fileClient.uploadRange(new ByteArrayInputStream(getRandomBuffer(Constants.KB)), Constants.KB)
        def snapshotId = primaryFileServiceClient.getShareClient(fileClient.getShareName())
            .createSnapshot()
            .getSnapshot()

        def rangesToUpdate = createFileRanges()
        def rangesToClear = createFileRanges()
        def expectedRanges = createFileRanges()
        def expectedClearRanges = createFileRanges()


        rangesToUpdate.forEach({
            def size = it.getEnd() - it.getStart() + 1
            fileClient.uploadWithResponse(new ByteArrayInputStream(getRandomBuffer((int) size)), size,
                it.getStart(), null, null)
        })

        rangesToClear.forEach({
            def size = it.getEnd() - it.getStart() + 1
            fileClient.clearRangeWithResponse(size, it.getStart(), null, null)
        })


        def rangeDiff = fileClient.listRangesDiff(snapshotId)


        rangeDiff.getRanges().size() == expectedRanges.size()
        rangeDiff.getClearRanges().size() == expectedClearRanges.size()

        for (def i = 0; i < expectedRanges.size(); i++) {
            def actualRange = rangeDiff.getRanges().get(i)
            def expectedRange = expectedRanges.get(i)
            expectedRange.getStart() == actualRange.getStart()
            expectedRange.getEnd() == actualRange.getEnd()
        }

        for (def i = 0; i < expectedClearRanges.size(); i++) {
            def actualRange = rangeDiff.getClearRanges().get(i)
            def expectedRange = expectedClearRanges.get(i)
            expectedRange.getStart() == actualRange.getStart()
            expectedRange.getEnd() == actualRange.getEnd()
        }
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    public void listRanges() diff with range() {
        given:
        def fileName = generateShareName()
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong())
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), 1024, null, null)


        def range = primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRange(new ShareFileRange(1025, 1026)), null, null).getValue().getRanges().get(0)


        range.getStart() == 1025
        range.getEnd() == 1026

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    public void listRanges() diff lease() {
        given:
        def fileName = generateShareName()
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong())
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), 1024, null, null)
        def leaseId = createLeaseClient(primaryFileClient).acquireLease()


        def range = primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseId)), null, null).getValue().getRanges().get(0)


        range.getStart() == 1024
        range.getEnd() == 1030

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void listRanges() diff trailing dot() {
        given:
        def primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null)

        def fileNameWithDot = generateShareName() + "."
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong())
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileNameWithDot)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        ShareFileUploadRangeOptions options = new ShareFileUploadRangeOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()).setOffset(1024)
        primaryFileClient.uploadRangeWithResponse(options, null, null)


        def range = primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRange(new ShareFileRange(1025, 1026)), null, null).getValue().getRanges().get(0)


        range.getStart() == 1025
        range.getEnd() == 1026

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileNameWithDot)
    }

    public void listRanges() diff lease fail() {
        given:
        def fileName = generateShareName()
        primaryFileClient.create(1024 + DATA.getDefaultDataSizeLong())
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong(), 1024, null, null)


        primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRequestConditions(new ShareRequestConditions().setLeaseId(namer.getRandomUuid())), null, null).getValue().getRanges().get(0)


        thrown(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @Test
    public void listRangesDiffFail() {
        String fileName = generateShareName();
        primaryFileClient.create(1024);
        String uploadFile = createRandomFileWithLength(1024, testFolder, fileName);
        primaryFileClient.uploadFromFile(uploadFile);


        assertThrows(ShareStorageException.class, () ->
            primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions("2020-08-07T16:58:02.0000000Z"),
                null, null).getValue().getRanges().get(0));

        deleteFileIfExists(testFolder.getPath(), fileName);
    }

    public void listHandles()() {
        given:
        primaryFileClient.create(1024)

        expect:
        primaryFileClient.listHandles().size() == 0
    }

    public void listHandles() with maxResult() {
        given:
        primaryFileClient.create(1024)

        expect:
        primaryFileClient.listHandles(2, null, null).size() == 0
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void listHandles() trailing dot() {
        given:
        def primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null)
        primaryFileClient.create(1024)

        expect:
        primaryFileClient.listHandles().size() == 0
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void listHandles() oAuth() {
        given:
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(Constants.KB)

        expect:
        fileClient.listHandles().size() == 0
    }

    @PlaybackOnly
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2023_01_03")
    public void listHandles() access rights() {
        given:
        def shareClient = primaryFileServiceClient.getShareClient("myshare")
        def directoryClient = shareClient.getDirectoryClient("mydirectory")
        def fileClient = directoryClient.getFileClient("myfile")


        def list = fileClient.listHandles().asList()


        list.get(0).getAccessRights()[0] == ShareFileHandleAccessRights.WRITE
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    public void forceClose() handle min() {
        given:
        primaryFileClient.create(512)


        def handlesClosedInfo = primaryFileClient.forceCloseHandle("1")


        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
        notThrown(ShareStorageException)
    }

    @Test
    public void forceCloseHandleInvalidHandleID() {
        primaryFileClient.create(512);
        assertThrows(ShareStorageException.class, () -> primaryFileClient.forceCloseHandle("invalidHandleId"));
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    public void forceClose() handle trailing dot() {
        given:
        def primaryFileClient = getFileClient(shareName, generatePathName() + ".", true, null)
        primaryFileClient.create(512)


        def handlesClosedInfo = primaryFileClient.forceCloseHandle("1")


        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
        notThrown(ShareStorageException)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void forceClose() handle oAuth() {
        given:
        ShareServiceClient oAuthServiceClient = getOAuthServiceClient(new ShareServiceClientBuilder().shareTokenIntent(ShareTokenIntent.BACKUP));
        ShareDirectoryClient dirClient = oAuthServiceClient.getShareClient(shareName).getDirectoryClient(generatePathName());
        dirClient.create()
        ShareFileClient fileClient = dirClient.getFileClient(generatePathName());
        fileClient.create(512)


        def handlesClosedInfo = fileClient.forceCloseHandle("1")


        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
        notThrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    public void forceClose() all handles min() {
        given:
        primaryFileClient.create(512)


        def handlesClosedInfo = primaryFileClient.forceCloseAllHandles(null, null)


        notThrown(ShareStorageException)
        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void renameMin()() {

        primaryFileClient.create(512)


        primaryFileClient.rename(generatePathName())


        notThrown(ShareStorageException)
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void renameWith() response() {

        primaryFileClient.create(512)


        def resp = primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName()), null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()


        notThrown(ShareStorageException)


        assertThrows(ShareStorageException.class, () -> primaryFileClient.getProperties());
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_02_12")
    public void renameSas() token() {

        ShareFileSasPermission permissions = new ShareFileSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)

        def expiryTime = testResourceNamer.now().plusDays(1)

        ShareServiceSasSignatureValues sasValues = new ShareServiceSasSignatureValues(expiryTime, permissions)

        def sas = shareClient.generateSas(sasValues)
        def client = getFileClient(sas, primaryFileClient.getFileUrl())
        primaryFileClient.create(1024)


        def fileName = generatePathName()
        def destClient = client.rename(fileName)


        notThrown(ShareStorageException)
        destClient.getProperties()
        destClient.getFilePath() == fileName
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void renameDifferent() directory() {

        primaryFileClient.create(512)
        def dc = shareClient.getDirectoryClient(generatePathName())
        dc.create()
        def destinationPath = dc.getFileClient(generatePathName())


        def resultClient = primaryFileClient.rename(destinationPath.getFilePath())


        destinationPath.exists()
        destinationPath.getFilePath() == resultClient.getFilePath()
    }

    @DisabledIf("olderThan20210410ServiceVersion")
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

    @DisabledIf("olderThan20210410ServiceVersion")
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
                .setIgnoreReadOnly(ignoreReadOnly).setReplaceIfExists(true), null, null)
        } catch (ShareStorageException ignored) {
            exception = true;
        }
        assertEquals(exception, !ignoreReadOnly);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameFilePermission() {
        primaryFileClient.create(512);
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";

        ShareFileClient destClient = primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission), null, null).getValue();

        assertNotNull(destClient.getProperties().getSmbProperties().getFilePermissionKey());
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameFilePermissionAndKeySet() {
        primaryFileClient.create(512);
        String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)";
        // permission and key cannot both be set
        assertThrows(ShareStorageException.class, () ->  primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission)
            .setSmbProperties(new FileSmbProperties().setFilePermissionKey("filePermissionkey")), null, null).getValue());
    }

    @DisabledIf("olderThan20210608ServiceVersion")
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

        assertEquals(destProperties.getSmbProperties().getNtfsFileAttributes(), EnumSet.of(NtfsFileAttributes.ARCHIVE, NtfsFileAttributes.READ_ONLY));
        assertNotNull(destProperties.getSmbProperties().getFileCreationTime());
        assertNotNull(destProperties.getSmbProperties().getFileLastWriteTime());
        compareDatesWithPrecision(destProperties.getSmbProperties().getFileChangeTime(), fileChangeTime);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameMetadata() {
        primaryFileClient.create(512);
        Map<String, String> updatedMetadata = Collections.singletonMap("update", "value");

        Response<ShareFileClient> resp = primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setMetadata(updatedMetadata), null, null);

        ShareFileClient renamedClient = resp.getValue();
        ShareFileProperties getPropertiesAfter = renamedClient.getProperties();
        assertEquals(updatedMetadata, getPropertiesAfter.getMetadata());
    }

    @DisabledIf("olderThan20221102ServiceVersion")
    @Test
    public void renameTrailingDot() {
        shareClient = getShareClient(shareName, true, true);

        ShareDirectoryClient rootDirectory = shareClient.getRootDirectoryClient();
        ShareFileClient primaryFileClient = rootDirectory.getFileClient(generatePathName() + ".");
        primaryFileClient.create(1024);

        Response<ShareFileClient> response = primaryFileClient
            .renameWithResponse(new ShareFileRenameOptions(generatePathName() + "."), null, null);

        assertResponseStatusCode(response, 200);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameError() {
        primaryFileClient = shareClient.getFileClient(generatePathName());
        assertThrows(ShareStorageException.class, () -> primaryFileClient.rename(generatePathName()));
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameSourceAC() {
        primaryFileClient.create(512);
        String leaseID = setupFileLeaseCondition(primaryFileClient, RECEIVED_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(leaseID);

        assertResponseStatusCode(primaryFileClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setSourceRequestConditions(src), null, null), 200);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
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

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameDestAC() {
        primaryFileClient.create(512);
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        String leaseID = setupFileLeaseCondition(destFile, RECEIVED_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(leaseID);

        assertResponseStatusCode(primaryFileClient.renameWithResponse(new ShareFileRenameOptions(pathName)
            .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null), 200);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    @Test
    public void renameDestACFail() {
        primaryFileClient.create(512);
        String pathName = generatePathName();
        ShareFileClient destFile = shareClient.getFileClient(pathName);
        destFile.create(512);
        setupFileLeaseCondition(destFile, GARBAGE_LEASE_ID);
        ShareRequestConditions src = new ShareRequestConditions()
            .setLeaseId(GARBAGE_LEASE_ID);

        assertThrows(ShareStorageException.class,
            () -> primaryFileClient.renameWithResponse(new ShareFileRenameOptions(pathName)
                .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null));
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void renameContentType() {
        primaryFileClient.create(512);
        Response<ShareFileClient> resp = primaryFileClient.renameWithResponse(
            new ShareFileRenameOptions(generatePathName()).setContentType("mytype"), null, null);

        ShareFileClient renamedClient = resp.getValue();
        ShareFileProperties props = renamedClient.getProperties();
        assertEquals(props.getContentType(), "mytype");
    }

    @DisabledIf("olderThan20210410ServiceVersion")
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
        Response<ShareFileClient> resp = fileClient.renameWithResponse(new ShareFileRenameOptions(fileRename), null, null);

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

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    public void perCallPolicy() {
        primaryFileClient.create(512);

        ShareFileClient fileClient = fileBuilderHelper(primaryFileClient.getShareName(), primaryFileClient.getFilePath())
            .addPolicy(getPerCallVersionPolicy()).buildFileClient();

        Response<ShareFileProperties> response = fileClient.getPropertiesWithResponse(null, null);
        assertEquals(response.getHeaders().getValue(X_MS_VERSION), "2017-11-09");
    }
}
