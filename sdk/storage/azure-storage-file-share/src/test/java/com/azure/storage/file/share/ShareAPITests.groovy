// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareSnapshotInfo
import com.azure.storage.file.share.models.ShareStorageException
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ShareAPITests extends APISpec {
    ShareClient primaryShareClient
    String shareName
    static Map<String, String> testMetadata
    static FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = primaryFileServiceClient.getShareClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get share URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s", accountName, shareName)

        when:
        def shareURL = primaryShareClient.getShareUrl()

        then:
        expectURL == shareURL
    }

    def "Get share snapshot URL"() {
        given:
        def accoutName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s", accoutName, shareName)
        primaryShareClient.create()
        when:
        ShareSnapshotInfo shareSnapshotInfo = primaryShareClient.createSnapshot()
        expectURL = expectURL + "?snapshot=" + shareSnapshotInfo.getSnapshot()
        ShareClient newShareClient = shareBuilderHelper(interceptorManager, shareName).snapshot(shareSnapshotInfo.getSnapshot())
                .buildClient()
        def shareURL = newShareClient.getShareUrl()

        then:
        expectURL == shareURL
    }

    def "Get root directory client"() {
        given:
        def directoryClient = primaryShareClient.getRootDirectoryClient()

        expect:
        directoryClient instanceof ShareDirectoryClient
    }

    def "Get file client does not create a file"() {
        given:
        def fileClient = primaryShareClient.getFileClient("testFile")
        expect:
        fileClient instanceof ShareFileClient
    }

    def "Create share"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(null, null, null, null), 201)
    }

    @Unroll
    def "Create share with args"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.createWithResponse(metadata, quota, null, null), 201)

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
        primaryShareClient.createWithResponse(metadata, quota, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)

        where:
        metadata                                       | quota | statusCode | errMessage
        Collections.singletonMap("", "value")          | 1     | 400        | ShareErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("metadata!", "value") | 1     | 400        | ShareErrorCode.INVALID_METADATA
        testMetadata                                   | 6000  | 400        | ShareErrorCode.INVALID_HEADER_VALUE
    }

    def "Create snapshot"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)

        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(null, null, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient()
        then:
        Objects.equals(createSnapshotResponse.getValue().getSnapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot error"() {
        when:
        primaryShareClient.createSnapshot()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create snapshot metadata"() {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName(methodName, 60)

        when:
        def createSnapshotResponse = primaryShareClient.createSnapshotWithResponse(testMetadata, null, null)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.getValue().getSnapshot()).httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient()
        then:
        Objects.equals(createSnapshotResponse.getValue().getSnapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot metadata error"() {
        when:
        primaryShareClient.createSnapshotWithResponse(Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Delete share"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteWithResponse(null, null), 202)
    }

    def "Delete share error"() {
        when:
        primaryShareClient.delete()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Get properties"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, 1, null, null)

        when:
        def getPropertiesResponse = primaryShareClient.getPropertiesWithResponse(null, null)

        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        testMetadata == getPropertiesResponse.getValue().getMetadata()
        getPropertiesResponse.getValue().getQuota() == 1
    }

    def "Get properties error"() {
        when:
        primaryShareClient.getProperties()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Set quota"() {
        given:
        primaryShareClient.createWithResponse(null, 1, null, null)

        when:
        def getQuotaBeforeResponse = primaryShareClient.getProperties()
        def setQuotaResponse = primaryShareClient.setQuotaWithResponse(2, null, null)
        def getQuotaAfterResponse = primaryShareClient.getProperties()

        then:
        getQuotaBeforeResponse.getQuota() == 1
        FileTestHelper.assertResponseStatusCode(setQuotaResponse, 200)
        getQuotaAfterResponse.getQuota() == 2
    }

    def "Set quota error"() {
        when:
        primaryShareClient.setQuota(2)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Set metadata"() {
        given:
        primaryShareClient.createWithResponse(testMetadata, null, null, null)
        def metadataAfterSet = Collections.singletonMap("afterset", "value")

        when:
        def getMetadataBeforeResponse = primaryShareClient.getProperties()
        def setMetadataResponse = primaryShareClient.setMetadataWithResponse(metadataAfterSet, null, null)
        def getMetadataAfterResponse = primaryShareClient.getProperties()

        then:
        testMetadata == getMetadataBeforeResponse.getMetadata()
        FileTestHelper.assertResponseStatusCode(setMetadataResponse, 200)
        metadataAfterSet == getMetadataAfterResponse.getMetadata()
    }

    def "Set metadata error"() {
        when:
        primaryShareClient.setMetadata(testMetadata)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, null, null, null), 201)
    }

    def "Create directory file permission"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, filePermission, null, null, null), 201)
    }

    def "Create directory file permission key"() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", smbProperties, null, null, null, null), 201)
    }

    def "Create directory invalid name"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectory("test/directory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    def "Create directory metadata"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectoryWithResponse("testCreateDirectory", null, null, testMetadata, null, null), 201)
    }

    def "Create directory metadata error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createDirectoryWithResponse("testdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create file"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null,  null, null, null, null, null), 201)
    }

    def "Create file file permission"() {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, null, filePermission, null, null, null), 201)
    }

    def "Create file file permission key"() {
        given:
        primaryShareClient.create()
        def filePermissionKey = primaryShareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, null, null, null, null), 201)
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryShareClient.create()
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFileWithResponse("testCreateFile", 1024, null, smbProperties, filePermission, testMetadata, null, null), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY
    }

    def "Delete directory"() {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectoryWithResponse(directoryName, null, null), 202)
    }

    def "Delete directory error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteDirectory("testdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFileWithResponse(fileName, null, null), 202)
    }

    def "Delete file error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.deleteFile("testdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Create permission"() {
        given:
        primaryShareClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createPermissionWithResponse(filePermission, null), 201)
    }

    def "Create and get permission"() {
        given:
        primaryShareClient.create()
        def permissionKey = primaryShareClient.createPermission(filePermission)

        when:
        def permission = primaryShareClient.getPermission(permissionKey)

        then:
        permission == filePermission
    }

    def "Create permission error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.createPermissionWithResponse("abcde", null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.fromString("FileInvalidPermission"))
    }

    def "Get permission error"() {
        given:
        primaryShareClient.create()

        when:
        primaryShareClient.getPermissionWithResponse("abcde", null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.INVALID_HEADER_VALUE)
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = shareBuilderHelper(interceptorManager, shareName).snapshot(snapshot).buildClient()

        then:
        snapshot == shareSnapshotClient.getSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryShareClient.getShareName()
    }
}
