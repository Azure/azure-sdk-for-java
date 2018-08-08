package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.*
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlockBlobCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobUploadResponse
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

class TransferManagerTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
    }

    def "Upload buffers single shot"() {
        setup:
        List<ByteBuffer> buffers = new ArrayList<>()
        buffers.add(defaultData)

        when:
        CommonRestResponse response = TransferManager.uploadByteBuffersToBlockBlob(buffers, bu,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, null)).blockingGet()

        then:
        response.response() instanceof BlockBlobUploadResponse // Ensure we did a single put for a small blob.
        validateBasicHeaders(response)
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false)
                .blockingGet().body()).blockingGet() == defaultData
    }

    def "Upload buffers parallel"() {
        setup:
        List<ByteBuffer> buffers = getListForMultiPartUpload()

        when:
        CommonRestResponse response = TransferManager.uploadByteBuffersToBlockBlob(buffers, bu,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, 20)).blockingGet()

        then:
        // Ensure we did a commitBlockList for large blobs.
        response.response() instanceof BlockBlobCommitBlockListResponse
        validateBasicHeaders(response)
        compareBufferListToFlowable(buffers, bu.download(null, null, false)
                .blockingGet().body())
    }

    def getListForMultiPartUpload() {
        int blockSize = 10 * 1024 * 1024
        List<ByteBuffer> buffers = new ArrayList<>()
        for (int i = 0; i <= BlockBlobURL.MAX_PUT_BLOB_BYTES; i += blockSize) {
            buffers.add(getRandomData(blockSize))
        }
        return buffers
    }

    def compareBufferListToFlowable(List<ByteBuffer> buffers, Flowable<ByteBuffer> flowable) {
        int count = 0
        for (ByteBuffer buffer : buffers) {
            count += buffer.remaining()
        }
        ByteBuffer largeBuffer = ByteBuffer.allocate(count)
        for (ByteBuffer buffer : buffers) {
            largeBuffer.put(buffer)
        }
        largeBuffer.position(0)
        return FlowableUtil.collectBytesInBuffer(flowable).blockingGet().compareTo(largeBuffer) == 0
    }

    @Unroll
    def "Upload buffers illegal arguments null"() {
        when:
        TransferManager.uploadByteBuffersToBlockBlob(data, blobURL, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        /*
        This test is just validating that exceptions are thrown if certain values are null. The values not being test do
        not need to be correct, simply not null. Because of the order in which Spock initializes values, we can't just
        use the bu property for the url.
         */
        where:
        data                       | blobURL
        null                       | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        Arrays.asList(defaultData) | null
    }

    def "Upload buffers illegal argument max blocks"() {
        setup:
        List<ByteBuffer> data = new ArrayList<>()
        // We also have to make sure it's still larger than the max put blob size.
        int blockSize = (int) ((BlockBlobURL.MAX_PUT_BLOB_BYTES / BlockBlobURL.MAX_BLOCKS) + 10)
        for (int i = 0; i <= BlockBlobURL.MAX_BLOCKS; i++) {
            data.add(getRandomData(blockSize))
        }
        data.add(getRandomData(blockSize)) // Add one more to exceed the limit.

        when:
        TransferManager.uploadByteBuffersToBlockBlob(data, bu,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, null)).blockingGet()

        then:
        thrown(IllegalArgumentException)
    }

    def "Upload buffers illegal argument max block size"() {
        when:
        /*
        We use max put blob bytes to force the TransferManager to try to upload in blocks. Since max put blob size is
        greater than max put block size, this will also fail the desired check.
         */
        TransferManager.uploadByteBuffersToBlockBlob(Arrays.asList(getRandomData(BlockBlobURL.MAX_PUT_BLOB_BYTES + 1)),
                bu, new TransferManager.UploadToBlockBlobOptions(null, null, null,
                null, null)).blockingGet()

        then:
        thrown(IllegalArgumentException)
    }

    // The following tests have to be sure to hit the paths for single-shot and multi-part upload.
    @Unroll
    def "Upload buffers headers"() {
        when:
        TransferManager.uploadByteBuffersToBlockBlob(data, bu,
                new TransferManager.UploadToBlockBlobOptions(null, new BlobHTTPHeaders(cacheControl,
                        contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType), null,
                        null, null)).blockingGet()

        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                data.size() == 1 && contentMD5 == null ?
                        MessageDigest.getInstance("MD5").digest(defaultData.array()) : contentMD5,
                contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present
        // HTTP default content type is application/octet-stream

        where:
        data                        | cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        Arrays.asList(defaultData)  | null         | null               | null            | null            | null                                                         | null
        Arrays.asList(defaultData)  | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
        getListForMultiPartUpload() | null         | null               | null            | null            | null                                                         | null
        getListForMultiPartUpload() | "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload buffers metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        TransferManager.uploadByteBuffersToBlockBlob(data, bu,
                new TransferManager.UploadToBlockBlobOptions(null, null, metadata,
                        null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        data                        | key1  | value1 | key2   | value2
        Arrays.asList(defaultData)  | null  | null   | null   | null
        Arrays.asList(defaultData)  | "foo" | "bar"  | "fizz" | "buzz"
        getListForMultiPartUpload() | null  | null   | null   | null
        getListForMultiPartUpload() | "foo" | "bar"  | "fizz" | "buzz"
    }
    /*
    We don't have to test invalid metadata because the previous test demonstrates that the metadata is passed to the
    convenience layer correctly, and other tests validate the convenience layer's handling of metadata. We have to test
    failures for access conditions in both paths because it's not clear from successes alone that they are passed
    correctly.
     */

    @Unroll
    def "Upload buffers AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        TransferManager.uploadByteBuffersToBlockBlob(data, bu, new TransferManager.UploadToBlockBlobOptions(
                null, null, null, bac, null))
                .blockingGet().statusCode() == 201

        where:
        data                        | modified | unmodified | match        | noneMatch   | leaseID
        Arrays.asList(defaultData)  | null     | null       | null         | null        | null
        Arrays.asList(defaultData)  | oldDate  | null       | null         | null        | null
        Arrays.asList(defaultData)  | null     | newDate    | null         | null        | null
        Arrays.asList(defaultData)  | null     | null       | receivedEtag | null        | null
        Arrays.asList(defaultData)  | null     | null       | null         | garbageEtag | null
        Arrays.asList(defaultData)  | null     | null       | null         | null        | receivedLeaseID
        getListForMultiPartUpload() | null     | null       | null         | null        | null
        getListForMultiPartUpload() | oldDate  | null       | null         | null        | null
        getListForMultiPartUpload() | null     | newDate    | null         | null        | null
        getListForMultiPartUpload() | null     | null       | receivedEtag | null        | null
        getListForMultiPartUpload() | null     | null       | null         | garbageEtag | null
        getListForMultiPartUpload() | null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Upload buffers AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        TransferManager.uploadByteBuffersToBlockBlob(data, bu, new TransferManager.UploadToBlockBlobOptions(
                null, null, null, bac, null))
                .blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                e.errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        data                        | modified | unmodified | match       | noneMatch    | leaseID
        Arrays.asList(defaultData)  | newDate  | null       | null        | null         | null
        Arrays.asList(defaultData)  | null     | oldDate    | null        | null         | null
        Arrays.asList(defaultData)  | null     | null       | garbageEtag | null         | null
        Arrays.asList(defaultData)  | null     | null       | null        | receivedEtag | null
        Arrays.asList(defaultData)  | null     | null       | null        | null         | garbageLeaseID
        getListForMultiPartUpload() | newDate  | null       | null        | null         | null
        getListForMultiPartUpload() | null     | oldDate    | null        | null         | null
        getListForMultiPartUpload() | null     | null       | garbageEtag | null         | null
        getListForMultiPartUpload() | null     | null       | null        | receivedEtag | null
        getListForMultiPartUpload() | null     | null       | null        | null         | garbageLeaseID
    }

    /*
    Because uploadBuffer and uploadFile call into uploadBuffers, we don't have to validate both the single-shot and
    multi-part scenarios other than validating the correctness of the data (to ensure it is split up correctly). In the
    cases of metadata, etc. validating one scenario is enough. If the implementation ever changes, we will have to add
    those tests.
     */

    @Unroll
    def "Upload buffer"() {
        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadByteBufferToBlockBlob(data, bu,
                (int) (BlockBlobURL.MAX_STAGE_BLOCK_BYTES / 10),
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, 20)).blockingGet()

        then:
        responseType.isInstance(response.response()) // Ensure we did the correct type of operation.
        validateBasicHeaders(response)
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false)
                .blockingGet().body()).blockingGet() == data

        where:
        data                                               | responseType
        defaultData                                        | BlockBlobUploadResponse
        getRandomData(BlockBlobURL.MAX_PUT_BLOB_BYTES + 1) | BlockBlobCommitBlockListResponse
    }

    @Unroll
    def "Upload buffer illegal arguments null"() {
        when:
        TransferManager.uploadByteBufferToBlockBlob(data, url, 5, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        data        | url
        null        | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        defaultData | null
    }

    @Unroll
    def "Upload buffer illegal arguments block length"() {
        when:
        TransferManager.uploadByteBufferToBlockBlob(defaultData, bu, blockLength, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        blockLength                            | _
        -1                                     | _
        BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 1 | _
    }

    @Unroll
    def "Upload buffer headers"() {
        when:
        TransferManager.uploadByteBufferToBlockBlob(defaultData, bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, new BlobHTTPHeaders(cacheControl,
                        contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType), null,
                        null, null)).blockingGet()

        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                MessageDigest.getInstance("MD5").digest(defaultData.array()),
                contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present
        // HTTP default content type is application/octet-stream

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                   | contentType
        null         | null               | null            | null            | null                                                         | null
        "control"    | "disposition"      | "encoding"      | "language"      | MessageDigest.getInstance("MD5").digest(defaultData.array()) | "type"
    }

    @Unroll
    def "Upload buffer metadata"() {
        setup:
        Metadata metadata = new Metadata()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }

        when:
        TransferManager.uploadByteBufferToBlockBlob(defaultData, bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, null, metadata,
                        null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Upload buffer AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        TransferManager.uploadByteBufferToBlockBlob(defaultData, bu, 5, new TransferManager.UploadToBlockBlobOptions(
                null, null, null, bac, null))
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
    def "Upload buffer AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        TransferManager.uploadByteBufferToBlockBlob(defaultData, bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        bac, null))
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

    @Unroll
    def "Upload file"() {
        when:
        // Block length will be ignored for single shot.
        CommonRestResponse response = TransferManager.uploadFileToBlockBlob(FileChannel.open(file.toPath()), bu,
                (int) (BlockBlobURL.MAX_STAGE_BLOCK_BYTES / 10),
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, 20)).blockingGet()

        then:
        responseType.isInstance(response.response()) // Ensure we did the correct type of operation.
        validateBasicHeaders(response)
        compareDataToFile(bu.download(null, null, false).blockingGet().body(),
                file)

        where:
        file                                               | responseType
        getRandomFile(10)                                  | BlockBlobUploadResponse
        getRandomFile(BlockBlobURL.MAX_PUT_BLOB_BYTES + 1) | BlockBlobCommitBlockListResponse
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
        file                                         | url
        null                                         | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        FileChannel.open(getRandomFile(10).toPath()) | null
    }

    @Unroll
    def "Upload file illegal arguments block length"() {
        when:
        TransferManager.uploadFileToBlockBlob(FileChannel.open(getRandomFile(10).toPath()), bu, blockLength,
                null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        where:
        blockLength                            | _
        -1                                     | _
        BlockBlobURL.MAX_STAGE_BLOCK_BYTES + 1 | _
    }

    @Unroll
    def "Upload file headers"() {
        setup:
        // We have to use the defaultData here so we can calculate the MD5. 
        File file = File.createTempFile("testUpload", ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)
        fos.write(defaultData.array())
        fos.close()

        when:
        TransferManager.uploadFileToBlockBlob(FileChannel.open(file.toPath()), bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, new BlobHTTPHeaders(cacheControl,
                        contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType), null,
                        null, null)).blockingGet()

        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        validateBlobHeaders(response.headers(), cacheControl, contentDisposition, contentEncoding, contentLanguage,
                MessageDigest.getInstance("MD5").digest(defaultData.array()),
                contentType == null ? "application/octet-stream" : contentType)
        // For uploading a block blob single-shot, the service will auto calculate an MD5 hash if not present
        // HTTP default content type is application/octet-stream

        where:
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

        when:
        TransferManager.uploadFileToBlockBlob(FileChannel.open(getRandomFile(10).toPath()), bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, null, metadata,
                        null, null)).blockingGet()
        BlobGetPropertiesResponse response = bu.getProperties(null).blockingGet()

        then:
        response.statusCode() == 200
        response.headers().metadata() == metadata

        where:
        key1  | value1 | key2   | value2
        null  | null   | null   | null
        "foo" | "bar"  | "fizz" | "buzz"
    }

    @Unroll
    def "Upload file AC"() {
        setup:
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        expect:
        TransferManager.uploadFileToBlockBlob(FileChannel.open(getRandomFile(10).toPath()), bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, null, null, bac,
                        null))
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
    def "Upload file AC fail"() {
        setup:
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        TransferManager.uploadFileToBlockBlob(FileChannel.open(getRandomFile(10).toPath()), bu, 5,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        bac, null))
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

    def "Upload options fail"() {
        when:
        new TransferManager.UploadToBlockBlobOptions(null, null, null,
                null, -1)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Download buffer"() {
        setup:
        bu.upload(Flowable.just(data), size, null, null, null).blockingGet()
        def outBuf = ByteBuffer.allocate(size.intValue())

        when:
        TransferManager.downloadBlobToBuffer(outBuf, bu, null, null).blockingGet()
        outBuf.position(0)

        then:
        outBuf.compareTo(data) == 0

        where:
        data                                | size
        defaultData                         | defaultDataSize // Small upload
        getRandomData(16 * 1024 * 1024)     | 16 * 1024 * 1024 // Large upload in several chunks
        getRandomData(8 * 1026 * 1024 + 10) | 8 * 1026 * 1024 + 10 // Medium upload not aligned on block boundary
    }

    @Unroll
    def "Download buffer range"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        ByteBuffer buffer = ByteBuffer.allocate((int) range.count)

        when:
        TransferManager.downloadBlobToBuffer(buffer, bu, range, null).blockingGet()
        buffer.position(0)

        then:
        buffer.compareTo(
                (ByteBuffer) defaultData.duplicate()
                        .position((int) range.offset).limit((int) (range.offset + range.count))) == 0

        where:
        range                                 | _
        new BlobRange(0, defaultDataSize)     | _
        new BlobRange(1, defaultDataSize - 1) | _
        new BlobRange(0, defaultDataSize - 1) | _
    }

    def "Download buffer count null"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        def buffer = ByteBuffer.allocate(defaultDataSize)

        when:
        TransferManager.downloadBlobToBuffer(buffer, bu, new BlobRange(0, null),
                null).blockingGet()

        then:
        buffer.compareTo(defaultData) == 0
    }

    @Unroll
    def "Download buffer AC"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        def buffer = ByteBuffer.allocate(defaultDataSize)
        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        TransferManager.downloadBlobToBuffer(buffer, bu, null, new TransferManager.DownloadFromBlobOptions(
                null, null, bac, null, null)).blockingGet()

        then:
        buffer.compareTo(defaultData) == 0

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
    def "Download buffer AC fail"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        def buffer = ByteBuffer.allocate(defaultDataSize)
        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        Throwable t = TransferManager.downloadBlobToBuffer(buffer, bu, null,
                new TransferManager.DownloadFromBlobOptions(null, null, bac, null,
                        null)).blockingGet()

        then:
        t instanceof StorageException
        ((StorageException) t).errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                ((StorageException) t).errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    def "Download buffer etag lock"() {
        setup:
        bu.upload(Flowable.just(getRandomData(1 * 1024 * 1024)), 1 * 1024 * 1024, null, null,
                null).blockingGet()
        def buffer = ByteBuffer.allocate(1 * 1024 * 1024)

        when:
        /*
         Set up a large download in small chunks so it makes a lot of requests. This will give us time to cut in an
         operation that will change the etag.
         */
        def success = false
        TransferManager.downloadBlobToBuffer(buffer, bu, null,
                new TransferManager.DownloadFromBlobOptions(1024, null, null,
                        null, null))
                .subscribe(
                new Action() {
                    @Override
                    void run() throws Exception {
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
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        sleep(1000) // Allow time for the upload operation

        then:
        success
    }

    @Unroll
    def "Download buffer options"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        def buffer = ByteBuffer.allocate(defaultDataSize)
        def retryReaderOptions = new RetryReaderOptions()
        retryReaderOptions.maxRetryRequests = retries

        when:
        TransferManager.downloadBlobToBuffer(buffer, bu, null, new TransferManager.DownloadFromBlobOptions(
                blockSize, null, null, parallelism, retryReaderOptions)).blockingGet()

        then:
        buffer.compareTo(defaultData) == 0

        where:
        blockSize | parallelism | retries
        1         | null        | 2
        null      | 1           | 2
        null      | null        | 1
    }

    @Unroll
    def "Download buffer IA null"() {
        when:
        TransferManager.downloadBlobToBuffer(buffer, blobURL, null, null).blockingGet()

        then:
        thrown(IllegalArgumentException)

        /*
        This test is just validating that exceptions are thrown if certain values are null. The values not being test do
        not need to be correct, simply not null. Because order in which Spock initializes values, we can't just use the
        bu property for the url.
         */
        where:
        buffer                  | blobURL
        null                    | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        ByteBuffer.allocate(10) | null
    }

    def "Download buffer IA buffer size"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        when:
        Throwable t = TransferManager.downloadBlobToBuffer(ByteBuffer.allocate(defaultDataSize - 1), bu, null
                , null).blockingGet()

        then:
        t instanceof IllegalArgumentException
        t.getMessage().contains("remaining size")
    }

    @Unroll
    def "Download file"() {
        setup:
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        FileChannel outChannel = FileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null, null).blockingGet()
        outChannel.position(0)

        then:
        compareFiles(channel, outChannel)
        channel.close() == null
        outChannel.close() == null

        where:
        file                                  | _
        getRandomFile(20)                     | _
        getRandomFile(16 * 1024 * 1024)       | _
        getRandomFile(8L * 1026 * 1024 + 10)   | _
        getRandomFile(5L * 1024 * 1024 * 1024) | _
        getRandomFile(0)                      | _
    }

    def compareFiles(FileChannel channel1, FileChannel channel2) {
        ByteBuffer buf1 = ByteBuffer.allocate(8 * 1024 * 1024)
        ByteBuffer buf2 = ByteBuffer.allocate(8 * 1024 * 1024)

        long read1 = 0
        long read2 = 0
        while (read1 != -1 && read2 - 1) {
            read1 = channel1.read(buf1)
            read2 = channel2.read(buf2)
            if (buf1.compareTo(buf2) != 0) {
                return false
            }
            if (read1 != read2) {
                return false
            }
            // Reset the positions so we can fill up the buffer again.
            // There will be some left over at the end on the last read, but it should still match.
            buf1.position(0)
            buf2.position(0)
        }
        return true
    }

    @Unroll
    def "Download file range"() {
        setup:
        FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        File outFile = getRandomFile(0)
        FileChannel outChannel = FileChannel.open(outFile.toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, range, null).blockingGet()
        outChannel.position(0)

        then:
        /*
        It would be nice to just reposition and truncate the source file to directly compare the files, but until the
        MappedByteBuffer from downloadToFile is GCed, we can't perform those operations. The best workaround is to
        leverage this other comparison function.
         */
        compareDataToFile(Flowable.just(channel.map(FileChannel.MapMode.READ_ONLY, range.offset, range.count)), outFile)
        channel.close() == null
        outChannel.close() == null

        where:
        file                           | range
        getRandomFile(defaultDataSize) | new BlobRange(0, defaultDataSize)
        getRandomFile(defaultDataSize) | new BlobRange(1, defaultDataSize - 1)
        getRandomFile(defaultDataSize) | new BlobRange(0, defaultDataSize - 1)
        getRandomFile(defaultDataSize) | new BlobRange(0, 10L * 1024 * 1024 * 1024)
    }

    def "Download file count null"() {
        setup:
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()
        File outFile = getRandomFile(0)
        FileChannel outChannel = FileChannel.open(outFile.toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, new BlobRange(0, null), null)
                .blockingGet()
        outChannel.position(0)

        then:
        /*
        It would be nice to just reposition and truncate the source file to directly compare the files, but until the
        MappedByteBuffer from downloadToFile is GCed, we can't perform those operations. The best workaround is to
        leverage this other comparison function.
         */
        compareDataToFile(defaultFlowable, outFile)
        outChannel.close() == null
    }

    @Unroll
    def "Download file AC"() {
        setup:
        FileChannel channel = FileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        FileChannel outChannel = FileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        match = setupBlobMatchCondition(bu, match)
        leaseID = setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null, new TransferManager.DownloadFromBlobOptions(
                null, null, bac, null, null)).blockingGet()
        outChannel.position(0)

        then:
        compareFiles(channel, outChannel)
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
        FileChannel channel = FileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        FileChannel outChannel = FileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        noneMatch = setupBlobMatchCondition(bu, noneMatch)
        setupBlobLeaseCondition(bu, leaseID)
        BlobAccessConditions bac = new BlobAccessConditions(
                new HTTPAccessConditions(modified, unmodified, match, noneMatch), new LeaseAccessConditions(leaseID),
                null, null)

        when:
        def t = TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManager.DownloadFromBlobOptions(null, null, bac, null,
                        null)).blockingGet()

        then:
        t instanceof StorageException
        ((StorageException) t).errorCode() == StorageErrorCode.CONDITION_NOT_MET ||
                ((StorageException) t).errorCode() == StorageErrorCode.LEASE_ID_MISMATCH_WITH_BLOB_OPERATION

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
                null).blockingGet()
        FileChannel outChannel = FileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)

        when:
        /*
         Set up a large download in small chunks so it makes a lot of requests. This will give us time to cut in an
         operation that will change the etag.
         */
        def success = false
        TransferManager.downloadBlobToFile(outChannel, bu, null,
                new TransferManager.DownloadFromBlobOptions(1024, null, null,
                        null, null))
                .subscribe(
                new Action() {
                    @Override
                    void run() throws Exception {
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
        bu.upload(defaultFlowable, defaultDataSize, null, null, null).blockingGet()

        sleep(1000) // Allow time for the upload operation

        then:
        success
        outChannel.close()
    }

    @Unroll
    def "Download file options"() {
        setup:
        FileChannel channel = FileChannel.open(getRandomFile(defaultDataSize).toPath(), StandardOpenOption.READ,
                StandardOpenOption.WRITE)
        TransferManager.uploadFileToBlockBlob(channel, bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null)
                .blockingGet()
        FileChannel outChannel = FileChannel.open(getRandomFile(0).toPath(), StandardOpenOption.WRITE,
                StandardOpenOption.READ)
        def retryReaderOptions = new RetryReaderOptions()
        retryReaderOptions.maxRetryRequests = retries

        when:
        TransferManager.downloadBlobToFile(outChannel, bu, null, new TransferManager.DownloadFromBlobOptions(
                blockSize, null, null, parallelism, retryReaderOptions)).blockingGet()
        outChannel.position(0)

        then:
        compareFiles(channel, outChannel)
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
        file                                         | blobURL
        null                                         | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        FileChannel.open(getRandomFile(10).toPath()) | null
    }

    @Unroll
    def "Download options fail"() {
        when:
        new TransferManager.DownloadFromBlobOptions(blockSize, null, null, parallelism,
                null)

        then:
        thrown(IllegalArgumentException)

        where:
        parallelism | blockSize
        0 | 40
        2 | 0
    }
}

