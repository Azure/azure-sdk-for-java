package com.azure.storage.blob.changefeed

import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import spock.lang.Specification

import java.time.OffsetDateTime

class ChangefeedCursorTest extends Specification {

    OffsetDateTime endTime = OffsetDateTime.now()
    OffsetDateTime segmentTime = OffsetDateTime.now()
    String shardPath = "shardPath"
    String chunkPath = "chunkPath"
    Long eventIndex = 83

    def "constructor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == null
        cursor.getShardPath() == null
        cursor.getShardCursors() == null
    }

    def "toSegmentCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == null
        cursor.getChunkPath() == null
        cursor.getObjectBlockIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toShardCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == null
        cursor.getObjectBlockIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toChunkCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == chunkPath
        cursor.getObjectBlockIndex() == null
        cursor.isEventToBeProcessed() == null
    }

    def "toEventCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(eventIndex)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == chunkPath
        cursor.getObjectBlockIndex() == eventIndex
        cursor.isEventToBeProcessed() == null
    }

    def "serialize"() {
        setup:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
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
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(eventIndex)
        String serialized = cursor.serialize()

        when:
        ChangefeedCursor deserialized = ChangefeedCursor.deserialize(serialized)

        then:
        deserialized.equals(cursor)
    }

}
