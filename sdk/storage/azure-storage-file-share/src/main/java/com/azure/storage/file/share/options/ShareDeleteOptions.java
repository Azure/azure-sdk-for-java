// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;

/**
 * Extended options that may be passed when deleting a share.
 */
@Fluent
public class ShareDeleteOptions {

    private ShareSnapshotsDeleteOptionType deleteSnapshotsOptions;
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
     * @return {@link ShareSnapshotsDeleteOptionType}
     */
    public ShareSnapshotsDeleteOptionType getDeleteSnapshotsOptions() {
        return deleteSnapshotsOptions;
    }

    /**
     * @param deleteSnapshotsOptions {@link ShareSnapshotsDeleteOptionType}
     * @return The updated options.
     */
    public ShareDeleteOptions setDeleteSnapshotsOptions(ShareSnapshotsDeleteOptionType deleteSnapshotsOptions) {
        this.deleteSnapshotsOptions = deleteSnapshotsOptions;
        return this;
    }
}
