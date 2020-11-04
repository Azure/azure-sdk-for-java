// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.storage.blob.models.BlobDeleteType;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;

/**
 * Extended options that may be passed when deleting a blob.
 */
public class BlobDeleteOptions {
    private DeleteSnapshotsOptionType deleteSnapshots;
    private BlobDeleteType deleteType;
    private BlobRequestConditions requestConditions;

    /**
     * @return Specifies the behavior for deleting the snapshots on this blob. {@code Include} will delete the base blob
     * and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being deleted, you must pass
     * null.
     */
    public DeleteSnapshotsOptionType getDeleteSnapshots() {
        return deleteSnapshots;
    }

    /**
     * @return Specifies whether the soft deleted blob should be permanently deleted.
     */
    public BlobDeleteType getDeleteType() {
        return deleteType;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param deleteSnapshots Specifies the behavior for deleting the snapshots on this blob. {@code Include} will
     * delete the base blob and all snapshots. {@code Only} will delete only the snapshots. If a snapshot is being
     * deleted, you must pass null.
     * @return The updated options.
     */
    public BlobDeleteOptions setDeleteSnapshots(DeleteSnapshotsOptionType deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
        return this;
    }

    /**
     * @param deleteType Specifies whether the soft deleted blob should be permanently deleted.
     * @return The updated options.
     */
    public BlobDeleteOptions setDeleteType(BlobDeleteType deleteType) {
        this.deleteType = deleteType;
        return this;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobDeleteOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
