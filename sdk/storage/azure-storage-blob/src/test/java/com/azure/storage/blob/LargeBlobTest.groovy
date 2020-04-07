// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.HttpPipelineCallContext
import com.azure.core.http.HttpPipelineNextPolicy
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.util.Context
import com.azure.storage.blob.models.BlockListType
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Ignore
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
    @Ignore("Takes really long time")
    // This test sends payload over the wire
    def "Stage Real Large Blob"() {
        given:
        def stream = createLargeInputStream(maxBlockSize)
        def blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8))
        def client = cc.getBlobClient(blobName)

        when:
        client.getBlockBlobClient().stageBlock(blockId, stream, maxBlockSize)
        client.getBlockBlobClient().commitBlockList([blockId])
        def blockList = client.getBlockBlobClient().listBlocks(BlockListType.COMMITTED)

        then:
        blockList.committedBlocks.size() == 1
        blockList.committedBlocks.get(0).getSizeLong() == maxBlockSize
    }

    @Requires({ liveMode() })
    // This test does not send large payload over the wire
    def "Stage Large Blob"() {
        given:
        def stream = createLargeInputStream(maxBlockSize)
        def blockId = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8))

        when:
        blobClient.getBlockBlobClient().stageBlock(blockId, stream, maxBlockSize)
        blobClient.getBlockBlobClient().commitBlockList([blockId])

        then:
        count.get() == 1
        putBlockPayloadSizes.size() == 1
        putBlockPayloadSizes[0].block() == maxBlockSize
    }

    @Requires({ liveMode() })
    // This test does not send large payload over the wire
    def "Upload Large Input"() {
        given:
        def length = maxBlockSize * 2
        def flux = createLargeBuffer(length)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSize(maxBlockSize);

        when:
        blobAsyncClient.upload(flux, parallelTransferOptions)
        .block()

        then:
        count.get() == 2
        putBlockPayloadSizes.size() == 2
        putBlockPayloadSizes[0].block() == maxBlockSize
        putBlockPayloadSizes[1].block() == maxBlockSize
    }

    @Requires({ liveMode() })
    @Ignore("Takes really long time")
    // This test does not send large payload over the wire
    def "Upload Largest Input"() {
        given:
        collectSize = false
        long blocks = 50 * 1000
        long length = maxBlockSize * blocks
        def flux = createLargeBuffer(length, Constants.MB)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSize(maxBlockSize);

        when:
        blobAsyncClient.upload(flux, parallelTransferOptions)
            .block()

        then:
        count.get() == blocks
    }

    @Requires({ liveMode() })
    // This test does not send large payload over the wire
    def "Upload Large Input Sync"() {
        given:
        long blocks = 2
        long length = maxBlockSize * blocks
        def stream = createLargeInputStream(length, Constants.MB)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSize(maxBlockSize);

        when:
        blobClient.uploadWithResponse(
            stream, length, parallelTransferOptions,
            null, null, null, null, null, Context.NONE);

        then:
        count.get() == blocks
        putBlockPayloadSizes.size() == 2
        putBlockPayloadSizes[0].block() == maxBlockSize
        putBlockPayloadSizes[1].block() == maxBlockSize
    }

    @Requires({ liveMode() })
    @Ignore("Takes really long time")
    // This test does not send large payload over the wire
    def "Upload Largest Input Sync"() {
        given:
        collectSize = false
        long blocks = 50 * 1000
        long length = maxBlockSize * blocks
        def stream = createLargeInputStream(length, 100 * Constants.MB)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSize(maxBlockSize);

        when:
        blobClient.uploadWithResponse(
            stream, length, parallelTransferOptions,
            null, null, null, null, null, Context.NONE);

        then:
        count.get() == blocks
    }

    @Requires({ liveMode() })
    @Ignore("Takes really long time")
    // This test sends payload over the wire
    def "Upload Large File"() {
        given:
        def file = getRandomLargeFile(maxBlockSize)
        def client = cc.getBlobClient(blobName)
        def parallelTransferOptions = new ParallelTransferOptions().setBlockSize(maxBlockSize);

        when:
        client.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
            null, null, null, null, null)

        then:
        true
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

    File getRandomLargeFile(long size) {
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
                count.incrementAndGet()
                request.setBody("dummyBody")
            }
            return httpPipelineNextPolicy.process()
        }
    }
}
