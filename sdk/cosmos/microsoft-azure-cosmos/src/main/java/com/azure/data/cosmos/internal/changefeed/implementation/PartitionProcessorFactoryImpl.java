// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.ChangeFeedProcessorOptions;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.LeaseCheckpointer;
import com.azure.data.cosmos.internal.changefeed.PartitionCheckpointer;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.data.cosmos.internal.changefeed.PartitionProcessorFactory;
import com.azure.data.cosmos.internal.changefeed.ProcessorSettings;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosContainer collectionSelfLink;

    public PartitionProcessorFactoryImpl(
        ChangeFeedContextClient documentClient,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        LeaseCheckpointer leaseCheckpointer,
        CosmosContainer collectionSelfLink) {

        if (documentClient == null) throw new IllegalArgumentException("documentClient");
        if (changeFeedProcessorOptions == null) throw new IllegalArgumentException("changeFeedProcessorOptions");
        if (leaseCheckpointer == null) throw new IllegalArgumentException("leaseCheckpointer");
        if (collectionSelfLink == null) throw new IllegalArgumentException("collectionSelfLink");

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver observer) {
        if (observer == null) throw new IllegalArgumentException("observer");
        if (lease == null) throw new IllegalArgumentException("lease");

        String startContinuation = lease.getContinuationToken();

        if (startContinuation == null || startContinuation.isEmpty()) {
            startContinuation = this.changeFeedProcessorOptions.startContinuation();
        }

        ProcessorSettings settings = new ProcessorSettings()
            .withCollectionLink(this.collectionSelfLink)
            .withStartContinuation(startContinuation)
            .withPartitionKeyRangeId(lease.getLeaseToken())
            .withFeedPollDelay(this.changeFeedProcessorOptions.feedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.maxItemCount())
            .withStartFromBeginning(this.changeFeedProcessorOptions.startFromBeginning())
            .withStartTime(this.changeFeedProcessorOptions.startTime());  // .sessionToken(this.changeFeedProcessorOptions.sessionToken());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer);
    }
}
