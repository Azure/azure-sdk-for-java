package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.CommonRestResponse
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.RequestRetryOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.TransferManager
import com.microsoft.azure.storage.blob.models.BlockBlobsCommitBlockListResponse
import com.microsoft.azure.storage.blob.models.BlockBlobsUploadResponse
import com.microsoft.rest.v2.http.HttpClient
import com.microsoft.rest.v2.http.HttpClientConfiguration
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.channels.FileChannel

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
        response.response() instanceof BlockBlobsUploadResponse // Ensure we did a single put for a small blob.
        FlowableUtil.collectBytesInBuffer(bu.download(null, null, false)
                .blockingGet().body()).blockingGet() == defaultData
    }

    def "Upload buffers parallel"() {
        setup:
        int blockSize = 10 * 1024 * 1024
        List<ByteBuffer> buffers = new ArrayList<>()
        for (int i = 0; i <= BlockBlobURL.MAX_PUT_BLOB_BYTES; i += blockSize) {
            buffers.add(getRandomData(blockSize))
        }

        when:
        CommonRestResponse response = TransferManager.uploadByteBuffersToBlockBlob(buffers, bu,
                new TransferManager.UploadToBlockBlobOptions(null, null, null,
                        null, 20)).blockingGet()

        then:
        // Ensure we did a commitBlockList for large blobs
        response.response() instanceof BlockBlobsCommitBlockListResponse
        /*compareBufferListToFlowable(buffers, bu.download(null, null, false)
                .blockingGet().body())*/
        compare(buffers, bu.download(null, null, false).blockingGet().body())
    }

    def compare(List<ByteBuffer> buffers, Flowable<ByteBuffer> flowable) {
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

    def compareBufferListToFlowable(List<ByteBuffer> buffers, Flowable<ByteBuffer> flowable) {
        // Since we're comparing buffers, they're already in memory. There's a perf hit in collecting the response all in one, but it doesn't much matter here
        int i = 0
        flowable.blockingForEach(new Consumer<ByteBuffer>() {
            @Override
            void accept(ByteBuffer received) throws Exception {
                if (buffers.get(i).remaining() > received.remaining()) {
                    compareBuffersWithBookkeeping(buffers.get(i), received)
                } else if (buffers.get(i).remaining() == received.remaining()) {
                    /*
                    This path will read all the data remaining in the current buffer. No need for bookkeeping. We can
                    just move on.
                     */
                    if (buffers.get(i).compareTo(received) != 0) {
                        throw new Exception("Data does not match")
                    }
                    i++
                } else {
                    while (buffers.get(i).remaining() < received.remaining()) {
                        compareBuffersWithBookkeeping(received, buffers.get(i))
                        i++ // Current local buffer exhausted. Move on to the next one.
                    }
                    // Make sure that we actually exhaust the received buffer.
                    if (received.remaining() > 0) {
                        if (buffers.get(i).remaining() == received.remaining()) {
                            if (buffers.get(i).compareTo(received) != 0) {
                                throw new Exception("Data does not match")
                            }
                            i++
                        }
                        else {
                            compareBuffersWithBookkeeping(buffers.get(i), received)
                        }
                    }
                }
            }
        })

        return true // If we make it here without exceptions, we win!
    }

    /**
     * This function will compare the contents of a smaller buffer to a subset of a larger one and advance the
     * position of the larger one.
     */
    def compareBuffersWithBookkeeping(ByteBuffer larger, ByteBuffer smaller) {
        /*
        Setup:
        Get a window the the source data which is equal in size to the received data so that compareTo
        (which considers remaining data) can succeed.
        */
        int compareAmount = smaller.remaining()
        int oldLimit = larger.limit()
        larger.limit(larger.position() + compareAmount)

        // Compare
        if (larger.compareTo(smaller) != 0) {
            throw new Exception("Data does not match")
        }

        /*
        Cleanup:
        Restore the original limit so we can compare against the rest of the data in this buffer. Advance
        the position to compare against new data.
         */
        larger.limit(oldLimit)
        larger.position(larger.position() + compareAmount)
    }

    def "Upload buffers illegal arguments"() {
        // Nulls
        // Empty list
        // Too many blocks
        // Blocks too large
    }

    def "Upload buffers headers"() {

    }

    @Unroll
    def "Upload buffers metadata"() {

    }

    def "Upload buffers metadata fail"() {

    }

    @Unroll
    def "Upload buffers AC"() {

    }

    @Unroll
    def "Upload buffers AC fail"() {

    }

    def "Upload options"() {
        // paralellism bounds
        // default values
    }

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
