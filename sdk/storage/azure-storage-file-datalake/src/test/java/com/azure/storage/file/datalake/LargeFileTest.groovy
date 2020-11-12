package com.azure.storage.file.datalake

import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.common.ParallelTransferOptions
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy
import com.azure.storage.file.datalake.models.DataLakeStorageException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Ignore
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiFunction

class LargeFileTest extends APISpec{
    static long defaultSingleUploadThreshold = 100L * Constants.MB
    long maxBlockSize =  4000L * Constants.MB
    boolean collectSize = true
    List<Long> appendPayloadSizes = Collections.synchronizedList(new ArrayList<>())
    AtomicLong count = new AtomicLong()

    DataLakeFileClient fc
    DataLakeFileClient fcPayloadDropping
    DataLakeFileAsyncClient fcAsyncPayloadDropping
    String fileName

    def setup() {
        fileName = generatePathName()
        fc = fsc.getFileClient(fileName)
        fcPayloadDropping = getFileClient(
            primaryCredential,
            fc.getFileUrl(),
            new PayloadDroppingPolicy(),
            new StorageSharedKeyCredentialPolicy(primaryCredential)
        )
        fcAsyncPayloadDropping = getFileAsyncClient(
            primaryCredential,
            fc.getFileUrl(),
            new PayloadDroppingPolicy(),
            new StorageSharedKeyCredentialPolicy(primaryCredential)
        )

        fc.create()
    }

    @Requires({ liveMode() })
    @Ignore("Takes really long time")
    // This test sends payload over the wire
    def "Append Large Block Real"() {
        given:
        def stream = createLargeInputStream(maxBlockSize)

        when:
        fc.append(stream, 0, maxBlockSize)

        then:
        notThrown(DataLakeStorageException)
    }

    @Requires({ liveMode() })
    @Ignore("IS mark/reset")
    // This test does not send large payload over the wire
    def "Append Large Block"() {
        given:
        def stream = createLargeInputStream(maxBlockSize)

        when:
        fcPayloadDropping.append(stream, 0, maxBlockSize)

        then:
        notThrown(DataLakeStorageException)
        count.get() == 1
        appendPayloadSizes[0] == maxBlockSize
    }

    @Requires({ liveMode() })
    // This test does not send large payload over the wire
    def "Append Large Block Async"() {
        given:
        def data = createLargeBuffer(maxBlockSize)

        when:
        fcAsyncPayloadDropping.append(data, 0, maxBlockSize).block()

        then:
        notThrown(DataLakeStorageException)
        count.get() == 1
        appendPayloadSizes[0] == maxBlockSize
    }

    @Requires({ liveMode() })
    @Ignore("Allocates too much memory for current CI machines") // TODO (rickle-msft): Enable when test resources can allocate 8GB
    // This test does not send large payload over the wire
    def "Upload Large Data Async"() {
        given:
        def data = createLargeBuffer(2 * maxBlockSize)

        when:
        fcAsyncPayloadDropping.upload(data, new ParallelTransferOptions().setBlockSizeLong(maxBlockSize), true).block()

        then:
        notThrown(DataLakeStorageException)
        count.get() == 2
    }

    @Requires({ liveMode() })
    @Ignore("Allocates too much memory for current CI machines") // TODO (rickle-msft): Enable when test resources can allocate 8GB
    // This test does not send large payload over the wire
    def "Append Large File"() {
        given:
        def file = getLargeRandomFile(2 * maxBlockSize)

        when:
        fcPayloadDropping.uploadFromFile(
            file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(maxBlockSize),
            null,
            null,
            null,
            null
        )

        then:
        notThrown(DataLakeStorageException)
        count.get() == 2
    }

    @Requires({ liveMode() })
    @Ignore("Takes really long time")
    // This test sends payload over the wire
    def "Append Large File Real"() {
        given:
        def file = getLargeRandomFile(2 * maxBlockSize)

        when:
        fc.uploadFromFile(
            file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(maxBlockSize),
            null,
            null,
            null,
            null
        )

        then:
        notThrown(DataLakeStorageException)
    }

    @Unroll
    @Requires({ liveMode() })
    // This test does not send large payload over the wire
    def "Should honor default single upload threshold"() {
        given:
        def data = createLargeBuffer(dataSize)
        def transferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(10L * Constants.MB) // set this much lower than default single upload size to make it tempting.

        when:
        fcAsyncPayloadDropping.upload(data, transferOptions, true).block()

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
        return new SequenceInputStream(subStreams.elements())
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
    private class PayloadDroppingPolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            def dummyBody = "dummyBody"
            def request = httpPipelineCallContext.httpRequest
            def urlString = request.getUrl().toString()
            // Substitute large body for put block requests and collect size of original body
            if (isAppendRequest(request)) {
                count.incrementAndGet()
                Mono<Long> count = interceptBody(request, dummyBody)
                if (count != null) {
                    return count.flatMap { bytes ->
                        appendPayloadSizes.add(bytes)
                        return httpPipelineNextPolicy.process()
                    }
                }
            } else if(isFlushRequest(request)) {
                request.setUrl(urlString.replaceAll("position=\\d+", "position=" + dummyBody.length()))
            }
            return httpPipelineNextPolicy.process()
        }

        private Mono<Long> interceptBody(HttpRequest request, String substitute) {
            Mono<Long> result = null
            if (collectSize) {
                result = request.getBody().reduce(0L, new BiFunction<Long, ByteBuffer, Long>() {
                    @Override
                    Long apply(Long a, ByteBuffer byteBuffer) {
                        return a + byteBuffer.remaining()
                    }
                })
            }
            request.setBody(substitute)

            return result
        }

        private boolean isAppendRequest(HttpRequest request) {
            return request.url.getQuery() != null && request.url.getQuery().contains("action=append")
        }

        private boolean isFlushRequest(HttpRequest request) {
            return request.url.getQuery() != null && request.url.getQuery().contains("action=flush")
        }
    }
}
