// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosItemIdentity;

/**
 * Notifies when a partition based gone happens
 */
public class PartitionBasedGoneNotifier {
    Notifiable<CosmosItemIdentity> notify;

    public PartitionBasedGoneNotifier(Notifiable<CosmosItemIdentity> notify) {
        this.notify = notify;
    }

    public void partitionBasedGone(CosmosItemIdentity cosmosItemIdentity) {
        notify.notify(cosmosItemIdentity);
    }

}
