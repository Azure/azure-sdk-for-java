// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.exception.HttpResponseException
import com.azure.storage.common.credentials.SharedKeyCredential
import com.azure.storage.file.models.FileHTTPHeaders
import com.azure.storage.file.models.FileRange
import com.azure.storage.file.models.FileRangeWriteType
import com.azure.storage.file.models.StorageErrorCode
import io.netty.buffer.Unpooled
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAsyncAPITests extends APISpec {
    def primaryFileAsyncClient
    def shareName
    def filePath
    static def defaultData = ByteBuffer.allocate(8).wrap("default".getBytes(StandardCharsets.UTF_8))
    static def dataLength = defaultData.remaining()
    static def testMetadata
    static def httpHeaders

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        def shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileAsyncClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildAsyncClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new FileHTTPHeaders().fileContentLanguage("en")
            .fileContentType("application/octet-stream")
    }

    def "Get file URL"() {
        given:
        def accoutName = SharedKeyCredential.fromConnectionString(connectionString).accountName()
        def expectURL = String.format("https://%s.file.core.windows.net", accoutName)
        when:
        def fileURL = primaryFileAsyncClient.getFileUrl().toString()
        then:
        expectURL.equals(fileURL)
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

    def "Create file with args"() {
        expect:
        StepVerifier.create(primaryFileAsyncClient.create(1024, httpHeaders, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    @Unroll
    def "Create file with args error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.create(maxSize, fileHttpHeaders, metadata))
        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, statusCode, errMsg)
        }
        where:
        maxSize | fileHttpHeaders | metadata                                      | statusCode | errMsg
        -1      | httpHeaders     | testMetadata                                  | 400        | StorageErrorCode.OUT_OF_RANGE_INPUT
        1024    | httpHeaders     | Collections.singletonMap("testMeta", "value") | 403        | StorageErrorCode.AUTHENTICATION_FAILED
    }

    def "Upload and download data"() {
        given:
        primaryFileAsyncClient.create(dataLength).block()
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.upload(Flux.just(defaultData), dataLength))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithProperties())

        then:
        uploadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext {
            assert it.value().contentLength() == dataLength
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        cleanup:
        defaultData.clear()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def dataBytes = new byte[dataLength]
        defaultData.get(dataBytes)

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.upload(Flux.just(defaultData), dataLength, 1, FileRangeWriteType.UPDATE))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithProperties(new FileRange(1, dataLength), true))

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
        def updateDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.upload(Flux.just(defaultData), dataLength, 1, FileRangeWriteType.UPDATE))
        then:
        updateDataErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Download data error"() {
        when:
        def downloadDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithProperties(new FileRange(0, 1023), false))
        then:
        downloadDataErrorVerifier.assertNext {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, StorageErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Upload and download file"() {
        given:
        File uploadFile = new File(testFolder.getPath() + "/helloworld")
        File downloadFile = new File(testFolder.getPath() + "/testDownload")

        if (!Files.exists(downloadFile.toPath())) {
            downloadFile.createNewFile().block()
        }

        primaryFileAsyncClient.create(uploadFile.length()).block()
        when:
        def uploadFileVerifier = StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile.toString()))
        def downloadFileVerifier = StepVerifier.create(primaryFileAsyncClient.downloadToFile(downloadFile.toString()))
        then:
        uploadFileVerifier.verifyComplete()
        downloadFileVerifier.verifyComplete()
        assert FileTestHelper.assertTwoFilesAreSame(uploadFile, downloadFile)
        cleanup:
        FileTestHelper.deleteFolderIfExists(testFolder.toString())
    }

    def "Start copy"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl().toString() + "/" + shareName + "/" + filePath
        when:
        def copyInfoVerifier = StepVerifier.create(primaryFileAsyncClient.startCopy(sourceURL, null))
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
        def startCopyErrorVerifier = StepVerifier.create(primaryFileAsyncClient.startCopy("some url", testMetadata))
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
        StepVerifier.create(primaryFileAsyncClient.delete())
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
        def getPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        then:
        getPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
            assert it.eTag() != null
            assert it.lastModified() != null
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

    def "Set httpHeaders"() {
        given:
        primaryFileAsyncClient.create(1024, httpHeaders, testMetadata).block()
        expect:
        StepVerifier.create(primaryFileAsyncClient.setHttpHeaders(512, httpHeaders))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
            }.verifyComplete()
    }

    def "Set httpHeaders error"() {
        given:
        primaryFileAsyncClient.create(1024, httpHeaders, testMetadata).block()
        when:
        def setHttpHeaderVerifier = StepVerifier.create(primaryFileAsyncClient.setHttpHeaders(-1, httpHeaders))
        then:
        setHttpHeaderVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Set metadata"() {
        given:
        primaryFileAsyncClient.create(1024, httpHeaders, testMetadata).block()
        def updatedMetadata = Collections.singletonMap("update", "value")
        when:
        def getPropertiesBeforeVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        def setPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadata(updatedMetadata))
        def getPropertiesAfterVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())
        then:
        getPropertiesBeforeVerifier.assertNext {
            assert testMetadata.equals(it.value().metadata())
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert updatedMetadata.equals(it.value().metadata())
        }.verifyComplete()
    }

    def "Set metadata error"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def errorMetadata = Collections.singletonMap("", "value")
        when:
        def setMetadataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadata(errorMetadata))
        then:
        setMetadataErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, StorageErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "List ranges"() {
        given:
        primaryFileAsyncClient.create(1024, null, null).block()
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
        primaryFileAsyncClient.create(1024, null, null).block()
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
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildAsyncClient()
        then:
        snapshot.equals(shareSnapshotClient.getShareSnapshotId())
    }
}
