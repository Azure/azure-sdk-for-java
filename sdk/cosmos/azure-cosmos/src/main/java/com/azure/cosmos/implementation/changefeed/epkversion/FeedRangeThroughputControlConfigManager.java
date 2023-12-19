// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.FeedRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

// Only used in CFP when customer configure throughput control config
// The main purpose of this class is to monitor the lease feedRange -> partitionKeyRange mapping changes, and create corresponding throughput control group
public class FeedRangeThroughputControlConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeThroughputControlConfigManager.class);

    private final ThroughputControlGroupConfig throughputControlGroupConfig;
    private final ChangeFeedContextClient documentClient;
    private final AtomicReference<List<FeedRangeEpkImpl>> leaseTokens; // epk leases

    public FeedRangeThroughputControlConfigManager(
        ThroughputControlGroupConfig throughputControlGroupConfig,
        ChangeFeedContextClient documentClient) {

        checkNotNull(throughputControlGroupConfig, "Argument 'throughputControlGroupConfig' can not be null");
        checkNotNull(documentClient, "Argument 'documentClient' can not be null");

        this.throughputControlGroupConfig = throughputControlGroupConfig;
        this.documentClient = documentClient;
        this.leaseTokens = new AtomicReference<>();
    }

    /**
     * This method will be called during the leases load balancing time from {@link com.azure.cosmos.implementation.changefeed.common.EqualPartitionsBalancingStrategy}.
     * We are going to track the up to date all leases and refresh the partitionKeyRangesCache
     *
     * @param leases all the current leases in lease container.
     * @return a representation of the deferred computation of this call.
     */
    public Mono<Void> refresh(List<Lease> leases) {
        if (leases != null && !leases.isEmpty()) {
            this.leaseTokens.set(leases.stream().map(lease -> (FeedRangeEpkImpl)lease.getFeedRange()).collect(Collectors.toList()));
        }

        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true)
            .onErrorResume(throwable -> {
                logger.warn("Refresh pkRanges failed", throwable);
                return Mono.empty();
            })
            .then();
    }

    public Mono<ThroughputControlGroupConfig> getThroughputControlConfigForLeaseFeedRange(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        // for epk leases, it is used to support both split and merge
        // when merge happens, the current lease will be reused, so it can happen for the same partition key range, there are multiple leases map to it
        // for cases like this, we are going to find all leases mapped to the same partition key range, and then equally divide the RU allocation among the instances
        return this.documentClient.getOverlappingRanges(((FeedRangeEpkImpl)lease.getFeedRange()).getRange(), false)
            .flatMap(partitionKeyRanges -> {
                if (partitionKeyRanges.isEmpty()) {
                    return Mono.error(new IllegalStateException("Failed to get overlapping partition key range for range " + lease.getFeedRange()));
                }

                if (partitionKeyRanges.size() > 1) {
                    return Mono.error(new IllegalStateException("There are more than one partition key ranges mapped to the lease feed range. This should never happen"));
                }

                // find all leases overlapping with the partition key range
                long leasesBelongToSamePartitionKeyRange =
                    this.leaseTokens
                        .get()
                        .stream()
                        .filter(leaseToken ->
                            leaseToken.getRange().getMin().compareTo(partitionKeyRanges.get(0).getMinInclusive()) >= 0
                            && leaseToken.getRange().getMax().compareTo(partitionKeyRanges.get(0).getMaxExclusive()) < 0)
                        .count();

                return Mono.just(
                    getThroughputControlGroupConfigInternal(
                        lease.getFeedRange(),
                        leasesBelongToSamePartitionKeyRange));
            })
            .onErrorResume(throwable -> {
                // Throughput control flow should not break the normal request flow
                // we will capture all exceptions and fall back to use partition divider factor 1
                logger.warn("getThroughputControlConfigForLeaseFeedRange failed, using divide factor 1", throwable);
                return Mono.just(
                    getThroughputControlGroupConfigInternal(lease.getFeedRange(), 1));
            });
    }

    private ThroughputControlGroupConfig getThroughputControlGroupConfigInternal(
        FeedRange feedRange,
        long perPartitionDivideFactor) {

        // For each feedRange, we create a local throughput control group
        // We choose to start from local throughput control based on the idea that usually each CFP instance will only process a unique subset of feedRanges
        ThroughputControlGroupConfigBuilder throughputControlGroupConfigForFeedRangeBuilder =
            new ThroughputControlGroupConfigBuilder()
                .groupName(this.throughputControlGroupConfig.getGroupName() + "-" + feedRange.toString())
                .continueOnInitError(this.throughputControlGroupConfig.continueOnInitError());

        if (this.throughputControlGroupConfig.getTargetThroughput() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughput(
                (int) Math.max(
                    this.throughputControlGroupConfig.getTargetThroughput() / perPartitionDivideFactor,
                    1
                ));
        }
        if (this.throughputControlGroupConfig.getTargetThroughputThreshold() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughputThreshold(
                this.throughputControlGroupConfig.getTargetThroughputThreshold() / perPartitionDivideFactor);
        }
        if (this.throughputControlGroupConfig.getPriorityLevel() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.priorityLevel(this.throughputControlGroupConfig.getPriorityLevel());
        }

        ThroughputControlGroupConfig throughputControlGroupConfigForFeedRange = throughputControlGroupConfigForFeedRangeBuilder.build();
        this.documentClient.getContainerClient().enableLocalThroughputControlGroup(throughputControlGroupConfigForFeedRange);

        return throughputControlGroupConfigForFeedRange;
    }
}
