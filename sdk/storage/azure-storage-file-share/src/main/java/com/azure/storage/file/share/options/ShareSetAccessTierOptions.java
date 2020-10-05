// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when setting quota on a share.
 */
@Fluent
public class ShareSetAccessTierOptions {

    private final ShareAccessTier accessTier;
    private ShareRequestConditions requestConditions;

    /**
     * @param accessTier {@link ShareAccessTier}
     */
    public ShareSetAccessTierOptions(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
    }

    /**
     * @return {@link ShareAccessTier}
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
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
    public ShareSetAccessTierOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
