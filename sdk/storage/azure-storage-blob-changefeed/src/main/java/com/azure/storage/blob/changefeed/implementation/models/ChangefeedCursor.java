// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for BlobChangefeed.
 */
@Fluent
public class ChangefeedCursor {

    private ClientLogger logger = new ClientLogger(ChangefeedCursor.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String endTime;
    private String segmentTime;
    private String shardPath;
    private String chunkPath;
    private long blockOffset;
    private long objectBlockIndex;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ChangefeedCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    private ChangefeedCursor(String endTime, String segmentTime, String shardPath, String chunkPath, long blockOffset,
        long objectBlockIndex) {
        this.endTime = endTime;
        this.segmentTime = segmentTime;
        this.shardPath = shardPath;
        this.chunkPath = chunkPath;
        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;
    }

    /**
     * Creates a new changefeed level cursor with the specified end time.
     *
     * @param endTime The {@link OffsetDateTime end time}.
     */
    public ChangefeedCursor(OffsetDateTime endTime) {
        this(endTime.toString(), null, null, null, 0, 0);
    }

    /**
     * Creates a new segment level cursor.
     *
     * @param segmentTime The {@link OffsetDateTime segment time}.
     * @return A new segment level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toSegmentCursor(OffsetDateTime segmentTime) {
        return new ChangefeedCursor(this.getEndTime(), segmentTime.toString(), null, null, 0, 0);
    }

    /**
     * Creates a new shard level cursor.
     *
     * @param shardPath The shard path.
     * @return A new shard level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toShardCursor(String shardPath) {
        return new ChangefeedCursor(this.getEndTime(), this.getSegmentTime(), shardPath, null, 0, 0);
    }

    /**
     * Creates a new chunk level cursor.
     *
     * @param chunkPath The chunk path.
     * @return A new chunk level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toChunkCursor(String chunkPath) {
        return new ChangefeedCursor(this.getEndTime(), this.getSegmentTime(), this.getShardPath(), chunkPath, 0, 0);
    }

    /**
     * Creates a new event level cursor.
     *
     * @param blockOffset The block offset.
     * @param objectBlockIndex The object block index.
     * @return A new event level {@link ChangefeedCursor cursor}.
     */
    public ChangefeedCursor toEventCursor(long blockOffset, long objectBlockIndex) {
        return new ChangefeedCursor(this.getEndTime(), this.getSegmentTime(), this.getShardPath(), this.getChunkPath(), blockOffset, objectBlockIndex);

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
     * @return the shard path.
     */
    public String getShardPath() {
        return shardPath;
    }

    /**
     * @return the chunk path.
     */
    public String getChunkPath() {
        return chunkPath;
    }

    /**
     * @return the block offset
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * @return the object block index.
     */
    public long getObjectBlockIndex() {
        return objectBlockIndex;
    }

    /**
     * @param endTime the end time.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setEndTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * @param segmentTime the segment time.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setSegmentTime(String segmentTime) {
        this.segmentTime = segmentTime;
        return this;
    }

    /**
     * @param shardPath the shard path.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setShardPath(String shardPath) {
        this.shardPath = shardPath;
        return this;
    }

    /**
     * @param chunkPath the chunk path.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setChunkPath(String chunkPath) {
        this.chunkPath = chunkPath;
        return this;
    }

    /**
     * @param blockOffset the block offset.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
        return this;
    }

    /**
     * @param objectBlockIndex the object block index.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setObjectBlockIndex(long objectBlockIndex) {
        this.objectBlockIndex = objectBlockIndex;
        return this;
    }

    /**
     * Serializes a {@link ChangefeedCursor} into a String.
     *
     * @return The resulting serialized cursor.
     */
    public String serialize() {
        try {
            return MAPPER.writer().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Deserializes a String into a {@link ChangefeedCursor}.
     *
     * @param cursor The cursor to deserialize.
     * @return The resulting {@link ChangefeedCursor cursor}.
     */
    public static ChangefeedCursor deserialize(String cursor, ClientLogger logger) {
        try {
            return MAPPER.readerFor(ChangefeedCursor.class).readValue(cursor);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChangefeedCursor)) {
            return false;
        }
        ChangefeedCursor that = (ChangefeedCursor) o;
        return Objects.equals(getEndTime(), that.getEndTime())
            && Objects.equals(getSegmentTime(), that.getSegmentTime())
            && Objects.equals(getShardPath(), that.getShardPath())
            && Objects.equals(getChunkPath(), that.getChunkPath())
            && Objects.equals(getBlockOffset(), that.getBlockOffset())
            && Objects.equals(getObjectBlockIndex(), that.getObjectBlockIndex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndTime(), getSegmentTime(), getShardPath(), getChunkPath(), getBlockOffset(),
            getObjectBlockIndex());
    }
}
