// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Options for obtaining a {@link java.nio.channels.SeekableByteChannel} backed by an Azure Storage Share File.
 */
public class ShareFileSeekableByteChannelReadOptions {
    private ShareRequestConditions conditions;
    private Long initialChannelPosition;

    /**
     * @return Request conditions to be used by the resulting channel.
     */
    public ShareRequestConditions getConditions() {
        return conditions;
    }

    /**
     * @param conditions Request conditions to be used by the resulting channel.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelReadOptions setConditions(ShareRequestConditions conditions) {
        this.conditions = conditions;
        return this;
    }

    /**
     * An initial range is downloaded from the file when opening a read channel, allowing the method to return
     * file properties alongside the channel. Setting this value controls the offset of the range that is initially
     * downloaded. The channel will be returned initialized to this position.
     *
     * @return The starting offset for this channel.
     */
    public Long getInitialChannelPosition() {
        return initialChannelPosition;
    }

    /**
     * An initial range is downloaded from the file when opening a read channel, allowing the method to return
     * file properties alongside the channel. Setting this value controls the offset of the range that is initially
     * downloaded. The channel will be returned initialized to this position.
     *
     * @param initialChannelPosition The starting offset for this channel.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelReadOptions setInitialChannelPosition(Long initialChannelPosition) {
        this.initialChannelPosition = initialChannelPosition;
        return this;
    }
}
