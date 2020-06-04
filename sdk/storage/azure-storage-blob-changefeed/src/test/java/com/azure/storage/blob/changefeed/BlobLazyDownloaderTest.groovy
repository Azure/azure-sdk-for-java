package com.azure.storage.blob.changefeed

import com.azure.core.util.FluxUtil
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import reactor.core.publisher.Flux
import spock.lang.Unroll

import java.nio.ByteBuffer

class BlobLazyDownloaderTest extends APISpec {

    BlobAsyncClient bc
    BlobLazyDownloaderFactory factory

    def setup() {
        def cc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName())
        cc.create().block()
        bc = cc.getBlobAsyncClient(generateBlobName())
        factory = new BlobLazyDownloaderFactory(cc)
    }

    byte[] downloadHelper(BlobLazyDownloader downloader) {
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
        byte[] output = downloadHelper(new BlobLazyDownloader(bc, blockSize, 0))

        then:
        output == input

        where:
        size                | blockSize        || _
        Constants.KB        | Constants.KB     || _        /* blockSize = size. 1 download call. */
        Constants.KB        | Constants.MB     || _        /* blockSize > size. 1 download call. */
        4 * Constants.KB    | Constants.KB     || _        /* blockSize < size. 4 download calls.*/
    }

    @Unroll
    def "download offset"() {
        setup:
        byte[] input = uploadHelper(Constants.KB)

        when:
        byte[] output = downloadHelper(factory.getBlobLazyDownloader(bc.getBlobName(), Constants.KB, offset))

        then:
        for (int i = 0; i < input.length - offset; i++) {
            assert output[i] == input[i + offset]
        }

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
        byte[] output = downloadHelper(factory.getBlobLazyDownloader(bc.getBlobName(), Constants.KB, offset))

        then:
        for (int i = 0; i < input.length - offset; i++) {
            assert output[i] == input[i + offset]
        }

        where:
        size                | blockSize        | offset           || _
        4 * Constants.KB    | Constants.KB     | Constants.KB     || _        /* 3 download calls. */
        4 * Constants.KB    | Constants.KB     | 2 * Constants.KB || _        /* 2 download calls. */
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
        byte[] output = downloadHelper(factory.getBlobLazyDownloader(bc.getBlobName(), downloadSize))

        then:
        assert output.length == downloadSize
        for (int i = 0; i < downloadSize; i++) {
            assert output[i] == input[i]
        }

        where:
        uploadSize     | downloadSize       || _
        Constants.MB   | Constants.KB       || _
        Constants.MB   | 4 * Constants.KB   || _
        Constants.MB   | Constants.MB       || _
    }
}
