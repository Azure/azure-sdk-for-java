package com.azure.storage.file.datalake

import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpPipelinePosition
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.TestEnvironment
import com.azure.storage.common.test.shared.TestHttpClientType
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.IgnoreIf
import spock.lang.ResourceLock
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

@LiveOnly
@ResourceLock("LargeFileTest")
@IgnoreIf({ TestEnvironment.getInstance().httpClientType == TestHttpClientType.OK_HTTP }) // https://github.com/Azure/azure-sdk-for-java/issues/23221
class LargeFileTest extends APISpec{
    static long defaultSingleUploadThreshold = 100L * Constants.MB
    long largeBlockSize =  2500L * Constants.MB

    List<Long> appendPayloadSizes = Collections.synchronizedList(new ArrayList<>())
    AtomicLong count = new AtomicLong()

    DataLakeFileClient fc
    DataLakeFileAsyncClient fcAsync
    String fileName

    def setup() {
        fileName = generatePathName()
        def fileClient = fsc.getFileClient(fileName)
        fc = getFileClient(
            env.dataLakeAccount.credential,
            fileClient.getFileUrl(),
            new CountingPolicy()
        )
        fcAsync = getFileAsyncClient(
            env.dataLakeAccount.credential,
            fileClient.getFileUrl(),
            new CountingPolicy()
        )

        fileClient.create()
    }

    def "Append Large Block"() {
        given:
        def stream = createLargeInputStream(largeBlockSize)

        when:
        fc.append(stream, 0, largeBlockSize)

        then:
        notThrown(DataLakeStorageException)
        count.get() == 1
        appendPayloadSizes[0] == largeBlockSize
    }

    def "Append Large Block Async"() {
        given:
        def data = createLargeBuffer(largeBlockSize)

        when:
        fcAsync.append(data, 0, largeBlockSize).block()

        then:
        notThrown(DataLakeStorageException)
        count.get() == 1
        appendPayloadSizes[0] == largeBlockSize
    }

    def "Upload Large Data Async"() {
        given:
        def tail = 1L * Constants.MB
        def data = createLargeBuffer(largeBlockSize + tail)

        when:
        fcAsync.upload(data, new ParallelTransferOptions().setBlockSizeLong(largeBlockSize), true).block()

        then:
        notThrown(DataLakeStorageException)
        count.get() == 2
        appendPayloadSizes.contains(largeBlockSize)
        appendPayloadSizes.contains(tail)
    }

    def "Upload Large File"() {
        given:
        def tail = 1L * Constants.MB
        def file = getLargeRandomFile(largeBlockSize + tail)

        when:
        fc.uploadFromFile(
            file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(largeBlockSize),
            null,
            null,
            null,
            null
        )

        then:
        notThrown(DataLakeStorageException)
        count.get() == 2
        appendPayloadSizes.contains(largeBlockSize)
        appendPayloadSizes.contains(tail)
    }

    @Unroll
    // This test does not send large payload over the wire
    def "Should honor default single upload threshold"() {
        given:
        def data = createLargeBuffer(dataSize)
        def transferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(10L * Constants.MB) // set this much lower than default single upload size to make it tempting.

        when:
        fcAsync.upload(data, transferOptions, true).block()

        then:
        notThrown(DataLakeStorageException)
        count.get() == expectedAppendRequests

        where:
        dataSize                         | expectedAppendRequests
        defaultSingleUploadThreshold     | 1
        defaultSingleUploadThreshold + 1 | 11
    }

    private Flux<ByteBuffer> createLargeBuffer(long size) {
        return createLargeBuffer(size, Constants.MB)
    }

    private Flux<ByteBuffer> createLargeBuffer(long size, int bufferSize) {
        def bytes = getRandomByteArray(bufferSize)
        long numberOfSubBuffers = (long) (size / bufferSize)
        int remainder = (int) (size - numberOfSubBuffers * bufferSize)
        Flux<ByteBuffer> result =  Flux.just(ByteBuffer.wrap(bytes))
            .map{buffer -> buffer.duplicate()}
            .repeat(numberOfSubBuffers - 1)
        if (remainder > 0) {
            def extraBytes = getRandomByteArray(remainder)
            result = Flux.concat(result, Flux.just(ByteBuffer.wrap(extraBytes)))
        }
        return result
    }

    private InputStream createLargeInputStream(long size) {
        return createLargeInputStream(size, Constants.MB)
    }

    private InputStream createLargeInputStream(long size, int chunkSize) {
        long numberOfSubStreams = (long) (size / chunkSize)
        def subStreams = new Vector()
        def bytes = getRandomByteArray(chunkSize)
        for (long i = 0; i < numberOfSubStreams; i++) {
            subStreams.add(new ByteArrayInputStream(bytes))
        }
        return new SequenceInputStream(subStreams.elements()) {
            @Override
            void reset() throws IOException {
                // no-op
            }
        }
    }

    File getLargeRandomFile(long size) {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".txt")
        file.deleteOnExit()
        FileOutputStream fos = new FileOutputStream(file)

        if (size > Constants.MB) {
            for (def i = 0; i < size / Constants.MB; i++) {
                def dataSize = (int) Math.min(Constants.MB, size - i * Constants.MB)
                fos.write(getRandomByteArray(dataSize))
            }
        } else {
            fos.write(getRandomByteArray((int) size))
        }

        fos.close()
        return file
    }

    /**
     * This class is intended for large payload test cases only and reports directly into this test class's
     * state members.
     */
    private class CountingPolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            def request = httpPipelineCallContext.httpRequest

            if (isAppendRequest(request)) {
                count.incrementAndGet()

                AtomicLong size = new AtomicLong();

                def bodySubstitute = request.getBody().map({ buffer ->
                    size.addAndGet(buffer.remaining())
                    return buffer
                }).doOnComplete({ appendPayloadSizes.add(size.get()) })
                request.setBody(bodySubstitute)
            }
            return httpPipelineNextPolicy.process()
        }

        private boolean isAppendRequest(HttpRequest request) {
            return request.url.getQuery() != null && request.url.getQuery().contains("action=append")
        }

        HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }
}
