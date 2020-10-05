// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;

import java.util.Map;

/**
 * Extended options that may be passed when creating a share.
 */
@Fluent
public class ShareCreateOptions {

    private Integer quotaInGb;
    private Map<String, String> metadata;
    private ShareAccessTier accessTier;

    /**
     * @return Size in GB to limit the share's growth.
     */
    public Integer getQuotaInGb() {
        return quotaInGb;
    }

    /**
     * @param quotaInGb Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The updated options.
     */
    public ShareCreateOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
        return this;
    }

    /**
     * @return Metadata to associate with the share
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata Metadata to associate with the share.
     * @return The updated options.
     */
    public ShareCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return {@link ShareAccessTier}.
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareCreateOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }
}
