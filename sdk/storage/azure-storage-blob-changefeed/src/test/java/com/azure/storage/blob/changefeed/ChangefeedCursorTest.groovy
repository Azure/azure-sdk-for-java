package com.azure.storage.blob.changefeed

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.OffsetDateTime

class ChangefeedCursorTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    String urlHost = 'testaccount.blob.core.windows.net'
    OffsetDateTime endTime = OffsetDateTime.MAX

    String segmentPath = "idx/segments/2020/08/02/2300/meta.json"
    String currentShardPath0 = "log/00/2020/08/02/2300/"
    String currentShardPath1 = "log/01/2020/08/02/2300/"

    String chunk0 = "log/00/2020/08/02/2300/00000.avro"
    String chunk1 = "log/00/2020/08/02/2300/00001.avro"
    String chunk2 = "log/01/2020/08/02/2300/00000.avro"

    long offset0 = 2434
    long offset1 = 18954

    long index0 = 2
    long index1 = 15

    def "constructor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(urlHost, endTime)

        then:
        cursor.getCursorVersion() == 1
        cursor.getUrlHost() == urlHost
        cursor.getEndTime() == endTime
        cursor.getCurrentSegmentCursor() == null
    }

    def "toSegmentCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)

        then:
        cursor.getCursorVersion() == 1
        cursor.getUrlHost() == urlHost
        cursor.getEndTime() == endTime
        cursor.getCurrentSegmentCursor() != null
        cursor.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        cursor.getCurrentSegmentCursor().getShardCursors() != null
        cursor.getCurrentSegmentCursor().getShardCursors().size() == 0
        cursor.getCurrentSegmentCursor().getCurrentShardPath() == null
    }

    def "toShardCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)

        then:
        cursor.getCursorVersion() == 1
        cursor.getUrlHost() == urlHost
        cursor.getEndTime() == endTime
        cursor.getCurrentSegmentCursor() != null
        cursor.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        cursor.getCurrentSegmentCursor().getShardCursors() != null
        cursor.getCurrentSegmentCursor().getShardCursors().size() == 0
        cursor.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
    }

    def "toEventCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)
            .toEventCursor(chunk0, offset0, index0)

        then:
        cursor.getCursorVersion() == 1
        cursor.getUrlHost() == urlHost
        cursor.getEndTime() == endTime
        cursor.getCurrentSegmentCursor() != null
        cursor.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        cursor.getCurrentSegmentCursor().getShardCursors() != null
        cursor.getCurrentSegmentCursor().getShardCursors().size() == 1
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk0
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset0
        cursor.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == index0
        cursor.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
    }

    def "state is saved in cursor across events"() {
        /* Note only state we care about is across events. */
        when:
        ChangefeedCursor changefeedCursor = new ChangefeedCursor(urlHost, endTime)

        ChangefeedCursor segmentCursor = changefeedCursor.toSegmentCursor(segmentPath, null)

        ChangefeedCursor shardCursor0 = segmentCursor.toShardCursor(currentShardPath0)

        ChangefeedCursor eventCursor0 = shardCursor0.toEventCursor(chunk0, offset0, 0)
        ChangefeedCursor eventCursor1 = shardCursor0.toEventCursor(chunk0, offset0, 1)
        ChangefeedCursor eventCursor2 = shardCursor0.toEventCursor(chunk1, offset1, 0) /* Make sure it still works across chunks. */

        ChangefeedCursor shardCursor1 = segmentCursor.toShardCursor(currentShardPath1)

        ChangefeedCursor eventCursor3 = shardCursor1.toEventCursor(chunk2, offset1, 0)
        ChangefeedCursor eventCursor4 = shardCursor1.toEventCursor(chunk2, offset1, 1)
        ChangefeedCursor eventCursor5 = shardCursor1.toEventCursor(chunk2, offset1, 2)

        then:
        // Changefeed cursor.
        changefeedCursor.getCursorVersion() == 1
        changefeedCursor.getUrlHost() == urlHost
        changefeedCursor.getEndTime() == endTime
        changefeedCursor.getCurrentSegmentCursor() == null

        // Segment cursor (the shard cursors list should be equivalent to the last event cursor.)
        segmentCursor.getCursorVersion() == 1
        segmentCursor.getUrlHost() == urlHost
        segmentCursor.getEndTime() == endTime
        segmentCursor.getCurrentSegmentCursor() != null
        segmentCursor.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        segmentCursor.getCurrentSegmentCursor().getCurrentShardPath() == null
        segmentCursor.getCurrentSegmentCursor().getShardCursors() != null
        segmentCursor.getCurrentSegmentCursor().getShardCursors().size() == 2
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 2

        // Shard cursor 0 (Should be equivalent to last event cursor with correct shard cursor populated)
        shardCursor0.getCursorVersion() == 1
        shardCursor0.getUrlHost() == urlHost
        shardCursor0.getEndTime() == endTime
        shardCursor0.getCurrentSegmentCursor() != null
        shardCursor0.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        shardCursor0.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        shardCursor0.getCurrentSegmentCursor().getShardCursors() != null
        shardCursor0.getCurrentSegmentCursor().getShardCursors().size() == 2
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 2

        // Shard cursor 1 (Should be equivalent to last event cursor with correct shard cursor populated)
        shardCursor1.getCursorVersion() == 1
        shardCursor1.getUrlHost() == urlHost
        shardCursor1.getEndTime() == endTime
        shardCursor1.getCurrentSegmentCursor() != null
        shardCursor1.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        shardCursor1.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        shardCursor1.getCurrentSegmentCursor().getShardCursors() != null
        shardCursor1.getCurrentSegmentCursor().getShardCursors().size() == 2
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 2

        // Event cursor 0
        eventCursor0.getCursorVersion() == 1
        eventCursor0.getUrlHost() == urlHost
        eventCursor0.getEndTime() == endTime
        eventCursor0.getCurrentSegmentCursor() != null
        eventCursor0.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor0.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        eventCursor0.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor0.getCurrentSegmentCursor().getShardCursors().size() == 1
        eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk0
        eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset0
        eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0

        // Event cursor 1
        eventCursor1.getCursorVersion() == 1
        eventCursor1.getUrlHost() == urlHost
        eventCursor1.getEndTime() == endTime
        eventCursor1.getCurrentSegmentCursor() != null
        eventCursor1.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor1.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        eventCursor1.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor1.getCurrentSegmentCursor().getShardCursors().size() == 1
        eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk0
        eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset0
        eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 1

        // Event cursor 2
        eventCursor2.getCursorVersion() == 1
        eventCursor2.getUrlHost() == urlHost
        eventCursor2.getEndTime() == endTime
        eventCursor2.getCurrentSegmentCursor() != null
        eventCursor2.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor2.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        eventCursor2.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor2.getCurrentSegmentCursor().getShardCursors().size() == 1
        eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0

        // Event cursor 3
        eventCursor3.getCursorVersion() == 1
        eventCursor3.getUrlHost() == urlHost
        eventCursor3.getEndTime() == endTime
        eventCursor3.getCurrentSegmentCursor() != null
        eventCursor3.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor3.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        eventCursor3.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor3.getCurrentSegmentCursor().getShardCursors().size() == 2
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 0

        // Event cursor 4
        eventCursor4.getCursorVersion() == 1
        eventCursor4.getUrlHost() == urlHost
        eventCursor4.getEndTime() == endTime
        eventCursor4.getCurrentSegmentCursor() != null
        eventCursor4.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor4.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        eventCursor4.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor4.getCurrentSegmentCursor().getShardCursors().size() == 2
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 1

        // Event cursor 5
        eventCursor5.getCursorVersion() == 1
        eventCursor5.getUrlHost() == urlHost
        eventCursor5.getEndTime() == endTime
        eventCursor5.getCurrentSegmentCursor() != null
        eventCursor5.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        eventCursor5.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        eventCursor5.getCurrentSegmentCursor().getShardCursors() != null
        eventCursor5.getCurrentSegmentCursor().getShardCursors().size() == 2
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk1
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset1
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 0
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 2
    }

    def "serialize"() {
        setup:
        List<ShardCursor> shardCursors = new ArrayList<>()
        shardCursors.add(new ShardCursor(chunk0, offset0, index0))
        shardCursors.add(new ShardCursor(chunk2, offset1, index1))
        SegmentCursor segmentCursor = new SegmentCursor(segmentPath, shardCursors, currentShardPath1)


        ChangefeedCursor cursor = new ChangefeedCursor(1, urlHost, endTime, segmentCursor)

        when:
        String serialized = cursor.serialize()

        then:
        serialized == '{"CursorVersion":1,"UrlHost":"testaccount.blob.core.windows.net","EndTime":"+999999999-12-31T23:59:59.999999999-18:00","CurrentSegmentCursor":{"ShardCursors":[{"CurrentChunkPath":"log/00/2020/08/02/2300/00000.avro","BlockOffset":2434,"EventIndex":2},{"CurrentChunkPath":"log/01/2020/08/02/2300/00000.avro","BlockOffset":18954,"EventIndex":15}],"CurrentShardPath":"log/01/2020/08/02/2300/","SegmentPath":"idx/segments/2020/08/02/2300/meta.json"}}'
    }

    def "deserialize"() {
        setup:
        String cursor = '{"CursorVersion":1,"UrlHost":"testaccount.blob.core.windows.net","EndTime":"+999999999-12-31T23:59:59.999999999-18:00","CurrentSegmentCursor":{"ShardCursors":[{"CurrentChunkPath":"log/00/2020/08/02/2300/00000.avro","BlockOffset":2434,"EventIndex":2},{"CurrentChunkPath":"log/01/2020/08/02/2300/00000.avro","BlockOffset":18954,"EventIndex":15}],"CurrentShardPath":"log/01/2020/08/02/2300/","SegmentPath":"idx/segments/2020/08/02/2300/meta.json"}}'

        when:
        ChangefeedCursor deserialized = ChangefeedCursor.deserialize(cursor, new ClientLogger(ChangefeedCursorTest.class))

        then:
        deserialized.getCursorVersion() == 1
        deserialized.getUrlHost() == urlHost
        deserialized.getEndTime() == endTime
        deserialized.getCurrentSegmentCursor().getSegmentPath() == segmentPath
        deserialized.getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunk0
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == offset0
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == index0
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunk2
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == offset1
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == index1
    }

}
