package com.azure.storage.blob.changefeed

import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
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
        cursor.getShardCursors() == null
    }

    def "toShardCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath, new HashMap<String, ShardCursor>())

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getShardCursors().size() == 0
    }

    def "toEventCursor"() {
        when:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath, new HashMap<String, ShardCursor>())
            .toEventCursor(chunkPath, blockOffset, objectBlockIndex)

        then:
        cursor.getEndTime() == endTime.toString()
        cursor.getSegmentTime() == segmentTime.toString()
        cursor.getShardPath() == shardPath
        cursor.getShardCursor(shardPath).getChunkPath() == chunkPath
        cursor.getShardCursor(shardPath).getBlockOffset() == blockOffset
        cursor.getShardCursor(shardPath).getObjectBlockIndex() == objectBlockIndex
    }

    def "serialize"() {
        setup:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath, new HashMap<String, ShardCursor>())
            .toEventCursor(chunkPath, blockOffset, objectBlockIndex)

        when:
        String serialized = cursor.serialize()

        then:
        serialized == "{" + "\"endTime\":\"" + endTime.toString() + "\"," + "\"segmentTime\":\"" + segmentTime.toString() + "\"," + "\"shardCursors\":{\"" + shardPath + "\":{\"chunkPath\":\"" + chunkPath + "\"," + "\"blockOffset\":" + blockOffset + "," + "\"objectBlockIndex\":" + objectBlockIndex + "}},\"shardPath\":\"" + shardPath + "\"}"
    }

    def "deserialize"() {
        setup:
        ChangefeedCursor cursor = new ChangefeedCursor(endTime)
            .toSegmentCursor(segmentTime)
            .toShardCursor(shardPath, new HashMap<String, ShardCursor>())
            .toEventCursor(chunkPath, blockOffset, objectBlockIndex)
        String serialized = cursor.serialize()

        when:
        ChangefeedCursor deserialized = ChangefeedCursor.deserialize(serialized)

        then:
        cursor.getEndTime() == deserialized.getEndTime()
        cursor.getSegmentTime() == deserialized.getSegmentTime()
        cursor.getShardPath() == deserialized.getShardPath()
        cursor.getShardCursors() == deserialized.getShardCursors()
    }

}
