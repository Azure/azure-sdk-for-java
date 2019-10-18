// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file

import com.azure.core.exception.HttpResponseException
import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.http.rest.Response
import com.azure.core.util.Context
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.models.*
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAPITests extends APISpec {
    FileClient primaryFileClient
    ShareClient shareClient
    String shareName
    String filePath
    def data = "default".getBytes(StandardCharsets.UTF_8)
    def defaultData = getInputStream(data)
    def dataLength = defaultData.available()
    static Map<String, String> testMetadata
    static FileHttpHeaders httpHeaders
    static FileSmbProperties smbProperties
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new FileHttpHeaders().setFileContentLanguage("en")
            .setFileContentType("application/octet-stream")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get file URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath)

        when:
        def fileURL = primaryFileClient.getFileUrl()

        then:
        expectURL == fileURL
    }

    def "Get share snapshot URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath)

        when:
        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot()
        expectURL = expectURL + "?snapshot=" + shareSnapshotInfo.getSnapshot()
        FileClient newFileClient = shareBuilderHelper(interceptorManager, shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getFileClient(filePath)
        def fileURL = newFileClient.getFileUrl()

        then:
        expectURL == fileURL
    }

    def "Create file"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null, null, null, null), 201)
    }

    def "Create file error"() {
        when:
        primaryFileClient.create(-1)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, FileErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Create file with args fpk"() {
        when:
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with getUTCNow()
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        def resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
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
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
        def resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
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
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, FileErrorCode.OUT_OF_RANGE_INPUT)
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
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Upload and download data"() {
        given:
        primaryFileClient.create(dataLength)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, null, null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, null, null, null, null)
        def headers = downloadResponse.getHeaders()

        then:
        FileTestHelper.assertResponseStatusCode(uploadResponse, 201)
        FileTestHelper.assertResponseStatusCode(downloadResponse, 200)
        Long.parseLong(headers.getValue("Content-Length")) == dataLength
        headers.getValue("ETag")
        headers.getValue("Last-Modified")
        headers.getValue("x-ms-file-permission-key")
        headers.getValue("x-ms-file-attributes")
        headers.getValue("x-ms-file-last-write-time")
        headers.getValue("x-ms-file-creation-time")
        headers.getValue("x-ms-file-change-time")
        headers.getValue("x-ms-file-parent-id")
        headers.getValue("x-ms-file-id")

        data == stream.toByteArray()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileClient.create(1024)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, null, null)
        def stream = new ByteArrayOutputStream()
        def downloadResponse = primaryFileClient.downloadWithResponse(stream, new FileRange(1, dataLength), true, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(uploadResponse, 201)
        FileTestHelper.assertResponseStatusCode(downloadResponse, 206)
        Long.parseLong(downloadResponse.getHeaders().getValue("Content-Length")) == dataLength

        data == stream.toByteArray()
    }

    def "Upload data error"() {
        when:
        primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, FileErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Upload and clear range" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRange(7)
        def stream = new ByteArrayOutputStream()
        primaryFileClient.downloadWithResponse(stream, new FileRange(0, 6), false, null, null)

        then:
        for (def b : stream.toByteArray()) {
            b == 0
        }
    }

    def "Upload and clear range with args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRangeWithResponse(7, 1, null, null)
        def stream = new ByteArrayOutputStream()
        primaryFileClient.downloadWithResponse(stream, new FileRange(1, 7), false, null, null)

        then:
        for (def b : stream.toByteArray()) {
            b == 0
        }
    }

    def "Clear range error" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRange(30)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 416, FileErrorCode.INVALID_RANGE)
    }

    def "Clear range error args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = getInputStream(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())

        when:
        primaryFileClient.clearRangeWithResponse(7, 20, null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 416, FileErrorCode.INVALID_RANGE)
    }

    @Unroll
    def "Upload data length mismatch"() {
        given:
        primaryFileClient.create(1024)

        when:
        primaryFileClient.uploadWithResponse(defaultData, size, 0, null, Context.NONE)

        then:
        def e = thrown(UnexpectedLengthException)
        e.getMessage().contains(errMsg)

        where:
        size | errMsg
        6 | "more than"
        8 | "less than"
    }

    def "Download data error"() {
        when:
        primaryFileClient.downloadWithResponse(new ByteArrayOutputStream(), new FileRange(0, 1023), false, null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, FileErrorCode.RESOURCE_NOT_FOUND)
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
        FileTestHelper.deleteFolderIfExists(testFolder.getPath())
    }

    def "Upload and download file exists"() {
        given:
        def data = "Download file exists"
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), methodName))

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
        FileTestHelper.deleteFolderIfExists(testFolder.getPath())
    }

    def "Upload and download to file does not exist"() {
        given:
        def data = "Download file does not exist"
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), methodName))

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
        FileTestHelper.deleteFolderIfExists(testFolder.getPath())
    }

    def "Upload range from URL"() {
        given:
        primaryFileClient.create(1024)
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileClient.upload(getInputStream(data.getBytes()), data.length())
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasToken = new FileServiceSasSignatureValues()
            .setExpiryTime(getUTCNow().plusDays(1))
            .setPermissions(new FileSasPermission().setReadPermission(true).toString())
            .setShareName(primaryFileClient.getShareName())
            .setFilePath(primaryFileClient.getFilePath())
            .generateSASQueryParameters(credential)
            .encode()

        when:
        FileClient client = fileBuilderHelper(interceptorManager, shareName, "destination")
            .endpoint(primaryFileClient.getFileUrl().toString())
            .buildFileClient()

        client.create(1024)
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileClient.getFileUrl() +"?" + sasToken)

        then:
        def stream = new ByteArrayOutputStream()
        client.download(stream)
        def result = new String(stream.toByteArray())

        for(int i = 0; i < length; i++) {
            result.charAt(destinationOffset + i) == data.charAt(sourceOffset + i)
        }
    }

    def "Start copy"() {
        given:
        primaryFileClient.create(1024)
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileClient.getFileUrl()

        when:
        Response<FileCopyInfo> copyInfoResponse = primaryFileClient.startCopyWithResponse(sourceURL, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(copyInfoResponse, 202)
        copyInfoResponse.getValue().getCopyId() != null
    }

    def "Start copy error"() {
        given:
        primaryFileClient.create(1024)

        when:
        primaryFileClient.startCopyWithResponse("some url", testMetadata, null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, FileErrorCode.INVALID_HEADER_VALUE)
    }

    @Ignore
    def "Abort copy"() {
        //TODO: Need to find a way of mocking pending copy status
    }

    def "Delete file"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.deleteWithResponse(null, null), 202)
    }

    def "Delete file error"() {
        when:
        primaryFileClient.deleteWithResponse(null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, FileErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryFileClient.create(1024)

        when:
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
        def resp = primaryFileClient.getPropertiesWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
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
        thrown(HttpResponseException)
    }

    def "Set httpHeaders fpk"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null, null)
        def filePermissionKey = shareClient.createPermission(filePermission)
        when:
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
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
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())

        def resp = primaryFileClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
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
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, FileErrorCode.OUT_OF_RANGE_INPUT)
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
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    def "Set metadata error"() {
        given:
        primaryFileClient.create(1024)
        def errorMetadata = Collections.singletonMap("", "value")

        when:
        primaryFileClient.setMetadataWithResponse(errorMetadata, null, null)

        then:
        def e = thrown(FileStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, FileErrorCode.EMPTY_METADATA_KEY)
    }

    def "List ranges"() {
        given:
        def fileName = testResourceName.randomName("file", 60)
        primaryFileClient.create(1024)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, tmpFolder.toString(), fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        expect:
        primaryFileClient.listRanges().each {
            assert it.getStart() == 0
            assert it.getEnd() == 1023
        }

        cleanup:
        FileTestHelper.deleteFolderIfExists(tmpFolder.toString())
    }

    def "List ranges with range"() {
        given:
        def fileName = testResourceName.randomName("file", 60)
        primaryFileClient.create(1024)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, tmpFolder.toString(), fileName)
        primaryFileClient.uploadFromFile(uploadFile)

        expect:
        primaryFileClient.listRanges(new FileRange(0, 511L), null, null).each {
            assert it.getStart() == 0
            assert it.getEnd() == 511
        }

        cleanup:
        FileTestHelper.deleteFolderIfExists(tmpFolder.toString())
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

    def "Force close handle min"() {
        given:
        primaryFileClient.create(512)

        when:
        primaryFileClient.forceCloseHandle("1")

        then:
        notThrown(FileStorageException)
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryFileClient.create(512)

        when:
        primaryFileClient.forceCloseHandle("invalidHandleId")

        then:
        thrown(FileStorageException)
    }

    def "Force close all handles min"() {
        given:
        primaryFileClient.create(512)

        when:
        def numberOfHandlesClosed = primaryFileClient.forceCloseAllHandles(null, null)

        then:
        notThrown(FileStorageException)
        numberOfHandlesClosed == 0
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildFileClient()

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
}
