package com.azure.cosmos.implementation.batch;


import com.azure.cosmos.models.CosmosItemIdentity;

/**
 * Used in bulk preserve ordering to notify when a partition split happens
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
