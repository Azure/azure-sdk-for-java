package com.microsoft.windowsazure.services.blob.client;

/**
 * Represents a range of pages in a page blob.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class PageRange {
    /**
     * Represents the ending offset of the page range.
     */
    private long endOffset;

    /**
     * Represents the starting offset of the page range.
     */
    private long startOffset;

    /**
     * Creates an instance of the <code>PageRange</code> class.
     * 
     * @param start
     *            The starting offset.
     * @param end
     *            The ending offset.
     */
    public PageRange(final long start, final long end) {
        this.setStartOffset(start);
        this.setEndOffset(end);
    }

    /**
     * @return the endOffset
     */
    public long getEndOffset() {
        return this.endOffset;
    }

    /**
     * @return the startOffset
     */
    public long getStartOffset() {
        return this.startOffset;
    }

    /**
     * @param endOffset
     *            the endOffset to set
     */
    public void setEndOffset(final long endOffset) {
        this.endOffset = endOffset;
    }

    /**
     * @param startOffset
     *            the startOffset to set
     */
    public void setStartOffset(final long startOffset) {
        this.startOffset = startOffset;
    }

    /**
     * Returns the content of the page range as a string.
     * 
     * @return A <code>String</code> that represents the contents of the page range.
     */
    @Override
    public String toString() {
        return String.format("bytes=%d-%d", this.getStartOffset(), this.getEndOffset());
    }
}
