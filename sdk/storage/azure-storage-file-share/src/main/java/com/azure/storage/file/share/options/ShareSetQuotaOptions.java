// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;

/**
 * Extended options that may be passed when setting quota on a share.
 */
@Fluent
public class ShareSetQuotaOptions {

    private final int quotaInGb;
    private String leaseId;

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
     * @return The lease id that the share must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param leaseId The lease id that the share must match.
     * @return The updated options.
     */
    public ShareSetQuotaOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
