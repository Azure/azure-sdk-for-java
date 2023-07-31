// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosItemIdentity;

/**
 * Notifies when a partition split happens
 */
public class PartitionSplitNotifier {
    Notifiable<CosmosItemIdentity> notify;

    public PartitionSplitNotifier(Notifiable<CosmosItemIdentity> notify) {
        this.notify = notify;
    }


    public void partitionSplit(CosmosItemIdentity cosmosItemIdentity) {
        notify.notify(cosmosItemIdentity);
    }

}
