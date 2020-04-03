package com.azure.storage.blob.changefeed.implementation.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for BlobChangefeed.
 */
@Fluent
public class BlobChangefeedCursor {

    private static final ClientLogger logger = new ClientLogger(BlobChangefeedCursor.class);

    private String endTime;
    private String segmentTime;
    private String shardPath;
    private String chunkPath;
    private Long eventIndex;

    /**
     * This parameter is used internally by the changefeed only on user provided cursors to determine if the
     * user cursor was hit yet. Once hit, this is set to true, allowing subsequent events to be deserialized and
     * provided back to the user.
     */
    private Boolean eventToBeProcessed;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public BlobChangefeedCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     *
     * @param endTime The changefeed's end time (the future changefeed end time).
     * @param segmentTime The last segment processed (the future changefeed start time).
     * @param shardPath The last shard processed.
     * @param chunkPath The last chunk processed.
     * @param eventIndex The last event processed.
     */
    private BlobChangefeedCursor(String endTime, String segmentTime, String shardPath, String chunkPath,
        Long eventIndex) {
        this.endTime = endTime;
        this.segmentTime = segmentTime;
        this.chunkPath = chunkPath;
        this.shardPath = shardPath;
        this.eventIndex = eventIndex;
    }

    /**
     * Creates a new changefeed level cursor with the specified end time.
     *
     * @param endTime The {@link OffsetDateTime end time}.
     */
    public BlobChangefeedCursor(OffsetDateTime endTime) {
        this(endTime.toString(), null, null, null, null);
    }

    /**
     * Creates a new segment level cursor from a changefeed level cursor with the specified time associated with the
     * segment.
     *
     * @param segmentTime The {@link OffsetDateTime segment time}.
     */
    public BlobChangefeedCursor toSegmentCursor(OffsetDateTime segmentTime) {
        return new BlobChangefeedCursor(this.getEndTime(), segmentTime.toString(),
            null, null, null);
    }

    /**
     * Creates a new shard level cursor from a segment level cursor with the specified path associated with the shard.
     *
     * @param shardPath The path to the shard.
     */
    public BlobChangefeedCursor toShardCursor(String shardPath) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            shardPath, null, null);
    }

    /**
     * Creates a new chunk level cursor from a shard level cursor with the specified path associated with the chunk.
     *
     * @param chunkPath The path to the chunk.
     */
    public BlobChangefeedCursor toChunkCursor(String chunkPath) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            this.getShardPath(), chunkPath, null);
    }

    /**
     * Creates a new event level cursor from a chunk level cursor with the specified event number.
     *
     * @param eventIndex The index to the event.
     */
    public BlobChangefeedCursor toEventCursor(Long eventIndex) {
        return new BlobChangefeedCursor(this.getEndTime(), this.getSegmentTime(),
            this.getShardPath(), this.getChunkPath(), eventIndex);
    }

    /**
     * @return the end time.
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @return the segment time.
     */
    public String getSegmentTime() {
        return segmentTime;
    }

    /**
     * @return the chunk path.
     */
    public String getChunkPath() {
        return chunkPath;
    }

    /**
     * @return the shard path.
     */
    public String getShardPath() {
        return shardPath;
    }

    /**
     * @return the event index.
     */
    public Long getEventIndex() {
        return eventIndex;
    }

    /**
     * @param endTime the end time.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * @param segmentTime the segment time.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setSegmentTime(String segmentTime) {
        this.segmentTime = segmentTime;
        return this;
    }

    /**
     * @param shardPath the shard path.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setShardPath(String shardPath) {
        this.shardPath = shardPath;
        return this;
    }

    /**
     * @param chunkPath the chunk path.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setChunkPath(String chunkPath) {
        this.chunkPath = chunkPath;
        return this;
    }

    /**
     * @param eventIndex the event index.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setEventIndex(Long eventIndex) {
        this.eventIndex = eventIndex;
        return this;
    }

    /**
     * @return whether or not the event should be processed.
     */
    public Boolean isEventToBeProcessed() {
        return eventToBeProcessed;
    }

    /**
     * @param eventToBeProcessed whether or not the event should be processed.
     * @return the updated BlobChangefeedCursor
     */
    public BlobChangefeedCursor setEventToBeProcessed(Boolean eventToBeProcessed) {
        this.eventToBeProcessed = eventToBeProcessed;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlobChangefeedCursor)) {
            return false;
        }

        BlobChangefeedCursor other = (BlobChangefeedCursor) obj;

        return other.getEndTime().equals(this.getEndTime())
            && other.getSegmentTime().equals(this.getSegmentTime())
            && other.getShardPath().equals(this.getShardPath())
            && other.getChunkPath().equals(this.getChunkPath())
            && other.getEventIndex().equals(this.getEventIndex());
    }

    /**
     * Serializes a {@link BlobChangefeedCursor} into a String.
     *
     * @return The resulting serialized cursor.
     */
    public String serialize() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Deserializes a String into a {@link BlobChangefeedCursor}.
     *
     * @param cursor The cursor to deserialize.
     * @return The resulting {@link BlobChangefeedCursor cursor}.
     */
    public static BlobChangefeedCursor deserialize(String cursor) {
        try {
            return new ObjectMapper().readValue(cursor, BlobChangefeedCursor.class);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
