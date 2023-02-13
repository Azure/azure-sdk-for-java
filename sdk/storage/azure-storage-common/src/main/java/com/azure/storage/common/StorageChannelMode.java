// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Mode to open a Storage-backed SeekableByteChannel.
 */
public enum StorageChannelMode {
    /**
     * Open for read-only mode.
     */
    READ,

    /**
     * Open for write-only mode.
     */
    // TODO (jaschrep): should this be `APPEND`?
    WRITE
}
