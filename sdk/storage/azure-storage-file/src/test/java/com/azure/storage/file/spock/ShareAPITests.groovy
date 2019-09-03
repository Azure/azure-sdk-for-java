// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.DirectoryClient
import com.azure.storage.file.FileClient
import com.azure.storage.file.FileSmbProperties
import com.azure.storage.file.ShareClient
import com.azure.storage.file.ShareClientBuilder
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileProperties
import com.azure.storage.file.models.NtfsFileAttributes
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageException
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ShareAPITests extends APISpec {
    ShareClient primaryShareClient
    def shareName
    static def testMetadata
    static def smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = primaryFileServiceClient.getShareClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties()
            .ntfsFileAttributes(EnumSet.of(NtfsFileAttributes.NORMAL))
    }

    def "Get share URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accoutName)
        when:
        def shareURL = primaryShareClient.getShareUrl().toString()
        then:
        expectURL.equals(shareURL)
    }

    def "Get root directory client"() {
        given:
        def directoryClient = primaryShareClient.getRootDirectoryClient()
        expect:
        directoryClient instanceof DirectoryClient
    }

    def "Get file client does not create a file"() {
        given:
        def fileClient = primaryShareClient.getFileClient("testFile")
        expect:
        fileClient instanceof FileClient
    }

    def "Create share"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(null, null, null), 201)
    }

    @Unroll
    def "Create share with args"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(metadata, quota, null), 201)
        where:
        metadata     | quota
        null         | null
        null         | 1
        testMetadata | null
        testMetadata | 1
    }

    @Unroll
    def "Create share with invalid args"() {
        when:
        primaryShareClient.createWithResponse(metadata, quota, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)
        where:
        metadata                                       | quota | statusCode | errMessage
        Collections.singletonMap("", "value")          | 1     | 400        | StorageErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("metadata!", "value") | 1     | 400        | StorageErrorCode.INVALID_METADATA
        testMetadata                                   | 6000  | 400        | StorageErrorCode.INVALID_HEADER_VALUE
    }

    def "Create snapshot"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)
        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(null, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.value().snapshot()).buildClient()
        then:
        Objects.equals(createSnapshotResponse.value().snapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot error"() {
        when:
        primaryShareClient.createSnapshot()
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Create snapshot metadata"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)
        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(testMetadata, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.value().snapshot()).buildClient()
        then:
        Objects.equals(createSnapshotResponse.value().snapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot metadata error"() {
        when:
        primaryShareClient.createSnapshotWithResponse(Collections.singletonMap("", "value"), null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "Delete share"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(null), 202)
    }

    def "Delete share error"() {
        when:
        primaryShareClient.delete()
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, 1, null)
        when:
        def getPropertiesResponse = primaryShareClient.getPropertiesWithResponse(null)
        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        testMetadata.equals(getPropertiesResponse.value().metadata())
        getPropertiesResponse.value().quota() == 1L
    }

    def "Get properties error"() {
        when:
        primaryShareClient.getProperties()
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Set quota"() {
        given:
        primaryShareClient.createWithResponse(null, 1, null)
        when:
        def getQuotaBeforeResponse = primaryShareClient.getProperties()
        def setQuotaResponse = primaryShareClient.setQuotaWithResponse(2, null)
        def getQuotaAfterResponse = primaryShareClient.getProperties()
        then:
        getQuotaBeforeResponse.quota() == 1
        FileTestHelper.assertResponseStatusCode(setQuotaResponse, 200)
        getQuotaAfterResponse.quota() == 2
    }

    def "Set quota error"() {
        when:
        primaryShareClient.setQuota(2)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Set metadata"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, null, null)
        def metadataAfterSet = Collections.singletonMap("afterset", "value")
        when:
        def getMetadataBeforeResponse = primaryShareClient.getProperties()
        def setMetadataResponse = primaryShareClient.setMetadataWithResponse(metadataAfterSet, null)
        def getMetadataAfterResponse = primaryShareClient.getProperties()
        then:
        testMetadata.equals(getMetadataBeforeResponse.metadata())
        FileTestHelper.assertResponseStatusCode(setMetadataResponse, 200)
        metadataAfterSet.equals(getMetadataAfterResponse.metadata())
    }

    def "Set metadata error"() {
        when:
        primaryShareClient.setMetadata(testMetadata)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, null, null), 201)
    }

    def "Create directory file permission"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, filePermission, null, null), 201)
    }

    def "Create directory file permission key"() {
        given:
        primaryShareClient.create()
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        // TODO: add file permission key
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null, null), 201)
    }

    def "Create directory invalid name"() {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createDirectory("test/directory")
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.PARENT_NOT_FOUND)
    }

    def "Create directory metadata"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, testMetadata, null), 201)
    }

    def "Create directory metadata error"() {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createDirectoryWithResponse("testdirectory", null, null, Collections.singletonMap("", "value"), null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create file"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null,  null), 201)
    }

    def "Create file file permission"() {
        given:
        primaryShareClient.create()
        FileProperties properties = new FileProperties(null, null, null, null, null, null, null, null, filePermission)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, properties, null), 201)
    }

    def "Create file file permission key"() {
        given:
        primaryShareClient.create()
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        // TODO: add file permission key
        FileProperties properties = new FileProperties(null, null, null, null, null, null, null, smbProperties, null)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, properties, null), 201)
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, null, null)
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryShareClient.create()
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        FileProperties properties = new FileProperties("txt", null, null, null, null, null, testMetadata, smbProperties, filePermission)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, properties, null), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryShareClient.create()

        when:
        FileProperties properties = new FileProperties("txt", null, null, null, fileContentMD5, null, metadata, null, null)
        primaryShareClient.createFileWithResponse(fileName, maxSize, properties, null)

        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | fileContentMD5  | metadata                              | errMsg
        "testfile:" | 1024    | null            | testMetadata                          | StorageErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null            | testMetadata                          | StorageErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new byte[0]     | testMetadata                          | StorageErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null            | Collections.singletonMap("", "value") | StorageErrorCode.EMPTY_METADATA_KEY
    }

    def "Delete directory"() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectoryWithResponse(directoryName, null), 202)
    }

    def "Delete directory error"() {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.deleteDirectory("testdirectory")
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFileWithResponse(fileName, null), 202)
    }

    def "Delete file error"() {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.deleteFile("testdirectory")
        then:
        def e = thrown(StorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = shareBuilderHelper(interceptorManager, shareName).snapshot(snapshot).buildClient()
        then:
        snapshot.equals(shareSnapshotClient.getSnapshotId())
    }

}
