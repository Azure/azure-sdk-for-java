// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for a shard in BlobChangefeed.
 */
@Fluent
public class ShardCursor {

    @JsonProperty("CurrentChunkPath")
    private String currentChunkPath;

    @JsonProperty("BlockOffset")
    private long blockOffset;

    @JsonProperty("EventIndex")
    private long eventIndex;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ShardCursor() {
    }

    /**
     * Constructor for use by to*Cursor methods.
     */
    public ShardCursor(String currentChunkPath, long blockOffset, long eventIndex) {
        this.currentChunkPath = currentChunkPath;
        this.blockOffset = blockOffset;
        this.eventIndex = eventIndex;
    }

    /**
     * @return the chunk path.
     */
    public String getCurrentChunkPath() {
        return currentChunkPath;
    }

    /**
     * @return the block offset.
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * @return the event index.
     */
    public long getEventIndex() {
        return eventIndex;
    }

    /**
     * @param currentChunkPath the chunk path.
     * @return the updated ShardCursor
     */
    public ShardCursor setCurrentChunkPath(String currentChunkPath) {
        this.currentChunkPath = currentChunkPath;
        return this;
    }

    /**
     * @param blockOffset the block offset.
     * @return the updated ShardCursor
     */
    public ShardCursor setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
        return this;
    }

    /**
     * @param eventIndex the event index.
     * @return the updated ShardCursor
     */
    public ShardCursor setEventIndex(long eventIndex) {
        this.eventIndex = eventIndex;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShardCursor)) {
            return false;
        }
        ShardCursor cursor = (ShardCursor) o;
        return getBlockOffset() == cursor.getBlockOffset()
            && getEventIndex() == cursor.getEventIndex()
            && Objects.equals(getCurrentChunkPath(), cursor.getCurrentChunkPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrentChunkPath(), getBlockOffset(), getEventIndex());
    }
}
