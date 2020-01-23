package com.azure.storage.blob.specialized.cryptography

import com.azure.core.util.CoreUtils
import com.azure.core.util.FluxUtil
import com.azure.storage.blob.BlobClientBuilder
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.ProgressReceiver
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.BlobType
import com.azure.storage.blob.models.DownloadRetryOptions
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.LeaseStatusType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.implementation.Constants
import reactor.core.Exceptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration

class EncryptedBlobDownloadTest extends APISpec {
    EncryptedBlobClient ebc
    BlobContainerClient cc
    String blobName

    String keyId

    @Shared
    def fakeKey

    @Shared
    def fakeKeyResolver

    def setup() {

        keyId = "keyId"
        fakeKey = new FakeKey(keyId, resourceNamer.randomName("fakekey", 256).getBytes())
        fakeKeyResolver = new FakeKeyResolver(fakeKey)

        blobName = generateBlobName()

        cc = getServiceClientBuilder(primaryCredential,
            String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .buildClient()
            .getBlobContainerClient(generateContainerName())
        cc.create()

        def builder = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            cc.getBlobContainerUrl().toString())
            .blobName(blobName)

        builder.buildEncryptedBlobAsyncClient().upload(defaultFlux, null).block()

        ebc = builder.buildEncryptedBlobClient()
    }

    @Requires({ liveMode() })
    def "Download all null"() {
        when:
        def stream = new ByteArrayOutputStream()
        def response = ebc.downloadWithResponse(stream, null, null, null, false, null, null)
        def body = ByteBuffer.wrap(stream.toByteArray())
        def headers = response.getDeserializedHeaders()

        then:
        body == defaultData
        headers.getMetadata()
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
        def builder = getEncryptedClientBuilder(fakeKey, null, primaryCredential,
            ebc.getBlobUrl(), new MockRetryRangeResponsePolicy())

        ebc = builder.buildEncryptedBlobClient()

        when:
        def range = new BlobRange(2, 5L)
        def options = new DownloadRetryOptions().setMaxRetryRequests(3)
        ebc.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false, null, null)

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
        ebc.download(outStream)
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
        ebc.downloadWithResponse(outStream, range, null, null, false, null, null)
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
        match = setupBlobMatchCondition(ebc, match)
        leaseID = setupBlobLeaseCondition(ebc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        def response = ebc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null)

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
        setupBlobLeaseCondition(ebc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(ebc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        ebc.downloadWithResponse(new ByteArrayOutputStream(), null, null, bac, false, null, null).getStatusCode()

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

    def "Download error"() {
        setup:
        ebc = new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .blobClient(cc.getBlobClient(generateBlobName()))
            .buildEncryptedBlobClient()

        when:
        ebc.download(null)

        then:
        thrown(NullPointerException)
    }

    def "Download snapshot"() {
        when:
        def originalStream = new ByteArrayOutputStream()
        ebc.download(originalStream)

        def bc2 = ebc.createSnapshot()
        new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .blobClient(ebc)
            .buildEncryptedBlobAsyncClient()
            .upload(Flux.just(ByteBuffer.wrap("ABC".getBytes())), null)

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
        ebc.downloadToFile(testFile.getPath())

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
        ebc.downloadToFile(testFile.getPath(), true)

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
        ebc.downloadToFile(testFile.getPath())

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
        ebc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

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
        ebc.downloadToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null)

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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60) + ".txt")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        def properties = ebc.downloadToFileWithResponse(outFile.toPath().toString(), null,
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

    @Requires({ liveMode() })
    @Unroll
    def "Download file range"() {
        setup:
        def file = getRandomFile(defaultDataSize)
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(resourceNamer.randomName(testName, 60))
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null)

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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(34), null, null, null, false,
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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), new BlobRange(0), null, null, null, false, null, null)

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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        match = setupBlobMatchCondition(ebc, match)
        leaseID = setupBlobLeaseCondition(ebc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        noneMatch = setupBlobMatchCondition(ebc, noneMatch)
        setupBlobLeaseCondition(ebc, leaseID)
        BlobRequestConditions bro = new BlobRequestConditions().setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified).setIfMatch(match).setIfNoneMatch(noneMatch)
            .setLeaseId(leaseID)

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null)

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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        Files.deleteIfExists(file.toPath())

        expect:
        def bac = new EncryptedBlobClientBuilder()
            .key(fakeKey, "keyWrapAlgorithm")
            .pipeline(ebc.getHttpPipeline())
            .endpoint(ebc.getBlobUrl())
            .buildEncryptedBlobAsyncClient()

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
            .doOnSubscribe({ bac.upload(defaultFlux, null, true).delaySubscription(Duration.ofMillis(500)).subscribe() }))
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
        ebc.uploadFromFile(file.toPath().toString(), true)
        def outFile = new File(testName + "")
        if (outFile.exists()) {
            assert outFile.delete()
        }

        def mockReceiver = Mock(ProgressReceiver)

        def numBlocks = fileSize / (4 * 1024 * 1024)
        def prevCount = 0

        when:
        ebc.downloadToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions(null, null, mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null)

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(fileSize)

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
}
