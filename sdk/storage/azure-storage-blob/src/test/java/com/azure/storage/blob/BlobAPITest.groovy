// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.RequestConditions
import com.azure.core.util.CoreUtils
import com.azure.core.util.polling.LongRunningOperationStatus
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.ArchiveStatus
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.CopyStatusType
import com.azure.storage.blob.models.CustomerProvidedKey
import com.azure.storage.blob.models.DeleteSnapshotsOptionType
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.models.RehydratePriority
import com.azure.storage.blob.models.SyncCopyStatusType
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.implementation.Constants
import reactor.core.Exceptions
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.NonWritableChannelException
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration
import java.time.OffsetDateTime

class BlobAPITest extends APISpec {
    BlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName)
        bc.getBlockBlobClient().upload(defaultInputStream.get(), defaultDataSize)
    }

    def "Upload input stream overwrite fails"() {
        when:
        bc.upload(defaultInputStream.get(), defaultDataSize)

        then:
        thrown(BlobStorageException)
    }

    def "Upload input stream overwrite"() {
        setup:
        def randomData = getRandomByteArray(Constants.KB)
        def input = new ByteArrayInputStream(randomData)

        when:
        bc.upload(input, Constants.KB, true)

        then:
        def stream = new ByteArrayOutputStream()
        bc.downloadWithResponse(stream, null, null, null, false, null, null)
        stream.toByteArray() == randomData
    }

    @Requires( { liveMode() } )
    def "Upload input stream large data"() {
        setup:
        def randomData = getRandomByteArray(20 * Constants.MB)
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions(null, null, null, Constants.MB)

        when:
        // Uses blob output stream under the hood.
        bc.uploadWithResponse(input, 20 * Constants.MB, pto, null, null, null, null, null, null)

        then:
        notThrown(BlobStorageException)
    }

    @Unroll
    def "Upload numBlocks"() {
        setup:
        def randomData = getRandomByteArray(size)
        def input = new ByteArrayInputStream(randomData)

        def pto = new ParallelTransferOptions((Integer) maxUploadSize, null, null, (Integer) maxUploadSize)

        when:
        bc.uploadWithResponse(input, size, pto, null, null, null, null, null, null)

        then:
        def blocksUploaded = bc.getBlockBlobClient().listBlocks(BlockListType.ALL).getCommittedBlocks()
        blocksUploaded.size() == (int) numBlocks

        where:
        size            | maxUploadSize || numBlocks
        0               | null          || 0
        Constants.KB    | null          || 0 // default is MAX_UPLOAD_BYTES
        Constants.MB    | null          || 0 // default is MAX_UPLOAD_BYTES
        3 * Constants.MB| Constants.MB  || 3
    }

    @Requires({ liveMode() }) // Reading from recordings will not allow for the timing of the test to work correctly.
    def "Upload timeout"() {
        setup:
        def size = 1024
        def randomData = getRandomByteArray(size)
        def input = new ByteArrayInputStream(randomData)

        when:
        bc.uploadWithResponse(input, size, null, null, null, null, null, Duration.ofNanos(5L), null)

        then:
        thrown(IllegalStateException)
    }

    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        def response = bc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getDeserializedHeaders()

        then:
        body == defaultData
        CoreUtils.isNullOrEmpty(headers.getMetadata())
        headers.getContentLength() != null
        headers.getContentType() != null
        headers.getContentRange() == null
        headers.getContentMd5() != null
        headers.getContentEncoding() == null
        headers.getCacheControl() == null
        headers.getContentDisposition() == null
        headers.getContentLanguage() == null
        headers.getBlobSequenceNumber() == null
        headers.getBlobType() == BlobType.BLOCK_BLOB
        headers.getCopyCompletionTime() == null
        headers.getCopyStatusDescription() == null
        headers.getCopyId() == null
        headers.getCopyProgress() == null
        headers.getCopySource() == null
        headers.getCopyStatus() == null
        headers.getLeaseDuration() == null
        headers.getLeaseState() == LeaseStateType.AVAILABLE
        headers.getLeaseStatus() == LeaseStatusType.UNLOCKED
        headers.getAcceptRanges() == "bytes"
        headers.getBlobCommittedBlockCount() == null
        headers.isServerEncrypted() != null
        headers.getBlobContentMD5() == null
    }

    def "Download empty file"() {
        setup:
        def bc = cc.getBlobClient("emptyAppendBlob").getAppendBlobClient()
        bc.create()

        when:
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        def result = outStream.toByteArray()

        then:
        notThrown(BlobStorageException)
        result.length == 0
    }

    /*
    This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    HttpGetterInfo.
     */

    def "Download with retry range"() {
        /*
        We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it was
        constructed in BlobClient.download().
         */
        setup:
        def bu2 = getBlobClient(primaryCredential, bc.getBlobUrl(), new MockRetryRangeResponsePolicy())

        when:
        def range = new BlobRange(2, 5L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
        bu2.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false, null, null)

        then:
        /*
        Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        NOT thrown because the types would not match.
         */
        def e = thrown(RuntimeException)
        e.getCause() instanceof IOException
    }

    def "Download min"() {
        when:
        def outStream = new ByteArrayOutputStream()
        bc.download(outStream)
        def result = outStream.toByteArray()

        then:
        result == defaultData.array()
    }

    @Unroll
    def "Download range"() {
        setup:
        def range = (count == null) ? new BlobRange(offset) : new BlobRange(offset, count)

        when:
        def outStream = new ByteArrayOutputStream()
        bc.downloadWithResponse(outStream, range, null, null, false, null, null)
        String bodyStr = outStream.toString()

        then:
        bodyStr == expectedData

        where:
        offset | count || expectedData
        0      | null  || defaultText
        0      | 5L    || defaultText.substring(0, 5)
        3      | 2L    || defaultText.substring(3, 3 + 2)
    }

    @Unroll
    def "Download AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

        then:
        response.getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Download AC fail"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Download md5"() {
        when:
        def response = bc.downloadWithResponse(new ByteArrayOutputStream(), new BlobRange(0, 3), null, null, true, null, null)
        def contentMD5 = response.getDeserializedHeaders().getContentMd5()

        then:
        contentMD5 == MessageDigest.getInstance("MD5").digest(defaultText.substring(0, 3).getBytes())
    }

    def "Download error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.download(null)

        then:
        thrown(NullPointerException)
    }

    def "Download snapshot"() {
        when:
        def originalStream = new ByteArrayOutputStream()
        bc.download(originalStream)

        def bc2 = bc.createSnapshot()
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream("ABC".getBytes()), 3, true)

        then:
        def snapshotStream = new ByteArrayOutputStream()
        bc2.download(snapshotStream)
        snapshotStream.toByteArray() == originalStream.toByteArray()
    }

    def "Download to file exists"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        // Default overwrite is false so this should fail
        bc.downloadToFile(testFile.getPath())

        then:
        def ex = thrown(UncheckedIOException)
        ex.getCause() instanceof FileAlreadyExistsException

        cleanup:
        testFile.delete()
    }

    def "Download to file exists succeeds"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        bc.downloadToFile(testFile.getPath(), true)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download to file does not exist"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        bc.downloadToFile(testFile.getPath())

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file does not exist open options"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (testFile.exists()) {
            assert testFile.delete()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE_NEW)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    def "Download file exist open options"() {
        setup:
        def testFile = new File(testName + ".txt")
        if (!testFile.exists()) {
            assert testFile.createNewFile()
        }

        when:
        Set<OpenOption> openOptions = new HashSet<>()
        openOptions.add(StandardOpenOption.CREATE)
        openOptions.add(StandardOpenOption.TRUNCATE_EXISTING)
        openOptions.add(StandardOpenOption.READ)
        openOptions.add(StandardOpenOption.WRITE)
        bc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

        then:
        new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8) == defaultText

        cleanup:
        testFile.delete()
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file"() {
        setup:
        def file = getRandomFile(fileSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        outFile.delete()
        file.delete()

        where:
        fileSize             | _
        0                    | _ // empty file
        20                   | _ // small file
        16 * 1024 * 1024     | _ // medium file in several chunks
        8 * 1026 * 1024 + 10 | _ // medium file not aligned to block
        50 * Constants.MB    | _ // large file requiring multiple requests
        // Files larger than 2GB to test no integer overflow are left to stress/perf tests to keep test passes short.
    }

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @Requires({ liveMode() })
    @Unroll
    def "Download file sync buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .credential(primaryCredential)
            .buildClient()

        def blobClient = blobServiceClient.createBlobContainer(containerName)
            .getBlobClient(generateBlobName())


        def file = getRandomFile(fileSize)
        blobClient.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = blobClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null), null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, fileSize)
        properties.getValue().getBlobType() == BlobType.BLOCK_BLOB

        cleanup:
        blobServiceClient.deleteBlobContainer(containerName)
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

    /*
     * Tests downloading a file using a default client that doesn't have a HttpClient passed to it.
     */
    @Requires({ liveMode() })
    @Unroll
    def "Download file async buffer copy"() {
        setup:
        def containerName = generateContainerName()
        def blobServiceAsyncClient = new BlobServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .credential(primaryCredential)
            .buildAsyncClient()

        def blobAsyncClient = blobServiceAsyncClient.createBlobContainer(containerName).block()
            .getBlobAsyncClient(generateBlobName())

        def file = getRandomFile(fileSize)
        blobAsyncClient.uploadFromFile(file.toPath().toString(), true).block()
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def downloadMono = blobAsyncClient.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(4 * 1024 * 1024, null, null), null, null, false)

        then:
        StepVerifier.create(downloadMono)
            .assertNext({ it -> it.getValue().getBlobType() == BlobType.BLOCK_BLOB })
            .verifyComplete()

        compareFiles(file, outFile, 0, fileSize)

        cleanup:
        blobServiceAsyncClient.deleteBlobContainer(containerName)
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

    @Requires({ liveMode() })
    @Unroll
    def "Download file range"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60))
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, range.getOffset(), range.getCount())

        cleanup:
        outFile.delete()
        file.delete()

        /*
        The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        where:
        range                                         | _
        new BlobRange(0, defaultDataSize)             | _ // Exact count
        new BlobRange(1, defaultDataSize - 1 as Long) | _ // Offset and exact count
        new BlobRange(3, 2)                           | _ // Narrow range in middle
        new BlobRange(0, defaultDataSize - 1 as Long) | _ // Count that is less than total
        new BlobRange(0, 10 * 1024)                   | _ // Count much larger than remaining data
    }

    /*
    This is to exercise some additional corner cases and ensure there are no arithmetic errors that give false success.
     */

    @Requires({ liveMode() })
    @Unroll
    def "Download file range fail"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(defaultDataSize + 1), null, null, null, false,
            null, null)

        then:
        thrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @Requires({ liveMode() })
    def "Download file count null"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false, null, null)

        then:
        compareFiles(file, outFile, 0, defaultDataSize)

        cleanup:
        outFile.delete()
        file.delete()
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file AC"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file AC fail"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET ||
            e.getErrorCode() == BlobErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        cleanup:
        outFile.delete()
        file.delete()

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    @Requires({ liveMode() })
    def "Download file etag lock"() {
        setup:
        def file = getRandomFile(Constants.MB)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        Files.deleteIfExists(file.toPath())

        expect:
        def bac = new BlobClientBuilder()
            .pipeline(bc.getHttpPipeline())
            .endpoint(bc.getBlobUrl())
            .buildAsyncClient()
            .getBlockBlobAsyncClient()

        /*
         * Setup the download to happen in small chunks so many requests need to be sent, this will give the upload time
         * to change the ETag therefore failing the download.
         */
        def options = new ParallelTransferOptions(Constants.KB, null, null)

        /*
         * This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
         * registered for onErrorDropped the error is logged at the ERROR level.
         *
         * onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
         * dropped.
         */
        Hooks.onErrorDropped({ ignored -> /* do nothing with it */ })

        /*
         * When the download begins trigger an upload to overwrite the downloading blob after waiting 500 milliseconds
         * so that the download is able to get an ETag before it is changed.
         */
        StepVerifier.create(bac.downloadToFileWithResponse(outFile.toPath().toString(), null, options, null, null, false)
            .doOnSubscribe({ bac.upload(defaultFlux, defaultDataSize, true).delaySubscription(Duration.ofMillis(500)).subscribe() }))
            .verifyErrorSatisfies({
                /*
                 * If an operation is running on multiple threads and multiple return an exception Reactor will combine
                 * them into a CompositeException which needs to be unwrapped. If there is only a single exception
                 * 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
                 *
                 * These exceptions may be wrapped exceptions where the exception we are expecting is contained within
                 * ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
                 * will be returned unmodified by 'Exceptions.unwrap'.
                 */
                assert Exceptions.unwrapMultiple(it).stream().anyMatch({ it2 ->
                    def exception = Exceptions.unwrap(it2)
                    if (exception instanceof BlobStorageException) {
                        assert ((BlobStorageException) exception).getStatusCode() == 412
                        return true
                    }
                })
            })

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        sleep(500)
        !outFile.exists()

        cleanup:
        file.delete()
        outFile.delete()
    }

    @Requires({ liveMode() })
    @Unroll
    def "Download file progress receiver"() {
        def file = getRandomFile(fileSize)
        bc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        def mockReceiver = Mock(ProgressReceiver)

        def numBlocks = fileSize / (4 * 1024 * 1024)
        def prevCount = 0

        when:
        bc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(null, null, mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null)

        then:
        /*
         * Should receive at least one notification indicating completed progress, multiple notifications may be
         * received if there are empty buffers in the stream.
         */
        (1.._) * mockReceiver.reportProgress(fileSize)

        // There should be NO notification with a larger than expected size.
        0 * mockReceiver.reportProgress({ it > fileSize })

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!file.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred >= prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > fileSize })

        cleanup:
        file.delete()
        outFile.delete()

        where:
        fileSize             | _
        100                  | _
        8 * 1026 * 1024 + 10 | _
    }

    def "Get properties default"() {
        when:
        def response = bc.getPropertiesWithResponse(null, null, null)
        def headers = response.getHeaders()
        def properties = response.getValue()

        then:
        validateBasicHeaders(headers)
        CoreUtils.isNullOrEmpty(properties.getMetadata())
        properties.getBlobType() == BlobType.BLOCK_BLOB
        properties.getCopyCompletionTime() == null // tested in "copy"
        properties.getCopyStatusDescription() == null // only returned when the service has errors; cannot validate.
        properties.getCopyId() == null // tested in "abort copy"
        properties.getCopyProgress() == null // tested in "copy"
        properties.getCopySource() == null // tested in "copy"
        properties.getCopyStatus() == null // tested in "copy"
        !properties.isIncrementalCopy() // tested in PageBlob."start incremental copy"
        properties.getCopyDestinationSnapshot() == null // tested in PageBlob."start incremental copy"
        properties.getLeaseDuration() == null // tested in "acquire lease"
        properties.getLeaseState() == LeaseStateType.AVAILABLE
        properties.getLeaseStatus() == LeaseStatusType.UNLOCKED
        properties.getBlobSize() >= 0
        properties.getContentType() != null
        properties.getContentMd5() != null
        properties.getContentEncoding() == null // tested in "set HTTP headers"
        properties.getContentDisposition() == null // tested in "set HTTP headers"
        properties.getContentLanguage() == null // tested in "set HTTP headers"
        properties.getCacheControl() == null // tested in "set HTTP headers"
        properties.getBlobSequenceNumber() == null // tested in PageBlob."create sequence number"
        headers.getValue("Accept-Ranges") == "bytes"
        properties.getCommittedBlockCount() == null // tested in AppendBlob."append block"
        properties.isServerEncrypted()
        properties.getAccessTier() == AccessTier.HOT
        properties.isAccessTierInferred()
        properties.getArchiveStatus() == null
        properties.getCreationTime() != null
    }

    def "Get properties min"() {
        expect:
        bc.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
    }

    @Unroll
    def "Get properties AC"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(setupBlobMatchCondition(bc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.getPropertiesWithResponse(bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Get properties AC fail"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.getPropertiesWithResponse(bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Get properties error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.getProperties()

        then:
        thrown(BlobStorageException)
    }

    def "Set HTTP headers null"() {
        setup:
        def response = bc.setHttpHeadersWithResponse(null, null, null, null)

        expect:
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
    }

    def "Set HTTP headers min"() {
        setup:
        def properties = bc.getProperties()
        def headers = new BlobHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())))

        bc.setHttpHeaders(headers)

        expect:
        bc.getProperties().getContentType() == "type"
    }

    @Unroll
    def "Set HTTP headers headers"() {
        setup:
        def putHeaders = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        bc.setHttpHeaders(putHeaders)

        expect:
        validateBlobProperties(
            bc.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }


    @Unroll
    def "Set HTTP headers AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.setHttpHeadersWithResponse(null, bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Set HTTP headers AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.setHttpHeadersWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Set HTTP headers error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.setHttpHeaders(null)

        then:
        thrown(BlobStorageException)
    }

    def "Set metadata all null"() {
        when:
        def response = bc.setMetadataWithResponse(null, null, null, null)

        then:
        bc.getProperties().getMetadata().size() == 0
        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        Boolean.parseBoolean(response.getHeaders().getValue("x-ms-request-server-encrypted"))
    }

    def "Set metadata min"() {
        setup:
        def metadata = new HashMap<String, String>()
        metadata.put("foo", "bar")

        when:
        bc.setMetadata(metadata)

        then:
        bc.getProperties().getMetadata() == metadata
    }

    @Unroll
    def "Set metadata metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        expect:
        bc.setMetadataWithResponse(metadata, null, null, null).getStatusCode() == statusCode
        bc.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
        "i0"  | "a"    | "i_"   | "a"    || 200 /* Test culture sensitive word sort */
    }

    @Unroll
    def "Set metadata AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.setMetadataWithResponse(null, bac, null, null).getStatusCode() == 200

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Set metadata AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)

        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.setMetadataWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Set metadata error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.setMetadata(null)

        then:
        thrown(BlobStorageException)
    }

    def "Snapshot"() {
        when:
        def response = bc.createSnapshotWithResponse(null, null, null, null)

        then:
        response.getValue().exists()
        validateBasicHeaders(response.getHeaders())
    }

    def "Snapshot min"() {
        bc.createSnapshotWithResponse(null, null, null, null).getStatusCode() == 201
    }

    @Unroll
    def "Snapshot metadata"() {
        setup:
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        def response = bc.createSnapshotWithResponse(metadata, null, null, null)
        def bcSnap = response.getValue()

        expect:
        response.getStatusCode() == 201
        bcSnap.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Snapshot AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.createSnapshotWithResponse(null, bac, null, null).getStatusCode() == 201

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Snapshot AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        when:
        bc.createSnapshotWithResponse(null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Snapshot error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.createSnapshot()

        then:
        thrown(BlobStorageException)
    }

    def "Copy"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), Duration.ofSeconds(1))

        when:
        def response = poller.blockLast()
        def properties = copyDestBlob.getProperties().block()

        then:
        properties.getCopyStatus() == CopyStatusType.SUCCESS
        properties.getCopyCompletionTime() != null
        properties.getCopyProgress() != null
        properties.getCopySource() != null

        response != null
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        def blobInfo = response.getValue()
        blobInfo != null
        blobInfo.getCopyId() == properties.getCopyId()
    }

    def "Copy min"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()

        when:
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), Duration.ofSeconds(1))
        def verifier = StepVerifier.create(poller.take(1))

        then:
        verifier.assertNext({
            assert it.getValue() != null
            assert it.getValue().getCopyId() != null
            assert it.getValue().getCopySourceUrl() == bc.getBlobUrl()
            assert it.getStatus() == LongRunningOperationStatus.IN_PROGRESS || it.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
        }).verifyComplete()
    }

    def "Copy poller"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()

        when:
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), null, null, null, null, null, null)

        then:
        def lastResponse = poller.doOnNext({
            assert it.getValue() != null
            assert it.getValue().getCopyId() != null
            assert it.getValue().getCopySourceUrl() == bc.getBlobUrl()
        }).blockLast()

        expect:
        lastResponse != null
        lastResponse.getValue() != null

        StepVerifier.create(copyDestBlob.getProperties())
            .assertNext({
                assert it.getCopyId() == lastResponse.getValue().getCopyId()
                assert it.getCopyStatus() == CopyStatusType.SUCCESS
                assert it.getCopyCompletionTime() != null
                assert it.getCopyProgress() != null
                assert it.getCopySource() != null
                assert it.getCopyId() != null
            })
            .verifyComplete()
    }

    @Unroll
    def "Copy metadata"() {
        setup:
        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), metadata, null, null, null, null, Duration.ofSeconds(1))
        poller.blockLast()

        then:
        StepVerifier.create(bu2.getProperties())
            .assertNext({ assert it.getMetadata() == metadata })
            .verifyComplete()

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Copy source AC"() {
        setup:
        def copyDestBlob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        def poller = copyDestBlob.beginCopy(bc.getBlobUrl(), null, null, null, mac, null, null)
        def response = poller.blockLast()

        then:
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Copy source AC fail"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    @Unroll
    def "Copy dest AC"() {
        setup:
        def bu2 = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient()
        bu2.upload(defaultFlux, defaultDataSize).block()
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, bac, Duration.ofSeconds(1))
        def response = poller.blockLast()

        then:
        response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Copy dest AC fail"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Abort copy lease fail"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)

        def leaseId = setupBlobLeaseCondition(bu2, receivedLeaseID)
        def blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, blobRequestConditions, Duration.ofMillis(500))
        def response = poller.poll()

        assert response.getStatus() != LongRunningOperationStatus.FAILED

        def blobCopyInfo = response.getValue()
        bu2.abortCopyFromUrlWithResponse(blobCopyInfo.getCopyId(), garbageLeaseID, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getStatusCode() == 412

        cleanup:
        cu2.delete()
    }

    def "Abort copy"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder()
            .blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateBlobName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName())

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, null, Duration.ofSeconds(1))
        def lastResponse = poller.poll()

        assert lastResponse != null
        assert lastResponse.getValue() != null

        def response = bu2.abortCopyFromUrlWithResponse(lastResponse.getValue().getCopyId(), null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 204
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null

        cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.deleteWithResponse(null, null, null).getStatusCode() == 202
    }

    def "Abort copy lease"() {
        setup:
        // Data has to be large enough and copied between accounts to give us enough time to abort
        new SpecializedBlobClientBuilder().blobClient(bc)
            .buildBlockBlobClient()
            .upload(new ByteArrayInputStream(getRandomByteArray(8 * 1024 * 1024)), 8 * 1024 * 1024, true)
        // So we don't have to create a SAS.
        cc.setAccessPolicy(PublicAccessType.BLOB, null)

        def cu2 = alternateBlobServiceClient.getBlobContainerClient(generateContainerName())
        cu2.create()
        def bu2 = cu2.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        def leaseId = setupBlobLeaseCondition(bu2, receivedLeaseID)
        def blobAccess = new BlobRequestConditions().setLeaseId(leaseId)

        when:
        def poller = bu2.beginCopy(bc.getBlobUrl(), null, null, null, null, blobAccess, Duration.ofSeconds(1))
        def lastResponse = poller.poll()

        then:
        lastResponse != null
        lastResponse.getValue() != null

        def copyId = lastResponse.getValue().getCopyId()
        bu2.abortCopyFromUrlWithResponse(copyId, leaseId, null, null).getStatusCode() == 204

        cleanup:
        // Normal test cleanup will not clean up containers in the alternate account.
        cu2.delete()
    }

    def "Copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.copyFromUrl("http://www.error.com")

        then:
        thrown(BlobStorageException)
    }

    def "Abort copy error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.abortCopyFromUrl("id")

        then:
        thrown(BlobStorageException)
    }

    def "Sync copy"() {
        setup:
        // Sync copy is a deep copy, which requires either sas or public access.
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def headers = bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).getHeaders()

        then:
        headers.getValue("x-ms-copy-status") == SyncCopyStatusType.SUCCESS.toString()
        headers.getValue("x-ms-copy-id") != null
        validateBasicHeaders(headers)
    }

    def "Sync copy min"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        expect:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Sync copy metadata"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), metadata, null, null, null, null, null)

        then:
        bu2.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Sync copy source AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        match = setupBlobMatchCondition(bc, match)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        expect:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch
        null     | null       | null         | null
        oldDate  | null       | null         | null
        null     | newDate    | null         | null
        null     | null       | receivedEtag | null
        null     | null       | null         | garbageEtag
    }

    @Unroll
    def "Sync copy source AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        def mac = new RequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, mac, null, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch
        newDate  | null       | null        | null
        null     | oldDate    | null        | null
        null     | null       | garbageEtag | null
        null     | null       | null        | receivedEtag
    }

    @Unroll
    def "Sync copy dest AC"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        match = setupBlobMatchCondition(bu2, match)
        leaseID = setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Sync copy dest AC fail"() {
        setup:
        cc.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu2, noneMatch)
        setupBlobLeaseCondition(bu2, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bu2.copyFromUrlWithResponse(bc.getBlobUrl(), null, null, null, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Sync copy error"() {
        setup:
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        bu2.copyFromUrl(bc.getBlobUrl())

        then:
        thrown(BlobStorageException)
    }

    def "Delete"() {
        when:
        def response = bc.deleteWithResponse(null, null, null, null)
        def headers = response.getHeaders()

        then:
        response.getStatusCode() == 202
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
    }

    def "Delete min"() {
        expect:
        bc.deleteWithResponse(null, null, null, null).getStatusCode() == 202
    }

    @Unroll
    def "Delete options"() {
        setup:
        bc.createSnapshot()
        // Create an extra blob so the list isn't empty (null) when we delete base blob, too
        def bu2 = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu2.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.deleteWithResponse(option, null, null, null)

        then:
        cc.listBlobs().stream().count() == blobsRemaining

        where:
        option                            | blobsRemaining
        DeleteSnapshotsOptionType.INCLUDE | 1
        DeleteSnapshotsOptionType.ONLY    | 2
    }

    @Unroll
    def "Delete AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        expect:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null).getStatusCode() == 202

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Delete AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bc, noneMatch)
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        bc.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, bac, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Blob delete error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.delete()

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Set tier block blob"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        def initialResponse = bc.setAccessTierWithResponse(tier, null, null, null, null)
        def headers = initialResponse.getHeaders()

        then:
        initialResponse.getStatusCode() == 200 || initialResponse.getStatusCode() == 202
        headers.getValue("x-ms-version") != null
        headers.getValue("x-ms-request-id") != null
        bc.getProperties().getAccessTier() == tier
        cc.listBlobs().iterator().next().getProperties().getAccessTier() == tier

        cleanup:
        cc.delete()

        where:
        tier               | _
        AccessTier.HOT     | _
        AccessTier.COOL    | _
        AccessTier.ARCHIVE | _
    }

    @Unroll
    def "Set tier page blob"() {
        setup:
        def cc = premiumBlobServiceClient.createBlobContainer(generateContainerName())

        def bc = cc.getBlobClient(generateBlobName()).getPageBlobClient()
        bc.create(512)

        when:
        bc.setAccessTier(tier)

        then:
        bc.getProperties().getAccessTier() == tier
        cc.listBlobs().iterator().next().getProperties().getAccessTier() == tier

        cleanup:
        cc.delete()

        where:
        tier           | _
        AccessTier.P4  | _
        AccessTier.P6  | _
        AccessTier.P10 | _
        AccessTier.P20 | _
        AccessTier.P30 | _
        AccessTier.P40 | _
        AccessTier.P50 | _
    }

    def "Set tier min"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bu = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bu.upload(defaultInputStream.get(), defaultData.remaining())

        when:
        def statusCode = bc.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode()

        then:
        statusCode == 200 || statusCode == 202

        cleanup:
        cc.delete()
    }

    def "Set tier inferred"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        def inferred1 = bc.getProperties().isAccessTierInferred()
        def inferredList1 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred()

        bc.setAccessTier(AccessTier.HOT)

        def inferred2 = bc.getProperties().isAccessTierInferred()
        def inferredList2 = cc.listBlobs().iterator().next().getProperties().isAccessTierInferred()

        then:
        inferred1
        inferredList1
        !inferred2
        inferredList2 == null
    }

    @Unroll
    def "Set tier archive status"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateBlobName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTier(sourceTier)
        bc.setAccessTier(destTier)

        then:
        bc.getProperties().getArchiveStatus() == status
        cc.listBlobs().iterator().next().getProperties().getArchiveStatus() == status

        where:
        sourceTier         | destTier        | priority                   | status
        AccessTier.ARCHIVE | AccessTier.COOL | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_COOL
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.STANDARD | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
        AccessTier.ARCHIVE | AccessTier.HOT  | RehydratePriority.HIGH     | ArchiveStatus.REHYDRATE_PENDING_TO_HOT
    }

    def "Set tier error"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTier(AccessTier.fromString("garbage"))

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.INVALID_HEADER_VALUE

        cleanup:
        cc.delete()
    }

    def "Set tier illegal argument"() {
        when:
        bc.setAccessTier(null)

        then:
        thrown(NullPointerException)
    }

    def "Set tier lease"() {
        setup:

        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)
        def leaseID = setupBlobLeaseCondition(bc, receivedLeaseID)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, leaseID, null, null)

        then:
        notThrown(BlobStorageException)

        cleanup:
        cc.delete()
    }

    def "Set tier lease fail"() {
        setup:
        def cc = blobServiceClient.createBlobContainer(generateContainerName())
        def bc = cc.getBlobClient(generateBlobName()).getBlockBlobClient()
        bc.upload(defaultInputStream.get(), defaultDataSize)

        when:
        bc.setAccessTierWithResponse(AccessTier.HOT, null, "garbage", null, null)

        then:
        thrown(BlobStorageException)
    }

    @Unroll
    def "Copy with tier"() {
        setup:
        def blobName = generateBlobName()
        def bc = cc.getBlobClient(blobName).getBlockBlobClient()
        bc.uploadWithResponse(defaultInputStream.get(), defaultDataSize, null, null, tier1, null, null, null, null)
        def bcCopy = cc.getBlobClient(generateBlobName()).getBlockBlobClient()

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setExpiryTime(OffsetDateTime.now().plusHours(1))
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName(cc.getBlobContainerName())
            .setBlobName(blobName)
            .generateSasQueryParameters(primaryCredential)
            .encode()
        bcCopy.copyFromUrlWithResponse(bc.getBlobUrl().toString() + "?" + sas, null, tier2, null, null, null, null)

        then:
        bcCopy.getProperties().getAccessTier() == tier2

        where:
        tier1           | tier2
        AccessTier.HOT  | AccessTier.COOL
        AccessTier.COOL | AccessTier.HOT
    }

    def "Undelete"() {
        setup:
        enableSoftDelete()
        bc.delete()

        when:
        def undeleteHeaders = bc.undeleteWithResponse(null, null).getHeaders()

        bc.getProperties()

        then:
        notThrown(BlobStorageException)
        undeleteHeaders.getValue("x-ms-request-id") != null
        undeleteHeaders.getValue("x-ms-version") != null
        undeleteHeaders.getValue("Date") != null

        disableSoftDelete() == null
    }

    def "Undelete min"() {
        setup:
        enableSoftDelete()
        bc.delete()

        expect:
        bc.undeleteWithResponse(null, null).getStatusCode() == 200
    }

    def "Undelete error"() {
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.undelete()

        then:
        thrown(BlobStorageException)
    }

    def "Get account info"() {
        when:
        def response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null)

        then:
        response.getHeaders().getValue("Date") != null
        response.getHeaders().getValue("x-ms-request-id") != null
        response.getHeaders().getValue("x-ms-version") != null
        response.getValue().getAccountKind() != null
        response.getValue().getSkuName() != null
    }

    def "Get account info min"() {
        expect:
        bc.getAccountInfoWithResponse(null, null).getStatusCode() == 200
    }

    def "Get Container Name"() {
        expect:
        containerName == bc.getContainerName()
    }

    def "Get Blob Name"() {
        expect:
        blobName == bc.getBlobName()
    }

    def "Get Blob Name and Build Client"() {
        when:
        BlobClient client = cc.getBlobClient(originalBlobName)
        BlobClientBase baseClient = cc.getBlobClient(client.getBlobName()).getBlockBlobClient()

        then:
        baseClient.getBlobName() == finalBlobName

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                 | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    def "Builder cpk validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl()
        def builder = new BlobClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }

    def "Builder bearer token validation"() {
        setup:
        String endpoint = BlobUrlParts.parse(bc.getBlobUrl()).setScheme("http").toUrl()
        def builder = new BlobClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)

        when:
        builder.buildClient()

        then:
        thrown(IllegalArgumentException)
    }
}
