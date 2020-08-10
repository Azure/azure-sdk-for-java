package com.azure.storage.blob.changefeed

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import spock.lang.Ignore
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.stream.Stream

/*
These tests requires an accounts having a fixed size changefeed It is not feasible to setup that
relationship programmatically, so we have recorded a successful interaction and only test recordings.
 */
class ChangefeedNetworkTest extends APISpec {

    @Ignore("For debugging larger Change Feeds locally. Infeasible to record due to large number of events. ")
    def "min"() {
        setup:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents()
        when:
        def sv = StepVerifier.create(
            flux
//            .map({ event -> System.out.println(event); return event; })
        )
        then:
        sv.expectNextCount(17513) /* Note this number should be adjusted to verify the number of events expected */
            .verifyComplete()
    }

    @Ignore("For debugging larger Change Feeds locally. Infeasible to record due to large number of events.")
    def "page size"() {
        setup:
        int pageSize = 10000
        int expectedPageCount = 50
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents()

        when:
        def sv = StepVerifier.create(
            pagedFlux.byPage(pageSize)
//                .map({ page -> System.out.println(page.getValue().size()); return page; })
        )

        then:
        sv.expectNextCount(expectedPageCount)
            .verifyComplete()
    }

    /* TODO : Record this. */
    @Ignore("For debugging larger Change Feeds locally.")
    def "historical"() {
        setup:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(OffsetDateTime.now().minusHours(2), OffsetDateTime.now().plusHours(1))
        when:
        def sv = StepVerifier.create(
            flux
//            .map({ event -> System.out.println(event); return event; })
        )
        then:
        sv.expectNextCount(1599) /* Note this number should be adjusted to verify the number of events expected */
            .verifyComplete()
    }

    /* TODO : Cursor test. */

    @Requires( { playbackMode() })
    def "last hour"() {
        setup:
        /* Uncomment when recording. */
//        OffsetDateTime startTime = OffsetDateTime.now()
//        System.out.println(startTime) /* Note: Account the offset when adjusting the below date. */

        /* Update and uncomment after recording. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 8, 10, 20, 0, 0, 0, ZoneOffset.UTC)
        when:
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, OffsetDateTime.MAX)
        Stream<BlobChangefeedEvent> stream = iterable.stream()

        then:
        stream.count() > 0
    }

    @Requires( { playbackMode() })
    def "resume from the middle of a chunk"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 7, 30, 23, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 7, 30, 23, 15, 0, 0, ZoneOffset.UTC)

        /* Collect all events within range */
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
        Set<String> allEventIds = new HashSet<String>(iterable.stream().collect( { event -> event.getId() } ))

        when: "Iterate over the first two pages."
        Set<String> eventIds1 = new HashSet<>()
        Iterator<BlobChangefeedPagedResponse> iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
            .iterableByPage(50)
            .iterator()
        BlobChangefeedPagedResponse lastPage = null
        int pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds1.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        String continuationToken = lastPage.getContinuationToken()
        ChangefeedCursor cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        long blockOffset = cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() /* Just get the first shard cursor since we only have one shard in this test account. */

        then: "Check block offset is in middle."
        blockOffset > 0 /* Make sure we actually finish in the middle of the chunk. If this fails, play with test data to pass. */

        when: "Iterate over the next two pages."
        Set<String> eventIds2 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        lastPage = null
        pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds2.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        continuationToken = lastPage.getContinuationToken()
        cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        blockOffset = cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() /* Just get the first shard cursor since we only have one shard in this test account. */

        then: "Check block offset is in middle."
        blockOffset > 0 /* Make sure we actually finish in the middle of the chunk. If this fails, play with test data to pass. */

        when: "Iterate over the remaining pages."
        Set<String> eventIds3 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds3.add(event.getId()) } )
        }

        then:
        Set<String> unionIds = new HashSet<>()
        unionIds.addAll(eventIds1)
        unionIds.addAll(eventIds2)
        unionIds.addAll(eventIds3)

        allEventIds.size() > 0
        eventIds1.size() > 0
        eventIds2.size() > 0
        eventIds3.size() > 0
        allEventIds.size() == eventIds1.size() + eventIds2.size() + eventIds3.size()

        allEventIds == unionIds
    }

    /**
     * This test requires an account with changefeed where multiple shards has been created.
     * However. Some shards should be empty. Easiest way to set this up is to have just one blob and keep modifying it.
     * Changes related to same blobName are guaranteed to end up in same shard.
     */
    @Requires( { playbackMode() })
    def "resume from the middle of a chunk with some empty shards"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 8, 5, 17, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 8, 5, 17, 15, 0, 0, ZoneOffset.UTC)
        int expectedNumberOfNonEmptyShards = 1

        /* Collect all events within range */
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
        Set<String> allEventIds = new HashSet<String>(iterable.stream().collect( { event -> event.getId() } ))

        when: "Iterate over the first two pages."
        Set<String> eventIds1 = new HashSet<>()
        Iterator<BlobChangefeedPagedResponse> iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
            .iterableByPage(50)
            .iterator()
        BlobChangefeedPagedResponse lastPage = null
        int pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds1.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        String continuationToken = lastPage.getContinuationToken()
        ChangefeedCursor cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        int numberOfNonEmptyShards = cursor.getCurrentSegmentCursor().getShardCursors().size()
        long blockOffset = cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() /* Just get the first shard cursor since we only have one shard in this test account. */

        then: "Check cursor info."
        numberOfNonEmptyShards == expectedNumberOfNonEmptyShards
        blockOffset > 0 /* Make sure we actually finish in the middle of the chunk. If this fails, play with test data to pass. */

        when: "Iterate over the next two pages."
        Set<String> eventIds2 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        lastPage = null
        pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds2.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        continuationToken = lastPage.getContinuationToken()
        cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        numberOfNonEmptyShards = cursor.getCurrentSegmentCursor().getShardCursors().size()
        blockOffset = cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() /* Just get the first shard cursor since we only have one shard in this test account. */

        then: "Check cursor info."
        numberOfNonEmptyShards == expectedNumberOfNonEmptyShards
        blockOffset > 0 /* Make sure we actually finish in the middle of the chunk. If this fails, play with test data to pass. */

        when: "Iterate over the remaining pages."
        Set<String> eventIds3 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds3.add(event.getId()) } )
        }

        then:
        Set<String> unionIds = new HashSet<>()
        unionIds.addAll(eventIds1)
        unionIds.addAll(eventIds2)
        unionIds.addAll(eventIds3)

        allEventIds.size() > 0
        eventIds1.size() > 0
        eventIds2.size() > 0
        eventIds3.size() > 0
        allEventIds.size() == eventIds1.size() + eventIds2.size() + eventIds3.size()

        allEventIds == unionIds
    }

    /**
     * This test requires an account with changefeed where multiple shards has been created.
     * However. Some shards should be empty. Easiest way to set this up is to have just one blob and keep modifying it.
     * Changes related to same blobName are guaranteed to end up in same shard.
     */
    @Requires( { playbackMode() })
    def "resume from the middle of a chunk with many non-empty shards"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 8, 5, 17, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 8, 5, 17, 15, 0, 0, ZoneOffset.UTC)

        /* Collect all events within range */
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
        Set<String> allEventIds = new HashSet<String>(iterable.stream().collect( { event -> event.getId() } ))

        when: "Iterate over the first two pages."
        Set<String> eventIds1 = new HashSet<>()
        Iterator<BlobChangefeedPagedResponse> iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
            .iterableByPage(50)
            .iterator()
        BlobChangefeedPagedResponse lastPage = null
        int pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds1.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        String continuationToken = lastPage.getContinuationToken()
        ChangefeedCursor cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        int numberOfNonEmptyShards = cursor.getCurrentSegmentCursor().getShardCursors().size()

        then: "Validate number of non empty shards."
        numberOfNonEmptyShards == 1 /* Note: in Java we do not round robin among shards, we play them sequentially, so another shard path will only show up once the first shard is done iterating through. */

        when: "Iterate over the next two pages."
        Set<String> eventIds2 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        lastPage = null
        pages = 0
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds2.add(event.getId()) } )
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        continuationToken = lastPage.getContinuationToken()
        cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        numberOfNonEmptyShards = cursor.getCurrentSegmentCursor().getShardCursors().size()

        then: "Validate number of non empty shards."
        numberOfNonEmptyShards == 2 /* Note: in Java we do not round robin among shards, we play them sequentially, so another shard path will only show up once the first shard is done iterating through. */

        when: "Iterate over the remaining pages."
        Set<String> eventIds3 = new HashSet<>()
        iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(continuationToken)
            .iterableByPage(50)
            .iterator()
        lastPage = null
        for (BlobChangefeedPagedResponse page : iterator) {
            page.getElements().stream().forEach( { event -> eventIds3.add(event.getId()) } )
            lastPage = page
        }
        continuationToken = lastPage.getContinuationToken()
        cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))
        numberOfNonEmptyShards = cursor.getCurrentSegmentCursor().getShardCursors().size()

        then:
        /* By this point we should have encountered all three shards. */
        numberOfNonEmptyShards == 3 /* Note: in Java we do not round robin among shards, we play them sequentially, so another shard path will only show up once the first shard is done iterating through. */
        Set<String> unionIds = new HashSet<>()
        unionIds.addAll(eventIds1)
        unionIds.addAll(eventIds2)
        unionIds.addAll(eventIds3)

        allEventIds.size() > 0
        eventIds1.size() > 0
        eventIds2.size() > 0
        eventIds3.size() > 0
        allEventIds.size() == eventIds1.size() + eventIds2.size() + eventIds3.size()

        allEventIds == unionIds
    }

    /**
     * To setup account for this test have a steady stream of events (i.e. some changes every 1 minute) that covers at least from an hour before start time
     * to an hour after end time.
     */
    @Requires( { playbackMode() })
    def "already rounded boundaries"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 8, 5, 16, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 8, 5, 18, 0, 0, 0, ZoneOffset.UTC)

        when:
        /* Collect all events within range */
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
        List<BlobChangefeedEvent> events = iterable.stream().collect()

        then:
        events.size() > 1
        events.any { event -> event.getEventTime().isAfter(startTime.plusMinutes(15)) } /* There is some event 15 minutes after start */
        events.any { event -> event.getEventTime().isBefore(endTime.minusMinutes(15)) } /* There is some event 15 minutes before end */
        !events.any { event -> event.getEventTime().isBefore(startTime.minusMinutes(15)) } /* There is no event 15 minutes before start */
        !events.any { event -> event.getEventTime().isAfter(endTime.plusMinutes(15)) } /* There is no event 15 minutes after end */
    }

    /**
     * To setup account for this test have a steady stream of events (i.e. some changes every 1 minute) that covers at least from an hour before start time
     * to an hour after end time.
     */
    @Requires( { playbackMode() })
    def "non rounded boundaries"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 8, 5, 16, 24, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 8, 5, 18, 35, 0, 0, ZoneOffset.UTC)
        OffsetDateTime roundedStartTime = OffsetDateTime.of(2020, 8, 5, 16, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime roundedEndTime = OffsetDateTime.of(2020, 8, 5, 19, 0, 0, 0, ZoneOffset.UTC)

        when:
        /* Collect all events within range */
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
        List<BlobChangefeedEvent> events = iterable.stream().collect()

        then:
        events.size() > 1
        events.any { event -> event.getEventTime().isAfter(roundedStartTime.plusMinutes(15)) } /* There is some event 15 minutes after start */
        events.any { event -> event.getEventTime().isBefore(roundedEndTime.minusMinutes(15)) } /* There is some event 15 minutes before end */
        !events.any { event -> event.getEventTime().isBefore(roundedStartTime.minusMinutes(15)) } /* There is no event 15 minutes before start */
        !events.any { event -> event.getEventTime().isAfter(roundedEndTime.plusMinutes(15)) } /* There is no event 15 minutes after end */
    }

    @Ignore
    def "min resume from cursor"() {
        when:
        String continuationToken = '{"CursorVersion":1,"UrlHash":"5gX9e+3lzudCNddoahIvqg==","EndTime":"+999999999-12-31T23:59:59.999999999-18:00","CurrentSegmentCursor":{"ShardCursors":[{"CurrentChunkPath":"log/00/2020/08/04/2000/00000.avro","BlockOffset":96853,"EventIndex":6}],"CurrentShardPath":"log/00/2020/08/04/2000/","SegmentPath":"idx/segments/2020/08/04/2000/meta.json"}}'
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(continuationToken)
        def sv = StepVerifier.create(flux)
        then:
        sv.expectNextCount(443) /* Note this number should be adjusted to verify the number of events expected if re-recording. */
            .verifyComplete()

    }

    @Requires( { playbackMode() })
    @Unroll
    @Ignore
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
    @Ignore
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
    @Requires( { playbackMode() })
    @Ignore("flaky in CI")
    def "get continuationToken"() {
        when:
        BlobChangefeedPagedIterable iterable = new BlobChangefeedClientBuilder(primaryBlobServiceClient)
            .buildClient()
            .getEvents()
        Iterator<BlobChangefeedPagedResponse> pagedResponses = iterable.iterableByPage(100).iterator()

        def i = 0
        def continuationToken = ""
        while (pagedResponses.hasNext() && i < numPagesToIterate) {
            BlobChangefeedPagedResponse resp = pagedResponses.next()
            continuationToken = resp.getContinuationToken()
            i++
        }

        then:
        continuationToken == expectedToken

        where:
        numPagesToIterate || expectedToken
        33                || "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-06-16T05:00Z\",\"shardPath\":\"log/00/2020/06/16/0500/\",\"chunkPath\":\"log/00/2020/06/16/0500/00000.avro\",\"blockOffset\":3706,\"objectBlockIndex\":1}"
        1                 || "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-12T22:00Z\",\"shardPath\":\"log/00/2020/05/12/2200/\",\"chunkPath\":\"log/00/2020/05/12/2200/00000.avro\",\"blockOffset\":2434,\"objectBlockIndex\":99}"
        5                 || "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-15T22:00Z\",\"shardPath\":\"log/00/2020/05/15/2200/\",\"chunkPath\":\"log/00/2020/05/15/2200/00000.avro\",\"blockOffset\":2434,\"objectBlockIndex\":27}"
        10                || "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-15T22:00Z\",\"shardPath\":\"log/00/2020/05/15/2200/\",\"chunkPath\":\"log/00/2020/05/15/2200/00000.avro\",\"blockOffset\":335596,\"objectBlockIndex\":1}"
    }

    @Unroll
    @Requires( { playbackMode() })
    @Ignore("flaky in CI")
    def "resume continuationToken"() {
        when:
        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(contToken)
        def sv = StepVerifier.create(flux)

        then:
        sv.expectNextCount(numEventsFromContinuationToken) /* Note this number should be adjusted to verify the number of events expected if re-recording. */
            .verifyComplete()

        where:
        contToken                                                                                                                                                                                                                                         || numEventsFromContinuationToken
        "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-06-16T05:00Z\",\"shardPath\":\"log/00/2020/06/16/0500/\",\"chunkPath\":\"log/00/2020/06/16/0500/00000.avro\",\"blockOffset\":3706,\"objectBlockIndex\":1}"     || 0
        "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-12T22:00Z\",\"shardPath\":\"log/00/2020/05/12/2200/\",\"chunkPath\":\"log/00/2020/05/12/2200/00000.avro\",\"blockOffset\":2434,\"objectBlockIndex\":99}"    || 3138
        "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-15T22:00Z\",\"shardPath\":\"log/00/2020/05/15/2200/\",\"chunkPath\":\"log/00/2020/05/15/2200/00000.avro\",\"blockOffset\":2434,\"objectBlockIndex\":27}"    || 2738
        "{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-05-15T22:00Z\",\"shardPath\":\"log/00/2020/05/15/2200/\",\"chunkPath\":\"log/00/2020/05/15/2200/00000.avro\",\"blockOffset\":335596,\"objectBlockIndex\":1}"   || 2238
    }
}
