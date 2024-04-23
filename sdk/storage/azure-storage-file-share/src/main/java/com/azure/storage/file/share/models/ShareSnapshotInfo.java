// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Contains information about a snapshot of a Share in the storage File service.
 */
@Immutable
public final class ShareSnapshotInfo {
    private final String snapshot;
    private final String eTag;
    private final OffsetDateTime lastModified;

    /**
     * Creates an instance of snapshot information for a specific Share.
     *
     * @param snapshot Identifier for the snapshot
     * @param eTag Entity tag that corresponds to the snapshot
     * @param lastModified Last time the Share was modified if the snapshot was created without metadata, if the
     * snapshot was created with metadata then it will be the time the snapshot was created
     */
    public ShareSnapshotInfo(String snapshot, String eTag, OffsetDateTime lastModified) {
        this.snapshot = snapshot;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    /**
     * @return the identifier of the snapshot
     */
    public String getSnapshot() {
        return snapshot;
    }

    /**
     * @return the entity tag that corresponds to the snapshot
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the last time the share was modified if the snapshot was created without metadata, otherwise this is the
     * time that the snapshot was created.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }
}
