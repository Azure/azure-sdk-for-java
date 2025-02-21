// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.nio.channels.SeekableByteChannel;

/**
 * Contains the results of opening a {@link SeekableByteChannel} to read from a blob.
 */
public final class BlobSeekableByteChannelReadResult {
    private final SeekableByteChannel channel;
    private final BlobProperties properties;

    /**
     * @param channel Channel to read the target blob.
     * @param properties Blob properties of the target blob.
     */
    public BlobSeekableByteChannelReadResult(SeekableByteChannel channel, BlobProperties properties) {
        this.channel = channel;
        this.properties = properties;
    }

    /**
     * @return Channel to read the target blob.
     */
    public SeekableByteChannel getChannel() {
        return channel;
    }

    /**
     * @return Blob properties of the target blob.
     */
    public BlobProperties getProperties() {
        return properties;
    }
}
