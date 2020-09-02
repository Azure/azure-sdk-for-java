// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when deleting a share.
 */
@Fluent
public class ShareDeleteOptions {

    private DeleteSnapshotsOptionType deleteSnapshotsOptions;
    private ShareRequestConditions requestConditions;

    /**
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions}.
     * @return The updated options.
     */
    public ShareDeleteOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
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
