// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when setting quota on a share.
 */
@Fluent
public class ShareSetQuotaOptions {

    private final int quotaInGb;
    private ShareRequestConditions requestConditions;

    /**
     * @param quotaInGb Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     */
    public ShareSetQuotaOptions(int quotaInGb) {
        this.quotaInGb = quotaInGb;
    }

    /**
     * @return Size in GB to limit the share's growth.
     */
    public int getQuotaInGb() {
        return quotaInGb;
    }

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
    public ShareSetQuotaOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
