// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

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
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl<T> implements PartitionProcessorFactory<T> {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosAsyncContainer collectionSelfLink;
    private final String collectionResourceId;
    private final ChangeFeedMode changeFeedMode;
    private final FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager;

    public PartitionProcessorFactoryImpl(
            ChangeFeedContextClient documentClient,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            LeaseCheckpointer leaseCheckpointer,
            CosmosAsyncContainer collectionSelfLink,
            String collectionResourceId,
            ChangeFeedMode changeFeedMode,
            FeedRangeThroughputControlConfigManager feedRangeThroughputControlConfigManager) {

        checkNotNull(documentClient, "Argument 'documentClient' can not be null");
        checkNotNull(changeFeedProcessorOptions, "Argument 'changeFeedProcessorOptions' can not be null");
        checkNotNull(leaseCheckpointer, "Argument 'leaseCheckPointer' can not be null");
        checkNotNull(collectionSelfLink, "Argument 'collectionSelfLink' can not be null");
        checkNotNull(collectionResourceId, "Argument 'collectionResourceId' can not be null");

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.collectionSelfLink = collectionSelfLink;
        this.collectionResourceId = collectionResourceId;
        this.changeFeedMode = changeFeedMode;
        this.feedRangeThroughputControlConfigManager = feedRangeThroughputControlConfigManager;
    }

    @Override
    public PartitionProcessor create(Lease lease, ChangeFeedObserver<T> observer, Class<T> classType) {
        checkNotNull(observer, "Argument 'observer' can not be null");
        checkNotNull(lease, "Argument 'lease' can not be null");

        ChangeFeedState state;
        if (Strings.isNullOrWhiteSpace(lease.getContinuationToken())) {
            state = new ChangeFeedStateV1(
                BridgeInternal.extractContainerSelfLink(this.collectionSelfLink),
                lease.getFeedRange(),
                this.changeFeedMode,
                PartitionProcessorHelper.getStartFromSettings(
                    lease.getFeedRange(),
                    this.changeFeedProcessorOptions,
                    this.changeFeedMode),
                null);
        } else {
            state = lease.getContinuationState(this.collectionResourceId, this.changeFeedMode);
        }

        ProcessorSettings settings = new ProcessorSettings(state, this.collectionSelfLink)
            .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount())
            .withResponseInterceptor(this.changeFeedProcessorOptions.getResponseInterceptor());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);

        return new PartitionProcessorImpl<>(
            observer,
            this.documentClient,
            settings,
            checkpointer,
            lease,
            classType,
            this.changeFeedMode,
            this.feedRangeThroughputControlConfigManager);
    }
}
