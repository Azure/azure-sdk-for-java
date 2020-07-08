package com.azure.storage.blob.changefeed

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor

import spock.lang.Specification

import java.time.OffsetDateTime

class ChangefeedCursorTest extends Specification {

    OffsetDateTime endTime = OffsetDateTime.now()
    OffsetDateTime segmentTime = OffsetDateTime.now()
    String shardPath = "shardPath"
    String chunkPath = "chunkPath"
    long blockOffset = 83
    long objectBlockIndex = 36

    def "constructor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == null
        cursor.getShardPath() == null
    }

    def "toSegmentCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == null
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
    }

    def "toEventCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(blockOffset, objectBlockIndex)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getChunkPath() == chunkPath
        cursor.getBlockOffset() == blockOffset
        cursor.getObjectBlockIndex() == objectBlockIndex
    }

    def "serialize"() {
        setup:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(blockOffset, objectBlockIndex)

        when:
        String serialized = cursor.serialize()

        then:
        serialized == "{\"endTime\":\"" + endTime.toString() + "\",\"segmentTime\":\"" +segmentTime.toString() + "\",\"shardPath\":\"" + shardPath + "\",\"chunkPath\":\"" + chunkPath + "\",\"blockOffset\":" + blockOffset + ",\"objectBlockIndex\":" + objectBlockIndex + "}"
    }

    def "deserialize"() {
        setup:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath)
            .toChunkCursor(chunkPath)
            .toEventCursor(blockOffset, objectBlockIndex)
        String serialized = cursor.serialize()

        when:
        ChangefeedCursor deserialized = ChangefeedCursor.deserialize(serialized, new ClientLogger(ChangefeedCursorTest.class))

        then:
        cursor.getEndTime() == deserialized.getEndTime()
        cursor.getSegmentTime() == deserialized.getSegmentTime()
        cursor.getShardPath() == deserialized.getShardPath()
    }

}
