package com.azure.storage.file.spock

import com.azure.core.exception.HttpResponseException
import com.azure.storage.file.ShareClientBuilder
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.StorageErrorCode
import com.azure.storage.file.models.StorageErrorException
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ShareAPITests extends APISpec {
    def primaryShareClient
    def shareName
    static def testMetadata
    
    def setup() {
        shareName = testResourceName.randomName("share", 16)
        primaryFileServiceClient = fileServiceBuilderHelper(interceptorManager).buildClient()
        primaryShareClient = primaryFileServiceClient.getShareClient(shareName)
        testMetadata = Collections.singletonMap("testmetadata", "value")
    }
    
    def "Get root directory client does not create a directory from share client" () {
        given:
        primaryShareClient.create()
        def directoryClient = primaryShareClient.getRootDirectoryClient()
        when:
        def getPropertiesResponse = directoryClient.getProperties()
        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        assertNotNull(getPropertiesResponse.value().eTag())
    }
    
    def "Get file client does not create a file from share client"() {
        given:
        primaryShareClient.create()
        when:
        def fileClient = primaryShareClient.getFileClient("testFile")
        fileClient.getProperties()
        then:
        assertNotNull(fileClient)
        def e = thrown(HttpResponseException)
        assertEquals(e.response().statusCode(), 404)
    }
    
    def "Create share from share client" () {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.create(), 201)
    }
    
    @Unroll
    def "Create share with args from share client" () {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.create(metadata, quota), 201)
        where:
        metadata | quota
        null | null
        null | 1
        testMetadata | null
        testMetadata | 1
    }
    
    @Unroll
    def "Create share with invalid args from share client" () {
        when:
        primaryShareClient.create(metadata, quota)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMessage)
        where:
        metadata | quota | statusCode | errMessage
        Collections.singletonMap("", "value") | 1 | 400 | StorageErrorCode.EMPTY_METADATA_KEY
        Collections.singletonMap("a@B", "value") | 1 | 400 | "Bad Request"
        testMetadata | 6000 | 400 | StorageErrorCode.INVALID_HEADER_VALUE
    }
    
    def "Create snapshot from share client" () {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName("share", 16)
        when:
        def createSnapshotResponse = primaryShareClient.createSnapshot()
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.value().snapshot()).buildClient()
        then:
        assertEquals(createSnapshotResponse.value().snapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot error from share client" () {
        when:
        primaryShareClient.createSnapshot()
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Create snapshot metadata from share client" () {
        given:
        primaryShareClient.create()
        def shareSnapshotName = testResourceName.randomName("share", 16)
        when:
        def createSnapshotResponse = primaryShareClient.createSnapshot(testMetadata)
        def shareSnapshotClient = new ShareClientBuilder().shareName(shareSnapshotName).connectionString(connectionString)
            .snapshot(createSnapshotResponse.value().snapshot()).buildClient()
        then:
        assertEquals(createSnapshotResponse.value().snapshot(),
            shareSnapshotClient.getSnapshotId())
    }

    def "Create snapshot metadata error from share client" () {
        when:
        primaryShareClient.createSnapshot(Collections.singletonMap("", "value"))
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "Delete share from share client" () {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.delete(), 202)
    }

    def "Delete share error from share client" () {
        when:
        primaryShareClient.delete()
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Get properties from share client" () {
        given:
        primaryShareClient.create(testMetadata, 1)
        when:
        def getPropertiesResponse = primaryShareClient.getProperties()
        then:
        FileTestHelper.assertResponseStatusCode(getPropertiesResponse, 200)
        testMetadata.equals(getPropertiesResponse.value().metadata())
        getPropertiesResponse.value().quota() == 1L
    }

    def "Get properties error from share client" () {
        when:
        primaryShareClient.getProperties()
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Set quota from share client" () {
        given:
        primaryShareClient.create(null, 1)
        when:
        def getQuotaBeforeResponse = primaryShareClient.getProperties().value()
        def setQuotaResponse = primaryShareClient.setQuota(2)
        def getQuotaAfterResponse = primaryShareClient.getProperties().value()
        then:
        getQuotaBeforeResponse.quota() == 1
        FileTestHelper.assertResponseStatusCode(setQuotaResponse, 200)
        getQuotaAfterResponse.quota() == 2
    }

    def "Set quota error from share client" () {
        when:
        primaryShareClient.setQuota(2)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Set metadata from share client" () {
        given:
        primaryShareClient.create(testMetadata, null)
        def metadataAfterSet = Collections.singletonMap("afterset", "value")
        when:
        def getMetadataBeforeResponse = primaryShareClient.getProperties().value()
        def setMetadataResponse = primaryShareClient.setMetadata(metadataAfterSet)
        def getMetadataAfterResponse = primaryShareClient.getProperties().value()
        then:
        testMetadata.equals(getMetadataBeforeResponse.metadata())
        FileTestHelper.assertResponseStatusCode(setMetadataResponse, 200)
        metadataAfterSet.equals(getMetadataAfterResponse.metadata())
    }

    def "Set metadata error from share client" () {
        when:
        primaryShareClient.setMetadata(testMetadata)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory from share client" () {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectory("testCreateDirectory"), 201)
    }

    def "Create directory invalid name from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createDirectory("test/directory")
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.PARENT_NOT_FOUND)
    }

    def "Create directory metadata from share client" () {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createDirectory("testCreateDirectory", testMetadata), 201)
    }

    def "Create directory metadata error from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createDirectory("testdirectory", Collections.singletonMap("", "value"))
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, StorageErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create file from share client" () {
        given:
        primaryShareClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFile("testCreateFile", 1024), 201)
    }

    @Unroll
    def "Create file invalid args from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createFile(fileName, maxSize)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)
        where:
        fileName | maxSize | statusCode | errMsg
        "test\file" | 1024 | 400 | "Bad Request"
        "fileName" | -1 | 400 | StorageErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload from share client" () {
        given:
        primaryShareClient.create()
        FileHTTPHeaders httpHeaders = new FileHTTPHeaders()
            .fileContentType("txt")
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.createFile("testCreateFile", 1024, httpHeaders, testMetadata), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.createFile("test\file", maxSize)
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, "Bad Request")
        where:
        fileName | maxSize | httpHeaders| metadata
        "test\file" | 1024 | new FileHTTPHeaders() | testMetadata
        "fileName" | -1 | new FileHTTPHeaders() | testMetadata
        "fileName" | 1024 | new FileHTTPHeaders().fileContentMD5(new byte[0]) | testMetadata
        "fileName" | 1024 | new FileHTTPHeaders() | Collections.singletonMap("", "value")

    }

    def "Delete directory from share client" () {
        given:
        def directoryName = "testCreateDirectory"
        primaryShareClient.create()
        primaryShareClient.createDirectory(directoryName)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryShareClient.deleteDirectory(directoryName), 202)
    }

    def "Delete directory error from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.deleteDirectory("testdirectory")
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete file from share client" () {
        given:
        def fileName = "testCreateFile"
        primaryShareClient.create()
        primaryShareClient.createFile(fileName, 1024)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryShareClient.deleteFile(fileName), 202)
    }

    def "Delete file invalid name from share client" () {
        given:
        primaryShareClient.create()
        when:
        primaryShareClient.deleteDirectory("testdirectory")
        then:
        def e = thrown(StorageErrorException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Get snapshot id from share client" () {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()
        when:
        def shareSnapshotClient = shareBuilderHelper(interceptorManager).snapshot(snapshot).buildClient()
        then:
        snapshot.equals(shareSnapshotClient.getSnapshotId())
    }

}
