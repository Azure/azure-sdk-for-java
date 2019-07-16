// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

public final class AzureStoragePartitionManagerOptions extends PartitionManagerOptions {
    public AzureStoragePartitionManagerOptions() {
    }

    @Override
    public void setLeaseDurationInSeconds(int duration) {
        // Max Azure Storage blob lease is 60 seconds
        if (duration > 60) {
            throw new IllegalArgumentException("Lease duration cannot be more than 60 seconds");
        }
        super.setLeaseDurationInSeconds(duration);
    }
}
