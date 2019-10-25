// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.internal.changefeed.implementation;

import com.azure.cosmos.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.internal.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.cosmos.internal.changefeed.Lease;
import com.azure.cosmos.internal.changefeed.LeaseCheckpointer;
import com.azure.cosmos.internal.changefeed.PartitionCheckpointer;
import com.azure.cosmos.internal.changefeed.PartitionProcessor;
import com.azure.cosmos.internal.changefeed.PartitionProcessorFactory;
import com.azure.cosmos.internal.changefeed.ProcessorSettings;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosAsyncContainer collectionSelfLink;

    public PartitionProcessorFactoryImpl(
            ChangeFeedContextClient documentClient,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            LeaseCheckpointer leaseCheckpointer,
            CosmosAsyncContainer collectionSelfLink) {

        if (documentClient == null) {
            throw new IllegalArgumentException("documentClient");
        }

        if (changeFeedProcessorOptions == null) {
            throw new IllegalArgumentException("changeFeedProcessorOptions");
        }

        if (leaseCheckpointer == null) {
            throw new IllegalArgumentException("leaseCheckpointer");
        }

        if (collectionSelfLink == null) {
            throw new IllegalArgumentException("collectionSelfLink");
        }

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer");
        }

        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        String startContinuation = lease.getContinuationToken();

        if (startContinuation == null || startContinuation.isEmpty()) {
            startContinuation = this.changeFeedProcessorOptions.getStartContinuation();
        }

        ProcessorSettings settings = new ProcessorSettings()
            .withCollectionLink(this.collectionSelfLink)
            .withStartContinuation(startContinuation)
            .withPartitionKeyRangeId(lease.getLeaseToken())
            .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount())
            .withStartFromBeginning(this.changeFeedProcessorOptions.getStartFromBeginning())
            .withStartTime(this.changeFeedProcessorOptions.getStartTime());  // .getSessionToken(this.changeFeedProcessorOptions.getSessionToken());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer);
    }
}
