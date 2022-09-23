// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionProcessor;
import com.azure.cosmos.implementation.changefeed.PartitionProcessorFactory;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory<ChangeFeedProcessorItem> {
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

        if (!Strings.isNullOrWhiteSpace(processorOptions.getStartContinuation())) {
            ChangeFeedState changeFeedState = ChangeFeedStateV1.fromString(processorOptions.getStartContinuation());
            return ChangeFeedStartFromInternal.createFromETagAndFeedRange(
                changeFeedState.getContinuation().getCurrentContinuationToken().getToken(),
                feedRange);
        }
        return ChangeFeedStartFromInternal.createFromNow();
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver<ChangeFeedProcessorItem> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }

        if (lease == null) {
            throw new IllegalArgumentException("lease cannot be null");
        }

        ChangeFeedState state;
        if (Strings.isNullOrWhiteSpace(lease.getContinuationToken())) {
            state = new ChangeFeedStateV1(
                BridgeInternal.extractContainerSelfLink(this.collectionSelfLink),
                lease.getFeedRange(),
                ChangeFeedMode.FULL_FIDELITY,
                getStartFromSettings(
                    lease.getFeedRange(),
                    this.changeFeedProcessorOptions),
                null);
        } else {
            state = lease.getEpkRangeBasedContinuationState(this.collectionResourceId);
        }

        ProcessorSettings settings = new ProcessorSettings(state, this.collectionSelfLink)
            .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer, lease);
    }
}
