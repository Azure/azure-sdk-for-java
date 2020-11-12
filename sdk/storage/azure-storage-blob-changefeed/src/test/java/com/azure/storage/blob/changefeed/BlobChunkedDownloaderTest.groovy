package com.azure.storage.blob.changefeed

import com.azure.core.util.FluxUtil
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import reactor.core.publisher.Flux
import spock.lang.Unroll

import java.nio.ByteBuffer

class BlobChunkedDownloaderTest extends APISpec {

    BlobAsyncClient bc
    BlobChunkedDownloaderFactory factory

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
        def cc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName())
        cc.create().block()
        bc = Spy(cc.getBlobAsyncClient(generateBlobName()))
        factory = new BlobChunkedDownloaderFactory(cc)
    }

    byte[] downloadHelper(BlobChunkedDownloader downloader) {
        OutputStream os = downloader.download()
            .reduce(new ByteArrayOutputStream(),  { outputStream, buffer ->
            outputStream.write(FluxUtil.byteBufferToArray(buffer))
            return outputStream;
        }).block()
        return os.toByteArray()
    }

    byte[] uploadHelper(int size) {
        def input = getRandomByteArray(size)
        def data = Flux.just(ByteBuffer.wrap(input))
        bc.upload(data, null).block()
        return input
    }

    @Unroll
    def "download blockSize"() {
        setup:
        byte[] input = uploadHelper(size)

        when:
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, blockSize, 0))

        then:
        output == input
        numDownloads * bc.downloadWithResponse(_,_,_,_)

        where:
        size                | blockSize        || numDownloads
        Constants.KB        | Constants.KB     || 1        /* blockSize = size. 1 download call. */
        Constants.KB        | Constants.MB     || 1        /* blockSize > size. 1 download call. */
        4 * Constants.KB    | Constants.KB     || 4        /* blockSize < size. 4 download calls.*/
    }

    @Unroll
    def "download offset"() {
        setup:
        byte[] input = uploadHelper(Constants.KB)

        when:
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, Constants.KB, offset))

        then:
        for (int i = 0; i < input.length - offset; i++) {
            assert output[i] == input[i + offset]
        }
        1 * bc.downloadWithResponse(_,_,_,_)

        where:
        offset              || _
        0                   || _
        200                 || _
        512                 || _
        1000                || _
    }

    @Unroll
    def "download blockSize offset"() {
        setup:
        byte[] input = uploadHelper(size)

        when:
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, Constants.KB, offset))

        then:
        for (int i = 0; i < input.length - offset; i++) {
            assert output[i] == input[i + offset]
        }
        numDownloads * bc.downloadWithResponse(_,_,_,_)

        where:
        size                | blockSize        | offset           || numDownloads
        4 * Constants.KB    | Constants.KB     | Constants.KB     || 3        /* 3 download calls. */
        4 * Constants.KB    | Constants.KB     | 2 * Constants.KB || 2        /* 2 download calls. */
    }

    /* Tests offset > length of blob. */
    def "download invalid offset"() {
        setup:
        uploadHelper(Constants.KB)

        when:
        downloadHelper(factory.getBlobLazyDownloader(bc.getBlobName(), Constants.KB, Constants.KB * 2))

        then:
        thrown(BlobStorageException)
    }

    /* Tests case for downloading only the header. */
    @Unroll
    def "download partial"() {
        setup:
        byte[] input = uploadHelper(uploadSize)

        when:
        byte[] output = downloadHelper(new BlobChunkedDownloader(bc, downloadSize))

        then:
        assert output.length == downloadSize
        for (int i = 0; i < downloadSize; i++) {
            assert output[i] == input[i]
        }
        1 * bc.downloadWithResponse(_,_,_,_)

        where:
        uploadSize     | downloadSize       || _
        Constants.MB   | Constants.KB       || _
        Constants.MB   | 4 * Constants.KB   || _
        Constants.MB   | Constants.MB       || _
    }
}
