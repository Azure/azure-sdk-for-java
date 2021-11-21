// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.exception.UnexpectedLengthException
import com.azure.core.util.FluxUtil
import com.azure.core.util.polling.PollerFlux
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.PermissionCopyModeType
import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileCopyInfo
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.ShareFileRange
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareStorageException
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

import static com.azure.storage.file.share.FileTestHelper.*

class FileAsyncAPITests extends APISpec {
    ShareFileAsyncClient primaryFileAsyncClient
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
        primaryFileAsyncClient = fileBuilderHelper(shareName, filePath).buildFileAsyncClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        httpHeaders = new ShareFileHttpHeaders().setContentLanguage("en")
            .setContentType("application/octet-stream")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes> of(NtfsFileAttributes.NORMAL))
    }

    def "Get file URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString).getAccountName()
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
                assert assertResponseStatusCode(it, 201)
            }.verifyComplete()
    }

    def "Create file error"() {
        when:
        def createFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.create(-1))

        then:
        createFileErrorVerifier.verifyErrorSatisfies {
            assert assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Create file with args fpk"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, null, testMetadata))
            .assertNext {
                assert assertResponseStatusCode(it, 201)
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
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())

        expect:
        StepVerifier.create(primaryFileAsyncClient.createWithResponse(1024, httpHeaders, smbProperties, filePermission, testMetadata))
            .assertNext {
                assert assertResponseStatusCode(it, 201)
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
            assert assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
        }
    }

    def "Create lease"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.createWithResponse(data.defaultDataSizeLong + 1, null, null, null,
            null, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
    }

    def "Create lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.createWithResponse(data.defaultDataSizeLong + 1, null, null, null,
            null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */

    @LiveOnly
    @Unroll
    def "Download file buffer copy"() {
        setup:
        def shareServiceAsyncClient = new ShareServiceClientBuilder()
            .connectionString(environment.primaryAccount.connectionString)
            .buildAsyncClient()

        def fileClient = shareServiceAsyncClient.getShareAsyncClient(shareName)
            .createFile(filePath, fileSize).block()

        def file = getRandomFile(fileSize)
        fileClient.uploadFromFile(file.toPath().toString()).block()
        def outFile = new File(namer.getRandomName(60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        fileClient.downloadToFile(outFile.toPath().toString()).block()

        then:
        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        shareServiceAsyncClient.deleteShare(shareName).block()
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

    def "Upload and download data"() {
        given:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 0L))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null))

        then:
        uploadVerifier.assertNext {
            assert assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext({ response ->
            assert assertResponseStatusCode(response, 200, 206)
            def headers = response.getDeserializedHeaders()
            assert headers.getContentLength() == data.defaultDataSizeLong
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
                .flatMap({ actualData -> assert data.defaultBytes == actualData })
                .then()
        }).verifyComplete()
    }

    def "Upload and download data with args"() {
        given:
        primaryFileAsyncClient.create(1024).block()

        when:
        def uploadVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 1L))
        def downloadVerifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(new ShareFileRange(1, data.defaultDataSizeLong), true))

        then:
        uploadVerifier.assertNext {
            assert assertResponseStatusCode(it, 201)
        }.verifyComplete()

        downloadVerifier.assertNext {
            assert assertResponseStatusCode(it, 206)
            assert it.getDeserializedHeaders().getContentLength() == data.defaultDataSizeLong
            FluxUtil.collectBytesInByteBufferStream(it.getValue())
                .flatMap({ actualData -> assert actualData == data.defaultBytes })
        }.verifyComplete()
    }

    def "Upload data error"() {
        when:
        def updateDataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.upload(data.defaultFlux, data.defaultDataSizeLong))

        then:
        updateDataErrorVerifier.verifyErrorSatisfies {
            assert assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Upload lease"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 0,
            new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
    }

    def "Upload lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 0,
            new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)
    }

    @Unroll
    def "Upload data length mismatch"() {
        given:
        primaryFileAsyncClient.create(1024).block()

        when:
        def uploadErrorVerifier = StepVerifier.create(primaryFileAsyncClient.uploadWithResponse(data.defaultFlux,
            size, 0))

        then:
        uploadErrorVerifier.verifyErrorSatisfies {
            assert it instanceof UnexpectedLengthException
            assert it.getMessage().contains(errMsg)
        }

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
            assert assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        })
    }

    def "Download lease"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null, null, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
    }

    def "Download lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.downloadWithResponse(null, null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)
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
            assertResponseStatusCode(it, 201)
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
            assertResponseStatusCode(it, 201)
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
            assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE)
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
            assertExceptionStatusCodeAndMessage(it, 416, ShareErrorCode.INVALID_RANGE)
        }

        cleanup:
        fullInfoData.clear()
    }

    def "Clear range lease"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(1, 0, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
    }

    def "Clear range lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.clearRangeWithResponse(1, 0, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)
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
        uploadFile.delete()
    }

    def "Upload and download file exists"() {
        given:
        def data = "Download file exists"
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        if (!downloadFile.exists()) {
            assert downloadFile.createNewFile()
        }

        primaryFileAsyncClient.create(data.length()).block()
        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8))),
            data.length()).block()

        when:
        def downloadToFileErrorVerifier = StepVerifier.create(
            primaryFileAsyncClient.downloadToFile(downloadFile.getPath()))

        then:
        downloadToFileErrorVerifier.verifyErrorSatisfies({ it instanceof FileAlreadyExistsException })

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
        downloadFile.delete()
    }

    def "Upload from file lease"() {
        setup:
        primaryFileAsyncClient.create(1024).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.verifyComplete()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "Upload from file lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)


        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.uploadFromFile(uploadFile,
            new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "Download to file lease"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 0).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.downloadToFileWithResponse(
            downloadFile.toPath().toString(), null, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
        verifier.verifyComplete()

        cleanup:
        downloadFile.delete()
    }

    def "Download to file lease fail"() {
        setup:
        primaryFileAsyncClient.create(data.defaultDataSizeLong).block()
        primaryFileAsyncClient.uploadWithResponse(data.defaultFlux, data.defaultDataSizeLong, 0).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()
        def downloadFile = new File(String.format("%s/%s.txt", testFolder.getPath(), namer.getResourcePrefix()))

        when:
        def verifier = StepVerifier.create(primaryFileAsyncClient.downloadToFileWithResponse(
            downloadFile.toPath().toString(), null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)

        cleanup:
        downloadFile.delete()
    }

    def "Upload range from URL"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block()
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(namer.getUtcNow().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        when:
        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
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

    def "Upload range from URL lease"() {
        setup:
        primaryFileAsyncClient.create(1024).block()
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block()
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(namer.getUtcNow().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        when:
        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl().toString())
            .buildFileAsyncClient()

        client.create(1024).block()
        def leaseId = createLeaseClient(client).acquireLease().block()
        def verifier = StepVerifier.create(client.uploadRangeFromUrlWithResponse(length, destinationOffset,
            sourceOffset, primaryFileAsyncClient.getFileUrl().toString() + "?" + sasToken, new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        verifier.expectNextCount(1)
        verifier.verifyComplete()
    }

    def "Upload range from URL lease fail"() {
        primaryFileAsyncClient.create(1024).block()
        def data = "The quick brown fox jumps over the lazy dog"
        def sourceOffset = 5
        def length = 5
        def destinationOffset = 0

        primaryFileAsyncClient.upload(Flux.just(ByteBuffer.wrap(data.getBytes())), data.length()).block()
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasToken = new ShareServiceSasSignatureValues()
            .setExpiryTime(namer.getUtcNow().plusDays(1))
            .setPermissions(new ShareFileSasPermission().setReadPermission(true))
            .setShareName(primaryFileAsyncClient.getShareName())
            .setFilePath(primaryFileAsyncClient.getFilePath())
            .generateSasQueryParameters(credential)
            .encode()

        when:
        ShareFileAsyncClient client = fileBuilderHelper(shareName, "destination")
            .endpoint(primaryFileAsyncClient.getFileUrl().toString())
            .buildFileAsyncClient()

        client.create(1024).block()
        createLeaseClient(client).acquireLease().block()
        def verifier = StepVerifier.create(client.uploadRangeFromUrlWithResponse(length, destinationOffset,
            sourceOffset, primaryFileAsyncClient.getFileUrl().toString() + "?" + sasToken, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        verifier.verifyError(ShareStorageException)
    }

    def "Start copy"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, null,
            getPollingDuration(1000))
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))
    }

    @Unroll
    def "Start copy with args"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def sourceURL = primaryFileAsyncClient.getFileUrl()
        def filePermissionKey = shareClient.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with namer.getUtcNow()
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey)
        }

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, smbProperties,
            setFilePermission ? filePermission : null, permissionType, ignoreReadOnly,
            setArchiveAttribute, null, getPollingDuration(1000), null)
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))

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
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, null,
            getPollingDuration(1000))
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))
    }

    @Ignore("There is a race condition in Poller where it misses the first observed event if there is a gap between the time subscribed and the time we start observing events.")
    def "Start copy lease"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, null, null, null,
            false, false, null, getPollingDuration(1000), new ShareRequestConditions().setLeaseId(leaseId))
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))
    }

    @Ignore("There is a race condition in Poller where it misses the first observed event if there is a gap between the time subscribed and the time we start observing events.")
    def "Start copy lease fail"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        // TODO: Need another test account if using SAS token for authentication.
        // TODO: SasToken auth cannot be used until the logging redaction
        def sourceURL = primaryFileAsyncClient.getFileUrl()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        PollerFlux<ShareFileCopyInfo, Void> poller = primaryFileAsyncClient.beginCopy(sourceURL, null, null, null,
            false, false, null, getPollingDuration(1000), new ShareRequestConditions().setLeaseId(namer.getRandomUuid()))
        def copyInfoVerifier = StepVerifier.create(poller)

        then:
        copyInfoVerifier.assertNext {
            assert it.getValue().getCopyId() != null
        }.expectComplete().verify(Duration.ofMinutes(1))
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
                assert assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete file error"() {
        when:
        def deleteFileErrorVerifier = StepVerifier.create(primaryFileAsyncClient.delete())

        then:
        deleteFileErrorVerifier.verifyErrorSatisfies {
            assert assertExceptionStatusCodeAndMessage(it, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
        }
    }

    def "Delete file lease"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse(new ShareRequestConditions().setLeaseId(leaseId)))
            .assertNext {
                assert assertResponseStatusCode(it, 202)
            }.verifyComplete()
    }

    def "Delete file lease fail"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.deleteWithResponse(new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)
    }

    def "Get properties"() {
        given:
        primaryFileAsyncClient.create(1024).block()

        when:
        def getPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse())

        then:
        getPropertiesVerifier.assertNext {
            assert assertResponseStatusCode(it, 200)
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

    def "Get properties lease"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def getPropertiesVerifier = StepVerifier.create(primaryFileAsyncClient.getPropertiesWithResponse(new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        getPropertiesVerifier.expectNextCount(1).verifyComplete()
    }

    def "Get properties lease fail"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def getPropertiesVerifier = StepVerifier.create(
            primaryFileAsyncClient.getPropertiesWithResponse(new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        getPropertiesVerifier.verifyError(ShareStorageException)
    }

    def "Get properties error"() {
        when:
        def getPropertiesErrorVerifier = StepVerifier.create(primaryFileAsyncClient.getProperties())

        then:
        getPropertiesErrorVerifier.verifyErrorSatisfies {
            assert it instanceof ShareStorageException
        }
    }

    def "Set httpHeaders fpk"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, null))
            .assertNext {
                assert assertResponseStatusCode(it, 200)
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
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, httpHeaders, smbProperties, filePermission))
            .assertNext {
                assert assertResponseStatusCode(it, 200)
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

    def "Set httpHeaders lease"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete()
    }

    def "Set httpHeaders lease fail"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.setPropertiesWithResponse(512, null, null, null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)
    }


    def "Set httpHeaders error"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()

        when:
        def setHttpHeaderVerifier = StepVerifier.create(primaryFileAsyncClient.setProperties(-1, null, null, null))

        then:
        setHttpHeaderVerifier.verifyErrorSatisfies {
            assert assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.OUT_OF_RANGE_INPUT)
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
            assert assertResponseStatusCode(it, 200)
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
            assert assertExceptionStatusCodeAndMessage(it, 400, ShareErrorCode.EMPTY_METADATA_KEY)
        }
    }

    def "Set metadata lease"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def metadata = Collections.singletonMap("key", "value")
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def setMetadataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(metadata,
            new ShareRequestConditions().setLeaseId(leaseId)))

        then:
        setMetadataErrorVerifier.expectNextCount(1).verifyComplete()
    }

    def "Set metadata lease fail"() {
        given:
        primaryFileAsyncClient.create(1024).block()
        def metadata = Collections.singletonMap("key", "value")
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        when:
        def setMetadataErrorVerifier = StepVerifier.create(primaryFileAsyncClient.setMetadataWithResponse(metadata,
            new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))

        then:
        setMetadataErrorVerifier.verifyError(ShareStorageException)
    }

    def "List ranges"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges())
            .assertNext {
                assert it.getStart() == 0
                assert it.getEnd() == 1023
            }.verifyComplete()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List ranges with range"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges(new ShareFileRange(0, 511L)))
            .assertNext {
                assert it.getStart() == 0
                assert it.getEnd() == 511
            }.verifyComplete()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List ranges lease"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()
        def leaseId = createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges(null, new ShareRequestConditions().setLeaseId(leaseId)))
            .expectNextCount(1).verifyComplete()

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    def "List ranges lease fail"() {
        given:
        primaryFileAsyncClient.createWithResponse(1024, null, null, null, null).block()
        def fileName = namer.getRandomName(60)
        def uploadFile = createRandomFileWithLength(1024, testFolder, fileName)
        primaryFileAsyncClient.uploadFromFile(uploadFile).block()
        createLeaseClient(primaryFileAsyncClient).acquireLease().block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRanges(null, new ShareRequestConditions().setLeaseId(namer.getRandomUuid())))
            .verifyError(ShareStorageException)

        cleanup:
        deleteFileIfExists(testFolder.getPath(), fileName)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "List ranges diff"() {
        setup:
        def snapshotId = primaryFileAsyncClient.create(4 * Constants.MB)
            .then(primaryFileAsyncClient.upload(Flux.just(getRandomByteBuffer(4 * Constants.MB)), 4 * Constants.MB))
            .then(primaryFileServiceAsyncClient.getShareAsyncClient(primaryFileAsyncClient.getShareName())
                .createSnapshot()
                .map({ it.getSnapshot() }))
            .block()

        Flux.fromIterable(rangesToUpdate)
            .flatMap({
                def size = it.getEnd() - it.getStart() + 1
                primaryFileAsyncClient.uploadWithResponse(Flux.just(getRandomByteBuffer((int) size)), size, it.getStart())
            }).blockLast()

        Flux.fromIterable(rangesToClear)
            .flatMap({
                primaryFileAsyncClient.clearRangeWithResponse(it.getEnd() - it.getStart() + 1, it.getStart())
            })
            .blockLast()

        expect:
        StepVerifier.create(primaryFileAsyncClient.listRangesDiff(snapshotId))
            .assertNext({
                it.getRanges().size() == expectedRanges.size()
                it.getClearRanges().size() == expectedClearRanges.size()

                for (def i = 0; i < expectedRanges.size(); i++) {
                    def actualRange = it.getRanges().get(i)
                    def expectedRange = expectedRanges.get(i)
                    expectedRange.getStart() == actualRange.getStart()
                    expectedRange.getEnd() == actualRange.getEnd()
                }

                for (def i = 0; i < expectedClearRanges.size(); i++) {
                    def actualRange = it.getClearRanges().get(i)
                    def expectedRange = expectedClearRanges.get(i)
                    expectedRange.getStart() == actualRange.getStart()
                    expectedRange.getEnd() == actualRange.getEnd()
                }
            })
            .verifyComplete()

        where:
        rangesToUpdate                       | rangesToClear                           | expectedRanges                       | expectedClearRanges
        createFileRanges()                   | createFileRanges()                      | createFileRanges()                   | createClearRanges()
        createFileRanges(0, 511)             | createFileRanges()                      | createFileRanges(0, 511)             | createClearRanges()
        createFileRanges()                   | createFileRanges(0, 511)                | createFileRanges()                   | createClearRanges(0, 511)
        createFileRanges(0, 511)             | createFileRanges(512, 1023)             | createFileRanges(0, 511)             | createClearRanges(512, 1023)
        createFileRanges(0, 511, 1024, 1535) | createFileRanges(512, 1023, 1536, 2047) | createFileRanges(0, 511, 1024, 1535) | createClearRanges(512, 1023, 1536, 2047)
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

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close handle min"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("1"))
            .assertNext {
                assert it.getClosedHandles() == 0
                assert it.getFailedHandles() == 0
            }.verifyComplete()
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseHandle("invalidHandleId"))
            .verifyErrorSatisfies({ it instanceof ShareStorageException })
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close all handles min"() {
        given:
        primaryFileAsyncClient.create(512).block()

        expect:
        StepVerifier.create(primaryFileAsyncClient.forceCloseAllHandles())
            .assertNext {
                assert it.getClosedHandles() == 0
                assert it.getFailedHandles() == 0
            }.verifyComplete()
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = fileBuilderHelper(shareName, filePath).snapshot(snapshot).buildFileAsyncClient()

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
