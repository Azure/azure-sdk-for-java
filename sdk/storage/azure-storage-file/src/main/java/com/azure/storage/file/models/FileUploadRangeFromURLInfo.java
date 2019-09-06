// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

public final class FileUploadRangeFromURLInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private Boolean isServerEncrypted;

    public FileUploadRangeFromURLInfo(final String eTag, final OffsetDateTime lastModified, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
