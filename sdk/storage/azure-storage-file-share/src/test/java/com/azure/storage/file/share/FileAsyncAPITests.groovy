// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.exception.HttpResponseException
import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.FluxUtil
import com.azure.core.util.polling.PollerFlux
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileCopyInfo
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareFileRange
import com.azure.storage.file.share.sas.ShareFileSasPermission
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Ignore
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class FileAsyncAPITests extends APISpec {
    ShareFileAsyncClient primaryFileAsyncClient
    ShareClient shareClient
    String shareName
    String filePath
    def defaultData = ByteBuffer.wrap("default".getBytes(StandardCharsets.UTF_8))
    Long dataLength = defaultData.remaining()
    static Map<String, String> testMetadata
    static ShareFileHttpHeaders httpHeaders
    static FileSmbProperties smbProperties
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = testResourceName.randomName(methodName, 60)
        filePath = testResourceName.randomName(methodName, 60)
        shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        primaryFileAsyncClient = fileBuilderHelper(interceptorManager, shareName, filePath).buildFileAsyncClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get file URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, filePath)

        when:
        def fileURL = primaryFileAsyncClient.getFileUrl()

        then:
        expectURL == fileURL
    }

    def "Create file"() {
        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, null, null, null, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create file error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.create(-1))

        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Create file with args fpk"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.getValue().getLastModified()
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Create file with args fp"() {
        setup:
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())

        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 201)
                assert it.getValue().getLastModified()
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Create file with args error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.createWithResponse(-1, null, null, null, testMetadata))

        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Upload and download data"() {
        given:
        primaryFileAsyncClient.create(dataLength).block()

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(Flux.just(defaultData), dataLength, 0L))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null, null))

        then:
        uploadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext({ response ->
            assert FileTestHelper.assertResponseStatusCode(response, 200)
            def headers = response.getDeserializedHeaders()
            assert headers.getContentLength() == dataLength
            assert headers.getETag()
            assert headers.getLastModified()
            assert headers.getFilePermissionKey()
            assert headers.getFileAttributes()
            assert headers.getFileLastWriteTime()
            assert headers.getFileCreationTime()
            assert headers.getFileChangeTime()
            assert headers.getFileParentId()
            assert headers.getFileId()

            FluxUtil.collectBytesInByteBufferStream(response.getValue())
                .flatMap({ data -> assert defaultData.array() == data })
                .then()
        }).verifyComplete()

        cleanup:
        defaultData.clear()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileAsyncClient.create(1024).block()

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(Flux.just(defaultData), dataLength, 1L))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, dataLength), true))

        then:
        uploadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 206)
            assert it.getDeserializedHeaders().getContentLength() == dataLength
            FluxUtil.collectBytesInByteBufferStream(it.getValue())
                .flatMap({ data -> assert data == defaultData.array()})
        }.verifyComplete()

        cleanup:
        defaultData.clear()
    }

    def "Upload data error"() {
        when:
        def updateDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.upload(Flux.just(defaultData), dataLength))

        then:
        updateDataErrorVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
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
        6    | "more than"
        8    | "less than"
    }

    def "Download data error"() {
        when:
        def downloadDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 1023), false))

        then:
        downloadDataErrorVerifier.verifyErrorSatisfies({
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        })
    }

    def "Upload and clear range"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()

        when:
        def clearRangeVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 0))
        def downloadResponseVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(0, 6), false))

        then:
        clearRangeVerifier.assertNext {
            FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadResponseVerifier.assertNext {
            FluxUtil.collectBytesInByteBufferStream(it.getValue())
                .flatMap({ data ->
                    for (def b : data) {
                        assert b == 0
                    }
                })
        }.verifyComplete()
    }

    def "Upload and clear range with args"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()

        when:
        def clearRangeVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 1))
        def downloadResponseVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, 7), false))

        then:
        clearRangeVerifier.assertNext {
            FileTestHelper.assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadResponseVerifier.assertNext {
            FluxUtil.collectBytesInByteBufferStream(it.getValue())
                .flatMap({ data ->
                    for (def b : data) {
                        assert b == 0
                    }
                })
        }.verifyComplete()

        cleanup:
        fullInfoData.clear()
    }

    def "Clear range error"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()

        when:
        def clearRangeErrorVerifier = StepVerifier.create(primaryFileAsyncClient.clearRange(30))

        then:
        clearRangeErrorVerifier.verifyErrorSatisfies {
            FileTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE)
        }
    }

    def "Clear range error args"() {
        given:
        def fullInfoString = "please clear the range"
        def fullInfoData = ByteBuffer.wrap(fullInfoString.getBytes(StandardCharsets.UTF_8))
        primaryFileAsyncClient.create(fullInfoString.length()).block()
        primaryFileAsyncClient.upload(Flux.just(fullInfoData), fullInfoString.length()).block()

        when:
        def clearRangeErrorVerifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(7, 20))

        then:
        clearRangeErrorVerifier.verifyErrorSatisfies {
            FileTestHelper.assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE)
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
        FileTestHelper.deleteFilesIfExists(testFolder.getPath())
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
        FileTestHelper.deleteFilesIfExists(testFolder.getPath())
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
        downloadFromFileVerifier.assertNext {
            assert it.getContentLength() == (long) data.length()
        }.verifyComplete()
        def scanner = new Scanner(downloadFile).useDelimiter("\\Z")
        data == scanner.next()
        scanner.close()

        cleanup:
        FileTestHelper.deleteFilesIfExists(testFolder.getPath())
    }

    def "Upload range from URL"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block()
        def credential = StorageSharedKeyCredential.fromConnectionString(connectionString)
        def sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(getUTCNow().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        when:
        ShareFileAsyncClient client = fileBuilderHelper(interceptorManager, shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl().toString())
            .buildFileAsyncClient()

        client.create(1024).block()
        client.uploadRangeFromUrl(length, destinationOffset, sourceOffset, primaryFileAsyncClient.getFileUrl().toString() + "?" + sasToken).block()

        then:
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.download()))
            .assertNext({
                def result = new String(it)
                for (int i = 0; i < length; i++) {
                    result.charAt(destinationOffset + i) == data.charAt(sourceOffset + i)
                }
            }).verifyComplete()
    }

    def "Start copy"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, null, Duration.ofSeconds(1))
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))
    }

    @Ignore("There is a race condition in Poller where it misses the first observed event if there is a gap between the time subscribed and the time we start observing events.")
    def "Start copy error"() {
        given:
        primaryFileAsyncClient.create(1024).block()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy("some url", testMetadata, Duration.ofSeconds(1))
        def startCopyErrorVerifier = StepVerifier.create(poller)

        then:
        startCopyErrorVerifier.expectErrorSatisfies({
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.INVALID_HEADER_VALUE)
        }).verify(Duration.ofSeconds(30))
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
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
            assert it.getValue().getETag()
            assert it.getValue().getLastModified()
            assert it.getValue().getLastModified()
            assert it.getValue().getSmbProperties()
            assert it.getValue().getSmbProperties().getFilePermissionKey()
            assert it.getValue().getSmbProperties().getNtfsFileAttributes()
            assert it.getValue().getSmbProperties().getFileLastWriteTime()
            assert it.getValue().getSmbProperties().getFileCreationTime()
            assert it.getValue().getSmbProperties().getFileChangeTime()
            assert it.getValue().getSmbProperties().getParentId()
            assert it.getValue().getSmbProperties().getFileId()
        }.verifyComplete()
    }

    def "Get properties error"() {
        when:
        def getPropertiesErrorVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())

        then:
        getPropertiesErrorVerifier.verifyErrorSatisfies {
            assert it instanceof HttpResponseException
        }
    }

    def "Set httpHeaders fpk"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Set httpHeaders fp"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission))
            .assertNext {
                assert FileTestHelper.assertResponseStatusCode(it, 200)
                assert it.getValue().getSmbProperties()
                assert it.getValue().getSmbProperties().getFilePermissionKey()
                assert it.getValue().getSmbProperties().getNtfsFileAttributes()
                assert it.getValue().getSmbProperties().getFileLastWriteTime()
                assert it.getValue().getSmbProperties().getFileCreationTime()
                assert it.getValue().getSmbProperties().getFileChangeTime()
                assert it.getValue().getSmbProperties().getParentId()
                assert it.getValue().getSmbProperties().getFileId()
            }.verifyComplete()
    }

    def "Set httpHeaders error"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()

        when:
        def setHttpHeaderVerifier = StepVerifier.create(primaryFileAsyncClient.setProperties(-1, null, null, null))

        then:
        setHttpHeaderVerifier.verifyErrorSatisfies {
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
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
            assert testMetadata == it.getMetadata()
        }.verifyComplete()
        setPropertiesVerifier.assertNext {
            assert FileTestHelper.assertResponseStatusCode(it, 200)
        }.verifyComplete()
        getPropertiesAfterVerifier.assertNext {
            assert updatedMetadata == it.getMetadata()
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
            assert FileTestHelper.assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "List ranges"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = testResourceName.randomName("file", 60)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges())
            .assertNext {
                assert it.getStart() == 0
                assert it.getEnd() == 1023
            }.verifyComplete()

        cleanup:
        FileTestHelper.deleteFilesIfExists(testFolder.getPath())
    }

    def "List ranges with range"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = testResourceName.randomName("file", 60)
        def uploadFile = FileTestHelper.createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges(new ShareFileRange(0, 511L)))
            .assertNext {
                assert it.getStart() == 0
                assert it.getEnd() == 511
            }.verifyComplete()

        cleanup:
        FileTestHelper.deleteFilesIfExists(testFolder.getPath())
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

    def "Force close handle min"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("1"))
            .assertNext {
                assert it.getClosedHandles() == 0
            }.verifyComplete()
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("invalidHandleId"))
            .verifyErrorSatisfies({ it instanceof  ShareStorageException })
    }

    def "Force close all handles min"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseAllHandles())
            .assertNext({ it.getClosedHandles() == 0 })
            .verifyComplete()
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = fileBuilderHelper(interceptorManager, shareName, filePath).snapshot(snapshot).buildFileAsyncClient()

        then:
        snapshot == shareSnapshotClient.getShareSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryFileAsyncClient.getShareName()
    }

    def "Get File Path"() {
        expect:
        filePath == primaryFileAsyncClient.getFilePath()
    }
}
