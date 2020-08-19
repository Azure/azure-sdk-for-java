// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.DeleteSnapshotsOptionType;

/**
 * Extended options that may be passed when deleting a share.
 */
@Fluent
public class ShareDeleteOptions {

    private DeleteSnapshotsOptionType deleteSnapshotsOptions;
    private String leaseId;

    /**
     * @return The lease id that the share must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param leaseId The lease id that the share must match.
     * @return The updated options.
     */
    public ShareDeleteOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * @return {@link DeleteSnapshotsOptionType}
     */
    public DeleteSnapshotsOptionType getDeleteSnapshotsOptions() {
        return deleteSnapshotsOptions;
    }

    /**
     * @param deleteSnapshotsOptions {@link DeleteSnapshotsOptionType}
     * @return The updated options.
     */
    public ShareDeleteOptions setDeleteSnapshotsOptions(DeleteSnapshotsOptionType deleteSnapshotsOptions) {
        this.deleteSnapshotsOptions = deleteSnapshotsOptions;
        return this;
    }
}
