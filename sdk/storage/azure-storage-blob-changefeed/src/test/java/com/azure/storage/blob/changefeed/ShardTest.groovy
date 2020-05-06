package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import org.mockito.Mockito
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Function

class ShardTest extends Specification {

    BlobContainerAsyncClient mockContainer
    String shardPath

    def setup() {
        shardPath = "shardPath"
        mockContainer = Mockito.mock(BlobContainerAsyncClient.class)
//        ClassLoader classLoader = getClass().getClassLoader()
//        File f = new File(classLoader.getResource(chunkPath).getFile())
//        Path path = Paths.get(f.getAbsolutePath())
    }

    @Unroll
    def "getEvents"() {
        setup:
        List<BlobItem> chunks = new ArrayList<>()
        for(int i = 0; i < numChunks; i++) {
            chunks.add(new BlobItem().setName("chunk" + i))
        }
        PagedResponse<BlobItem> mockPagedResponse = Mockito.mock(PagedResponse.class)
        Mockito.when(mockPagedResponse.getValue())
            .thenReturn(chunks)
        Function<String, Mono<PagedResponse<BlobItem>>> func = { marker ->
            mockPagedResponse
        }
        Mockito.when(mockContainer.listBlobs(new ListBlobsOptions().setPrefix(shardPath)))
            .thenReturn(new PagedFlux<>({ marker -> func.apply(null) } ))

        def segmentCursor = new ChangefeedCursor("endTime", "segmentTime", shardPath, null, null)
        def userCursor = null

        Shard shard = Mockito.spy(new Shard(mockContainer, shardPath, segmentCursor, userCursor))
        for (BlobItem chunk: chunks) {
            List<BlobChangefeedEventWrapper> wrappers = new ArrayList<>()
            for (int i = 0; i < 10; i++) {
                def event = new BlobChangefeedEvent(null, i as String, null, null, null, null, null, null)
                def cursor = new ChangefeedCursor("endTime", "segmentTime", shardPath, chunk.getName(), null)
                wrappers.add(new BlobChangefeedEventWrapper(event, cursor))
            }
            Mockito.doReturn(Flux.fromIterable(wrappers)).when(shard).getEventsForChunk(chunk.getName())
        }

        when:
        def sv = StepVerifier.create(
            shard.getEvents()
                .index()
        )

        then:
        sv.expectNextCount(count)
            .verifyComplete()

        where:
        numChunks || count
        1         || 10     /* No chunks -> no events. */

    }
}
