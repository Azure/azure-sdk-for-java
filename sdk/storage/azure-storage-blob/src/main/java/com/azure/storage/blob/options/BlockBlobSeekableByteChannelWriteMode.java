// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

/**
 * Mode to open a new SeekableByteChannel for writing when targeting a block blob.
 */
public enum BlockBlobSeekableByteChannelWriteMode {
    /**
     * Replaces the existing block blob, if any, with the newly written contents. Creates a new blob if none exists.
     */
    OVERWRITE,

    /**
     * Appends the newly written contents to the end of the existing block blob, if any. Will fail if the blob does
     * not already exist.
     */
    APPEND,

    /**
     * Prepends the newly written contents to the start of the existing block blob, if any. Will fail if the blob does
     * not already exist.
     */
    PREPEND
}
