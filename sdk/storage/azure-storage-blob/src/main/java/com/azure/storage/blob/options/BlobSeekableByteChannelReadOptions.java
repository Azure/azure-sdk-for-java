// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ConsistentReadControl;

import java.nio.channels.SeekableByteChannel;

/**
 * Extended options that may be passed when opening a blob seekable byte channel for reading.
 */
@Fluent
public class BlobSeekableByteChannelReadOptions {

    private Long initialPosition;
    private BlobRequestConditions requestConditions;
    private Integer blockSize;
    private ConsistentReadControl consistentReadControl;

    /**
     * Gets the starting position of the resulting {@link SeekableByteChannel}. The channel will come with a prefetched
     * range starting at this position.
     * @return Initial position of the resulting channel.
     */
    public Long getInitialPosition() {
        return initialPosition;
    }

    /**
     * Sets the starting position of the resulting {@link SeekableByteChannel}. The channel will come with a prefetched
     * range starting at this position.
     * @param initialPosition Initial position of the resulting channel.
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setInitialPosition(Long initialPosition) {
        this.initialPosition = initialPosition;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return The size of each data chunk returned from the service. If block size is large, the channel will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     */
    public Integer getBlockSize() {
        return blockSize;
    }

    /**
     * @param blockSize The size of each data chunk returned from the service. If block size is large, the channel
     * will make fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /**
     * @return {@link ConsistentReadControl} Default is E-Tag.
     */
    public ConsistentReadControl getConsistentReadControl() {
        return consistentReadControl;
    }

    /**
     * @param consistentReadControl {@link ConsistentReadControl} Default is E-Tag.
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setConsistentReadControl(ConsistentReadControl consistentReadControl) {
        this.consistentReadControl = consistentReadControl;
        return this;
    }
}
