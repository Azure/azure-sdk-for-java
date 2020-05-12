package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

import java.time.OffsetDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class BlobChangefeedPagedFluxTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    ChangefeedFactory mockChangefeedFactory
    Changefeed mockChangefeed

    List<ChangefeedCursor> mockCursors

    def setup() {
        mockCursors = getMockCursors()

        mockContainer = mock(BlobContainerAsyncClient.class)
        mockChangefeedFactory = mock(ChangefeedFactory.class)
        mockChangefeed = mock(Changefeed.class)

        when(mockChangefeed.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers()))

    }

    List<ChangefeedCursor> getMockCursors() {
        List<ChangefeedCursor> mockCursors = new LinkedList<>()
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:15:00", new HashMap<String, ShardCursor>(), "shard0"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:16:00", new HashMap<String, ShardCursor>(), "shard1"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:17:00", new HashMap<String, ShardCursor>(), "shard2"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-02T20:01:00", new HashMap<String, ShardCursor>(), "shard3"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-03T20:08:00", new HashMap<String, ShardCursor>(), "shard4"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-04T20:06:00", new HashMap<String, ShardCursor>(), "shard5"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-05T20:04:00", new HashMap<String, ShardCursor>(), "shard6"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:03:00", new HashMap<String, ShardCursor>(), "shard7"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:06:00", new HashMap<String, ShardCursor>(), "shard8"))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:12:00", new HashMap<String, ShardCursor>(), "shard9"))
        return mockCursors
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers() {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new LinkedList<>()

        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(0), mockCursors.get(0)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(1), mockCursors.get(1)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(2), mockCursors.get(2)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(3), mockCursors.get(3)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(4), mockCursors.get(4)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(5), mockCursors.get(5)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(6), mockCursors.get(6)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(7), mockCursors.get(7)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(8), mockCursors.get(8)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(9), mockCursors.get(9)))

        return mockEventWrappers
    }

    /* No user cursor. */
    def "subscribe min"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux)

        then:
        sv.expectNext(mockEvents.get(0))
            .expectNext(mockEvents.get(1))
            .expectNext(mockEvents.get(2))
            .expectNext(mockEvents.get(3))
            .expectNext(mockEvents.get(4))
            .expectNext(mockEvents.get(5))
            .expectNext(mockEvents.get(6))
            .expectNext(mockEvents.get(7))
            .expectNext(mockEvents.get(8))
            .expectNext(mockEvents.get(9))
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    /* user cursor. */
    def "subscribe cursor"() {
        setup:
        String cursor = "cursor"
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(String.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, cursor)

        def sv = StepVerifier.create(pagedFlux)

        then:
        sv.expectNext(mockEvents.get(0))
            .expectNext(mockEvents.get(1))
            .expectNext(mockEvents.get(2))
            .expectNext(mockEvents.get(3))
            .expectNext(mockEvents.get(4))
            .expectNext(mockEvents.get(5))
            .expectNext(mockEvents.get(6))
            .expectNext(mockEvents.get(7))
            .expectNext(mockEvents.get(8))
            .expectNext(mockEvents.get(9))
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(mockContainer, cursor) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage min"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage())

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage size"() {
        setup:
        int size = 3
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage(size))

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(2).serialize(), [0, 1, 2])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(5).serialize(), [3, 4, 5])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(8).serialize(), [ 6, 7, 8])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage size continuationToken overload"() {
        setup:
        int size = 7
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage(null, size))

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(6).serialize(), [0, 1, 2, 3, 4, 5, 6])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [7, 8, 9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage continuationToken error"() {
        setup:
        String randomCursor = "randomCursor"
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)
        def sv = StepVerifier.create(pagedFlux.byPage(randomCursor))

        then:
        sv.expectError(UnsupportedOperationException)
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true

        when:
        sv = StepVerifier.create(pagedFlux.byPage(randomCursor, 7))

        then:
        sv.expectError(UnsupportedOperationException)
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
    }

    def "byPage IA"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(BlobContainerAsyncClient.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFluxFactory factory = new BlobChangefeedPagedFluxFactory(mockChangefeedFactory)
        BlobChangefeedPagedFlux pagedFlux = factory.getBlobChangefeedPagedFlux(mockContainer, startTime, endTime)
        def sv = StepVerifier.create(pagedFlux.byPage(null,  -35))

        then:
        sv.expectError(IllegalArgumentException)
        verify(mockChangefeedFactory).getChangefeed(mockContainer, startTime, endTime) || true
    }

    boolean validatePagedResponse(BlobChangefeedPagedResponse pagedResponse, String expectedCursor, List<Integer> expectedEvents) {
        boolean validate = true;
        validate &= pagedResponse.getContinuationToken() == expectedCursor
        for (int i : expectedEvents) {
            validate &= pagedResponse.getValue().contains(mockEvents.get(i))
        }
        validate &= pagedResponse.getValue().size() == expectedEvents.size()
        return validate
    }

}
