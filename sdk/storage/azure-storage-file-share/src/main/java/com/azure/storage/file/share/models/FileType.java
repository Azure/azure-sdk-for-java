// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

/**
 * The type of a file or directory returned in a listing.
 *
 * @see ShareFileItem#getFileType()
 */
public enum FileType {
    /**
     * Regular file.
     */
    REGULAR,

    /**
     * Directory.
     */
    DIRECTORY,

    /**
     * Symbolic link.
     */
    SYM_LINK,

    /**
     * Block device.
     */
    BLOCK_DEVICE,

    /**
     * Character device.
     */
    CHARACTER_DEVICE,

    /**
     * Socket.
     */
    SOCKET,

    /**
     * FIFO.
     */
    FIFO;
}
