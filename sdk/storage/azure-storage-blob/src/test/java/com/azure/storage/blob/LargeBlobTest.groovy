// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.specialized.BlockBlobAsyncClient
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.common.Utility
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Requires

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicLong
import java.util.function.BiFunction

class LargeBlobTest extends APISpec {

    long maxBlockSize =  4000L * Constants.MB
    BlobClient blobClient
    BlobAsyncClient blobAsyncClient
    String blobName
    List<Mono<Long>> putBlockPayloadSizes = Collections.synchronizedList(new ArrayList<>())
    AtomicLong count = new AtomicLong()
    boolean collectSize = true

    def setup() {
        blobName = generateBlobName()
        def basic = cc.getBlobClient(blobName)
        this.blobClient = getBlobClient(
            primaryCredential,
            basic.getBlobUrl(),
            new PayloadDroppingPolicy(),
            // Regenerate auth header after body got substituted
            new StorageSharedKeyCredentialPolicy(primaryCredential)
        )
        this.blobAsyncClient = getBlobAsyncClient(
            primaryCredential,
            basic.getBlobUrl(),
            new PayloadDroppingPolicy(),
            // Regenerate auth header after body got substituted
            new StorageSharedKeyCredentialPolicy(primaryCredential)
        )
    }

    @Requires({ liveMode() })
    def "Stage Large Blob"() {
        given:
        def stream = createLargeInputStream(maxBlockSize)
        def blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8))

        when:
        blobClient.getBlockBlobClient().stageBlock(blockId, stream, maxBlockSize)
        blobClient.getBlockBlobClient().commitBlockList([blockId])

        then:
        putBlockPayloadSizes.size() == 1
        putBlockPayloadSizes[0].block() == maxBlockSize
    }

    @Requires({ liveMode() })
    def "Upload Large Input"() {
        given:
        def length = maxBlockSize * 2
        def flux = createLargeBuffer(length)

        when:
        blobAsyncClient.upload(flux, new ParallelTransferOptions(maxBlockSize, null, null, null))
        .block()

        then:
        putBlockPayloadSizes.size() == 2
        putBlockPayloadSizes[0].block() == maxBlockSize
        putBlockPayloadSizes[1].block() == maxBlockSize
    }

    @Requires({ liveMode() })
    def "Upload Largest Input"() {
        given:
        collectSize = false
        long blocks = 50 * 1000
        long length = maxBlockSize * blocks
        def flux = createLargeBuffer(length, Constants.MB)

        when:
        blobAsyncClient.upload(flux, new ParallelTransferOptions(maxBlockSize, null, null, null))
            .block()

        then:
        count.get() == blocks
    }

    private InputStream createLargeInputStream(long size) {
        long numberOfSubStreams = (long) (size / Constants.MB)
        def subStreams = new Vector()
        def bytes = getRandomByteArray(Constants.MB)
        for (long i = 0; i < numberOfSubStreams; i++) {
            subStreams.add(new ByteArrayInputStream(bytes))
        }
        return new SequenceInputStream(subStreams.elements())
    }

    private Flux<ByteBuffer> createLargeBuffer(long size) {
        return createLargeBuffer(size, Constants.MB)
    }

    private Flux<ByteBuffer> createLargeBuffer(long size, int bufferSize) {
        def bytes = getRandomByteArray(bufferSize)
        long numberOfSubBuffers = (long) (size / bufferSize)
        return Flux.just(ByteBuffer.wrap(bytes))
            .map{buffer -> buffer.duplicate()}
            .repeat(numberOfSubBuffers - 1)
    }

    private class PayloadDroppingPolicy implements HttpPipelinePolicy {
        @Override
        Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
            def request = httpPipelineCallContext.httpRequest
            // Substitute large body for put block requests and collect size of original body
            if (request.url.getQuery() != null && request.url.getQuery().endsWith("comp=block")) {
                if (collectSize) {
                    def bytesReceived = request.getBody().reduce(0L, new BiFunction<Long, ByteBuffer, Long>() {
                        @Override
                        Long apply(Long a, ByteBuffer byteBuffer) {
                            return a + byteBuffer.remaining()
                        }
                    })
                    putBlockPayloadSizes.add(bytesReceived)
                }
                def currentCount = count.incrementAndGet()
                println(currentCount) // TODO remove this
                request.setBody("dummyBody")
            }
            return httpPipelineNextPolicy.process()
        }
    }
}
