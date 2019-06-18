// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

public final class FileInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private Boolean isServerEncrypted;

    public FileInfo(final String eTag, final OffsetDateTime lastModified, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public String eTag(final String eTag) {
        this.eTag = eTag;
        return this.eTag;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public OffsetDateTime lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this.lastModified;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public Boolean isServerEncrypted(final Boolean serverEncrypted) {
        this.isServerEncrypted = serverEncrypted;
        return this.isServerEncrypted;
    }
}
