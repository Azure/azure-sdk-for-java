package com.azure.storage.blob.changefeed

import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
These tests requires an accounts having a fixed size changefeed It is not feasible to setup that
relationship programmatically, so we have recorded a successful interaction and only test recordings.
 */
class ChangefeedNetworkTest extends APISpec {

    @Requires( { playbackMode() })
    def "min"() {
        setup:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents()
        when:
        def sv = StepVerifier.create(flux)
        then:
        sv.expectNextCount(3238) /* Note this number should be adjusted to verify the number of events expected if re-recording. */
            .verifyComplete()
    }

    @Requires( { playbackMode() })
    @Unroll
    def "min byPage"() {
        setup:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents()
        when:
        def sv = StepVerifier.create(
            pagedFlux.byPage(pageSize)
        )
        then:
        sv.expectNextCount(pageCount).verifyComplete()

        where:
        pageSize || pageCount
        50       || 65 /* Note these numbers should be adjusted to verify the number of events expected if re-recording. */
        100      || 33
        1000     || 4
        5000     || 1
    }

    @Unroll
    @Requires( { playbackMode() })
    def "startTime endTime"() {
        setup:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(startTime, endTime)

        when:
        def sv = StepVerifier.create(flux)

        then:
        sv.expectNextCount(numEvents) /* Note this number should be adjusted to verify the number of events expected if re-recording. */
            .verifyComplete()

        where:
        startTime                                                   | endTime                                                     || numEvents
        OffsetDateTime.of(2020, 5, 12, 0, 0, 0, 0, ZoneOffset.UTC)  | OffsetDateTime.of(2020, 5, 13, 0, 0, 0, 0, ZoneOffset.UTC)  || 472
        OffsetDateTime.of(2020, 6, 4, 0, 0, 0, 0, ZoneOffset.UTC)   | OffsetDateTime.of(2020, 6, 18, 0, 0, 0, 0, ZoneOffset.UTC)  || 445
        OffsetDateTime.of(2020, 6, 4, 0, 0, 0, 0, ZoneOffset.UTC)   | OffsetDateTime.of(2020, 6, 11, 3, 0, 0, 0, ZoneOffset.UTC)  || 425
        OffsetDateTime.of(2020, 6, 11, 3, 0, 0, 0, ZoneOffset.UTC)  | OffsetDateTime.of(2020, 6, 18, 0, 0, 0, 0, ZoneOffset.UTC)  || 20
    }

    @Unroll
//    @Requires( { playbackMode() })
    def "continuationToken"() {
        setup:
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceClient)
            .buildClient()
            .getEvents()
        Iterator<BlobChangefeedPagedResponse> pagedResponses = iterable.iterableByPage(100).iterator()

        def i = 0
        def contToken = ""
        while (pagedResponses.hasNext() && i < numPagesToIterate) {
            BlobChangefeedPagedResponse resp = pagedResponses.next()
            contToken = resp.getContinuationToken()
            i++
        }

        when:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(contToken)
        def sv = StepVerifier.create(flux)

        then:
        sv.expectNextCount(numEventsFromContinuationToken) /* Note this number should be adjusted to verify the number of events expected if re-recording. */
            .verifyComplete()

        where:
        numPagesToIterate || numEventsFromContinuationToken
        33                || 0
        1                 || 3138
//        5                 || 2738
//        10                || 2238
    }
}
