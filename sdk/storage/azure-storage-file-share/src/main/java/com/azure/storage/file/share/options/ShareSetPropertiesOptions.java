// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when setting properties on a share.
 */
@Fluent
public class ShareSetPropertiesOptions {

    private Integer quotaInGb;
    private ShareAccessTier accessTier;
    private ShareRequestConditions requestConditions;

    /**
     * @return {@link ShareAccessTier}
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

    /**
     * @return Size in GB to limit the share's growth.
     */
    public Integer getQuotaInGb() {
        return quotaInGb;
    }

    /**
     * @param quotaInGb Size in GB to limit the share's growth.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
        return this;
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
    public ShareSetPropertiesOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
