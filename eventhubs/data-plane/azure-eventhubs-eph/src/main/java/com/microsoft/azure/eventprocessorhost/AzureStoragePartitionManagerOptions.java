/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

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
