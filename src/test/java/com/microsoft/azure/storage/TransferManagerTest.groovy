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
        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadFileToBlockBlob(channel,
                bu, (int) (BlockBlobURL.MAX_STAGE_BLOCK_BYTES / 10),
                new TransferManagerUploadToBlockBlobOptions(null, null, null, null, 20)).blockingGet()

        then:
        responseType.isInstance(response.response()) // Ensure we did the correct type of operation.
        validateBasicHeaders(response)
        compareDataToFile(bu.download(null, null, false, null).blockingGet().body(null), file)

        cleanup:
        channel.close()

        where:
        file                                                  || responseType
        getRandomFile(10)                                     || BlockBlobUploadResponse // Single shot
        getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1) || BlockBlobCommitBlockListResponse // Multi part
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
        TransferManager.uploadFileToBlockBlob(file, url, 5, null).blockingGet()

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
        TransferManager.uploadFileToBlockBlob(channel, bu,
                blockLength, null).blockingGet()

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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
                new TransferManagerUploadToBlockBlobOptions(null, new BlobHTTPHeaders()
                        .withBlobCacheControl(cacheControl).withBlobContentDisposition(contentDisposition)
                        .withBlobContentEncoding(contentEncoding).withBlobContentLanguage(contentLanguage)
                        .withBlobContentMD5(contentMD5).withBlobContentType(contentType), null, null, null)).blockingGet()

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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
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
        TransferManager.uploadFileToBlockBlob(channel, bu, 50, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.statusCode() == 500

        cleanup:
        channel.close()
    }

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
                bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
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
            }
            else {
                prevCount = bytesTransferred
            }
        }

        0 * mockReceiver.reportProgress({it > BlockBlobURL.MAX_UPLOAD_BLOB_BYTES - 1})

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
                bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES,
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
            }
            else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({it > channel.size()})
        notThrown(IllegalArgumentException)

        cleanup:
        channel.close()
    }

    @Unroll
    def "Download file"() {
        setup:
        def channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        def headers = TransferManager.downloadBlobToFile(outChannel, bu, null, null).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)
        headers.blobType() == BlobType.BLOCK_BLOB

        cleanup:
        channel.close() == null
        outChannel.close() == null

        where:
        file                                   | _
        getRandomFile(20)                      | _ // small file
        getRandomFile(16 * 1024 * 1024)        | _ // medium file in several chunks
        getRandomFile(8L * 1026 * 1024 + 10)   | _ // medium file not aligned to block
        getRandomFile(0)                       | _ // empty file
        getRandomFile(5L * 1024 * 1024 * 1024) | _ // file size exceeds max int
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
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

        where:
        file                           | range                                                        | dataSize
        getRandomFile(defaultDataSize) | new BlobRange().withCount(defaultDataSize)                   | defaultDataSize
        getRandomFile(defaultDataSize) | new BlobRange().withOffset(1).withCount(defaultDataSize - 1) | defaultDataSize - 1
        getRandomFile(defaultDataSize) | new BlobRange().withCount(defaultDataSize - 1)               | defaultDataSize - 1
        getRandomFile(defaultDataSize) | new BlobRange().withCount(10L * 1024 * 1024 * 1024)          | defaultDataSize
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
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
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
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

    def "Download options progress receiver"() {
        def fileSize = 8L * 1026 * 1024 + 10
        def channel = AsynchronousFileChannel.open(getRandomFile(fileSize).toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
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
            }
            else {
                prevCount = bytesTransferred
            }
        }

        // We should receive no notifications that report more progress than the size of the file.
        0 * mockReceiver.reportProgress({it > fileSize})

        cleanup:
        channel.close()
    }
}

