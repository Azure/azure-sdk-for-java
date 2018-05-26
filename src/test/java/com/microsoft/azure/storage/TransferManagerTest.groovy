package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlobAccessConditions
import com.microsoft.azure.storage.blob.BlobHTTPHeaders
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.CommonRestResponse
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.HTTPAccessConditions
import com.microsoft.azure.storage.blob.LeaseAccessConditions
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.RequestRetryOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.TransferManager
import com.microsoft.azure.storage.blob.models.BlobsGetPropertiesResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadResponse
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.channels.FileChannel
import java.security.MessageDigest

class TransferManagerTest extends APISpec {
    BlockBlobURL bu

    def setup() {
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(defaultFlowable, defaultDataSize, null, null,
                null).blockingGet()
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
        response.response() instanceof BlockBlobsUploadResponse // Ensure we did a single put for a small blob.
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
        // Ensure we did a commitBlockList for large blobs
        response.response() instanceof BlockBlobsCommitBlockListResponse
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
        not need to be correct, simply not null. Because order in which Spock initializes values, we can't just use the
        bu property for the url.
         */
        where:
        data                       | blobURL
        null                       | new BlockBlobURL(new URL("http://account.com"), StorageURL.createPipeline(primaryCreds, new PipelineOptions()))
        Arrays.asList(defaultData) | null
    }

    def "Upload buffers illegal argument max blocks"() {
        setup:
        List<ByteBuffer> data = new ArrayList<>()
        for (int i = 0; i <= BlockBlobURL.MAX_BLOCKS; i++) {
            // We also have to make sure it's still larger than the max put blob size.
            data.add(getRandomData((int) (BlockBlobURL.MAX_PUT_BLOB_BYTES / BlockBlobURL.MAX_BLOCKS + 10)))
        }
        data.add(getRandomData((int) (BlockBlobURL.MAX_PUT_BLOB_BYTES / BlockBlobURL.MAX_BLOCKS + 10))) // Add one more.

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

        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
        bu.upload(defaultFlowable, defaultDataSize,
                null, metadata, null).blockingGet()
        BlobsGetPropertiesResponse response = bu.getProperties(null).blockingGet()

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
    failures for access conditions in both paths because it's not clear from successes alone that they are implemented
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
        data                        | modified | unmodified | match        | noneMatch    | leaseID
        Arrays.asList(defaultData)  | newDate  | null       | null         | null         | null
        Arrays.asList(defaultData)  | null     | oldDate    | null         | null         | null
        Arrays.asList(defaultData)  | null     | null       | garbageEtag  | null         | null
        Arrays.asList(defaultData)  | null     | null       | null         | receivedEtag | null
        Arrays.asList(defaultData)  | null     | null       | null         | null         | garbageLeaseID
        getListForMultiPartUpload() | newDate  | null       | null         | null         | null
        getListForMultiPartUpload() | null     | oldDate    | null         | null         | null
        getListForMultiPartUpload() | null     | null       | garbageEtag  | null         | null
        getListForMultiPartUpload() | null     | null       | null         | receivedEtag | null
        getListForMultiPartUpload() | null     | null       | null         | null         | garbageLeaseID
    }

    def "Upload options"() {
        expect:
        TransferManager.UploadToBlockBlobOptions.DEFAULT ==
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, null)
        TransferManager.UploadToBlockBlobOptions.DEFAULT ==
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, 5)

    }

    def "Upload options fail"() {
        when:
        new TransferManager.UploadToBlockBlobOptions(null, null, null,
                null, -1)

        then:
        thrown(IllegalArgumentException)
    }

    /*
    Because uploadBuffer and uploadFile call into uploadBuffers, we don't have to validate both the single-shot and
    multi-part scenarios other than validating the correctness of the data (to ensure it is split up correctly). In the
    cases of metadata, etc. validating one scenario is enough. If the implementation ever changes, we will have to add
    those tests.
     */

    // Upload and check data integrity and response type: single-shot, multi-part
    // Illegal arguments null
    // Illegal arguments bounds
    // headers
    // metadata (fail)
    // AC
    // AC fail

    def "Https parallel file upload"() {
        setup:
        PipelineOptions po = new PipelineOptions()
        RequestRetryOptions retryOptions = new RequestRetryOptions(null, null, 300,
                null, null, null)
        po.requestRetryOptions = retryOptions
        po.client = getHttpClient()

        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, po)

        // This test requires https.
        ServiceURL surl = new ServiceURL(new URL("https://" + primaryCreds.getAccountName() + ".blob.core.windows.net"),
                pipeline)

        ContainerURL containerURL = surl.createContainerURL(generateContainerName())
        containerURL.create(null, null).blockingGet()

        when:
        /*
         We are simply testing for no errors here. There has historically been a problem with Netty that caused it to
         crash when uploading multiple medium size files in parallel over https. Here we validate that behavior is
         fixed. We will test for correctness of the parallel upload elsewhere.
         */
        Observable.range(0, 4000)
                .flatMap(new Function<Integer, ObservableSource>() {
            @Override
            ObservableSource apply(@NonNull Integer i) throws Exception {
                BlockBlobURL asyncblob = containerURL.createBlockBlobURL("asyncblob" + i)
                TransferManager.UploadToBlockBlobOptions asyncOptions = new TransferManager.UploadToBlockBlobOptions(
                        null, null, null, null, 1)

                return TransferManager.uploadFileToBlockBlob(
                        FileChannel.open(new File(getClass().getClassLoader().getResource("15mb.txt").getFile())
                                .toPath()), asyncblob, BlockBlobURL.MAX_PUT_BLOCK_BYTES, asyncOptions).toObservable()
            }
        }, 2000)
                .onErrorReturn((new Function<Throwable, Object>() {
            @Override
            Object apply(Throwable throwable) throws Exception {
                /*
                We only care about the ReadOnlyBufferException as an indication of the netty failure with memory mapped
                files. Everything else, like throttling, is fine here.
                 */
                if (throwable instanceof ReadOnlyBufferException) {
                    throw throwable
                }
                // This value is not meaningful. We just want the observable to continue.
                return new Object()
            }
        })).blockingSubscribe()
        containerURL.delete(null).blockingGet()

        then:
        notThrown(ReadOnlyBufferException)
    }
}
