// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;

public final class ShareSnapshotInfo {
    private String snapshot;

    private String eTag;

    private OffsetDateTime lastModified;

    public ShareSnapshotInfo snapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String snapshot() {
        return snapshot;
    }

    public ShareSnapshotInfo eTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public String eTag() {
        return eTag;
    }

    public ShareSnapshotInfo lastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }
}
