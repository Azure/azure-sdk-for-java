// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionProcessor;
import com.azure.cosmos.implementation.changefeed.PartitionProcessorFactory;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosAsyncContainer collectionSelfLink;
    private final String collectionResourceId;

    public PartitionProcessorFactoryImpl(
            ChangeFeedContextClient documentClient,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            LeaseCheckpointer leaseCheckpointer,
            CosmosAsyncContainer collectionSelfLink,
            String collectionResourceId) {

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

        if (collectionResourceId == null) {
            throw new IllegalArgumentException("collectionResourceId");
        }

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
        this.collectionResourceId = collectionResourceId;
    }

    private static ChangeFeedStartFromInternal getStartFromSettings(
        FeedRangeInternal feedRange,
        ChangeFeedProcessorOptions processorOptions) {

        if (!Strings.isNullOrWhiteSpace(processorOptions.getStartContinuation()))
        {
            return ChangeFeedStartFromInternal.createFromETagAndFeedRange(
                processorOptions.getStartContinuation(),
                feedRange);
        }

        if (processorOptions.getStartTime() != null) {
            return ChangeFeedStartFromInternal.createFromPointInTime(processorOptions.getStartTime());
        }

        if (processorOptions.isStartFromBeginning()) {
            return ChangeFeedStartFromInternal.createFromBeginning();
        }

        return ChangeFeedStartFromInternal.createFromNow();
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer");
        }

        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        FeedRangeInternal feedRange = new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken());
        ChangeFeedState state;
        if (Strings.isNullOrWhiteSpace(lease.getContinuationToken())) {
            state = new ChangeFeedStateV1(
                BridgeInternal.extractContainerSelfLink(this.collectionSelfLink),
                new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken()),
                ChangeFeedMode.INCREMENTAL,
                getStartFromSettings(
                    feedRange,
                    this.changeFeedProcessorOptions),
                null);
        } else {
            state = lease.getContinuationState(this.collectionResourceId, feedRange);
        }

        ProcessorSettings settings = new ProcessorSettings(state, this.collectionSelfLink)
            .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer);
    }
}
