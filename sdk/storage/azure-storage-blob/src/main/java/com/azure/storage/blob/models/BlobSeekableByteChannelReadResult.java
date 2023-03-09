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

    public BlobSeekableByteChannelReadResult(SeekableByteChannel channel, BlobProperties properties) {
        this.channel = channel;
        this.properties = properties;
    }

    public SeekableByteChannel getChannel() {
        return channel;
    }

    public BlobProperties getProperties() {
        return properties;
    }
}
