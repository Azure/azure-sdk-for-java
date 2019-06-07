package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.*
import org.junit.Assume
import spock.lang.Unroll

import java.nio.channels.AsynchronousFileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

class TransferManagerLargeFileTest extends APISpec {
    BlockBlobURL bu

    def setupSpec() {
        Assume.assumeTrue("This test class only run in live mode.", testMode == null)
    }

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())

        /*
        We just print something out in between each test to keep Travis from being idle for too long. The tests seem
        to run slower on Travis, and without this keep-alive, it may exceed the 10 minutes of no output and error the
        CI build.
         */
        System.out.println("Starting large file test")
    }

    //Multi part
    def "Upload file"() {
        setup:
        def file = getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
        def channel = AsynchronousFileChannel.open(file.toPath())

        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadFileToBlockBlob(channel,
            bu, (int) (BlockBlobURL.MAX_STAGE_BLOCK_BYTES / 10), null,
            new TransferManagerUploadToBlockBlobOptions(null, null, null, null, 20)).blockingGet()

        then:
        BlockBlobCommitBlockListResponse.isInstance(response.response()) // Ensure we did the correct type of operation.
        validateBasicHeaders(response)
        compareDataToFile(bu.download(new BlobRange().withCount(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1), null, false, null)
            .blockingGet().body(null), file)

        cleanup:
        channel.close()
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
        BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 1 | BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 10 // Block size too big.
        10                                     | BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 // Too many blocks.
    }

    @Unroll
    def "Upload file headers"() {
        setup:
        // We have to use the defaultData here so we can calculate the MD5 on the uploadBlob case.
        File file = File.createTempFile("testUpload", ".txt")
        file.deleteOnExit()
        file = getRandomFile(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10)

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
            contentMD5, contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present.
        // HTTP default content type is application/octet-stream.

        cleanup:
        channel.close()

        where:
        // The MD5 is simply set on the blob for commitBlockList, not validated.
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
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
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | newDate  | null       | null        | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | oldDate    | null        | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | garbageEtag | null         | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null        | receivedEtag | null
        BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 10 | null     | null       | null        | null         | garbageLeaseID
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

    def "Download file medium file in several chunks"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(16 * 1024 * 1024).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
            .blockingGet()
        when:
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
            StandardOpenOption.READ)
        def headers = TransferManager.downloadBlobToFile(outChannel, bu, null,
            new TransferManagerDownloadFromBlobOptions(4 * 1024 * 1024L, null, null, null, null)).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)
        headers.blobType() == BlobType.BLOCK_BLOB

        cleanup:
        channel.close() == null
        outChannel.close() == null
    }

    def "Download file progress receiver"() {
        def fileSize = 8 * 1026 * 1024 + 10
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
    }


    def "Download file small medium file not aligned to block"() {
        setup:
        def channel = AsynchronousFileChannel.open(getRandomFile(8 * 1026 * 1024 + 10).toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null)
            .blockingGet()
        when:
        def outChannel = AsynchronousFileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
            StandardOpenOption.READ)
        def headers = TransferManager.downloadBlobToFile(outChannel, bu, null,
            new TransferManagerDownloadFromBlobOptions(4 * 1024 * 1024L, null, null, null, null)).blockingGet()

        then:
        compareFiles(channel, 0, channel.size(), outChannel)
        headers.blobType() == BlobType.BLOCK_BLOB

        cleanup:
        channel.close() == null
        outChannel.close() == null

    }

}
