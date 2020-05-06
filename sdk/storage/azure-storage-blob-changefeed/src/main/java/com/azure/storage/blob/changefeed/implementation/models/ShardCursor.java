package com.azure.storage.blob.changefeed.implementation.models;

public class ShardCursor {

    private String chunkPath;

    private long blockOffset;

    private long objectBlockIndex;

    public ShardCursor(String chunkPath, long blockOffset, long objectBlockIndex) {
        this.chunkPath = chunkPath;
        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;
    }

    public ShardCursor(ShardCursor value) {
        this.chunkPath = value.getChunkPath();
        this.blockOffset = value.getBlockOffset();
        this.objectBlockIndex = value.getObjectBlockIndex();
    }

    public String getChunkPath() {
        return chunkPath;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public long getObjectBlockIndex() {
        return objectBlockIndex;
    }

    public ShardCursor setChunkPath(String chunkPath) {
        this.chunkPath = chunkPath;
        return this;
    }

    public ShardCursor setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
        return this;
    }

    public ShardCursor setObjectBlockIndex(long objectBlockIndex) {
        this.objectBlockIndex = objectBlockIndex;
        return this;
    }
}
