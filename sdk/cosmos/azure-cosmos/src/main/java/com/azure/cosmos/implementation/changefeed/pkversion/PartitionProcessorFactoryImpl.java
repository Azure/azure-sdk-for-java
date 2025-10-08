// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
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
    public PartitionProcessor create(Lease lease, ChangeFeedObserver<JsonNode> observer) {
        checkNotNull(observer, "Argument 'observer' can not be null");
        checkNotNull(lease, "Argument 'lease' can not be null");

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
            state = lease.getContinuationState(this.collectionResourceId, ChangeFeedMode.INCREMENTAL);
        }

        ProcessorSettings settings =
            new ProcessorSettings(state, this.collectionSelfLink)
                .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
                .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount())
                .withResponseInterceptor(this.changeFeedProcessorOptions.getResponseInterceptor());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(
            observer,
            this.documentClient,
            settings,
            checkpointer,
            lease,
            this.feedRangeThroughputControlConfigManager);
    }
}
