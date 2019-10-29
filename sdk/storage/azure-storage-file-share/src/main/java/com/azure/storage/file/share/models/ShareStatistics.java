// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

/**
 * Contains statistics about a Share in the storage File service.
 */
public final class ShareStatistics {
    private final int shareUsageInGB;

    /**
     * Creates an instance of storage statistics for a Share.
     *
     * @param shareUsageInGB Size in GB of the Share
     */
    public ShareStatistics(int shareUsageInGB) {
        this.shareUsageInGB = shareUsageInGB;
    }

    /**
     * @return the size in GB of the Share
     */
    public int getShareUsageInGB() {
        return shareUsageInGB;
    }
}
