package com.azure.storage.blob.changefeed

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import spock.lang.Ignore
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
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

    @Requires( { playbackMode() })
    def "historical"() {
        setup:
        /* Uncomment when recording. */
//        OffsetDateTime currentTime = OffsetDateTime.now()
//        System.out.println(currentTime) /* Note: Account the offset when adjusting the below date. */

        /* Update and uncomment after recording. */
        OffsetDateTime currentTime = OffsetDateTime.of(2020, 8, 10, 23, 59, 0, 597700200, ZoneOffset.UTC)

        BlobChangefeedPagedFlux flux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()
            .getEvents(currentTime.minusHours(2), currentTime.plusHours(1))
        when:
        def sv = StepVerifier.create(
            flux
        )
        then:
        sv.expectNextCount(1790) /* Note this number should be adjusted to verify the number of events expected */
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

    @Requires( { playbackMode() })
    def "cursor format"() {
        setup:
        /* Hardcoded for playback stability. If modifying, make sure to re-record. */
        OffsetDateTime startTime = OffsetDateTime.of(2020, 7, 30, 23, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime endTime = OffsetDateTime.of(2020, 7, 30, 23, 15, 0, 0, ZoneOffset.UTC)

        when: "Iterate over first two pages"
        Iterator<BlobChangefeedPagedResponse> iterator = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildClient()
            .getEvents(startTime, endTime)
            .iterableByPage(50)
            .iterator()
        BlobChangefeedPagedResponse lastPage = null
        int pages = 0
        int i = 1
        for (BlobChangefeedPagedResponse page : iterator) {
            for (BlobChangefeedEvent event : page.getValue()) {
                i++
            }
            pages++
            lastPage = page
            if (pages > 2) {
                break
            }
        }
        String continuationToken = lastPage.getContinuationToken()
        ChangefeedCursor cursor = ChangefeedCursor.deserialize(continuationToken, new ClientLogger(ChangefeedNetworkTest.class))

        then:
        /* You may need to update expected values when re-recording */
        cursor.getCursorVersion() == 1
        Base64.getEncoder().encodeToString(cursor.getUrlHash()) == "0d+T8JRzJjMbtLIDi1P4gA==" /* Note this will not match the recorded one since playback uses a different account name. This hash is the playback account url hash. */
        cursor.getEndTime() == OffsetDateTime.of(2020, 7, 31, 0, 0, 0, 0, ZoneOffset.UTC)
        cursor.getCurrentSegmentCursor().getSegmentPath() == "idx/segments/2020/07/30/2300/meta.json"
        cursor.getCurrentSegmentCursor().getCurrentShardPath() == "log/00/2020/07/30/2300/"
        cursor.getCurrentSegmentCursor().getShardCursors().size() == 1
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == "log/00/2020/07/30/2300/00000.avro"
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == 89996
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 9
    }

}
