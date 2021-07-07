// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.Context
import com.azure.core.util.CoreUtils
import com.azure.core.util.polling.SyncPoller
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy
import com.azure.storage.file.share.models.DownloadRetryOptions
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.PermissionCopyModeType
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileCopyInfo
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareFileRange
import com.azure.storage.file.share.models.ShareFileUploadOptions
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareSnapshotInfo
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareFileDownloadOptions
import com.azure.storage.file.share.options.ShareFileListRangesDiffOptions
import com.azure.storage.file.share.sas.ShareFileSasPermission
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static com.azure.storage.file.share.FileTestHelper.assertExceptionStatusCodeAndMessage
import static com.azure.storage.file.share.FileTestHelper.assertResponseStatusCode
import static com.azure.storage.file.share.FileTestHelper.compareFiles
import static com.azure.storage.file.share.FileTestHelper.createClearRanges
import static com.azure.storage.file.share.FileTestHelper.createFileRanges
import static com.azure.storage.file.share.FileTestHelper.createRandomFileWithLength
import static com.azure.storage.file.share.FileTestHelper.deleteFileIfExists
import static com.azure.storage.file.share.FileTestHelper.getRandomBuffer
import static com.azure.storage.file.share.FileTestHelper.getRandomFile

class FileAPITests extends APISpec {
    ShareFileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath
    static Map<String, String> testMetadata
    static ShareFileHttpHeaders httpHeaders
    FileSmbProperties smbProperties
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = namer.getRandomName(60)
        filePath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL))
    }

    def "Get file URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath)

        when:
        def fileURL = primaryFileClient.getFileUrl()

        then:
        expectURL == fileURL
    }

    def "Get share snapshot URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath)

        when:
        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot()
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot()
        ShareFileClient newFileClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getFileClient(filePath)
        def fileURL = newFileClient.getFileUrl()

        then:
        expectURL == fileURL

        when:
        def snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s/%s?sharesnapshot=%s", accountName, shareName, filePath, shareSnapshotInfo.getSnapshot())
        ShareFileClient client = getFileClient(StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString), snapshotEndpoint)

        then:
        client.getFileUrl() == snapshotEndpoint
    }

    def "Exists"() {
        when:
        primaryFileClient.create(Constants.KB)

        then:
        primaryFileClient.exists()
    }

    def "Does not exist"() {
        expect:
        !primaryFileClient.exists()
    }

    def "Exists error"() {
        setup:
        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .sasToken("sig=dummyToken").buildFileClient()

        when:
        primaryFileClient.exists()

        then:
        def e = thrown(ShareStorageException)
        e.getResponse().getStatusCode() == 403
    }

    def "Create file"() {
        expect:
        assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null, null, null, null), 201)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Create file 4TB"() {
        expect:
        assertResponseStatusCode(primaryFileClient.createWithResponse(4 * Constants.TB, null, null, null, null, null, null), 201)
    }

    def "Create file error"() {
        when:
        primaryFileClient.create(-1)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Create file with args fpk"() {
        when:
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with namer.getUtcNow()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        def resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata, null, null)

        then:
        assertResponseStatusCode(resp, 201)
        resp.getValue().getETag()
        resp.getValue().getLastModified()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create file with args fp"() {
        when:
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        def resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata, null, null)
        then:
        assertResponseStatusCode(resp, 201)
        resp.getValue().getETag()
        resp.getValue().getLastModified()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create file with args error"() {
        when:
        primaryFileClient.createWithResponse(-1, null, null, null, testMetadata, null, null)
        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
    }

    @Unroll
    def "Create file permission and key error"() {
        when:
        FileSmbProperties smbProperties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
        primaryFileClient.createWithResponse(1024, null, smbProperties, permission, null, null, null)
        then:
        thrown(IllegalArgumentException)
        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(getRandomBuffer(9 * Constants.KB))
    }

    def "Upload and download data"() {
        given:
        primaryFileClient.create(data.defaultDataSizeLong)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, null, null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null)
        def headers = downloadResponse.getDeserializedHeaders()

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 200, 206)
        headers.getContentLength() == data.defaultDataSizeLong
        headers.getETag()
        headers.getLastModified()
        headers.getFilePermissionKey()
        headers.getFileAttributes()
        headers.getFileLastWriteTime()
        headers.getFileCreationTime()
        headers.getFileChangeTime()
        headers.getFileParentId()
        headers.getFileId()

        data.defaultBytes == stream.toByteArray()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileClient.create(1024)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, 1, null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, data.defaultDataSizeLong), true, null, null)

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 206)
        downloadResponse.getDeserializedHeaders().getContentLength() == data.defaultDataSizeLong

        data.defaultBytes == stream.toByteArray()
    }

    def "Parallel upload and download data"() {
        given:
        primaryFileClient.create(data.defaultDataSizeLong)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data.defaultInputStream, data.defaultDataSizeLong),
            null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null)
        def headers = downloadResponse.getDeserializedHeaders()

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 200, 206)
        headers.getContentLength() == data.defaultDataSizeLong
        headers.getETag()
        headers.getLastModified()
        headers.getFilePermissionKey()
        headers.getFileAttributes()
        headers.getFileLastWriteTime()
        headers.getFileCreationTime()
        headers.getFileChangeTime()
        headers.getFileParentId()
        headers.getFileId()

        data.defaultBytes == stream.toByteArray()
    }

    def "Parallel upload and download data with args"() {
        given:
        primaryFileClient.create(1024)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(
            new ShareFileUploadOptions(data.defaultInputStream, data.defaultDataSizeLong).setOffset(1), null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, data.defaultDataSizeLong), true, null, null)

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 206)
        downloadResponse.getDeserializedHeaders().getContentLength() == data.defaultDataSizeLong

        data.defaultBytes == stream.toByteArray()
    }

    def "Parallel Upload InputStream no length"() {
        when:
        primaryFileClient.create(data.defaultDataSize)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data.defaultInputStream), null, null)

        then:
        notThrown(Exception)
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        primaryFileClient.download(os)
        os.toByteArray() == data.defaultBytes
    }

    def "Parallel Upload InputStream bad length"() {
        when:
        primaryFileClient.create(data.defaultDataSize)
        primaryFileClient.uploadWithResponse(new ShareFileUploadOptions(data.defaultInputStream, length), null, null)

        then:
        thrown(Exception)

        where:
        _ | length
        _ | 0
        _ | -100
        _ | data.defaultDataSize - 1
        _ | data.defaultDataSize + 1
    }

    def "Upload successful retry"() {
        given:
        primaryFileClient.create(data.defaultDataSize)
        def clientWithFailure = getFileClient(
            env.primaryAccount.credential,
            primaryFileClient.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy())

        when:
        clientWithFailure.uploadWithResponse(new ShareFileUploadOptions(data.defaultInputStream), null, null)

        then:
        notThrown(Exception)
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        primaryFileClient.download(os)
        os.toByteArray() == data.defaultBytes
    }

    def "Upload range and download data"() {
        given:
        primaryFileClient.create(data.defaultDataSizeLong)

        when:
        def uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(data.defaultInputStream, data.defaultDataSizeLong), null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null)
        def headers = downloadResponse.getDeserializedHeaders()

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 200, 206)
        headers.getContentLength() == data.defaultDataSizeLong
        headers.getETag()
        headers.getLastModified()
        headers.getFilePermissionKey()
        headers.getFileAttributes()
        headers.getFileLastWriteTime()
        headers.getFileCreationTime()
        headers.getFileChangeTime()
        headers.getFileParentId()
        headers.getFileId()

        data.defaultBytes == stream.toByteArray()
    }

    def "Upload range and download data with args"() {
        given:
        primaryFileClient.create(1024)

        when:
        def uploadResponse = primaryFileClient.uploadRangeWithResponse(
            new ShareFileUploadRangeOptions(data.defaultInputStream, data.defaultDataSizeLong).setOffset(1), null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, data.defaultDataSizeLong), true, null, null)

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 206)
        downloadResponse.getDeserializedHeaders().getContentLength() == (long) data.defaultDataSizeLong

        data.defaultBytes == stream.toByteArray()
    }

    def "Download all null"() {
        given:
        primaryFileClient.create(data.defaultDataSizeLong)
        primaryFileClient.upload(data.defaultInputStream, data.defaultDataSizeLong)

        when:
        def stream = new ByteArrayOutputStream()
        def response = primaryFileClient.downloadWithResponse(stream, null, null, null)
        def body = stream.toByteArray()
        def headers = response.getDeserializedHeaders()

        then:
        body == data.defaultBytes
        CoreUtils.isNullOrEmpty(headers.getMetadata())
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() != null
        headers.getContentMd5() == null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
    }

    def "Download empty file"() {
        setup:
        primaryFileClient.create(1)

        when:
        def outStream = new ByteArrayOutputStream()
        primaryFileClient.download(outStream)
        def result = outStream.toByteArray()

        then:
        notThrown(ShareStorageException)
        result.length == 1
        !result[0]
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in FileClient.download().
         */
        setup:
        primaryFileClient.create(data.defaultDataSizeLong)
        primaryFileClient.upload(data.defaultInputStream, data.defaultDataSizeLong)
        def fc2 = getFileClient(env.primaryAccount.credential, primaryFileClient.getFileUrl(), new MockRetryRangeResponsePolicy("bytes=2-6"))

        when:
        def range = new ShareFileRange(2, 6L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
        fc2.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileDownloadOptions().setRange(range).setRetryOptions(options), null, null)

        then:
        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "Download retry default"() {
        setup:
        primaryFileClient.create(data.defaultDataSizeLong)
        primaryFileClient.upload(data.defaultInputStream, data.defaultDataSizeLong)
        def failureClient = getFileClient(env.primaryAccount.credential, primaryFileClient.getFileUrl(), new MockFailureResponsePolicy(5))

        when:
        def outStream = new ByteArrayOutputStream()
        failureClient.download(outStream)
        String bodyStr = outStream.toString()

        then:
        bodyStr == data.defaultText
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "Upload Range 4TB"() {
        given:
        def fileSize = 4 * Constants.TB
        primaryFileClient.create(fileSize)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, fileSize - data.defaultDataSizeLong, null, null) /* Upload to end of file. */
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, new ShareFileRange(fileSize - data.defaultDataSizeLong, fileSize), true, null, null)

        then:
        assertResponseStatusCode(uploadResponse, 201)
        assertResponseStatusCode(downloadResponse, 206)
        downloadResponse.getDeserializedHeaders().getContentLength() == data.defaultDataSizeLong
    }

    @Unroll
    def "Upload buffered range greater than max put range"() {
        given:
        primaryFileClient.create(length)
        def data = new ByteArrayInputStream(getRandomBuffer(length));

        when:
        primaryFileClient.upload(data, length, null)

        then:
        notThrown(Exception)

        where:
        _ || length
        _ || 4 * Constants.MB // max put range
        _ || 5 * Constants.MB

    }

    @Unroll
    def "Buffered upload various partitions"() {
        given:
        primaryFileClient.create(length)
        def data = new ByteArrayInputStream(getRandomBuffer(length))

        when:
        primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength))

        then:
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

    @Unroll
    def "Buffered upload error partition too big"() {
        given:
        primaryFileClient.create(length)
        def data = new ByteArrayInputStream(getRandomBuffer(length))

        when:
        primaryFileClient.upload(data, length, new ParallelTransferOptions()
            .setBlockSizeLong(uploadChunkLength).setMaxSingleUploadSizeLong(uploadChunkLength))

        then:
        thrown(Exception)

        where:
        length            | uploadChunkLength
        20 * Constants.MB | 20 * Constants.MB
    }

    def "Upload data error"() {
        when:
        primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, 1, null, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Parallel upload data error"() {
        when:
        primaryFileClient.upload(data.defaultInputStream, data.defaultDataSizeLong, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Upload range data error"() {
        when:
        primaryFileClient.uploadRange(data.defaultInputStream, data.defaultDataSizeLong)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Upload data retry on transient failure"() {
        setup:
        def clientWithFailure = getFileClient(
            env.primaryAccount.credential,
            primaryFileClient.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )

        primaryFileClient.create(1024)

        when:
        clientWithFailure.upload(data.defaultInputStream, data.defaultDataSizeLong)

        then:
        def os = new ByteArrayOutputStream()
        primaryFileClient.downloadWithResponse(os, new ShareFileRange(0, data.defaultDataSizeLong - 1), null, null, null)
        os.toByteArray() == data.defaultBytes
    }

    def "Upload and clear range"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRange(7)
        def stream = new ByteArrayOutputStream()
        primaryFileClient.downloadWithResponse(stream, new ShareFileRange(0, 6), false, null, null)

        then:
        for (def b : stream.toByteArray()) {
            b == 0
        }
    }

    def "Upload and clear range with args"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRangeWithResponse(7, 1, null, null)
        def stream = new ByteArrayOutputStream()
        primaryFileClient.downloadWithResponse(stream, new ShareFileRange(1, 7), false, null, null)

        then:
        for (def b : stream.toByteArray()) {
            b == 0
        }
    }

    def "Clear range error"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRange(30)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE)
    }

    def "Clear range error args"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRangeWithResponse(7, 20, null, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 416, ShareErrorCode.INVALID_RANGE)
    }

    @Unroll
    def "Upload data length mismatch"() {
        given:
        primaryFileClient.create(1024)

        when:
        primaryFileClient.uploadWithResponse(data.defaultInputStream, size, 0, null, Context.NONE)

        then:
        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    @Unroll
    def "Parallel upload data length mismatch"() {
        given:
        primaryFileClient.create(1024)

        when:
        primaryFileClient.upload(data.defaultInputStream, size, null)

        then:
        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    @Unroll
    def "Upload range length mismatch"() {
        given:
        primaryFileClient.create(1024)

        when:
        primaryFileClient.uploadRange(data.defaultInputStream, size)

        then:
        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6    | "more than"
        8    | "less than"
    }

    def "Download data error"() {
        when:
        primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(), new ShareFileRange(0, 1023), false, null, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Upload file does not exist"() {
        given:
        def uploadFile = new File(testFolder.getPath() + "/fakefile.txt")

        if (uploadFile.exists()) {
            assert uploadFile.delete()
        }

        when:
        primaryFileClient.uploadFromFile(uploadFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof NoSuchFileException

        cleanup:
        uploadFile.delete()
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */

    @LiveOnly
    @Unroll
    def "Download file buffer copy"() {
        setup:
        def shareServiceClient = new ShareServiceClientBuilder()
            .connectionString(env.primaryAccount.connectionString)
            .buildClient()

        def fileClient = shareServiceClient.getShareClient(shareName)
            .createFile(filePath, fileSize)

        def file = getRandomFile(fileSize)
        fileClient.uploadFromFile(file.toPath().toString())
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        fileClient.downloadToFile(outFile.toPath().toString())

        then:
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

    def "Upload and download file exists"() {
        given:
        def data = "Download file exists"
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        if (!downloadFile.exists()) {
            assert downloadFile.createNewFile()
        }

        primaryFileClient.create(data.length())
        primaryFileClient.upload(getInputStream(data.getBytes(StandardCharsets.UTF_8)), data.length())

        when:
        primaryFileClient.downloadToFile(downloadFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        downloadFile.delete()
    }

    def "Upload and download to file does not exist"() {
        given:
        def data = "Download file does not exist"
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        if (downloadFile.exists()) {
            assert downloadFile.delete()
        }

        primaryFileClient.create(data.length())
        primaryFileClient.upload(getInputStream(data.getBytes(StandardCharsets.UTF_8)), data.length())

        when:
        primaryFileClient.downloadToFile(downloadFile.getPath())

        then:
        def scanner = new Scanner(downloadFile).useDelimiter("\\Z")
        data == scanner.next()
        scanner.close()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), downloadFile.getName())
    }

    @Unroll
    def "Upload range from URL"() {
        given:
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient()
        primaryFileClient.create(1024)
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileClient.upload(getInputStream(data.getBytes()), data.length())
        def credential = StorageSharedKeyCredential.fromConnectionString(env.primaryAccount.connectionString)
        def sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(namer.getUtcNow().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        when:
        ShareFileClient client = fileBuilderHelper(shareName, "destination" + pathSuffix)
            .endpoint(primaryFileClient.getFileUrl().toString())
            .buildFileClient()

        client.create(1024)
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileClient.getFileUrl() + "?" + sasToken)

        then:
        def stream = new ByteArrayOutputStream()
        client.download(stream)
        def result = new String(stream.toByteArray())

        for (int i = 0; i < length; i++) {
            result.charAt(destinationOffset + i) == data.charAt(sourceOffset + i)
        }
        where:
        pathSuffix || _
        ""         || _
        "ü1ü"      || _ /* Something that needs to be url encoded. */
    }

    @Unroll
    def "Start copy"() {
        given:
        primaryFileClient = fileBuilderHelper(shareName, filePath + pathSuffix).buildFileClient()
        primaryFileClient.create(1024)
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileClient.getFileUrl()

        when:
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL,
            null,
            null)

        def pollResponse = poller.poll()

        then:
        assert pollResponse.getValue().getCopyId() != null

        where:
        pathSuffix || _
        ""         || _
        "ü1ü"      || _ /* Something that needs to be url encoded. */
    }

    @Unroll
    def "Start copy with args"() {
        given:
        primaryFileClient.create(1024)
        def sourceURL = primaryFileClient.getFileUrl()
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with namer.getUtcNow()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey)
        }

        when:
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy(sourceURL, smbProperties,
            setFilePermission ? filePermission : null, permissionType, ignoreReadOnly,
            setArchiveAttribute, null, null, null)

        def pollResponse = poller.poll()

        then:
        pollResponse.getValue().getCopyId() != null

        where:
        setFilePermissionKey | setFilePermission | ignoreReadOnly | setArchiveAttribute | permissionType
        true                 | false             | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | true              | false          | false               | PermissionCopyModeType.OVERRIDE
        false                | false             | true           | false               | PermissionCopyModeType.SOURCE
        false                | false             | false          | true                | PermissionCopyModeType.SOURCE
    }

    @Ignore("There is a race condition in Poller where it misses the first observed event if there is a gap between the time subscribed and the time we start observing events.")
    def "Start copy error"() {
        given:
        primaryFileClient.create(1024)

        when:
        SyncPoller<ShareFileCopyInfo, Void> poller = primaryFileClient.beginCopy("some url",
            testMetadata,
            null)
        poller.waitForCompletion()

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE)
    }

    @Ignore
    def "Abort copy"() {
        //TODO: Need to find a way of mocking pending copy status
    }

    def "Delete file"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)

        expect:
        assertResponseStatusCode(primaryFileClient.deleteWithResponse(null, null), 202)
    }

    def "Delete file error"() {
        when:
        primaryFileClient.deleteWithResponse(null, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryFileClient.create(1024)

        when:
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        def resp = primaryFileClient.getPropertiesWithResponse(null, null)

        then:
        assertResponseStatusCode(resp, 200)
        resp.getValue().getETag()
        resp.getValue().getLastModified()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Get properties error"() {
        when:
        primaryFileClient.getProperties()

        then:
        def ex = thrown(ShareStorageException)
        ex.getMessage().contains("ResourceNotFound")
    }

    def "Set httpHeaders fpk"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)
        def filePermissionKey = shareClient.createPermission(filePermission)
        when:
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null, null, null)
        then:
        assertResponseStatusCode(resp, 200)
        resp.getValue().getETag()
        resp.getValue().getLastModified()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Set httpHeaders fp"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)
        when:
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission, null, null)
        then:
        assertResponseStatusCode(resp, 200)
        resp.getValue().getETag()
        resp.getValue().getLastModified()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Set httpHeaders error"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)
        when:
        primaryFileClient.setPropertiesWithResponse(-1, null, null, null, null, null)
        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Set metadata"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null, null)
        def updatedMetadata = Collections.singletonMap("update", "value")

        when:
        def getPropertiesBefore = primaryFileClient.getProperties()
        def setPropertiesResponse = primaryFileClient.setMetadataWithResponse(updatedMetadata, null, null)
        def getPropertiesAfter = primaryFileClient.getProperties()

        then:
        testMetadata == getPropertiesBefore.getMetadata()
        assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    def "Set metadata error"() {
        given:
        primaryFileClient.create(1024)
        def errorMetadata = Collections.singletonMap("", "value")

        when:
        primaryFileClient.setMetadataWithResponse(errorMetadata, null, null)

        then:
        def e = thrown(ShareStorageException)
        assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "List ranges"() {
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

    def "List ranges with range"() {
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

    def "List ranges snapshot"() {
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

    def "List ranges snapshot fail"() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        primaryFileClient = fileBuilderHelper(shareName, filePath)
            .snapshot("2020-08-07T16:58:02.0000000Z")
            .buildFileClient()

        when:
        primaryFileClient.listRanges(new ShareFileRange(0, 511L), null, null).each {
            assert it.getStart() == 0
            assert it.getEnd() == 511
        }

        then:
        def e = thrown(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "List ranges diff"() {
        setup:
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

        when:
        def rangeDiff = primaryFileClient.listRangesDiff(snapshotId)

        then:
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "List ranges diff with range"() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024 + data.defaultDataSizeLong)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, 1024, null, null)

        when:
        def range = primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRange(new ShareFileRange(1025, 1026)), null, null).getValue().getRanges().get(0)

        then:
        range.getStart() == 1025
        range.getEnd() == 1026

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    def "List ranges diff lease"() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024 + data.defaultDataSizeLong)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, 1024, null, null)
        def leaseId = createLeaseClient(primaryFileClient).acquireLease()

        when:
        def range = primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRequestConditions(new ShareRequestConditions().setLeaseId(leaseId)), null, null).getValue().getRanges().get(0)

        then:
        range.getStart() == 1024
        range.getEnd() == 1030

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List ranges diff lease fail"() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024 + data.defaultDataSizeLong)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        def snapInfo = shareClient.createSnapshot()

        primaryFileClient.uploadWithResponse(data.defaultInputStream, data.defaultDataSizeLong, 1024, null, null)

        when:
        primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions(snapInfo.getSnapshot()).setRequestConditions(new ShareRequestConditions().setLeaseId(namer.getRandomUuid())), null, null).getValue().getRanges().get(0)

        then:
        thrown(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List ranges diff fail"() {
        given:
        def fileName = namer.getRandomName(60)
        primaryFileClient.create(1024)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        when:
        primaryFileClient.listRangesDiffWithResponse(new ShareFileListRangesDiffOptions("2020-08-07T16:58:02.0000000Z"), null, null).getValue().getRanges().get(0)

        then:
        thrown(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List handles"() {
        given:
        primaryFileClient.create(1024)

        expect:
        primaryFileClient.listHandles().size() == 0
    }

    def "List handles with maxResult"() {
        given:
        primaryFileClient.create(1024)

        expect:
        primaryFileClient.listHandles(2, null, null).size() == 0
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close handle min"() {
        given:
        primaryFileClient.create(512)

        when:
        def handlesClosedInfo = primaryFileClient.forceCloseHandle("1")

        then:
        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
        notThrown(ShareStorageException)
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryFileClient.create(512)

        when:
        primaryFileClient.forceCloseHandle("invalidHandleId")

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close all handles min"() {
        given:
        primaryFileClient.create(512)

        when:
        def handlesClosedInfo = primaryFileClient.forceCloseAllHandles(null, null)

        then:
        notThrown(ShareStorageException)
        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = fileBuilderHelper(shareName, filePath).snapshot(snapshot).buildFileClient()

        then:
        snapshot == shareSnapshotClient.getShareSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryFileClient.getShareName()
    }

    def "Get File Path"() {
        expect:
        filePath == primaryFileClient.getFilePath()
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        given:
        primaryFileClient.create(512)

        def fileClient = fileBuilderHelper(primaryFileClient.getShareName(), primaryFileClient.getFilePath())
            .addPolicy(getPerCallVersionPolicy()).buildFileClient()

        when:
        def response = fileClient.getPropertiesWithResponse(null, null)

        then:
        notThrown(ShareStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }
}
