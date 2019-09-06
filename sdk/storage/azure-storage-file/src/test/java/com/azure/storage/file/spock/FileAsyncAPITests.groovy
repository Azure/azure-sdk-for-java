// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.exception.HttpResponseException
import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.FileAsyncClient
import com.azure.storage.file.FileClient
import com.azure.storage.file.FileSASPermission
import com.azure.storage.file.ShareClient
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileRange
import com.azure.storage.file.FileSmbProperties
import com.azure.storage.file.models.NtfsFileAttributes
import com.azure.storage.file.models.StorageErrorCode
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAsyncAPITests extends APISpec {
    FileAsyncClient primaryFileAsyncClient
    ShareClient shareClient
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
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileAsyncClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileAsyncClient()
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
        def fileURL = primaryFileAsyncClient.getFileUrl().toString()
        then:
        expectURL == fileURL
    }

    def "Create file"() {
        expect:
        StepVerifier.create(primaryFileAsyncClient.create(1024))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }
    }

    def "Create file error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.create(-1))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Create file with args fpk"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
            .filePermissionKey(filePermissionKey)
        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.value().lastModified()
                assert it.value().smbProperties()
                assert it.value().smbProperties().filePermissionKey()
                assert it.value().smbProperties().ntfsFileAttributes()
                assert it.value().smbProperties().fileLastWriteTime()
                assert it.value().smbProperties().fileCreationTime()
                assert it.value().smbProperties().fileChangeTime()
                assert it.value().smbProperties().parentId()
                assert it.value().smbProperties().fileId()
            }.verifyComplete()
    }

    def "Create file with args fp"() {
        setup:
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.value().lastModified()
                assert it.value().smbProperties()
                assert it.value().smbProperties().filePermissionKey()
                assert it.value().smbProperties().ntfsFileAttributes()
                assert it.value().smbProperties().fileLastWriteTime()
                assert it.value().smbProperties().fileCreationTime()
                assert it.value().smbProperties().fileChangeTime()
                assert it.value().smbProperties().parentId()
                assert it.value().smbProperties().fileId()
            }.verifyComplete()
    }

    @Unroll
    def "Create file with args error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.createWithResponse(maxSize, null, null, null, metadata))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        maxSize | metadata                                      | statusCode | errMsg
        -1      | testMetadata                                  | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        1024    | Collections.singletonMap("testMeta", "value") | 403        | StorageErrorCode.AUTHENTICATION_FAILED
    }

    def "Upload and download data"() {
        given:
        primaryFileAsyncClient.create(dataLength).block()
        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(Flux.just(defaultData), dataLength))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithPropertiesWithResponse(null, null))

        then:
        uploadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext {
            assert it.value().contentLength() == dataLength
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert it.value().smbProperties()
            assert it.value().smbProperties().filePermissionKey()
            assert it.value().smbProperties().ntfsFileAttributes()
            assert it.value().smbProperties().fileLastWriteTime()
            assert it.value().smbProperties().fileCreationTime()
            assert it.value().smbProperties().fileChangeTime()
            assert it.value().smbProperties().parentId()
            assert it.value().smbProperties().fileId()
        }.verifyComplete()
        cleanup:
        defaultData.clear()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(Flux.just(defaultData), dataLength, 1))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(1, dataLength), true))

        then:
        uploadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 206)
            assert it.value().contentLength() == dataLength
        }.verifyComplete()
        cleanup:
        defaultData.clear()
    }

    def "Upload data error"() {
        when:
        def updateDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.upload(Flux.just(defaultData), dataLength, 1))
        then:
        updateDataErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
        cleanup:
        defaultData.clear()
    }

    @Unroll
    def "Upload data length mismatch"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        when:
        def uploadErrorVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(Flux.just(defaultData),
            size, 0))
        then:
        uploadErrorVerifier.verifyErrorSatisfies {
            assert it instanceof UnexpectedLengthException
            assert it.getMessage().contains(errMsg)
        }
        cleanup:
        defaultData.clear()
        where:
        size | errMsg
        6 | "more than"
        8 | "less than"
    }

    def "Download data error"() {
        when:
        def downloadDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(0, 1023), false))
        then:
        downloadDataErrorVerifier.verifyErrorSatisfies({
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        })
    }

    def "Upload and clear range" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()
        when:
        def clearRangeVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 0))
        def downloadResponseVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(0, 6), false))
        then:
        clearRangeVerifier.assertNext {
            FileTestHelper.assertResponseStatusCode(it, 201)
        }
        downloadResponseVerifier.assertNext {
            assert it.value().body() != null
        }
    }

    def "Upload and clear range with args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()
        when:
        def clearRangeVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 1))
        def downloadResponseVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(1, 7), false))
        then:
        clearRangeVerifier.assertNext {
            FileTestHelper.assertResponseStatusCode(it, 201)
        }
        downloadResponseVerifier.assertNext {
            assert it.value().body() != null
        }
        cleanup:
        fullInfoData.clear()
    }

    def "Clear range error" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()
        when:
        def clearRangeErrorVerifier = StepVerifier.create(primaryFileAsyncClient.clearRange(30))
        then:
        clearRangeErrorVerifier.verifyErrorSatisfies {
            FileTestHelper.assertExceptionStatusCodeAndMessage(it, 416, StorageErrorCode.INVALID_RANGE)
        }
    }

    def "Clear range error args" () {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()
        when:
        def clearRangeErrorVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 20))
        then:
        clearRangeErrorVerifier.verifyErrorSatisfies {
            FileTestHelper.assertExceptionStatusCodeAndMessage(it, 416, StorageErrorCode.INVALID_RANGE)
        }
        cleanup:
        fullInfoData.clear()
    }

    def "Upload file does not exist"() {
        given:
        def uploadFile = new File(testFolder.getPath() + "/fakefile.txt")

        if (uploadFile.exists()) {
            assert uploadFile.delete()
        }

        when:
        def uploadFromFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile.getPath()))

        then:
        uploadFromFileErrorVerifier.verifyErrorSatisfies({ it instanceof NoSuchFileException })

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

        primaryFileAsyncClient.create(data.length()).block()
        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))), data.length()).block()

        when:
        def downloadToFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.downloadToFile(downloadFile.getPath()))

        then:
        downloadToFileErrorVerifier.verifyErrorSatisfies({ it instanceof FileAlreadyExistsException })

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

        primaryFileAsyncClient.create(data.length()).block()
        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))), data.length()).block()

        when:
        def downloadFromFileVerifier = StepVerifier.create(primaryFileAsyncClient.downloadToFile(downloadFile.getPath()))

        then:
        downloadFromFileVerifier.verifyComplete()
        def scanner = new Scanner(downloadFile).useDelimiter("\\Z")
        data == scanner.next()
        scanner.close()

        cleanup:
        FileTestHelper.deleteFolderIfExists(testFolder.getPath())
    }

    def "Upload range from URL"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block()
        def sasToken = primaryFileAsyncClient.generateSAS(new FileSASPermission().read(true), getUTCNow().plusDays(1))

        when:
        FileAsyncClient client = fileBuilderHelper(interceptorManager, shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl().toString())
            .buildFileAsyncClient()

        client.create(1024).block()
        client.uploadRangeFromURL(length, destinationOffset, sourceOffset, (primaryFileAsyncClient.getFileUrl().toString() + "/" + shareName + "/" + filePath +"?" + sasToken).toURI()).block()

        then:
        def result = new String(client.downloadWithProperties().block().body().blockLast().array())

        for(int i = 0; i < length; i++) {
            result.charAt(destinationOffset + i) == data.charAt(sourceOffset + i)
        }
    }

    def "Start copy"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl().toString() + "/" + shareName + "/" + filePath
        when:
        def copyInfoVerifier = StepVerifier.create(primaryFileAsyncClient.startCopyWithResponse(sourceURL, null))
        then:
        copyInfoVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 202)
            assert it.value().copyId() != null
        }.verifyComplete()
    }

    def "Start copy error"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        when:
        def startCopyErrorVerifier = StepVerifier.create(primaryFileAsyncClient.startCopyWithResponse("some url", testMetadata))
        then:
        startCopyErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.INVALID_HEADER_VALUE)
        }
    }

    @Ignore
    def "Abort copy"() {
        //TODO: Need to find a way of mocking pending copy status
    }

    def "Delete file"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse())
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete file error"() {
        when:
        def deleteFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.delete())
        then:
        deleteFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Get properties"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        when:
        def getPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse())
        then:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert it.value().eTag()
            assert it.value().lastModified()
            assert it.value().lastModified()
            assert it.value().smbProperties()
            assert it.value().smbProperties().filePermissionKey()
            assert it.value().smbProperties().ntfsFileAttributes()
            assert it.value().smbProperties().fileLastWriteTime()
            assert it.value().smbProperties().fileCreationTime()
            assert it.value().smbProperties().fileChangeTime()
            assert it.value().smbProperties().parentId()
            assert it.value().smbProperties().fileId()
        }
    }

    def "Get properties error"() {
        when:
        def getProperitesErrorVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        then:
        getProperitesErrorVerifier.verifyErrorSatisfies {
            assert it instanceof HttpResponseException
        }
    }

    def "Set httpHeaders fpk"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())
            .filePermissionKey(filePermissionKey)

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.value().smbProperties()
                assert it.value().smbProperties().filePermissionKey()
                assert it.value().smbProperties().ntfsFileAttributes()
                assert it.value().smbProperties().fileLastWriteTime()
                assert it.value().smbProperties().fileCreationTime()
                assert it.value().smbProperties().fileChangeTime()
                assert it.value().smbProperties().parentId()
                assert it.value().smbProperties().fileId()
            }.verifyComplete()
    }

    def "Set httpHeaders fp"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        smbProperties.fileCreationTime(getUTCNow())
            .fileLastWriteTime(getUTCNow())

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.value().smbProperties()
                assert it.value().smbProperties().filePermissionKey()
                assert it.value().smbProperties().ntfsFileAttributes()
                assert it.value().smbProperties().fileLastWriteTime()
                assert it.value().smbProperties().fileCreationTime()
                assert it.value().smbProperties().fileChangeTime()
                assert it.value().smbProperties().parentId()
                assert it.value().smbProperties().fileId()
            }.verifyComplete()
    }

    def "Set httpHeaders error"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        when:
        def setHttpHeaderVerifier = StepVerifier.create(primaryFileAsyncClient.setProperties(-1, null, null, null))
        then:
        setHttpHeaderVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Set metadata"() {
        given:

        primaryFileAsyncClient.createWithResponse(1024, httpHeaders, null, null, testMetadata).block()
        def updatedMetadata = Collections.singletonMap("update", "value")
        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        def setPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(updatedMetadata))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        then:
        getPropertiesBeforeVerifier.assertNext {
            assert testMetadata.equals(it.metadata())
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert updatedMetadata.equals(it.metadata())
        }.verifyComplete()
    }

    def "Set metadata error"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def errorMetadata = Collections.singletonMap("", "value")
        when:
        def setMetadataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(errorMetadata))
        then:
        setMetadataErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "List ranges"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = testResourceName.randomName("file", 60)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, tmpFolder.toString(), fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges())
            .assertNext {
                assert it.start() == 0
                assert it.end() == 1023
            }.verifyComplete()
        cleanup:
        FileTestHelper.deleteFolderIfExists(tmpFolder.toString())
    }

    def "List ranges with range"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = testResourceName.randomName("file", 60)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, tmpFolder.toString(), fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges(new FileRange(0, 511L)))
            .assertNext {
                assert it.start() == 0
                assert it.end() == 511
            }.verifyComplete()
        cleanup:
        FileTestHelper.deleteFolderIfExists(tmpFolder.toString())
    }

    def "List handles"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.listHandles())
            .verifyComplete()
    }

    def "List handles with maxResult"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.listHandles(2))
            .verifyComplete()
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
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildFileAsyncClient()
        then:
        snapshot.equals(shareSnapshotClient.getShareSnapshotId())
    }
}
