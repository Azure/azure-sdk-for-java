package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.OffsetDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class BlobChangefeedPagedFluxTest extends Specification {

    BlobContainerAsyncClient mockContainer
    ChangefeedFactory mockChangefeedFactory
    Changefeed mockChangefeed

    List<ChangefeedCursor> mockCursors
    List<BlobChangefeedEvent> mockEvents
    List<BlobChangefeedEventWrapper> mockEventWrappers

    def setup() {
        setupEvents()
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockChangefeedFactory = mock(ChangefeedFactory.class)
        mockChangefeed = mock(Changefeed.class)

        when(mockChangefeed.getEvents())
            .thenReturn(Flux.fromIterable(mockEventWrappers))
    }

    /* No user cursor. */
    def "subscribe min"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)

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
        verify(mockChangefeedFactory).getChangefeed(startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    /* user cursor. */
    def "subscribe cursor"() {
        setup:
        String cursor = "cursor"
        when(mockChangefeedFactory.getChangefeed(any(String.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, cursor)

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
        verify(mockChangefeedFactory).getChangefeed(cursor) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage min"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage())

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage size"() {
        setup:
        int size = 3
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage(size))

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(2).serialize(), [0, 1, 2])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(5).serialize(), [3, 4, 5])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(8).serialize(), [ 6, 7, 8])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage size continuationToken overload"() {
        setup:
        int size = 7
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)

        def sv = StepVerifier.create(pagedFlux.byPage(null, size))

        then:
        sv.expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(6).serialize(), [0, 1, 2, 3, 4, 5, 6])})
            .expectNextMatches({pagedResponse -> this.&validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(), [7, 8, 9])})
            .verifyComplete()
        verify(mockChangefeedFactory).getChangefeed(startTime, endTime) || true
        verify(mockChangefeed).getEvents() || true
    }

    def "byPage continuationToken error"() {
        setup:
        String randomCursor = "randomCursor"
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed( any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)
        def sv = StepVerifier.create(pagedFlux.byPage(randomCursor))

        then:
        sv.expectError(UnsupportedOperationException)

        when:
        sv = StepVerifier.create(pagedFlux.byPage(randomCursor, 7))

        then:
        sv.expectError(UnsupportedOperationException)
    }

    def "byPage IA"() {
        setup:
        OffsetDateTime startTime = OffsetDateTime.MIN
        OffsetDateTime endTime = OffsetDateTime.MAX
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed)

        when:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime)
        def sv = StepVerifier.create(pagedFlux.byPage(null,  -35))

        then:
        sv.expectError(IllegalArgumentException)
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

    def setupEvents() {
        mockEvents = new LinkedList<>()
        mockCursors = new LinkedList<>()
        mockEventWrappers = new LinkedList<>()

        for (int i = 0; i < 10; i++) {
            BlobChangefeedEvent event = MockedChangefeedResources.getMockBlobChangefeedEvent(i)
            mockEvents.add(event)
        }
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:15:00", "shard0", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:16:00", "shard1", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-10-02T20:17:00", "shard2", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-02T20:01:00", "shard3", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-03T20:08:00", "shard4", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-04T20:06:00", "shard5", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-11-05T20:04:00", "shard6", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:03:00", "shard7", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:06:00", "shard8", null, 0, 0))
        mockCursors.add(new ChangefeedCursor("2020-10-02T20:15:00", "2016-12-02T20:12:00", "shard9", null, 0, 0))
        for (int i = 0; i < 10; i++) {
            mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(i), mockCursors.get(i)))
        }
    }

}
