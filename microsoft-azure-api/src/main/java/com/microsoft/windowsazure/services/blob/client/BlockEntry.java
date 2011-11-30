package com.microsoft.windowsazure.services.blob.client;

/**
 * A class which is used to list and commit blocks of a {@link CloudBlockBlob}.
 */
public final class BlockEntry {
    /**
     * Represents the name of the block.
     */
    private String id;

    /**
     * Represents the size, in bytes, of the block.
     */
    private long size;

    /**
     * Represents the block search mode. The default value is {@link BlockSearchMode#LATEST}.
     */
    public BlockSearchMode searchMode = BlockSearchMode.LATEST;

    /**
     * Creates an instance of the <code>BlockEntry</code> class.
     * 
     * @param id
     *            A <code>String</code> that represents the name of the block.
     * @param searchMode
     *            A {@link BlockSearchMode} value that represents the block search mode.
     */
    public BlockEntry(final String id, final BlockSearchMode searchMode) {
        this.setId(id);
        this.searchMode = searchMode;
    }

    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return this.size;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(final long size) {
        this.size = size;
    }
}
