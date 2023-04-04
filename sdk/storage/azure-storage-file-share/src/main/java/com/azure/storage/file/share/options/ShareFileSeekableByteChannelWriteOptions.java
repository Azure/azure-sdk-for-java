// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.share.options;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;
import java.util.Objects;

/**
 * Options for obtaining a {@link java.nio.channels.SeekableByteChannel} backed by an Azure Storage Share File.
 */
public final class ShareFileSeekableByteChannelWriteOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ShareFileSeekableByteChannelWriteOptions.class);

    /**
     * Mode to open the channel for writing.
     */
    public static final class WriteMode extends ExpandableStringEnum<WriteMode> {
        /**
         * Opens channel to an existing file for writing.
         */
        public static final WriteMode WRITE = fromString("Write");

        /**
         * Creates a new file for writing and opens the channel. If the file already exists, it will be overwritten.
         * Requires a value be set with {@link ShareFileSeekableByteChannelWriteOptions#setFileSize(Long)}.
         */
        public static final WriteMode OVERWRITE = fromString("Overwrite");

        /**
         * Creates or finds a AccessTier from its string representation.
         *
         * @param name a name to look for.
         * @return the corresponding AccessTier.
         */
        @JsonCreator
        public static WriteMode fromString(String name) {
            return fromString(name, WriteMode.class);
        }

        /**
         * Gets known WriteMode values.
         *
         * @return known WriteMode values.
         */
        public static Collection<WriteMode> values() {
            return values(WriteMode.class);
        }
    }

    private final WriteMode channelMode;
    private Long fileSize;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode fileLastWrittenMode;
    private Long chunkSizeInBytes;

    /**
     * Options constructor.
     * @param mode What usage mode to open the channel in.
     */
    public ShareFileSeekableByteChannelWriteOptions(WriteMode mode) {
        channelMode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    /**
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
