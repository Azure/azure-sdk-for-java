// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
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

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for {@link PartitionProcessorFactory}.
 */
class PartitionProcessorFactoryImpl implements PartitionProcessorFactory {
    private final ChangeFeedContextClient documentClient;
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final LeaseCheckpointer leaseCheckpointer;
    private final CosmosAsyncContainer monitoredContainer;
    private final String collectionResourceId;

    public PartitionProcessorFactoryImpl(
            ChangeFeedContextClient documentClient,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            LeaseCheckpointer leaseCheckpointer,
            CosmosAsyncContainer monitoredContainer,
            String collectionResourceId) {

        checkNotNull(documentClient, "Argument 'documentClient' can not be null");
        checkNotNull(changeFeedProcessorOptions, "Argument 'changeFeedProcessorOptions' can not be null");
        checkNotNull(leaseCheckpointer, "Argument 'leaseCheckpointer' can not be null");
        checkNotNull(monitoredContainer, "Argument 'monitoredContainer' can not be null");
        checkArgument(StringUtils.isNotEmpty(collectionResourceId), "Argument 'collectionResourceId' can not be null nor empty");

        this.documentClient = documentClient;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.leaseCheckpointer = leaseCheckpointer;
        this.monitoredContainer = monitoredContainer;
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
        checkNotNull(observer, "Argument 'observer' can not be null");
        checkNotNull(lease, "Argument 'lease' can not be null");


        ChangeFeedState state;
        if (Strings.isNullOrWhiteSpace(lease.getContinuationToken())) {
            // If the lease represents a full partition (old schema) then use a FeedRangePartitionKeyRange,
            // If the lease represents an EPK range (new schema) the use the FeedRange in the lease
            //
            // Different type of feedRange will populate headers differently
            // for epk feed range, it will add startEpk and endEpk extra in the headers
            // There is no harm to always populate startEpk and endEpk in the headers, but a thumb of rule is that
            // we only populate them when necessary
            FeedRangeInternal feedRange =
                    lease instanceof ServiceItemLeaseEpk ? lease.getFeedRange() : new FeedRangePartitionKeyRangeImpl(lease.getLeaseToken());

            state = new ChangeFeedStateV1(
                BridgeInternal.extractContainerSelfLink(this.monitoredContainer),
                feedRange,
                ChangeFeedMode.INCREMENTAL,
                getStartFromSettings(
                    feedRange,
                    this.changeFeedProcessorOptions),
                null);
        } else {
            state = lease.getContinuationState(this.collectionResourceId);
        }

        ProcessorSettings settings = new ProcessorSettings(state, this.monitoredContainer)
            .withFeedPollDelay(this.changeFeedProcessorOptions.getFeedPollDelay())
            .withMaxItemCount(this.changeFeedProcessorOptions.getMaxItemCount());

        PartitionCheckpointer checkpointer = new PartitionCheckpointerImpl(this.leaseCheckpointer, lease);
        return new PartitionProcessorImpl(observer, this.documentClient, settings, checkpointer, lease);
    }
}
