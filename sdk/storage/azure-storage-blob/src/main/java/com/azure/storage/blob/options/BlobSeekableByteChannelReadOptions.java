// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.common.DownloadContentValidationOptions;

import java.nio.channels.SeekableByteChannel;

/**
 * Extended options that may be passed when opening a blob seekable byte channel for reading.
 */
@Fluent
public final class BlobSeekableByteChannelReadOptions {
    private Long initialPosition;
    private BlobRequestConditions requestConditions;
    private Integer readSizeInBytes;
    private ConsistentReadControl consistentReadControl;
    private DownloadContentValidationOptions contentValidationOptions;

    /**
     * Creates a new instance of {@link BlobSeekableByteChannelReadOptions}.
     */
    public BlobSeekableByteChannelReadOptions() {
    }

    /**
     * Gets the {@link DownloadContentValidationOptions}.
     *
     * @return {@link DownloadContentValidationOptions}
     */
    public DownloadContentValidationOptions getContentValidationOptions() {
        return contentValidationOptions;
    }

    /**
     * Sets the {@link DownloadContentValidationOptions}.
     *
     * @param contentValidationOptions {@link DownloadContentValidationOptions}
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions
        setContentValidationOptions(DownloadContentValidationOptions contentValidationOptions) {
        this.contentValidationOptions = contentValidationOptions;
        return this;
    }

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
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the size of each data read from the service. If read size is large, the channel will make fewer network
     * calls, but each individual call will be larger. The default value is 4 MB.
     *
     * @return The size of each data read from the service. If read size is large, the channel will make fewer network
     * calls, but each individual call will be larger. The default value is 4 MB.
     */
    public Integer getReadSizeInBytes() {
        return readSizeInBytes;
    }

    /**
     * Sets the size of each data read from the service. If read size is large, the channel will make fewer network
     * calls, but each individual call will be larger. The default value is 4 MB.
     *
     * @param readSizeInBytes The size of each data read from the service. If read size is large, the channel will make
     * fewer network calls, but each individual call will be larger. The default value is 4 MB.
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setReadSizeInBytes(Integer readSizeInBytes) {
        this.readSizeInBytes = readSizeInBytes;
        return this;
    }

    /**
     * Gets the {@link ConsistentReadControl} Default is E-Tag.
     *
     * @return {@link ConsistentReadControl} Default is E-Tag.
     */
    public ConsistentReadControl getConsistentReadControl() {
        return consistentReadControl;
    }

    /**
     * Sets the {@link ConsistentReadControl} Default is E-Tag.
     *
     * @param consistentReadControl {@link ConsistentReadControl} Default is E-Tag.
     * @return The updated options.
     */
    public BlobSeekableByteChannelReadOptions setConsistentReadControl(ConsistentReadControl consistentReadControl) {
        this.consistentReadControl = consistentReadControl;
        return this;
    }
}
