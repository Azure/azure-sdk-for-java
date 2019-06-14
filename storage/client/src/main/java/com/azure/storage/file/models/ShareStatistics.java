// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

public final class ShareStatistics {
    private final int shareUsageInGB;

    public ShareStatistics(int shareUsageInGB) {
        this.shareUsageInGB = shareUsageInGB;
    }

    public int getGhareUsageInGB() {
        return shareUsageInGB;
    }
}
