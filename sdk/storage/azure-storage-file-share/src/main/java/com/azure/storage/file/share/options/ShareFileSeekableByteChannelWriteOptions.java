// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Options for obtaining a {@link java.nio.channels.SeekableByteChannel} backed by an Azure Storage Share File.
 */
public final class ShareFileSeekableByteChannelWriteOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileSeekableByteChannelWriteOptions.class);

    private final boolean overwriteMode;
    private Long fileSize;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode fileLastWrittenMode;
    private Long chunkSizeInBytes;

    /**
     * Options constructor.
     * @param overwriteMode If {@code true}, the channel will be opened in overwrite mode. Otherwise, the channel will
     * be opened in write mode.
     */
    public ShareFileSeekableByteChannelWriteOptions(boolean overwriteMode) {
        this.overwriteMode = overwriteMode;
    }

    /**
     * @return Whether the channel is in write mode.
     */
    public boolean isOverwriteMode() {
        return overwriteMode;
    }


    /**
     * This parameter is required when opening the channel to write.
     * @return New size of the target file.
     */
    public Long getFileSizeInBytes() {
        return fileSize;
    }

    /**
     * @param fileSize New size of the target file.
     * @return The updated instance.
     * @throws UnsupportedOperationException When setting a file size on options that don't create a new file.
     */
    public ShareFileSeekableByteChannelWriteOptions setFileSize(Long fileSize) {
        if (!overwriteMode) {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Cannot set 'fileSize' unless creating a new file."));
        }
        if (fileSize != null && fileSize < 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'fileSize' must be a non-negative number if provided."));
        }

        this.fileSize = fileSize;
        return this;
    }

    /**
     * @return The size of individual writes to the service.
     */
    public Long getChunkSizeInBytes() {
        return chunkSizeInBytes;
    }

    /**
     * @param chunkSizeInBytes The size of individual writes to the service.
     * @return The updated instance.
     */
    public ShareFileSeekableByteChannelWriteOptions setChunkSizeInBytes(Long chunkSizeInBytes) {
        this.chunkSizeInBytes = chunkSizeInBytes;
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
