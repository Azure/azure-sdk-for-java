// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.storage.common.implementation.Constants;

/**
 * Contains statistics about a Share in the storage File service.
 */
public final class ShareStatistics {
    private final int shareUsageInGB;
    private final long shareUsageInBytes;

    /**
     * Creates an instance of storage statistics for a Share.
     *
     * @param shareUsageInGB Size in GB of the Share
     */
    public ShareStatistics(int shareUsageInGB) {
        this.shareUsageInGB = shareUsageInGB;
        this.shareUsageInBytes = -1;
    }

    /**
     * Creates an instance of storage statistics for a Share.
     *
     * @param shareUsageInBytes Size in bytes of the Share
     */
    public ShareStatistics(long shareUsageInBytes) {
        this.shareUsageInGB = (int) Math.ceil((double) shareUsageInBytes / Constants.GB);
        this.shareUsageInBytes = shareUsageInBytes;
    }

    /**
     * Get the size in GB of the Share.
     *
     * @return the size in GB of the Share
     */
    public int getShareUsageInGB() {
        return shareUsageInGB;
    }

    /**
     * Get the size in bytes of the Share.
     *
     * @return the size in bytes of the Share
     */
    public long getShareUsageInBytes() {
        return shareUsageInBytes;
    }
}
