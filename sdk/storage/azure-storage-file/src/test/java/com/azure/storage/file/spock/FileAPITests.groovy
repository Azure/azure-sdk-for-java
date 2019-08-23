// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.exception.HttpResponseException
import com.azure.core.http.rest.Response
import com.azure.core.implementation.util.FluxUtil
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.FileClient
import com.azure.storage.file.ShareClient
import com.azure.storage.file.models.FileCopyInfo
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileRange
import com.azure.storage.file.models.FileRangeWriteType
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageErrorException
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAPITests extends APISpec {
    FileClient primaryFileClient
    def shareName
    def filePath
    static def defaultData = ByteBuffer.allocate(8).wrap("default".getBytes(StandardCharsets.UTF_8))
    static def dataLength = defaultData.remaining()
    static def testMetadata
    static def httpHeaders

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        ShareClient shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new FileHTTPHeaders().fileContentLanguage("en")
            .fileContentType("application/octet-stream")
    }

    def "Get file URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accoutName)
        when:
        def fileURL = primaryFileClient.getFileUrl().toString()
        then:
        expectURL.equals(fileURL)
    }

    def "Create file"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(1024, null, null, null), 201)
    }

    def "Create file error"() {
        when:
        primaryFileClient.create(-1)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Create file with args"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.createWithResponse(1024, httpHeaders, testMetadata, null), 201)
    }

    @Unroll
    def "Create file with args error"() {
        when:
        primaryFileClient.createWithResponse(maxSize, fileHttpHeaders, metadata, null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        maxSize | fileHttpHeaders | metadata                                      | statusCode | errMsg
        -1      | httpHeaders     | testMetadata                                  | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        1024    | httpHeaders     | Collections.singletonMap("testMeta", "value") | 403        | StorageErrorCode.AUTHENTICATION_FAILED
    }

    @Ignore
    def "Upload and download data"() {
        given:
        primaryFileClient.create(dataLength)
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, null)
        def downloadResponse = primaryFileClient.downloadWithPropertiesWithResponse(null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(uploadResponse, 201)
        FileTestHelper.assertResponseStatusCode(downloadResponse, 200)
        downloadResponse.value().contentLength() == dataLength

        Arrays.equals(dataBytes, FluxUtil.collectBytesInByteBufferStream(downloadResponse.value().body()).block())
        cleanup:
        defaultData.clear()
    }

    @Ignore
    def "Upload and download data with args"() {
        given:
        primaryFileClient.create(1024)
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)

        when:
        def uploadResponse = primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, FileRangeWriteType.UPDATE, null)
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
        primaryFileClient.uploadWithResponse(defaultData, dataLength, 1, FileRangeWriteType.UPDATE, null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Download data error"() {
        when:
        primaryFileClient.downloadWithPropertiesWithResponse(new FileRange(0, 1023), false, null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    @Ignore
    def "Upload and download file"() {
        given:
        File uploadFile = new File(testFolder.getPath() + "/helloworld")
        File downloadFile = new File(testFolder.getPath() + "/testDownload")

        if (!Files.exists(downloadFile.toPath())) {
            downloadFile.createNewFile()
        }

        primaryFileClient.create(uploadFile.length())
        when:
        primaryFileClient.uploadFromFile(uploadFile.toString())
        primaryFileClient.downloadToFile(downloadFile.toString())
        then:
        FileTestHelper.assertTwoFilesAreSame(uploadFile, downloadFile)
        cleanup:
        FileTestHelper.deleteFolderIfExists(testFolder.toString())

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
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.INVALID_HEADER_VALUE)
    }

    @Ignore
    def "Abort copy"() {
        //TODO: Need to find a way of mocking pending copy status
    }

    def "Delete file"() {
        given:
        primaryFileClient.createWithResponse(1024, null, null, null)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.deleteWithResponse(null), 202)
    }

    def "Delete file error"() {
        when:
        primaryFileClient.deleteWithResponse(null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryFileClient.create(1024)
        when:
        def getPropertiesResponse = primaryFileClient.getPropertiesWithResponse(null)
        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        getPropertiesResponse.value().eTag() != null
        getPropertiesResponse.value().lastModified() != null
    }

    def "Get properties error"() {
        when:
        primaryFileClient.getProperties()
        then:
        thrown(HttpResponseException)
    }

    @Ignore
    def "Set httpHeaders"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, testMetadata, null)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryFileClient.setHttpHeadersWithResponse(512, httpHeaders, null), 200)
    }

    def "Set httpHeaders error"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, testMetadata, null)
        when:
        primaryFileClient.setHttpHeadersWithResponse(-1, httpHeaders, null)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
    }

    def "Set metadata"() {
        given:
        primaryFileClient.createWithResponse(1024, httpHeaders, testMetadata, null)
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
        def e = thrown(StorageErrorException)
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
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildClient()
        then:
        snapshot.equals(shareSnapshotClient.getShareSnapshotId())
    }
}
