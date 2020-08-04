package com.azure.storage.blob.changefeed

import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import spock.lang.Specification

import javax.xml.bind.DatatypeConverter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
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
        OffsetDateTime endTime = OffsetDateTime.MAX
        List<ShardCursor> shardCursors = new ArrayList<>()
        shardCursors.add(new ShardCursor("log/00/2020/08/02/2300/00000.avro", 2434, 2))
        shardCursors.add(new ShardCursor("log/01/2020/08/02/2300/00000.avro", 18954, 15))
        SegmentCursor segmentCursor = new SegmentCursor("idx/segments/2020/08/02/2300/meta.json", shardCursors, "log/01/2020/08/02/2300/")
        byte[] urlHash = MessageDigest.getInstance("MD5").digest('https://testaccount.blob.core.windows.net/$blobchangefeed'.getBytes(StandardCharsets.UTF_8))

        BlobChangefeedCursor cursor = new BlobChangefeedCursor(1, urlHash, endTime, segmentCursor)

        when:
        String serialized = cursor.serialize()

        then:
        serialized == '{"CursorVersion":1,"UrlHash":"47n9w8I/1TCXguW7cH7ePw==","EndTime":"+999999999-12-31T23:59:59.999999999-18:00","CurrentSegmentCursor":{"ShardCursors":[{"CurrentChunkPath":"log/00/2020/08/02/2300/00000.avro","BlockOffset":2434,"EventIndex":2},{"CurrentChunkPath":"log/01/2020/08/02/2300/00000.avro","BlockOffset":18954,"EventIndex":15}],"CurrentShardPath":"log/01/2020/08/02/2300/","SegmentPath":"idx/segments/2020/08/02/2300/meta.json"}}'
    }

    def "deserialize"() {
        setup:
        String cursor = '{"CursorVersion":1,"UrlHash":"47n9w8I/1TCXguW7cH7ePw==","EndTime":"+999999999-12-31T23:59:59.999999999-18:00","CurrentSegmentCursor":{"ShardCursors":[{"CurrentChunkPath":"log/00/2020/08/02/2300/00000.avro","BlockOffset":2434,"EventIndex":2},{"CurrentChunkPath":"log/01/2020/08/02/2300/00000.avro","BlockOffset":18954,"EventIndex":15}],"CurrentShardPath":"log/01/2020/08/02/2300/","SegmentPath":"idx/segments/2020/08/02/2300/meta.json"}}'

        when:
        BlobChangefeedCursor deserialized = BlobChangefeedCursor.deserialize(cursor, new ClientLogger(ChangefeedCursorTest.class))

        then:
        deserialized.getCursorVersion() == 1
        deserialized.getUrlHash() == MessageDigest.getInstance("MD5").digest('https://testaccount.blob.core.windows.net/$blobchangefeed'.getBytes(StandardCharsets.UTF_8))
        deserialized.getEndTime() == OffsetDateTime.MAX
        List<ShardCursor> shardCursors = new ArrayList<>()
        shardCursors.add(new ShardCursor("log/00/2020/08/02/2300/00000.avro", 2434, 2))
        deserialized.getCurrentSegmentCursor().getSegmentPath() == "idx/segments/2020/08/02/2300/meta.json"
        deserialized.getCurrentSegmentCursor().getCurrentShardPath() == "log/01/2020/08/02/2300/"
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == "log/00/2020/08/02/2300/00000.avro"
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == 2434
        deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 2
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == "log/01/2020/08/02/2300/00000.avro"
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == 18954
        deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == 15
    }

}
