// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.exception.HttpResponseException
import com.azure.core.http.rest.Response
import com.azure.core.implementation.util.FluxUtil
import com.azure.storage.common.Constants
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.FileClient
import com.azure.storage.file.ShareClient
import com.azure.storage.file.models.FileCopyInfo
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileRange
import com.azure.storage.file.FileSmbProperties
import com.azure.storage.file.models.NtfsFileAttributes
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageException
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAPITests extends APISpec {
    FileClient primaryFileClient
    def shareName
    def filePath
    def defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8))
    def dataLength = defaultData.remaining()
    static def testMetadata
    static def httpHeaders
    static def smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        ShareClient shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new FileHTTPHeaders().fileContentLanguage("en")
            .fileContentType("application/octet-stream")
        smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL))
    }

    def "Get file URL"() {
        given:
        def accountName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accountName)
        when:
        def fileURL = primaryFileClient.getFileUrl().toString()
        then:
        expectURL == fileURL
    }

    def "Create file"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null, null, null), 201)
    }

    def "Create file error"() {
        when:
        primaryFileClient.create(-1)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Create file with args"() {
        when:
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        def resp = primaryFileClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata, null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.value().eTag()
        resp.value().lastModified()
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    // TODO: Add test that tests create with a file permission key once create file permission and get file permission APIs are created

    @Unroll
    def "Create file with args error"() {
        when:
        primaryFileClient.createWithResponse(maxSize, fileHttpHeaders as FileHTTPHeaders, null, null, metadata, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        maxSize | fileHttpHeaders | metadata                                      | statusCode | errMsg
        -1      | httpHeaders     | testMetadata                                  | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        1024    | httpHeaders     | Collections.singletonMap("testMeta", "value") | 403        | StorageErrorCode.AUTHENTICATION_FAILED
    }

    @Unroll
    def "Create file permission and key error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().filePermissionKey(filePermissionKey)
        primaryFileClient.createWithResponse(1024, null, properties, permission, null, null)
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
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)
        defaultData.rewind()
        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, null)
        def downloadResponse = primaryFileClient.downloadWithPropertiesWithResponse(null, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(uploadResponse, 201)
        FileTestHelper.assertResponseStatusCode(downloadResponse, 200)
        downloadResponse.value().contentLength() == dataLength
        downloadResponse.value().eTag()
        downloadResponse.value().lastModified()
        downloadResponse.value().smbProperties()
        downloadResponse.value().smbProperties().filePermissionKey()
        downloadResponse.value().smbProperties().ntfsFileAttributes()
        downloadResponse.value().smbProperties().fileLastWriteTime()
        downloadResponse.value().smbProperties().fileCreationTime()
        downloadResponse.value().smbProperties().fileChangeTime()
        downloadResponse.value().smbProperties().parentId()
        downloadResponse.value().smbProperties().fileId()

        Arrays.equals(dataBytes, FluxUtil.collectBytesInByteBufferStream(downloadResponse.value().body()).block())


        cleanup:
        defaultData.clear()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileClient.create(1024)
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)
        defaultData.rewind()
        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, null)
        def downloadResponse = primaryFileClient.downloadWithPropertiesWithResponse(new FileRange(1, dataLength), true, null)

        then:
        FileTestHelper.assertResponseStatusCode(uploadResponse, 201)
        FileTestHelper.assertResponseStatusCode(downloadResponse, 206)
        downloadResponse.value().contentLength() == dataLength

        Arrays.equals(dataBytes, FluxUtil.collectBytesInByteBufferStream(downloadResponse.value().body()).block())
        cleanup:
        defaultData.clear()
    }

    def "Upload data error"() {
        when:
        primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        cleanup:
        defaultData.clear()
    }

    def "Upload and clear range" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())
        when:
        primaryFileClient.clearRange(7)
        def downloadResponse = primaryFileClient.downloadWithPropertiesWithResponse(new FileRange(0, 6), false, null)
        then:
        def downloadArray = FluxUtil.collectBytesInByteBufferStream(downloadResponse.value().body()).block()
        downloadArray.eachByte {
            assert it == 0
        }
        cleanup:
        fullInfoData.clear()
    }

    def "Upload and clear range with args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())
        when:
        primaryFileClient.clearRangeWithResponse(7, 1, null)
        def downloadResponse = primaryFileClient.downloadWithPropertiesWithResponse(new FileRange(1, 7), false, null)
        then:
        def downloadArray = FluxUtil.collectBytesInByteBufferStream(downloadResponse.value().body()).block()
        downloadArray.eachByte {
            assert it == 0
        }
    }

    def "Clear range error" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())
        when:
        primaryFileClient.clearRange(30)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 416, StorageErrorCode.INVALID_RANGE)
    }

    def "Clear range error args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileClient.create(fullInfoString.length())
        primaryFileClient.upload(fullInfoData, fullInfoString.length())
        when:
        primaryFileClient.clearRangeWithResponse(7, 20, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 416, StorageErrorCode.INVALID_RANGE)
    }

    def "Download data error"() {
        when:
        primaryFileClient.downloadWithPropertiesWithResponse(new FileRange(0, 1023), false, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
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
        primaryFileClient.upload(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)), data.length())

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
        primaryFileClient.upload(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)), data.length())

        when:
        primaryFileClient.downloadToFile(downloadFile.getPath())

        then:
        def scanner = new Scanner(downloadFile).useDelimiter("\\Z")
        data == scanner.next()
        scanner.close()

        cleanup:
        FileTestHelper.deleteFolderIfExists(testFolder.getPath())
    }

    def "Start copy"() {
        given:
        primaryFileClient.create(1024)
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileClient.getFileUrl().toString() + "/" + shareName + "/" + filePath
        when:
        Response<FileCopyInfo> copyInfoResponse = primaryFileClient.startCopyWithResponse(sourceURL, null, null)
        then:
        FileTestHelper.assertResponseStatusCode(copyInfoResponse, 202)
        copyInfoResponse.value().copyId() != null
    }

    def "Start copy error"() {
        given:
        primaryFileClient.create(1024)
        when:
        primaryFileClient.startCopyWithResponse("some url", testMetadata, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_HEADER_VALUE)
    }

    @Ignore
    def "Abort copy"() {
        //TODO: Need to find a way of mocking pending copy status
    }

    def "Delete file"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null, null, null)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.deleteWithResponse(null), 202)
    }

    def "Delete file error"() {
        when:
        primaryFileClient.deleteWithResponse(null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryFileClient.create(1024)
        when:
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        def resp = primaryFileClient.getPropertiesWithResponse(null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.value().eTag()
        resp.value().lastModified()
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()

    }

    def "Get properties error"() {
        when:
        primaryFileClient.getProperties()
        then:
        thrown(HttpResponseException)
    }

    def "Set httpHeaders"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null)
        when:
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        def resp = primaryFileClient.setHttpHeadersWithResponse(512, httpHeaders, smbProperties, filePermission, null)
        then:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.value().eTag()
        resp.value().lastModified()
        resp.value().smbProperties()
        resp.value().smbProperties().filePermissionKey()
        resp.value().smbProperties().ntfsFileAttributes()
        resp.value().smbProperties().fileLastWriteTime()
        resp.value().smbProperties().fileCreationTime()
        resp.value().smbProperties().fileChangeTime()
        resp.value().smbProperties().parentId()
        resp.value().smbProperties().fileId()
    }

    // TODO: Add test that tests set with a file permission key once create file permission and get file permission APIs are created

    def "Set httpHeaders error"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null)
        when:
        primaryFileClient.setHttpHeadersWithResponse(-1, httpHeaders, null, null, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Set metadata"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, null, null, testMetadata, null)
        def updatedMetadata = Collections.singletonMap("update", "value")
        when:
        def getPropertiesBefore = primaryFileClient.getProperties()
        def setPropertiesResponse = primaryFileClient.setMetadataWithResponse(updatedMetadata, null)
        def getPropertiesAfter = primaryFileClient.getProperties()
        then:
        testMetadata.equals(getPropertiesBefore.metadata())
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata.equals(getPropertiesAfter.metadata())
    }

    def "Set metadata error"() {
        given:
        primaryFileClient.create(1024)
        def errorMetadata = Collections.singletonMap("", "value")
        when:
        primaryFileClient.setMetadataWithResponse(errorMetadata, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "List ranges"() {
        given:
        def fileName = testResourceName.randomName("file", 60)
        primaryFileClient.create(1024)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, tmpFolder.toString(), fileName)
        primaryFileClient.uploadFromFile(uploadFile)
        expect:
        primaryFileClient.listRanges().each {
            assert it.start() == 0
            assert it.end() == 1023
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
        primaryFileClient.listRanges(new FileRange(0, 511L)).each {
            assert it.start() == 0
            assert it.end() == 511
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
        primaryFileClient.listHandles(2).size() == 0
    }

    @Ignore
    def "Force close handles"() {
        // TODO: Need to find a way of mocking handles.
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildFileClient()
        then:
        snapshot.equals(shareSnapshotClient.getShareSnapshotId())
    }
}
