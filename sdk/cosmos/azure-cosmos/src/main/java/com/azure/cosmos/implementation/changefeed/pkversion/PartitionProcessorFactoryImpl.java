// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.fasterxml.jackson.databind.JsonNode;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory<JsonNode> {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosAsyncContainer collectionSelfLink;
    private final String collectionResourceId;
    private final FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager;

    public PartitionProcessorFactoryImpl(
        ChangeFeedContextClient documentClient,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        LeaseCheckpointer leaseCheckpointer,
        CosmosAsyncContainer collectionSelfLink,
        String collectionResourceId,
        FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager) {

        checkNotNull(documentClient, "Argument 'documentClient' can not be null");
        checkNotNull(changeFeedProcessorOptions, "Argument 'changeFeedProcessorOptions' can not be null");
        checkNotNull(leaseCheckpointer, "Argument 'leaseCheckpointer' can not be null");
        checkNotNull(collectionSelfLink, "Argument 'collectionSelfLink' can not be null");
        checkNotNull(collectionResourceId, "Argument 'collectionResourceId' can not be null");

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
        this.collectionResourceId = collectionResourceId;
        this.feedRangeThroughputControlConfigManager = feedRangeThroughputControlConfigManager;
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver<JsonNode> observer) {
        checkNotNull(observer, "Argument 'observer' can not be null");
        checkNotNull(lease, "Argument 'lease' can not be null");


        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(
            observer,
            this.documentClient,
            this.collectionSelfLink,
            this.changeFeedProcessorOptions,
            this.collectionResourceId,
            checkpointer,
            lease,
            this.feedRangeThroughputControlConfigManager);
    }
}
