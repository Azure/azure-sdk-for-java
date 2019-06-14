// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

public final class ShareInfo {
    private String eTag;

    private OffsetDateTime lastModified;

    public ShareInfo eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public String eTag() {
        return eTag;
    }

    public ShareInfo lastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }
}
