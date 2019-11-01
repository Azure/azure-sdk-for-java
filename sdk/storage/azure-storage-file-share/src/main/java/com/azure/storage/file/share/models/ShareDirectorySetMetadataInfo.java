// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

/**
 * Contains metadata information about a Directory in the storage File service.
 */
@Immutable
public class ShareDirectorySetMetadataInfo {
    private final String eTag;
    private final boolean isServerEncrypted;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the share
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely
     * encrypted using the specified algorithm. Otherwise, the value is set to false.
     */
    public ShareDirectorySetMetadataInfo(final String eTag, final boolean isServerEncrypted) {
        this.eTag = eTag;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified
     * algorithm. Otherwise, the value is false.
     */
    public boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
