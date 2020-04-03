package com.azure.storage.blob.changefeed


import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor
import spock.lang.Specification

import java.time.OffsetDateTime

class BlobChangefeedCursorTest extends Specification {

    OffsetDateTime endTime = OffsetDateTime.now()
    OffsetDateTime segmentTime = OffsetDateTime.now()
    String shardPath = "shardPath"
    String chunkPath = "chunkPath"
    Long eventIndex = 83

    def "constructor"() {
        when:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == null
        cursor.getShardPath() == null
        cursor.getChunkPath() == null
        cursor.getEventIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toSegmentCursor"() {
        when:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == null
        cursor.getChunkPath() == null
        cursor.getEventIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toShardCursor"() {
        when:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == null
        cursor.getEventIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toChunkCursor"() {
        when:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == chunkPath
        cursor.getEventIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toEventCursor"() {
        when:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(eventIndex)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == chunkPath
        cursor.getEventIndex() == eventIndex
        cursor.isEventToBeProcessed() == null
    }

    def "serialize"() {
        setup:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(eventIndex)

        when:
        String serialized = cursor.serialize()

        then:
        serialized == "{" + "\"endTime\":\"" + endTime.toString() + "\"," + "\"segmentTime\":\"" + segmentTime.toString() + "\"," + "\"shardPath\":\"" + shardPath + "\"," + "\"chunkPath\":\"" + chunkPath + "\"," + "\"eventIndex\":" + eventIndex + "," + "\"eventToBeProcessed\":null" + "}"
    }

    def "deserialize"() {
        setup:
        BlobChangefeedCursor cursor = new BlobChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(eventIndex)
        String serialized = cursor.serialize()

        when:
        BlobChangefeedCursor deserialized = BlobChangefeedCursor.deserialize(serialized)

        then:
        deserialized.equals(cursor)
    }

}
