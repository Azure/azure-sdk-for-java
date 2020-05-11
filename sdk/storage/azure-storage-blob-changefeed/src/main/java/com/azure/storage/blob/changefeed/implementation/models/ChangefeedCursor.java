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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for BlobChangefeed.
 */
@Fluent
public class ChangefeedCursor {

    private static final ClientLogger logger = new ClientLogger(ChangefeedCursor.class);

    private String endTime;
    private String segmentTime; /* curr segment */
    private Map<String, ShardCursor> shardCursors; /* shards under segment. */
    private String shardPath; /* current shard. */

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ChangefeedCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     *
     * @param endTime The changefeed's end time (the future changefeed end time).
     * @param segmentTime The last segment processed (the future changefeed start time).
     */
    private ChangefeedCursor(String endTime, String segmentTime, Map<String, ShardCursor> shardCursors, String shardPath) {
        this.endTime = endTime;
        this.segmentTime = segmentTime;
        this.shardCursors = shardCursors;
        this.shardPath = shardPath;
    }

    /**
     * Creates a new changefeed level cursor with the specified end time.
     *
     * @param endTime The {@link OffsetDateTime end time}.
     */
    public ChangefeedCursor(OffsetDateTime endTime) {
        this(endTime.toString(), null, null, null);
    }

    public ChangefeedCursor toSegmentCursor(OffsetDateTime segmentTime) {
        return new ChangefeedCursor(this.getEndTime(), segmentTime.toString(), null, null);
    }

    public ChangefeedCursor toShardCursor(String shardPath, Map<String, ShardCursor> shardCursors) {
        return new ChangefeedCursor(this.getEndTime(), this.getSegmentTime(), shardCursors, shardPath);
    }

    public ChangefeedCursor toEventCursor(String chunkPath, long blockOffset, long objectBlockIndex) {
        shardCursors.put(shardPath, new ShardCursor(chunkPath, blockOffset, objectBlockIndex));
        Map<String, ShardCursor> updatedMap = new HashMap<>();
        /* Deep copy the map. */
        for (Map.Entry<String, ShardCursor> entry: shardCursors.entrySet()) {
            updatedMap.put(entry.getKey(), entry.getValue() == null ? null : new ShardCursor(entry.getValue()));
        }
        return new ChangefeedCursor(this.getEndTime(), this.getSegmentTime(), updatedMap, shardPath);
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
     * @return the shard cursors.
     */
    public Map<String, ShardCursor> getShardCursors() {
        return shardCursors;
    }

    /**
     * @param shardPath the shard path.
     * @return the shard cursor associated with the shard path.
     */
    public ShardCursor getShardCursor(String shardPath) {
        return shardCursors.get(shardPath);
    }

    /**
     * @return the shard path.
     */
    public String getShardPath() {
        return shardPath;
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
     * @param shardCursors the shard cursors.
     * @return the updated BlobChangefeedCursor
     */
    public ChangefeedCursor setShardCursors(Map<String, ShardCursor> shardCursors) {
        this.shardCursors = shardCursors;
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
     * Serializes a {@link ChangefeedCursor} into a String.
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
     * Deserializes a String into a {@link ChangefeedCursor}.
     *
     * @param cursor The cursor to deserialize.
     * @return The resulting {@link ChangefeedCursor cursor}.
     */
    public static ChangefeedCursor deserialize(String cursor) {
        try {
            return new ObjectMapper().readValue(cursor, ChangefeedCursor.class);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangefeedCursor)) return false;
        ChangefeedCursor that = (ChangefeedCursor) o;
        return Objects.equals(getEndTime(), that.getEndTime()) &&
            Objects.equals(getSegmentTime(), that.getSegmentTime()) &&
            Objects.equals(getShardCursors(), that.getShardCursors()) &&
            Objects.equals(getShardPath(), that.getShardPath());
    }
}
