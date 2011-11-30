package com.microsoft.windowsazure.services.core.storage.utils;

/**
 * RESERVED FOR INTERNAL USE. Represents a stream descriptor that contains the stream size and MD5 hash.
 */
public final class StreamDescriptor {
    /**
     * Contains the MD5 hash for the stream data.
     */
    private String streamMd5;

    /**
     * Contains the length, in bytes, for the stream.
     */
    private long streamLength;

    /**
     * @return the length
     */
    public long getLength() {
        return this.streamLength;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        return this.streamMd5;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(final long length) {
        this.streamLength = length;
    }

    /**
     * @param md5
     *            the md5 to set
     */
    public void setMd5(final String md5) {
        this.streamMd5 = md5;
    }
}
