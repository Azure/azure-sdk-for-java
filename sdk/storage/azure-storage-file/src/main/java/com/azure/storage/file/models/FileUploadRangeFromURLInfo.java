// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

public final class FileUploadRangeFromURLInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Boolean isServerEncrypted;

    public FileUploadRangeFromURLInfo(final String eTag, final OffsetDateTime lastModified, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return The last time the share was modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified
     * algorithm. Otherwise, the value is false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
