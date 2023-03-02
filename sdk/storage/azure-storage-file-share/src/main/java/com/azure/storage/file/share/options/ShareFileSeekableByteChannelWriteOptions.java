// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Objects;

/**
 * Options for obtaining a {@link java.nio.channels.SeekableByteChannel} backed by an Azure Storage Share File.
 */
public final class ShareFileSeekableByteChannelWriteOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileSeekableByteChannelWriteOptions.class);

    /**
     * Mode to open the channel for writing.
     */
    public enum WriteMode {
        /**
         * Opens channel to an existing file for writing.
         */
        WRITE,

        /**
         * Creates a new file for writing and opens the channel. If the file already exists, it will be overwritten.
         * Requires a value be set with {@link ShareFileSeekableByteChannelWriteOptions#setFileSize(Long)}.
         */
        OVERWRITE
    }

    private final WriteMode channelMode;
    private Long fileSize;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode fileLastWrittenMode;

    /**
     * Options constructor.
     * @param mode What usage mode to open the channel in.
     */
    public ShareFileSeekableByteChannelWriteOptions(WriteMode mode) {
        channelMode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    /**
     * This parameter is required when this instance is configured to {@link WriteMode#OVERWRITE}.
     * @return Usage mode to be used by the resulting channel.
     */
    public WriteMode getChannelMode() {
        return channelMode;
    }


    /**
     * This parameter is required when this instance is configured to {@link WriteMode#OVERWRITE}.
     * @return New size of the target file.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize New size of the target file.
     * @return The updated instance.
     * @throws UnsupportedOperationException When setting a file size on options that don't create a new file.
     */
    public ShareFileSeekableByteChannelWriteOptions setFileSize(Long fileSize) {
        if (channelMode != WriteMode.OVERWRITE) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Cannot set 'fileSize' unless creating a new file."));
        }
        this.fileSize = fileSize;
        return this;
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
    public ShareFileSeekableByteChannelWriteOptions setRequestConditions(ShareRequestConditions requestConditions) {
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
    public ShareFileSeekableByteChannelWriteOptions setFileLastWrittenMode(FileLastWrittenMode fileLastWrittenMode) {
        this.fileLastWrittenMode = fileLastWrittenMode;
        return this;
    }
}
