// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.storage.common.StorageChannelMode;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Objects;

/**
 * Options for obtaining a {@link java.nio.channels.SeekableByteChannel} backed by an Azure Storage Share File.
 */
public class ShareFileSeekableByteChannelOptions {
    private final StorageChannelMode channelMode;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode fileLastWrittenMode;

    /**
     * Options constructor.
     * @param mode What usage mode to open the channel in.
     */
    public ShareFileSeekableByteChannelOptions(StorageChannelMode mode) {
        channelMode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    /**
     * @return Usage mode to be used by the resulting channel.
     */
    public StorageChannelMode getChannelMode() {
        return channelMode;
    }

    /**
     * @return Request conditions to be used by the resulting channel.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions Request conditions to be used by the resulting channel.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return The last wriiten mode to be used by the resulting channel.
     */
    public FileLastWrittenMode getFileLastWrittenMode() {
        return fileLastWrittenMode;
    }

    /**
     * @param fileLastWrittenMode The last wriiten mode to be used by the resulting channel.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelOptions setFileLastWrittenMode(FileLastWrittenMode fileLastWrittenMode) {
        if (channelMode != StorageChannelMode.WRITE) {
            throw new IllegalArgumentException("'fileLastWrittenMode' not allowed for channel mode "
                + channelMode.toString());
        }
        this.fileLastWrittenMode = fileLastWrittenMode;
        return this;
    }
}
