package com.azure.storage.blob.changefeed.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

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
}
