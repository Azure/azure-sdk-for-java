package com.azure.storage.blob.changefeed.implementation.models;

/**
 * FOR INTERNAL USE ONLY.
 * Represents a cursor for Shard.
 */
public class ShardCursor {

    private String chunkPath;
    private long blockOffset;
    private long objectBlockIndex;

    /**
     * Default constructor (used to serialize and deserialize).
     */
    public ShardCursor() {
    }

    /**
     * Creates a new ShardCursor.
     *
     * @param chunkPath The chunk path.
     * @param blockOffset The block offset.
     * @param objectBlockIndex The object block index.
     */
    ShardCursor(String chunkPath, long blockOffset, long objectBlockIndex) {
        this.chunkPath = chunkPath;
        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;
    }

    /**
     * Creates a new ShardCursor.
     *
     * @param value {@link ShardCursor}.
     */
    ShardCursor(ShardCursor value) {
        this.chunkPath = value.getChunkPath();
        this.blockOffset = value.getBlockOffset();
        this.objectBlockIndex = value.getObjectBlockIndex();
    }

    /**
     * @return The chunk path.
     */
    public String getChunkPath() {
        return chunkPath;
    }

    /**
     * @return The block offset.
     */
    public long getBlockOffset() {
        return blockOffset;
    }

    /**
     * @return The object block index.
     */
    public long getObjectBlockIndex() {
        return objectBlockIndex;
    }

    /**
     * @param chunkPath the chunk path.
     * @return the updated ShardCursor.
     */
    public ShardCursor setChunkPath(String chunkPath) {
        this.chunkPath = chunkPath;
        return this;
    }

    /**
     * @param blockOffset the block offset.
     * @return the updated ShardCursor.
     */
    public ShardCursor setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
        return this;
    }

    /**
     * @param objectBlockIndex the object block index.
     * @return the updated ShardCursor.
     */
    public ShardCursor setObjectBlockIndex(long objectBlockIndex) {
        this.objectBlockIndex = objectBlockIndex;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ShardCursor)) {
            return false;
        }

        ShardCursor otherCursor = (ShardCursor) other;
        return this.chunkPath.equals(otherCursor.getChunkPath())
            && this.blockOffset == otherCursor.getBlockOffset()
            && this.objectBlockIndex == otherCursor.getObjectBlockIndex();
    }
}
