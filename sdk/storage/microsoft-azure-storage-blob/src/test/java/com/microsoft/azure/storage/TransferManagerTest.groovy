// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.http.HttpResponse
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyFactory
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

class TransferManagerTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())

        /*
        We just print something out in between each test to keep Travis from being idle for too long. The tests seem
        to run slower on Travis, and without this keep-alive, it may exceed the 10 minutes of no output and error the
        CI build.
         */
        System.out.println("Starting test")
    }

    @Unroll
    def "Upload file"() {
        setup:
        def file = getRandomFile(fileSize)
        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadFileToBlockBlob(channel,
                bu, (int) (BlockBlobURL.MAX_STAGE_BLOCK_BYTES / 10), null,
                new TransferManagerUploadToBlockBlobOptions(null, null, null, null, 20)).blockingGet()

        then:
        responseType.isInstance(response.response()) // Ensure we did the correct type of operation.
        validateBasicHeaders(response)
        compareDataToFile(bu.download(new BlobRange().withCount(fileSize), null, false, null)
                .blockingGet().body(null), file)

        cleanup:
        channel.close()

        where:
        fileSize                               || responseType
        0                                      || BlockBlobUploadResponse // Empty file
        10                                     || BlockBlobUploadResponse // Single shot
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1 || BlockBlobCommitBlockListResponse // Multi part
    }

    def compareDataToFile(Flowable<ByteBuffer> data, File file) {
        FileInputStream fis = new FileInputStream(file)

        for (ByteBuffer received : data.blockingIterable()) {
            byte[] readBuffer = new byte[received.remaining()]
            fis.read(readBuffer)
            for (int i = 0; i < received.remaining(); i++) {
                if (readBuffer[i] != received.get(i)) {
                    return false
                }
            }
        }

        fis.close()
        return true
    }

    def "Upload file illegal arguments null"() {
        when:
        TransferManager.uploadFileToBlockBlob(file, url, 5, null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        file                                                     | url
        null                                                     | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        AsynchronousFileChannel.open(getRandomFile(10).toPath()) | null
    }

    @Unroll
    def "Upload file illegal arguments blocks"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(fileSize).toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, blockLength, null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        cleanup:
        channel.close()

        where:
        blockLength                            | fileSize
        -1                                     | 10 // -1 is invalid.
        BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 1 | BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 10 // Block size too big.
        10                                     | BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 // Too many blocks.
    }

    @Unroll
    def "Upload file headers"() {
        setup:
        // We have to use the defaultData here so we can calculate the MD5 on the uploadBlob case.
        File file = File.createTempFile("testUpload", ".txt")
        file.deleteOnExit()
        if (fileSize == "small") {
            FileOutputStream fos = new FileOutputStream(file)
            fos.write(defaultData.array())
            fos.close()
        } else {
            file = getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10)
        }

        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
            new TransferManagerUploadToBlockBlobOptions(null, new BlobHTTPHeaders()
                .withBlobCacheControl(cacheControl).withBlobContentDisposition(contentDisposition)
                .withBlobContentEncoding(contentEncoding).withBlobContentLanguage(contentLanguage)
                .withBlobContentMD5(contentMD5).withBlobContentType(contentType), null, null, null))
            .blockingGet()

        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
            fileSize == "small" ? MessageDigest.getInstance("MD5").digest(defaultData.array()) : contentMD5,
            contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present.
        // HTTP default content type is application/octet-stream.

        cleanup:
        channel.close()

        where:
        // The MD5 is simply set on the blob for commitBlockList, not validated.
        fileSize | cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        "small"  | null         | null               | null            | null            | null                                                         | null
        "small"  | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
        "large"  | null         | null               | null            | null            | null                                                         | null
        "large"  | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload file metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }
        def channel = AsynchronousFileChannel.open(getRandomFile(dataSize).toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
            new TransferManagerUploadToBlockBlobOptions(null, null, metadata, null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        cleanup:
        channel.close()

        where:
        dataSize                                | key1  | value1 | key2   | value2
        10                                      | null  | null   | null   | null
        10                                      | "foo" | "bar"  | "fizz" | "buzz"
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null  | null   | null   | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Upload file AC"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
        def channel = AsynchronousFileChannel.open(getRandomFile(dataSize).toPath())

        expect:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
                new TransferManagerUploadToBlockBlobOptions(null, null, null, bac, null))
                .blockingGet().statusCode() == 201

        cleanup:
        channel.close()

        where:
        dataSize                                | modified | unmodified | match        | noneMatch   | leaseID
        10                                      | null     | null       | null         | null        | null
        10                                      | oldDate  | null       | null         | null        | null
        10                                      | null     | newDate    | null         | null        | null
        10                                      | null     | null       | receivedEtag | null        | null
        10                                      | null     | null       | null         | garbageEtag | null
        10                                      | null     | null       | null         | null        | receivedLeaseID
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null         | null        | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | oldDate  | null       | null         | null        | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | newDate    | null         | null        | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | receivedEtag | null        | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null         | garbageEtag | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Upload file AC fail"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))
        def channel = AsynchronousFileChannel.open(getRandomFile(dataSize).toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
                new TransferManagerUploadToBlockBlobOptions(null, null, null, bac, null)).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        cleanup:
        channel.close()

        where:
        dataSize                                | modified | unmodified | match       | noneMatch    | leaseID
        10                                      | newDate  | null       | null        | null         | null
        10                                      | null     | oldDate    | null        | null         | null
        10                                      | null     | null       | garbageEtag | null         | null
        10                                      | null     | null       | null        | receivedEtag | null
        10                                      | null     | null       | null        | null         | garbageLeaseID
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | newDate  | null       | null        | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | oldDate    | null        | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | garbageEtag | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null        | receivedEtag | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null        | null         | garbageLeaseID
    }

    /*
    We require that any Flowable passed as a request body be replayable to support retries. This test ensures that
    whatever means of getting data from a file we use produces a replayable Flowable so that we abide by our own
    contract.
     */

    def "Upload replayable flowable"() {
        setup:
        // Write default data to a file
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)
        fos.write(defaultData.array())

        // Mock a response that will always be retried.
        def mockHttpResponse = Mock(HttpResponse) {
            statusCode() >> 500
            bodyAsString() >> Single.just("")
        }

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        def mockPolicy = Mock(RequestPolicy) {
            sendAsync(_) >> { HttpRequest request ->
                if (!(FlowableUtil.collectBytesInBuffer(request.body()).blockingGet() == defaultData)) {
                    throw new IllegalArgumentException()
                }
                return Single.just(mockHttpResponse)
            }
        }

        // Mock a factory that always returns our mock policy.
        def mockFactory = Mock(RequestPolicyFactory) {
            create(*_) >> mockPolicy
        }

        // Build the pipeline
        def testPipeline = HttpPipeline.build(new RequestRetryFactory(new RequestRetryOptions(null, 3, null, null, null,
                null)), mockFactory)
        bu = bu.withPipeline(testPipeline)
        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        TransferManager.uploadFileToBlockBlob(channel, bu, 50, null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.statusCode() == 500

        cleanup:
        channel.close()
    }

    def "Upload single shot size"() {
        setup:
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(getRandomFile(defaultDataSize).toPath())

        when:
        def response = TransferManager.uploadFileToBlockBlob(channel, bu, 50, defaultDataSize - 1, null).blockingGet()

        then:
        // The fact that we did a commit block list on such a small blob indicates that the options were respected.
        response.response() instanceof BlockBlobCommitBlockListResponse
    }

    def "Upload single shot size fail"() {
        when:
        TransferManager.uploadFileToBlockBlob(AsynchronousFileChannel.open(getRandomFile(0).toPath()), bu, 50,
                maxSingleShot, null)

        then:
        thrown(IllegalArgumentException)

        where:
        maxSingleShot                          | _
        -1                                     | _
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1 | _
    }

    @Unroll
    def "Upload options fail"() {
        when:
        new TransferManagerUploadToBlockBlobOptions(null, null, null, null, -1)

        then:
        thrown(IllegalArgumentException)
    }

    /*
    Here we're testing that progress is properly added to a single upload. The size of the file must be less than
    the max upload value.
     */

    def "Upload file progress sequential"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES - 1).toPath())
        def mockReceiver = Mock(IProgressReceiver)
        def prevCount = 0

        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadFileToBlockBlob(channel,
                bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
                new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()

        then:
        /*
        The best we can do here is to check that the total is reported at the end. It is unclear how many ByteBuffers
        will be needed to break up the file, so we can't check intermediary values.
         */
        1 * mockReceiver.reportProgress(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES - 1)

        /*
        We may receive any number of intermediary calls depending on the implementation. For any of these notifications,
        we assert that they are strictly increasing.
         */
        _ * mockReceiver.reportProgress(!channel.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred > prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        0 * mockReceiver.reportProgress({ it > BlockBlobURL.MAX_UPLOAD_BLOB_BYTES - 1 })

        cleanup:
        channel.close()
    }

    def "Upload file progress parallel"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1).toPath())
        def numBlocks = channel.size() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
        long prevCount = 0
        def mockReceiver = Mock(IProgressReceiver)


        when:
        TransferManager.uploadFileToBlockBlob(channel,
                bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
                new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(channel.size())

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!channel.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred > prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > channel.size() })
        notThrown(IllegalArgumentException)

        cleanup:
        channel.close()
    }

    @Unroll
    def "Download file"() {
        setup:
        def channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        def headers = TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManagerDownloadFromBlobOptions(4 * 1024 * 1024L, null, null, null, null)).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)
        headers.blobType() == BlobType.BLOCK_BLOB

        cleanup:
        channel.close() == null
        outChannel.close() == null

        where:
        file                                | _
        getRandomFile(0)                    | _ // empty file
        getRandomFile(20)                   | _ // small file
        getRandomFile(16 * 1024 * 1024)     | _ // medium file in several chunks
        getRandomFile(8 * 1026 * 1024 + 10) | _ // medium file not aligned to block
        // Files larger than 2GB to test no integer overflow are left to stress/perf tests to keep test passes short.
    }

    def compareFiles(AsynchronousFileChannel channel1, long offset, long count, AsynchronousFileChannel channel2) {
        int chunkSize = 8 * 1024 * 1024
        long pos = 0

        while (pos < count) {
            chunkSize = Math.min(chunkSize, count - pos)
            def buf1 = FlowableUtil.collectBytesInBuffer(FlowableUtil.readFile(channel1, offset + pos, chunkSize))
                    .blockingGet()
            def buf2 = FlowableUtil.collectBytesInBuffer(FlowableUtil.readFile(channel2, pos, chunkSize)).blockingGet()

            buf1.position(0)
            buf2.position(0)

            if (buf1.compareTo(buf2) != 0) {
                return false
            }

            pos += chunkSize
        }
        if (pos != count && pos != channel2.size()) {
            return false
        }
        return true
    }

    @Unroll
    def "Download file range"() {
        setup:
        def channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null).blockingGet()
        File outFile = getRandomFile(0)
        def outChannel = AsynchronousFileChannel.open(outFile.toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, range, null).blockingGet()

        then:
        compareFiles(channel, range.offset(), range.count(), outChannel)

        cleanup:
        channel.close()
        outChannel.close()

        /*
        The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
        send off parallel requests with invalid ranges.
         */
        where:
        file                           | range
        getRandomFile(defaultDataSize) | new BlobRange().withCount(defaultDataSize) // Exact count
        getRandomFile(defaultDataSize) | new BlobRange().withOffset(1).withCount(defaultDataSize - 1) // Offset and exact count
        getRandomFile(defaultDataSize) | new BlobRange().withOffset(3).withCount(2) // Narrow range in middle
        getRandomFile(defaultDataSize) | new BlobRange().withCount(defaultDataSize - 1) // Count that is less than total
        getRandomFile(defaultDataSize) | new BlobRange().withCount(10L * 1024 * 1024 * 1024) // Count much larger than remaining data
    }

    /*
    This is to exercise some additional corner cases and ensure there are no arithmetic errors that give false success.
     */

    @Unroll
    def "Download file range fail"() {
        setup:
        def channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null).blockingGet()
        File outFile = getRandomFile(0)
        def outChannel = AsynchronousFileChannel.open(outFile.toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, new BlobRange().withOffset(offset).withCount(count), null)
                .blockingGet()

        then:
        thrown(StorageException)

        cleanup:
        channel.close()
        outChannel.close()

        where:
        file                           | offset              | count
        getRandomFile(defaultDataSize) | defaultDataSize + 1 | null
    }

    def "Download file count null"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        File outFile = getRandomFile(0)
        def outChannel = AsynchronousFileChannel.open(outFile.toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, new BlobRange(), null)
                .blockingGet()

        then:
        compareDataToFile(defaultFlowable, outFile)

        cleanup:
        outChannel.close()
    }

    @Unroll
    def "Download file AC"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null, new TransferManagerDownloadFromBlobOptions(
                null, null, bac, null, null)).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)

        cleanup:
        channel.close()
        outChannel.close()

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
    def "Download file AC fail"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManagerDownloadFromBlobOptions(null, null, bac, null, null)).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Download file etag lock"() {
        setup:
        bu.upload(Flowable.just(getRandomData(1 * 1024 * 1024)), 1 * 1024 * 1024, null, null,
                null, null).blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        /*
         Set up a large download in small chunks so it makes a lot of requests. This will give us time to cut in an
         operation that will change the etag.
         */
        def success = false
        TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManagerDownloadFromBlobOptions(1024, null, null, null, null))
                .subscribe(
                new Consumer<BlobDownloadHeaders>() {
                    @Override
                    void accept(BlobDownloadHeaders headers) throws Exception {
                        success = false
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof StorageException &&
                                ((StorageException) throwable).statusCode() == 412) {
                            success = true
                            return
                        }
                        success = false
                    }
                })


        sleep(500) // Give some time for the download request to start.
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()

        sleep(1000) // Allow time for the upload operation

        then:
        success

        cleanup:
        outChannel.close()
    }

    @Unroll
    def "Download file options"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)
        def reliableDownloadOptions = new ReliableDownloadOptions()
        reliableDownloadOptions.withMaxRetryRequests(retries)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null, new TransferManagerDownloadFromBlobOptions(
                blockSize, null, null, reliableDownloadOptions, parallelism)).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)

        cleanup:
        channel.close()
        outChannel.close()

        where:
        blockSize | parallelism | retries
        1         | null        | 2
        null      | 1           | 2
        null      | null        | 1
    }

    @Unroll
    def "Download file progress receiver"() {
        def channel = AsynchronousFileChannel.open(getRandomFile(fileSize).toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        def mockReceiver = Mock(IProgressReceiver)

        def numBlocks = fileSize / TransferManager.BLOB_DEFAULT_DOWNLOAD_BLOCK_SIZE
        def prevCount = 0

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManagerDownloadFromBlobOptions(null, mockReceiver, null,
                        new ReliableDownloadOptions().withMaxRetryRequests(3), 20)).blockingGet()

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(fileSize)

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!channel.size()) >> { long bytesTransferred ->
            if (!(bytesTransferred > prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > fileSize })

        cleanup:
        channel.close()

        where:
        fileSize             | _
        100                  | _
        8 * 1026 * 1024 + 10 | _
    }

    @Unroll
    def "Download file IA null"() {
        when:
        TransferManager.downloadBlobToFile(file, blobURL, null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        /*
        This test is just validating that exceptions are thrown if certain values are null. The values not being test do
        not need to be correct, simply not null. Because order in which Spock initializes values, we can't just use the
        bu property for the url.
         */
        where:
        file                                                     | blobURL
        null                                                     | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        AsynchronousFileChannel.open(getRandomFile(10).toPath()) | null
    }

    @Unroll
    def "Upload NRF"() {
        when:
        def data = getRandomData(dataSize)
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, bufferSize, numBuffs, null)
                .blockingGet()
        data.position(0)

        then:
        //Due to memory issues, this check only runs on small to medium sized files.
        if(dataSize < 100 * 1024 * 1024){
            FlowableUtil.collectBytesInBuffer(bu.download().blockingGet().body(null)).blockingGet() == data
        }
        bu.getBlockList(BlockListType.ALL).blockingGet().body().committedBlocks().size() == blockCount

        where:
        dataSize          | bufferSize        | numBuffs || blockCount
        350               | 50                | 2        || 7
        350               | 50                | 5        || 7
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 2        || 10
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 5        || 10
        10 * 1024 * 1024  | 1 * 1024 * 1024   | 10       || 10
        // TODO 500 * 1024 * 1024 | 100 * 1024 * 1024 | 2        || 5
        100 * 1024 * 1024 | 20 * 1024 * 1024  | 4        || 5
        10 * 1024 * 1024  | 3 * 512 * 1024    | 3        || 7
    }

    def compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0)
        for (ByteBuffer buffer : buffers) {
            buffer.position(0)
            result.limit(result.position() + buffer.remaining())
            if (buffer != result) {
                return false
            }
            result.position(result.position() + buffer.remaining())
        }
        return result.remaining() == 0
    }

    @Unroll
    def "Upload NRF chunked source"() {
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        setup:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.fromIterable(dataList), bu, bufferSize, numBuffers,
                null).blockingGet()

        expect:
        compareListToBuffer(dataList, FlowableUtil.collectBytesInBuffer(bu.download().blockingGet().body(null))
                .blockingGet())
        bu.getBlockList(BlockListType.ALL).blockingGet().body().committedBlocks().size() == blockCount

        where:
        dataList                                                                                                                       | bufferSize | numBuffers || blockCount
        [getRandomData(7), getRandomData(7)]                                                                                           | 10         | 2          || 2
        [getRandomData(3), getRandomData(3), getRandomData(3), getRandomData(3), getRandomData(3), getRandomData(3), getRandomData(3)] | 10         | 2          || 3
        [getRandomData(10), getRandomData(10)]                                                                                         | 10         | 2          || 2
        [getRandomData(50), getRandomData(51), getRandomData(49)]                                                                      | 10         | 2          || 15
        // The case of one large buffer needing to be broken up is tested in the previous test.
    }

    @Unroll
    def "Upload NRF illegal arguments null"() {
        when:
        TransferManager.uploadFromNonReplayableFlowable(source, url, 4, 4, null)

        then:
        thrown(IllegalArgumentException)

        where:
        source                     | url
        null                       | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds))
        Flowable.just(defaultData) | null
    }

    @Unroll
    def "Upload NRF illegal args out of bounds"() {
        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(defaultData), bu, bufferSize, numBuffs, null)

        then:
        thrown(IllegalArgumentException)

        where:
        bufferSize                             | numBuffs
        0                                      | 5
        BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 1 | 5
        5                                      | 1
    }

    @Unroll
    def "Upload NRF headers"() {
        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(defaultData), bu, 10, 2,
                new TransferManagerUploadToBlockBlobOptions(null, new BlobHTTPHeaders()
                        .withBlobCacheControl(cacheControl).withBlobContentDisposition(contentDisposition)
                        .withBlobContentEncoding(contentEncoding).withBlobContentLanguage(contentLanguage)
                        .withBlobContentMD5(contentMD5).withBlobContentType(contentType), null, null, null))
                .blockingGet()

        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()
        defaultData.position(0)

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // HTTP default content type is application/octet-stream.

        where:
        // The MD5 is simply set on the blob for commitBlockList, not validated.
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload NRF metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(getRandomData(10)), bu, 10, 10,
                new TransferManagerUploadToBlockBlobOptions(null, null, metadata, null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null, null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Upload NRF AC"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        expect:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(getRandomData(10)), bu, 10, 2,
                new TransferManagerUploadToBlockBlobOptions(null, null, null, bac, null))
                .blockingGet().statusCode() == 201

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
    def "Upload NRF AC fail"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null, null).blockingGet()
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions().withModifiedAccessConditions(
                new ModifiedAccessConditions().withIfModifiedSince(modified).withIfUnmodifiedSince(unmodified)
                        .withIfMatch(match).withIfNoneMatch(noneMatch))
                .withLeaseAccessConditions(new LeaseAccessConditions().withLeaseId(leaseID))

        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(getRandomData(10)), bu, 10, 2,
                new TransferManagerUploadToBlockBlobOptions(null, null, null, bac, null))
                .blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Upload NRF progress"() {
        setup:
        def data = getRandomData(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
        def numBlocks = data.remaining() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
        long prevCount = 0
        def mockReceiver = Mock(IProgressReceiver)


        when:
        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, 10,
                new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()
        data.position(0)

        then:
        // We should receive exactly one notification of the completed progress.
        1 * mockReceiver.reportProgress(data.remaining())

        /*
        We should receive at least one notification reporting an intermediary value per block, but possibly more
        notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
        will be the total size as above. Finally, we assert that the number reported monotonically increases.
         */
        (numBlocks - 1.._) * mockReceiver.reportProgress(!data.remaining()) >> { long bytesTransferred ->
            if (!(bytesTransferred > prevCount)) {
                throw new IllegalArgumentException("Reported progress should monotonically increase")
            } else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({ it > data.remaining() })
        notThrown(IllegalArgumentException)
    }

    def "Upload NRF network error"() {
        setup:
        /*
         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
         buffering properly to allow for retries even given this source behavior.
         */
        bu.upload(Flowable.just(defaultData), defaultDataSize).blockingGet()
        def nrf = bu.download().blockingGet().body(null)

        // Mock a response that will always be retried.
        def mockHttpResponse = Mock(HttpResponse) {
            statusCode() >> 500
            bodyAsString() >> Single.just("")
        }

        // Mock a policy that will always then check that the data is still the same and return a retryable error.
        def mockPolicy = Mock(RequestPolicy) {
            sendAsync(_) >> { HttpRequest request ->
                if (!(FlowableUtil.collectBytesInBuffer(request.body()).blockingGet() == defaultData)) {
                    throw new IllegalArgumentException()
                }
                return Single.just(mockHttpResponse)
            }
        }

        // Mock a factory that always returns our mock policy.
        def mockFactory = Mock(RequestPolicyFactory) {
            create(*_) >> mockPolicy
        }

        // Build the pipeline
        def testPipeline = HttpPipeline.build(new RequestRetryFactory(new RequestRetryOptions(null, 3, null, null, null,
                null)), mockFactory)
        bu = bu.withPipeline(testPipeline)

        when:
        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
        TransferManager.uploadFromNonReplayableFlowable(nrf, bu, 1024, 4, null).blockingGet()

        then:
        // A second subscription to a download stream will
        def e = thrown(StorageException)
        e.statusCode() == 500
    }

    // Test that, when we are downloading a small file, we only make a single Download call and no HEAD request
    def "Download file single shot optimization"() {
        setup:
        /*
        A spy allows us to put constraints on method invoations for a real object instead of a mock object.
         */
        def spyBu = Spy(BlobURL, constructorArgs: [bu.toURL(), StorageURL.createPipeline(primaryCreds)])

        when:
        TransferManager.downloadBlobToFile(AsynchronousFileChannel.open(getRandomFile(0).toPath()), spyBu,
                new BlobRange().withOffset(0).withCount(5L), null)

        then:
        0 * spyBu.getProperties(*_)
        1 * spyBu.download(*_)
    }

    @Unroll
    def "Download options fail"() {
        when:
        new TransferManagerDownloadFromBlobOptions(blockSize, null, null, null, parallelism
        )

        then:
        thrown(IllegalArgumentException)

        where:
        parallelism | blockSize
        0           | 40
        2           | 0
    }
}
